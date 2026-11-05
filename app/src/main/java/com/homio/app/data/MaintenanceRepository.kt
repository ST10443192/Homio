package com.homio.app.data

import android.content.Context
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.homio.app.Labels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.homio.app.data.api.ApiClient
import com.homio.app.data.api.CreateMaintenanceBody
import com.homio.app.data.api.StatusBody

class MaintenanceRepository private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val dao = HomioDatabase.get(appContext).maintenanceDao()
    private val firestore = FirebaseFirestore.getInstance()

    private val api = ApiClient.service
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()   // one cache refresh at a time
    private val pushMutex = Mutex()   // one upload run at a time

    // ---------- Reading (screens observe these; they always come from Room) ----------

    fun observeForTenant(tenantId: String): Flow<List<MaintenanceEntity>> =
        dao.observeForTenant(tenantId)

    fun observeAll(): Flow<List<MaintenanceEntity>> = dao.observeAll()

    // ---------- Firestore -> Room ----------

    /**
     * Keeps the Room cache in step with Firestore while online.
     * Pass the tenant's uid to cache only their requests, or null for all (landlord).
     * Call remove() on the result when the screen stops.
     */
    fun startSync(tenantId: String?): ListenerRegistration {
        val collection = firestore.collection("maintenance")
        val query = if (tenantId != null) collection.whereEqualTo("tenantId", tenantId) else collection

        return query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            // Offline snapshots come from Firestore's own cache and can be empty on a first
            // offline launch. Ignore them so we never wipe good data out of Room.
            if (snapshot.metadata.isFromCache) return@addSnapshotListener

            val remote = snapshot.documents.map { it.toEntity() }
            scope.launch {
                syncMutex.withLock {
                    // Local changes that haven't been uploaded yet win over the server copy
                    val pendingIds = dao.getPendingIds().toSet()
                    val items = remote.filter { it.id !in pendingIds }
                    if (tenantId != null) dao.replaceSyncedForTenant(tenantId, items)
                    else dao.replaceAllSynced(items)
                }
            }
        }
    }

    // ---------- Local writes (work offline) ----------

    suspend fun createRequest(
        tenantId: String,
        title: String,
        description: String,
        category: String
    ) {
        // document() with no argument only generates an id locally - no network needed
        val id = firestore.collection("maintenance").document().id
        dao.upsert(
            MaintenanceEntity(
                id = id,
                tenantId = tenantId,
                title = title,
                description = description,
                category = category,
                status = Labels.STATUS_SUBMITTED,
                createdDate = System.currentTimeMillis(),
                pendingSync = true
            )
        )
        SyncWorker.enqueue(appContext)
    }

    suspend fun markComplete(id: String) {
        dao.updateStatusLocally(id, Labels.STATUS_COMPLETED)
        SyncWorker.enqueue(appContext)
    }

    // ---------- Room -> Firestore ----------

    /** Uploads every waiting change. Returns true if all of them reached the server. */
    /** Sends every waiting change to the REST API. Returns true if all of them are done. */
    suspend fun pushPending(): Boolean = withContext(Dispatchers.IO) {
        pushMutex.withLock {
            var allUploaded = true
            for (item in dao.getPending()) {
                try {
                    val response = if (item.status == Labels.STATUS_SUBMITTED) {
                        // A request created on this phone. The server treats the same id as
                        // the same request, so retrying can never create a duplicate.
                        api.createMaintenance(
                            CreateMaintenanceBody(item.id, item.title, item.description, item.category)
                        )
                    } else {
                        // A status change made by the landlord
                        api.updateMaintenanceStatus(item.id, StatusBody(item.status))
                    }

                    val code = response.code()
                    when {
                        response.isSuccessful -> dao.markSynced(item.id, item.status)

                        // The server gave a final answer (invalid, not allowed, not found, conflict).
                        // Retrying won't change it, so stop waiting; the next Firestore sync
                        // replaces the local copy with the server's version.
                        code in 400..499 && code != 401 && code != 408 && code != 429 ->
                            dao.markSynced(item.id, item.status)

                        // 401 (token problem), 5xx (server down) etc: keep it and retry later
                        else -> allUploaded = false
                    }
                } catch (e: Exception) {
                    allUploaded = false   // no connection - WorkManager will retry
                }
            }
            allUploaded
        }
    }

    // ---------- Logout ----------

    /** Removes the cached data so the next person to sign in on this phone never sees it. */
    fun clearLocalData() {
        SyncWorker.cancel(appContext)
        scope.launch { dao.clearAll() }
    }

    private fun DocumentSnapshot.toEntity() = MaintenanceEntity(
        id = id,
        tenantId = getString("tenantId") ?: "",
        title = getString("title") ?: "",
        description = getString("description") ?: "",
        category = getString("category") ?: "",
        status = getString("status") ?: Labels.STATUS_SUBMITTED,
        createdDate = getTimestamp("createdDate")?.toDate()?.time ?: 0L,
        pendingSync = false
    )

    companion object {
        @Volatile private var instance: MaintenanceRepository? = null

        fun get(context: Context): MaintenanceRepository =
            instance ?: synchronized(this) {
                instance ?: MaintenanceRepository(context).also { instance = it }
            }
    }
}
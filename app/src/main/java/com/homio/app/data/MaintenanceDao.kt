package com.homio.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MaintenanceDao {

    // Flows re-emit automatically whenever the table changes
    @Query("SELECT * FROM maintenance_requests WHERE tenantId = :tenantId ORDER BY createdDate DESC")
    abstract fun observeForTenant(tenantId: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_requests ORDER BY createdDate DESC")
    abstract fun observeAll(): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance_requests WHERE pendingSync = 1")
    abstract suspend fun getPending(): List<MaintenanceEntity>

    @Query("SELECT id FROM maintenance_requests WHERE pendingSync = 1")
    abstract suspend fun getPendingIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(item: MaintenanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(items: List<MaintenanceEntity>)

    @Query("UPDATE maintenance_requests SET status = :status, pendingSync = 1 WHERE id = :id")
    abstract suspend fun updateStatusLocally(id: String, status: String)

    // Only clears the flag if the row wasn't changed again while it was uploading
    @Query("UPDATE maintenance_requests SET pendingSync = 0 WHERE id = :id AND status = :status")
    abstract suspend fun markSynced(id: String, status: String)

    @Query("DELETE FROM maintenance_requests WHERE pendingSync = 0 AND tenantId = :tenantId")
    abstract suspend fun deleteSyncedForTenant(tenantId: String)

    @Query("DELETE FROM maintenance_requests WHERE pendingSync = 0")
    abstract suspend fun deleteAllSynced()

    @Query("DELETE FROM maintenance_requests")
    abstract suspend fun clearAll()

    // One transaction = the list updates once, with no flicker of an empty list
    @Transaction
    open suspend fun replaceSyncedForTenant(tenantId: String, items: List<MaintenanceEntity>) {
        deleteSyncedForTenant(tenantId)
        upsertAll(items)
    }

    @Transaction
    open suspend fun replaceAllSynced(items: List<MaintenanceEntity>) {
        deleteAllSynced()
        upsertAll(items)
    }
}
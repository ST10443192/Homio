package com.homio.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.homio.app.data.SyncWorker

class HomioApp : Application() {

    private lateinit var notifier: RealtimeNotifier
    private var resumedScreen: Class<*>? = null

    override fun onCreate() {
        super.onCreate()

        NotificationHelper.createChannels(this)
        notifier = RealtimeNotifier(this)

        SyncWorker.enqueue(this)

        // Track the visible screen so we don't notify about what the user is looking at
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) { resumedScreen = activity.javaClass }
            override fun onActivityPaused(activity: Activity) {
                if (resumedScreen == activity.javaClass) resumedScreen = null
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // Stop on log out; resume automatically if the app restarts while signed in
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                notifier.stop()
            } else {
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnSuccessListener { doc ->
                        // During registration the profile doc may not exist yet;
                        // the dashboard starts notifications in that case.
                        if (doc.exists() && FirebaseAuth.getInstance().currentUser?.uid == user.uid) {
                            notifier.start(user.uid, doc.getString("userType") ?: "tenant")
                        }
                    }
            }
        }
    }

    fun startNotifications(uid: String, userType: String) = notifier.start(uid, userType)

    fun isOnScreen(screen: Class<*>): Boolean = resumedScreen == screen
}
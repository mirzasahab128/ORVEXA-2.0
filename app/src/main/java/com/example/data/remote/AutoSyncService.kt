package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.IdeaDao
import com.example.data.repository.IdeaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Background auto-sync service that monitors network connectivity and
 * automatically reconciles local Room database records with Firebase Firestore.
 */
class AutoSyncService(
    private val context: Context,
    private val repository: IdeaRepository,
    private val firebaseSyncManager: FirebaseSyncManager,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + Job())
) {
    private val connectivityObserver: ConnectivityObserver = NetworkConnectivityObserver(context)
    private var syncJob: Job? = null

    init {
        startObserving()
    }

    private fun startObserving() {
        syncJob = coroutineScope.launch {
            connectivityObserver.observe().collect { status ->
                when (status) {
                    ConnectivityObserver.Status.Available -> {
                        Log.d("AutoSyncService", "Network available: Triggering automatic reconciliation...")
                        reconcileLocalWithCloud()
                    }
                    ConnectivityObserver.Status.Lost,
                    ConnectivityObserver.Status.Unavailable -> {
                        Log.d("AutoSyncService", "Network unavailable: Working in local offline Room mode")
                    }
                    ConnectivityObserver.Status.Losing -> { }
                }
            }
        }
    }

    /**
     * Automatic reconciliation, delegated to the repository's two-way
     * last-write-wins merge (pull newer cloud ideas, push newer local
     * ideas). The previous version only pushed local ideas upward despite
     * its comment claiming a pull; cross-device changes never arrived.
     * NOTE: this service is only effective once something actually creates
     * it; IdeaViewModel now creates it at startup (it previously had no
     * creation site anywhere in the app, so auto-sync never ran).
     */
    suspend fun reconcileLocalWithCloud() {
        try {
            val user = firebaseSyncManager.currentUser.value
            if (user == null) {
                Log.d("AutoSyncService", "No active user logged in; skipping cloud sync")
                return
            }
            repository.reconcileWithCloud(firebaseSyncManager)
            Log.d("AutoSyncService", "Successfully reconciled local ideas with Firestore")
        } catch (e: Exception) {
            Log.e("AutoSyncService", "Error during automatic reconciliation", e)
        }
    }

    fun stop() {
        syncJob?.cancel()
    }
}

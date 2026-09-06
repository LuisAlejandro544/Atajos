package com.example.executor

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

object UserInteractionBridge {
    private val pendingInteractions = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    fun register(id: String, deferred: CompletableDeferred<Boolean>) {
        pendingInteractions[id] = deferred
    }

    fun resolve(id: String, success: Boolean) {
        pendingInteractions.remove(id)?.complete(success)
    }

    fun cancelAll() {
        pendingInteractions.forEach { (_, deferred) -> deferred.complete(false) }
        pendingInteractions.clear()
    }
}

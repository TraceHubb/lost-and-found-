package com.lostandfound.data.repositories

import com.google.firebase.auth.FirebaseUser
import com.lostandfound.data.firebase.FirebaseProviders
import kotlinx.coroutines.tasks.await

object AuthRepository {
    val currentUser: FirebaseUser?
        get() = FirebaseProviders.auth.currentUser

    suspend fun login(email: String, password: String) {
        FirebaseProviders.auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun register(email: String, password: String) {
        FirebaseProviders.auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    fun logout() {
        FirebaseProviders.auth.signOut()
    }
}


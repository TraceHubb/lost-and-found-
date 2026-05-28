package com.lostandfound.data.repositories

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.lostandfound.data.firebase.FirebaseProviders
import kotlinx.coroutines.tasks.await

object AuthRepository {
    val currentUser: FirebaseUser?
        get() = FirebaseProviders.auth.currentUser

    suspend fun login(email: String, password: String) {
        FirebaseProviders.auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun register(email: String, password: String, fullName: String) {
        val result = FirebaseProviders.auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: return
        
        // Set Firebase Auth display name (used by Profile)
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName.trim())
            .build()
        user.updateProfile(request).await()
        
        // Persist basic user profile in Firestore as well
        FirebaseProviders.firestore
            .collection("users")
            .document(user.uid)
            .set(
                mapOf(
                    "uid" to user.uid,
                    "email" to (user.email ?: email.trim()),
                    "displayName" to fullName.trim()
                )
            ).await()
    }

    fun logout() {
        FirebaseProviders.auth.signOut()
    }
}


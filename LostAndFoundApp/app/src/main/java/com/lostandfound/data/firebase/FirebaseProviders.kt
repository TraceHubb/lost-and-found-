package com.lostandfound.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseProviders {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val storage: FirebaseStorage by lazy { 
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            // If default storage fails, try with a specific bucket
            FirebaseStorage.getInstance("gs://your-project-id.appspot.com")
        }
    }
}


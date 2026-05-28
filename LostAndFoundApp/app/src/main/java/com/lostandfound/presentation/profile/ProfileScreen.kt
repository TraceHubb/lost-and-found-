package com.lostandfound.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lostandfound.data.repositories.AuthRepository

@Composable
fun ProfileScreen() {
    val user = AuthRepository.currentUser
    val email = user?.email ?: "Not signed in"
    val fullName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Unnamed user"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Profile header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "User Profile",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(fullName)
                Spacer(modifier = Modifier.height(8.dp))
                Text(email)
                Spacer(modifier = Modifier.height(8.dp))
                Text("⭐ Rating: 4.5/5.0")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("My Posted Items", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(5) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Item ${index + 1}", style = MaterialTheme.typography.titleMedium)
                    Text("Status: Active")
                    Text("Posted: 2 days ago", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO: Edit profile */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { /* TODO: Logout */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}

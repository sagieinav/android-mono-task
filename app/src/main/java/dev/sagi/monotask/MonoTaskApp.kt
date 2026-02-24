package dev.sagi.monotask

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import dev.sagi.monotask.data.repository.AuthRepository
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository

class MonoTaskApp : Application() {

    // Firebase services: initialized once, shared across the entire app
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // Repositories: initialized once, shared across all ViewModels
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val userRepository: UserRepository by lazy { UserRepository() }
    val taskRepository: TaskRepository by lazy { TaskRepository() }
    val workspaceRepository: WorkspaceRepository by lazy { WorkspaceRepository() }

    companion object {
        // Global reference to the Application instance
        lateinit var instance: MonoTaskApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        FirebaseApp.initializeApp(this)
    }
}

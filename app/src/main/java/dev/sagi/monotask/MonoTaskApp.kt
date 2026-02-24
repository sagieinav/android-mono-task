package dev.sagi.monotask

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class MonoTaskApp : Application() {

    // Initialized once, shared across the entire app
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

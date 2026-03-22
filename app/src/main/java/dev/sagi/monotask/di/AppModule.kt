package dev.sagi.monotask.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.sagi.monotask.data.repository.AuthRepository
import dev.sagi.monotask.data.repository.TaskRepository
import dev.sagi.monotask.data.repository.UserPrefsRepository
import dev.sagi.monotask.data.repository.UserRepository
import dev.sagi.monotask.data.repository.WorkspaceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository =
        AuthRepository(auth)

    @Provides @Singleton
    fun provideUserRepository(db: FirebaseFirestore): UserRepository =
        UserRepository(db)

    @Provides @Singleton
    fun provideTaskRepository(db: FirebaseFirestore): TaskRepository =
        TaskRepository(db)

    @Provides @Singleton
    fun provideWorkspaceRepository(db: FirebaseFirestore): WorkspaceRepository =
        WorkspaceRepository(db)

    @Provides @Singleton
    fun provideUserPrefsRepository(@ApplicationContext context: Context): UserPrefsRepository =
        UserPrefsRepository(context)
}

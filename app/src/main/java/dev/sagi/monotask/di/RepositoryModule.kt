package dev.sagi.monotask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sagi.monotask.data.repository.AuthRepositoryImpl
import dev.sagi.monotask.data.repository.TaskRepositoryImpl
import dev.sagi.monotask.data.repository.UserPrefsRepositoryImpl
import dev.sagi.monotask.data.repository.UserRepositoryImpl
import dev.sagi.monotask.data.repository.WorkspaceRepositoryImpl
import dev.sagi.monotask.domain.repository.AuthRepository
import dev.sagi.monotask.domain.repository.TaskRepository
import dev.sagi.monotask.domain.repository.UserPrefsRepository
import dev.sagi.monotask.domain.repository.UserRepository
import dev.sagi.monotask.domain.repository.WorkspaceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindWorkspaceRepository(impl: WorkspaceRepositoryImpl): WorkspaceRepository

    @Binds @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPrefsRepositoryImpl): UserPrefsRepository
}

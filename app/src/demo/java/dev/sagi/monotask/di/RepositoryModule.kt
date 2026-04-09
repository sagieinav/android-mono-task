package dev.sagi.monotask.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sagi.monotask.data.demo.DemoActivityRepository
import dev.sagi.monotask.data.demo.DemoStatsRepository
import dev.sagi.monotask.data.demo.DemoTaskRepository
import dev.sagi.monotask.data.demo.DemoUserRepository
import dev.sagi.monotask.data.demo.DemoWorkspaceRepository
import dev.sagi.monotask.data.repository.AuthRepositoryImpl
import dev.sagi.monotask.data.repository.UserPrefsRepositoryImpl
import dev.sagi.monotask.domain.repository.ActivityRepository
import dev.sagi.monotask.domain.repository.AuthRepository
import dev.sagi.monotask.domain.repository.StatsRepository
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
    abstract fun bindTaskRepository(impl: DemoTaskRepository): TaskRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: DemoUserRepository): UserRepository

    @Binds @Singleton
    abstract fun bindActivityRepository(impl: DemoActivityRepository): ActivityRepository

    @Binds @Singleton
    abstract fun bindStatsRepository(impl: DemoStatsRepository): StatsRepository

    @Binds @Singleton
    abstract fun bindWorkspaceRepository(impl: DemoWorkspaceRepository): WorkspaceRepository

    @Binds @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPrefsRepositoryImpl): UserPrefsRepository
}

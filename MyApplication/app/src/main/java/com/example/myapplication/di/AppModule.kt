package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.repository.MetroRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMetroRepository(@ApplicationContext context: Context): MetroRepository {
        return MetroRepository(context)
    }
} 
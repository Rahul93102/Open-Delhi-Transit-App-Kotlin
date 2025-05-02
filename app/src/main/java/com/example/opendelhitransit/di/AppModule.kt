package com.example.opendelhitransit.di

import android.content.Context
import com.example.opendelhitransit.data.network.GoMapsFuelStationApiService
import com.example.opendelhitransit.data.network.TransitApiService
import com.example.opendelhitransit.data.repository.GoMapsFuelStationRepository
import com.example.opendelhitransit.data.repository.MetroRepository
import com.example.opendelhitransit.data.repository.TransitRepository
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
    
    @Provides
    @Singleton
    fun provideGoMapsFuelStationApiService(): GoMapsFuelStationApiService {
        return GoMapsFuelStationApiService.create()
    }
    
    @Provides
    @Singleton
    fun provideTransitApiService(): TransitApiService {
        return TransitApiService.create()
    }
    
    @Provides
    @Singleton
    fun provideGoMapsFuelStationRepository(apiService: GoMapsFuelStationApiService): GoMapsFuelStationRepository {
        return GoMapsFuelStationRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideTransitRepository(apiService: TransitApiService): TransitRepository {
        return TransitRepository(apiService)
    }
} 
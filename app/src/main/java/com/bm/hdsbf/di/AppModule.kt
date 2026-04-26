package com.bm.hdsbf.di

import com.bm.hdsbf.BuildConfig
import com.bm.hdsbf.data.remote.service.AbsensiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val CONNECT_TIMEOUT: Long = 5
    private const val READ_TIMEOUT: Long = 5
    private const val WRITE_TIMEOUT: Long = 5

    @Provides
    @Singleton
    fun provideAbsensiService(): AbsensiService {
        val absensiService: AbsensiService
        val client = OkHttpClient.Builder()
        if(BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            client.addInterceptor(interceptor)
        }
        client
            .followRedirects(true)
            .followSslRedirects(true)
            .retryOnConnectionFailure(true)
            .cache(null)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://atom.mobile.bimasakti.hrcules.co.id/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client.build())
            .build()

        absensiService = retrofit.create(AbsensiService::class.java)
        return absensiService
    }
}
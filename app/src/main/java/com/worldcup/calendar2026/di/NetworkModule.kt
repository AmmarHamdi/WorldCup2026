package com.worldcup.calendar2026.di

import com.squareup.moshi.Moshi
import com.worldcup.calendar2026.BuildConfig
import com.worldcup.calendar2026.data.ApiKeyStore
import com.worldcup.calendar2026.data.remote.ApiFootballService
import com.worldcup.calendar2026.data.remote.FlexibleErrorsAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "https://v3.football.api-sports.io/"

/** Adds the API-Football key header to every request, reading it dynamically from [ApiKeyStore]. */
class AuthInterceptor(private val apiKeyStore: ApiKeyStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-apisports-key", apiKeyStore.getKey())
            .build()
        return chain.proceed(request)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(FlexibleErrorsAdapter.FACTORY)
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(apiKeyStore: ApiKeyStore): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(apiKeyStore))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideService(retrofit: Retrofit): ApiFootballService =
        retrofit.create(ApiFootballService::class.java)
}

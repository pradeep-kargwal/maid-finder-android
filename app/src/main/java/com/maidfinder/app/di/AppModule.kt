package com.maidfinder.app.di

import android.content.Context
import androidx.room.Room
import com.maidfinder.app.BuildConfig
import com.maidfinder.app.data.local.MaidFinderDatabase
import com.maidfinder.app.data.local.dao.*
import com.maidfinder.app.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MaidFinderDatabase =
        Room.databaseBuilder(context, MaidFinderDatabase::class.java, "maidfinder.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: MaidFinderDatabase): UserDao = db.userDao()
    @Provides fun provideMaidProfileDao(db: MaidFinderDatabase): MaidProfileDao = db.maidProfileDao()
    @Provides fun provideJobDao(db: MaidFinderDatabase): JobDao = db.jobDao()
    @Provides fun provideBookingDao(db: MaidFinderDatabase): BookingDao = db.bookingDao()
    @Provides fun provideMessageDao(db: MaidFinderDatabase): MessageDao = db.messageDao()
    @Provides fun provideSavedMaidDao(db: MaidFinderDatabase): SavedMaidDao = db.savedMaidDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides fun provideAuthApi(retrofit: Retrofit): AuthApiService = retrofit.create(AuthApiService::class.java)
    @Provides fun provideUserApi(retrofit: Retrofit): UserApiService = retrofit.create(UserApiService::class.java)
    @Provides fun provideJobApi(retrofit: Retrofit): JobApiService = retrofit.create(JobApiService::class.java)
    @Provides fun provideBookingApi(retrofit: Retrofit): BookingApiService = retrofit.create(BookingApiService::class.java)
    @Provides fun provideMessageApi(retrofit: Retrofit): MessageApiService = retrofit.create(MessageApiService::class.java)
}

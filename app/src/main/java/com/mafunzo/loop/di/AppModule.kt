package com.mafunzo.loop.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.launchdarkly.sdk.LDUser
import com.launchdarkly.sdk.android.LDClient
import com.launchdarkly.sdk.android.LDConfig
import com.mafunzo.loop.App.Companion.application
import com.mafunzo.loop.data.local.database.MafunzoDatabase
import com.mafunzo.loop.data.local.database.daos.UserDao
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.repository.UserRepositoryImpl
import com.mafunzo.loop.domain.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @InstallIn(SingletonComponent::class)
    @Module
    class MafunzoDatabaseModule {
        @Provides
        fun provideUserDao(appDatabase: MafunzoDatabase): UserDao {
            return appDatabase.userDao()
        }
    }

    @Singleton
    @Provides
    fun provideUserRepository(
        userPrefs: AppDatasource,
        userDao: UserDao
    ): UserRepository = UserRepositoryImpl(userDao,userPrefs)

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): MafunzoDatabase {
        return Room.databaseBuilder(
            appContext,
            MafunzoDatabase::class.java,
            "mafunzo.db"
        ).build()
    }

    @Singleton
    @Provides
    fun providesFirebaseDatabase() = FirebaseDatabase.getInstance()

    @Singleton
    @Provides
    fun providesAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirestore() = Firebase.firestore

    @Singleton
    @Provides
    fun provideLaunchDarklyClient(): LDClient {
        val mobileKey = if(BuildConfig.BUILD_TYPE == "release")
            Constants.LAUNCH_DARKLY_SDK_KEY
        else
            Constants.LAUNCH_DARKLY_TEST_SDK_KEY

        val ldConfig: LDConfig = LDConfig.Builder()
            .mobileKey(mobileKey)
            .build()
        val user: LDUser = LDUser.Builder(FirebaseAuth.getInstance().currentUser?.uid)
            .build()

        return LDClient.init(application, ldConfig, user, 5)
    }
}
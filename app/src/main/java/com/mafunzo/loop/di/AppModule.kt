package com.mafunzo.loop.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mafunzo.loop.App
import com.mafunzo.loop.data.local.database.MafunzoDatabase
import com.mafunzo.loop.data.local.database.daos.UserDao
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
}
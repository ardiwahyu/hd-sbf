package com.bm.hdsbf.di

import android.content.Context
import androidx.room.Room
import com.bm.hdsbf.data.local.db.HdSbfDB
import com.bm.hdsbf.data.local.db.dao.ScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DBModule {
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): HdSbfDB {
        return Room
            .databaseBuilder(context, HdSbfDB::class.java, HdSbfDB.NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideScheduleDao(db: HdSbfDB): ScheduleDao {
        return db.scheduleDao()
    }
}
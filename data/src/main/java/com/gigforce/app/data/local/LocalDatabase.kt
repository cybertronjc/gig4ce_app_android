package com.gigforce.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gigforce.app.data.local.entities.CachedTLWorkSpaceSectionModel
import com.gigforce.app.data.local.room_type_converters.LocalDateTimeTypeConverter

@Database(
    entities = [
        CachedTLWorkSpaceSectionModel::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    LocalDateTimeTypeConverter::class
)
abstract class LocalDatabase : RoomDatabase() {

    companion object {
        private const val DATABASE_NAME = "gigforce.db"

        fun createDatabase(
            applicationContext: Context
        ): LocalDatabase {
            return Room.databaseBuilder(
                applicationContext,
                LocalDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }


}
package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Idea
import com.example.data.model.UserProfile

@Database(entities = [Idea::class, UserProfile::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class IdeaIntelligenceDatabase : RoomDatabase() {
    abstract fun ideaDao(): IdeaDao

    companion object {
        @Volatile
        private var INSTANCE: IdeaIntelligenceDatabase? = null

        fun getDatabase(context: Context): IdeaIntelligenceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IdeaIntelligenceDatabase::class.java,
                    "idea_intelligence_os.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

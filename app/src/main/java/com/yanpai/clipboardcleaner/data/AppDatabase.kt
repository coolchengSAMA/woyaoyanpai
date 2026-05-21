package com.yanpai.clipboardcleaner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [NoteEntry::class, ThemePresetEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun themePresetDao(): ThemePresetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE note_entries ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE note_entries ADD COLUMN deleted_at INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_note_deleted ON note_entries(deleted_at)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS theme_presets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        is_built_in INTEGER NOT NULL DEFAULT 0,
                        light_primary INTEGER NOT NULL,
                        light_secondary INTEGER NOT NULL,
                        light_tertiary INTEGER NOT NULL,
                        light_background INTEGER NOT NULL,
                        light_surface INTEGER NOT NULL,
                        light_error INTEGER NOT NULL,
                        dark_primary INTEGER NOT NULL,
                        dark_secondary INTEGER NOT NULL,
                        dark_tertiary INTEGER NOT NULL,
                        dark_background INTEGER NOT NULL,
                        dark_surface INTEGER NOT NULL,
                        dark_error INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN light_bg_image TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN dark_bg_image TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN bg_scale_mode INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN bg_offset_x REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN bg_offset_y REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE theme_presets ADD COLUMN bg_scale REAL NOT NULL DEFAULT 1")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clipboard_cleaner.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
        }
    }
}

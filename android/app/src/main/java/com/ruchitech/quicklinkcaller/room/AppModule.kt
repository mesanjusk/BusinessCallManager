package com.ruchitech.quicklinkcaller.room


import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new column with a default value
            db.execSQL("ALTER TABLE Call_log_details ADD COLUMN countryCode TEXT DEFAULT NULL")
        }
    }
    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add the new columns with default values
            db.execSQL("ALTER TABLE Call_log_details ADD COLUMN regionCode TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE Call_log_details ADD COLUMN typeOfNumber TEXT DEFAULT NULL")
            db.execSQL("ALTER TABLE Call_log_details ADD COLUMN isValid INTEGER DEFAULT 0 NOT NULL")
        }
    }

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
            CREATE TABLE IF NOT EXISTS notifications_queue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                notificationId INTEGER NOT NULL UNIQUE,
                createdTime INTEGER NOT NULL,
                type INTEGER NOT NULL
            )
            """.trimIndent()
            )
        }
    }

    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS team_members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    email TEXT NOT NULL DEFAULT '',
                    role TEXT NOT NULL DEFAULT 'Member',
                    created_at INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS lead_assignments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    callerId TEXT NOT NULL,
                    teamMemberId INTEGER NOT NULL,
                    assignedDate INTEGER NOT NULL DEFAULT 0,
                    note TEXT NOT NULL DEFAULT ''
                )
                """.trimIndent()
            )
        }
    }

    @Singleton
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        DatabaseDao::class.java,
        "test_task_db"
    )
        .addMigrations(MIGRATION_10_11)
        .addMigrations(MIGRATION_11_12)
        .addMigrations(MIGRATION_12_13)
        .addMigrations(MIGRATION_13_14)
        .build()


    @Singleton
    @Provides
    fun provideYourDao(db: DatabaseDao) = db.dataDao()
}
/*
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE data_table ADD COLUMN password TEXT")
        }
    }
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create a temporary table with the new schema
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS Call_logs_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "callerId TEXT NOT NULL," +
                        "callNote TEXT," +  // Make callNote nullable
                        "UNIQUE(callerId) ON CONFLICT REPLACE" +
                        ")"
            )

            // Copy data from the old table to the new table
            database.execSQL(
                "INSERT INTO Call_logs_temp (id, callerId, callNote) SELECT id, callerId, callNote FROM Call_logs"
            )

            // Remove the old table
            database.execSQL("DROP TABLE Call_logs")

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE Call_logs_temp RENAME TO Call_logs")
        }
    }
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create a temporary table with the new schema
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS Call_logs_temp (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "callerId TEXT NOT NULL," +
                        "callNote TEXT," +  // Make callNote nullable
                        "UNIQUE(callerId) ON CONFLICT REPLACE" +
                        ")"
            )

            // Copy data from the old table to the new table (excluding callLogDetails)
            database.execSQL(
                "INSERT INTO Call_logs_temp (id, callerId, callNote) SELECT id, callerId, '' FROM Call_logs"
            )

            // Remove the old table
            database.execSQL("DROP TABLE Call_logs")

            // Rename the new table to the original table name
            database.execSQL("ALTER TABLE Call_logs_temp RENAME TO Call_logs")
        }
    }
}*/

package io.github.acedroidx.danmaku

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.acedroidx.danmaku.data.home.DanmakuConfigDatabase
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DBMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), DanmakuConfigDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate2To3() {
        val TABLE_NAME = "DanmakuConfig"
        val db2 = helper.createDatabase(TEST_DB, 2).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("""CREATE TABLE IF NOT EXISTS `${TABLE_NAME}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `msg` TEXT NOT NULL, `shootMode` TEXT NOT NULL, `interval` INTEGER NOT NULL, `color` INTEGER NOT NULL, `roomid` INTEGER NOT NULL)""")
            execSQL(
                """INSERT INTO `${TABLE_NAME}` ("id", "name", "msg", "shootMode", "interval", "color", "roomid") VALUES (1, 'test-name', 'test-msg', 'NORMAL', 8000, 14893055, 21452505);"""
            )

            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        val db3 = helper.runMigrationsAndValidate(TEST_DB, 3, true)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }
}

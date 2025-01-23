package pt.ipp.estg.cmu_restaurants.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Models.User
import pt.ipp.estg.cmu_restaurants.Room.DAOs.ReviewDao
import pt.ipp.estg.cmu_restaurants.Room.DAOs.UserDao

@Database(
    entities = [User::class, Review::class],
    version = 2,
    exportSchema = false
)
abstract class DatabaseProvider : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao


    companion object {
        @Volatile
        private var INSTANCE: DatabaseProvider? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
            CREATE TABLE IF NOT EXISTS `Review` (
                `reviewId` TEXT PRIMARY KEY NOT NULL,
                `userId` TEXT NOT NULL,
                `rating` INTEGER NOT NULL,
                `comment` TEXT NOT NULL,
                FOREIGN KEY(`userId`) REFERENCES `users`(`userId`) ON DELETE CASCADE
                )
            """.trimIndent()
                )

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_reviews_userId` ON `reviews`(`userId`)"
                )
            }
        }

        fun getDatabase(context: Context): DatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseProvider::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
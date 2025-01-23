package pt.ipp.estg.cmu_restaurants.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.ipp.estg.cmu_restaurants.Models.Review
import pt.ipp.estg.cmu_restaurants.Models.User
import pt.ipp.estg.cmu_restaurants.Room.DAOs.ReviewDao
import pt.ipp.estg.cmu_restaurants.Room.DAOs.UserDao

@Database(
    entities = [User::class, Review::class],
    version = 1,
    exportSchema = false
)
abstract class DatabaseProvider : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun reviewDao(): ReviewDao


    companion object {
        @Volatile
        private var INSTANCE: DatabaseProvider? = null

        fun getDatabase(context: Context): DatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatabaseProvider::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
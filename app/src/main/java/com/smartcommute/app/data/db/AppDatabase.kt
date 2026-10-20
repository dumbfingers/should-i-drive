package com.smartcommute.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.smartcommute.app.data.model.EfficiencyUnit
import com.smartcommute.app.data.model.FuelType
import com.smartcommute.app.data.model.WeatherCondition

class Converters {
    @TypeConverter
    fun fromFuelType(value: FuelType): String = value.name

    @TypeConverter
    fun toFuelType(value: String): FuelType = FuelType.valueOf(value)

    @TypeConverter
    fun fromEfficiencyUnit(value: EfficiencyUnit): String = value.name

    @TypeConverter
    fun toEfficiencyUnit(value: String): EfficiencyUnit = EfficiencyUnit.valueOf(value)

    @TypeConverter
    fun fromWeatherCondition(value: WeatherCondition): String = value.name

    @TypeConverter
    fun toWeatherCondition(value: String): WeatherCondition = WeatherCondition.valueOf(value)
}

@Database(
    entities = [VehicleEntity::class, TripComparisonEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripComparisonDao(): TripComparisonDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_commute_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

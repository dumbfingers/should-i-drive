package com.smartcommute.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY id ASC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getVehicleById(id: Long): VehicleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity): Long

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Delete
    suspend fun deleteVehicle(vehicle: VehicleEntity)
}

@Dao
interface TripComparisonDao {
    @Query("SELECT * FROM trip_comparisons ORDER BY timestamp DESC")
    fun getAllComparisons(): Flow<List<TripComparisonEntity>>

    @Query("SELECT * FROM trip_comparisons WHERE id = :id")
    suspend fun getComparisonById(id: Long): TripComparisonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComparison(comparison: TripComparisonEntity): Long

    @Delete
    suspend fun deleteComparison(comparison: TripComparisonEntity)

    @Query("DELETE FROM trip_comparisons")
    suspend fun clearAll()
}

package com.ruchitech.quicklinkcaller.room.dao

import androidx.room.*
import com.ruchitech.quicklinkcaller.room.data.Business
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business)

    @Query("SELECT * FROM business LIMIT 1")
    fun getBusiness(): Flow<Business?>

    @Query("SELECT * FROM business LIMIT 1")
    suspend fun getBusinessOnce(): Business?

    @Update
    suspend fun updateBusiness(business: Business)

    @Query("DELETE FROM business")
    suspend fun clearBusiness()
}

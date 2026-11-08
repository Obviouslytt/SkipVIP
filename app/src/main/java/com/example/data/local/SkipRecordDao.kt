package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SkipRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SkipRecordDao {
    @Query("SELECT * FROM skip_records WHERE year = :year AND month = :month")
    fun getRecordsForMonthFlow(year: Int, month: Int): Flow<List<SkipRecord>>

    @Query("SELECT * FROM skip_records WHERE year = :year AND month = :month")
    suspend fun getRecordsForMonthSync(year: Int, month: Int): List<SkipRecord>

    @Query("SELECT * FROM skip_records WHERE membershipId = :membershipId ORDER BY year DESC, month DESC")
    fun getRecordsForMembershipFlow(membershipId: Long): Flow<List<SkipRecord>>

    @Query("SELECT * FROM skip_records WHERE membershipId = :membershipId AND year = :year AND month = :month LIMIT 1")
    suspend fun getRecord(membershipId: Long, year: Int, month: Int): SkipRecord?

    @Query("SELECT * FROM skip_records ORDER BY year DESC, month DESC")
    fun getAllRecordsFlow(): Flow<List<SkipRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: SkipRecord): Long

    @Query("DELETE FROM skip_records WHERE membershipId = :membershipId AND year = :year AND month = :month")
    suspend fun deleteRecord(membershipId: Long, year: Int, month: Int)

    @Query("DELETE FROM skip_records WHERE membershipId = :membershipId")
    suspend fun deleteRecordsForMembership(membershipId: Long)

    @Query("SELECT COALESCE(SUM(savedAmount), 0.0) FROM skip_records WHERE status = 'SKIPPED'")
    fun getAllTimeSavedFlow(): Flow<Double>

    @Query("SELECT COUNT(*) FROM skip_records WHERE status = 'SKIPPED'")
    fun getAllTimeSkipsCountFlow(): Flow<Int>
}

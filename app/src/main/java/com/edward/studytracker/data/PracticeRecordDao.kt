package com.edward.studytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeRecordDao {
    @Query("SELECT * FROM practice_records ORDER BY timestamp DESC")
    fun getAllPracticeRecords(): Flow<List<PracticeRecord>>
    
    @Query("SELECT * FROM practice_records WHERE problemId = :problemId ORDER BY timestamp DESC")
    fun getPracticeRecordsForProblem(problemId: Int): Flow<List<PracticeRecord>>
    
    @Query("SELECT * FROM practice_records WHERE DATE(timestamp / 1000, 'unixepoch') = DATE(:timestamp / 1000, 'unixepoch')")
    suspend fun getPracticeRecordsForDay(timestamp: Long): List<PracticeRecord>
    
    @Query("SELECT * FROM practice_records WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getPracticeRecordsBetween(startOfDay: Long, endOfDay: Long): List<PracticeRecord>
    
    @Query("SELECT COUNT(*) FROM practice_records")
    suspend fun getTotalPracticeCount(): Int
    
    @Query("SELECT COUNT(*) FROM practice_records WHERE isCorrect = 1")
    suspend fun getCorrectPracticeCount(): Int
    
    @Query("SELECT COUNT(*) FROM practice_records WHERE isCorrect = 0")
    suspend fun getWrongPracticeCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeRecord(record: PracticeRecord): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPracticeRecords(records: List<PracticeRecord>): List<Long>
}
package com.edward.studytracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<Project>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long
    
    @Update
    suspend fun updateProject(project: Project)
    
    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProject(projectId: Int)
}

@Dao
interface StudyUnitDao {
    @Query("SELECT * FROM study_units WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun getUnitsForProject(projectId: Int): Flow<List<StudyUnit>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: StudyUnit): Long
    
    @Query("DELETE FROM study_units WHERE id = :unitId")
    suspend fun deleteUnit(unitId: Int)
}

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems WHERE unitId = :unitId ORDER BY problemIndex ASC")
    fun getProblemsForUnit(unitId: Int): Flow<List<Problem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: Problem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<Problem>): List<Long>
    
    @Update
    suspend fun updateProblem(problem: Problem)
    
    @Query("DELETE FROM problems WHERE unitId = :unitId")
    suspend fun deleteProblemsForUnit(unitId: Int)
}

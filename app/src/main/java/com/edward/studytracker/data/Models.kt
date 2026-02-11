package com.edward.studytracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "study_units",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class StudyUnit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String,
    val problemCount: Int,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "problems",
    foreignKeys = [
        ForeignKey(
            entity = StudyUnit::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["unitId"])]
)
data class Problem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val unitId: Int,
    val problemIndex: Int,
    val proficiencyLevel: Int = 0
)

@Entity(
    tableName = "practice_records",
    foreignKeys = [
        ForeignKey(
            entity = Problem::class,
            parentColumns = ["id"],
            childColumns = ["problemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["problemId"]), Index(value = ["timestamp"])]
)
data class PracticeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val problemId: Int,
    val isCorrect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ExportData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val projects: List<Project>,
    val studyUnits: List<StudyUnit>,
    val problems: List<Problem>,
    val practiceRecords: List<PracticeRecord>
)

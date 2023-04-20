package com.digitalinterruption.lex.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "sym_data_table", indices = [Index(value = ["id"], unique = true)])
data class DataSymptoms(@PrimaryKey(autoGenerate = true) val id: Int, val date: LocalDate?, val symptom: String, val intensity: String)

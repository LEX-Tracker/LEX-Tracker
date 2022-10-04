package com.digitalinterruption.lex.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "sym_table", indices = [Index(value = ["id"], unique = true)])
data class SymptomModel(@PrimaryKey(autoGenerate = true) val id: Int, val date: String?, val symptom: String?, val intensity: String?)

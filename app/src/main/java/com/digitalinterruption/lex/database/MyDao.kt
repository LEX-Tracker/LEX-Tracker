package com.digitalinterruption.lex.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.digitalinterruption.lex.models.SymptomModel
import java.time.LocalDateTime

@Dao
interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addData(listData: ArrayList<SymptomModel>)

    @Query("UPDATE sym_table SET intensity =:newIntensity WHERE date =:date and symptom =:symptom")
    fun updateValues(newIntensity: String, date: String, symptom: String)

    @Update
    fun update(listData: ArrayList<SymptomModel>)

    @Query("SELECT * from sym_table")
    fun getAllData(): LiveData<List<SymptomModel>>
}
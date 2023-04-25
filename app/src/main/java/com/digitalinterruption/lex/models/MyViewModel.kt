package com.digitalinterruption.lex.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.sqlite.db.SimpleSQLiteQuery
import com.digitalinterruption.lex.database.MyDao
import com.digitalinterruption.lex.database.MyDatabase
import com.digitalinterruption.lex.database.MyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.time.LocalDateTime

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MyRepository
    val readAllData: LiveData<List<SymptomModel>>
    val myDatabase: MyDatabase
    val myDao: MyDao

    init {
        myDatabase = MyDatabase.getDatabase(application)!!
        myDao = myDatabase.myDao()
        repository = MyRepository(myDao)
        readAllData = repository.readAllData
    }

    fun addData(listData: ArrayList<SymptomModel>) {
        viewModelScope.launch {
            repository.addData(listData)
        }
    }

    fun updateData(newIntensity: String, date: String, symptom: String) {
        viewModelScope.launch {
            repository.updateData(newIntensity, date, symptom)
        }
    }

    fun clearSymptoms(){
        viewModelScope.launch {
            repository.clearSymptoms()
        }
    }



}
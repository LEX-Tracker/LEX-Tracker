package com.digitalinterruption.lex.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.digitalinterruption.lex.models.SymptomModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MyRepository(val myDao: MyDao) {

    val readAllData: LiveData<List<SymptomModel>> = myDao.getAllData()

    fun addData(listData: ArrayList<SymptomModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            myDao.addData(listData)
        }
    }

    fun updateData(newIntensity: String, date: String, symptom:String) {
        CoroutineScope(Dispatchers.IO).launch {
            myDao.updateValues(newIntensity, date, symptom)
        }
    }

    fun importData(columns: StringBuilder, values: StringBuilder){
        CoroutineScope(IO).launch {
            // TODO rewrite this shite
            val query = SimpleSQLiteQuery("INSERT INTO sym_table ($columns) values($values)", arrayOf())
            try {
                myDao.insertDataRawFormat(query)
            }catch (e: Exception){
                Log.i("Logs", "pushCustomerData: ${e.message}")
            }
        }
    }

}
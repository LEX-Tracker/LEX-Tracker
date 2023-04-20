package com.digitalinterruption.lex.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.digitalinterruption.lex.models.SymptomModel
import com.digitalinterruption.lex.SharedPrefs
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [SymptomModel::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {

    abstract fun myDao(): MyDao

    companion object {
        @Volatile
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase? {
            val encryptionPW = SharedPrefs(context).getNickName() // user entered data when they first setup the app
            val tempINSTANCE = INSTANCE
            if (tempINSTANCE != null) {
                return tempINSTANCE
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "lex_database")
                val factory = SupportFactory(SQLiteDatabase.getBytes(encryptionPW?.toCharArray()))// this bit
                return instance
                    .openHelperFactory(factory) //this bit
                    .enableMultiInstanceInvalidation()
                    .build()

                /*
                * if trying to debug a DB issue swap the above for the below to allow app inspection to read the db
                * val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyDatabase::class.java,
                    "lex_unencrypted_database")
                return instance
                    .enableMultiInstanceInvalidation()
                    .build()
                    * */


            }
        }
    }
}
package com.digitalinterruption.lex

import android.content.Context
import android.content.SharedPreferences

class SharedPrefs(context: Context?) {

    var sharedPreferences: SharedPreferences? = null

    fun setPremium(value: Boolean) {
        sharedPreferences?.edit()?.putBoolean("premium", value)?.apply()
    }

    fun getPremium(): Boolean {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getBoolean("premium", false)
        }
        return false
    }

    companion object {
        var instance: SharedPrefs? = null
        fun getInstance(context: Context?): SharedPrefs? {
            if (instance == null) {
                instance = SharedPrefs(context)
            }
            return instance
        }
    }

    init {
        sharedPreferences = context?.getSharedPreferences("lex", Context.MODE_PRIVATE)
    }


    fun setPassCode(value: Boolean) {
        sharedPreferences?.edit()?.putBoolean("passCode", value)?.apply()
    }

    fun getPassCode(): Boolean {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getBoolean("passCode", false)
        }
        return false
    }


    fun setPassPin(value: String) {
        sharedPreferences?.edit()?.putString("passCodePin", value)?.apply()
    }

    fun getPassPin(): String? {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getString("passCodePin", "")
        }
        return ""
    }


    fun setSecondPassCode(value: Boolean) {
        sharedPreferences?.edit()?.putBoolean("secondPassCode", value)?.apply()
    }

    fun getSecondPassCode(): Boolean {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getBoolean("secondPassCode", false)
        }
        return false
    }


    fun setSecondPassPin(value: String) {
        sharedPreferences?.edit()?.putString("secondPassCodePin", value)?.apply()
    }

    fun getSecondPassPin(): String? {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getString("secondPassCodePin", "")
        }
        return ""
    }

    fun isFirst(): Boolean {
        val first = sharedPreferences!!.getBoolean("is_first", true)
        if (first) {
            val editor = sharedPreferences!!.edit()
            editor.putBoolean("is_first", false)
            editor.apply()
        }
        return first
    }

    fun getOvulationEnabled(): Boolean {
        return sharedPreferences!!.getBoolean("ov_enabled", true)
    }

    fun setOvEnabled(enable: Boolean) {
        sharedPreferences?.edit()?.putBoolean("ov_enabled", enable)?.apply()
    }

    fun getPmsEnabled(): Boolean {
        return sharedPreferences!!.getBoolean("pms_enabled", true)
    }

    fun setPmsEnabled(enable: Boolean) {
        sharedPreferences?.edit()?.putBoolean("pms_enabled", enable)?.apply()
    }


}
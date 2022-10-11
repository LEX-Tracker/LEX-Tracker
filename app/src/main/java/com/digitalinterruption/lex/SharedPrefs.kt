package com.digitalinterruption.lex

import android.content.Context
import com.digitalinterruption.lex.data.EncryptSharedPreferences
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SharedPrefs(context: Context) {

    var sharedPreferences = EncryptSharedPreferences.getInstance(context).sharedPreferences
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    companion object {
        lateinit var instance: SharedPrefs
        fun getInstance(context: Context): SharedPrefs? {
            if (instance == null) {
                instance = SharedPrefs(context)
            }
            return instance
        }
    }

    init {
        sharedPreferences = EncryptSharedPreferences.getInstance(context).sharedPreferences

    }


    fun setIsPinSet(value: Boolean) {
        sharedPreferences?.edit()?.putBoolean("PIN_CODE_SET", value)?.apply()
    }

    fun getIsPinSet(): Boolean {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getBoolean("PIN_CODE_SET", false)
        }
        return false
    }

    fun setDuressPin(value: String?){
        if (sharedPreferences != null) {
            sharedPreferences?.edit()?.putString("DURESS_PIN", value)?.apply()
        }
    }

    fun getDuressPin(): String? {
        if (sharedPreferences != null){
            return sharedPreferences!!.getString("DURESS_PIN", "")
        }
        return ""
    }

    fun setDuressPinEnabled(value: Boolean?){
            sharedPreferences?.edit()?.putBoolean("DURESS_PIN_ENABLED", false)?.apply()
    }

    fun getDuressPinEnabled(): Boolean?{
        if (sharedPreferences != null){
            return sharedPreferences!!.getBoolean("DURESS_PIN_ENABLED", false)
        }
        return false
    }

    fun setIncorrectTries(value: Int){
        sharedPreferences?.edit()?.putInt("INCORRECT_TRIES", value)?.apply()
    }

    fun getIncorrectTries(): Int{
        if (sharedPreferences != null){
            return sharedPreferences!!.getInt("INCORRECT_TRIES", 0)
        }
        return 0
    }

    fun setIsLockedOut(value: Boolean){
        sharedPreferences?.edit()?.putBoolean("LOCKED_OUT", value)?.apply()
    }

    fun getIsLockedOut(): Boolean{
        if (sharedPreferences != null){
            return sharedPreferences!!.getBoolean("LOCKED_OUT", false)
        }
        return false
    }

    fun setLockOutDate(value: String){
        val lockoutDate = value
        sharedPreferences?.edit()?.putString("LOCKOUT_UNTIL", lockoutDate)?.apply()
    }

    fun getLockOutDate(): LocalDateTime? {

        if (sharedPreferences != null){
            return LocalDateTime.parse(sharedPreferences.getString("LOCKOUT_UNTIL", "01/01/1970 00:00"),formatter)

        }
        return LocalDateTime.parse("01/01/1970 00:00", formatter)
    }

    fun setPin(value: String) {
        sharedPreferences?.edit()?.putString("passCodePin", value)?.apply()
        this.setIsPinSet(true)
    }

    fun getPin(): String? {
        if (sharedPreferences != null) {
            return sharedPreferences!!.getString("passCodePin", "")
        }
        return ""
    }

    fun setNickName(value: String){
        sharedPreferences?.edit()?.putString("Username", value)?.apply()
    }

    fun getNickName(): String? {
        if (sharedPreferences != null){
            return sharedPreferences!!.getString("Username", "Lex-User")
        }
        return "Lex-User"
    }

    fun setIsDuressPin(value: Boolean) {
        sharedPreferences?.edit()?.putBoolean("IS_DURESS_PIN", value)?.apply()
    }

    fun getIsDuressPin(): Boolean {
        return sharedPreferences!!.getBoolean("IS_DURESS_PIN", false)
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
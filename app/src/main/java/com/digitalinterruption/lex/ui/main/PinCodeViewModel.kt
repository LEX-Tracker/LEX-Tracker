package com.digitalinterruption.lex.ui.main



import android.content.Context
import android.util.Log

import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.helpers.Event
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PinCodeUiState(
    val title: String = "",
    val userMessage: String = ""
)

class PinCodeViewModel(val prefs: SharedPrefs) : ViewModel() {
    private val _navigateScreen = MutableLiveData<Event<Int>>()
    val navigateScreen: LiveData<Event<Int>> = _navigateScreen

    val pinCode = MutableLiveData<String>()
    val securedPinCode = MutableLiveData<String?>()

    val maxIncorrectAttempts = 5
    lateinit var context: Context

    private val _snackboxMessage = MutableSharedFlow<String>()
    val snackboxMessage = _snackboxMessage.asSharedFlow()

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _snackboxMessage.emit(message)
        }
    }

    // TODO: get it to navigate to HomeFragment
    val numPadListener = object : NumPadListener {
        override fun onNumberClicked(number: Char) {
            val existingPinCode = pinCode.value ?: ""
            val newPassCode = existingPinCode + number
            pinCode.postValue(newPassCode)

            val formatter =  DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val currentTimestamp =  LocalDateTime.now()
            var message: String

            if (newPassCode.length == 6) {
                if (
                    prefs.getLockOutDate()?.isBefore(currentTimestamp)!!  &&
                    prefs.getPin()  == newPassCode ||
                    prefs.getDuressPin() == newPassCode) {
                    if (prefs.getDuressPin() == newPassCode){
                        prefs.setIsDuressPin(true)
                        pinCode.postValue("")
                    }else{
                        prefs.setIsDuressPin(false)
                        pinCode.postValue("")
                        prefs.setIncorrectTries(0)
                    }
                    // TODO: navigate to main
                    _navigateScreen.value = Event(R.id.action_PinFragment_to_homeFragment)



                } else{
                    pinCode.postValue("")
                    if (prefs.getLockOutDate()?.isAfter(currentTimestamp)!!){
                        // TODO: locked out bro!
                        message = "Max retries reached app locked until ${prefs.getLockOutDate()}"
                        Log.i("Locked Out!",message)
                    }else{

                    var incorrectAttempts = prefs.getIncorrectTries()

                    if (incorrectAttempts < maxIncorrectAttempts){
                        incorrectAttempts +=1
                        prefs.setIncorrectTries(incorrectAttempts)
                        message = "Incorrect Pin! ${maxIncorrectAttempts - incorrectAttempts } Attempt(s) Remaining!"
                        Log.i("Pin Entry", message)
                    } else {
                        val lockoutTimestamp = currentTimestamp.plusHours(12)
                        prefs.setLockOutDate(lockoutTimestamp.format(formatter))
                        prefs.setIsLockedOut(true)

                        message = "Max retries reached! app locked until ${prefs.getLockOutDate()}"
                        Log.i("Locked Out!",message)
                        }
                    }
                }

            }
        }

        override fun onEraseClicked() {
            val droppedLast = pinCode.value?.dropLast(1) ?: ""
            pinCode.postValue(droppedLast)
        }
    }



}
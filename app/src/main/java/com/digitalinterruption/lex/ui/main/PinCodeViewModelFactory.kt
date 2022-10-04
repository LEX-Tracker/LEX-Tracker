package com.digitalinterruption.lex.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.digitalinterruption.lex.SharedPrefs

class PinCodeViewModelFactory(
    private val sharedPrefs: SharedPrefs
) : ViewModelProvider.Factory{

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PinCodeViewModel(sharedPrefs) as T
    }
}
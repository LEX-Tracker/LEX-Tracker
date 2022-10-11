package com.digitalinterruption.lex.ui.main

import android.content.Context
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.data.EncryptSharedPreferences
import com.digitalinterruption.lex.databinding.FragmentLockBinding

class LockFragment : Fragment() {

    lateinit var binding: FragmentLockBinding

    private lateinit var viewModel: PinCodeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLockBinding.inflate(inflater)
        val sharedPrefs = SharedPrefs(requireContext())

        viewModel = ViewModelProvider(this, PinCodeViewModelFactory(sharedPrefs))
            .get(PinCodeViewModel::class.java)

        binding.lifecycleOwner= viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    companion object {
        fun newInstance() = LockFragment()
    }

}
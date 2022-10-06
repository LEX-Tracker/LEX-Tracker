package com.digitalinterruption.lex.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentRegisterBinding
import com.digitalinterruption.lex.models.MyViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding
    private lateinit var callback: OnBackPressedCallback

    var exit: Boolean = false
    lateinit var countDown: CountDownTimer
    private lateinit var navController: NavController
    lateinit var prefs: SharedPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        prefs = SharedPrefs(requireContext())
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var message: String
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        navController = Navigation.findNavController(view)

        _binding?.btnSignUp?.setOnClickListener(){
            val nickName = _binding?.etName?.text.toString()
            val pinCode = binding?.etPassword?.text.toString()

            if (!nickName.isNullOrEmpty()){
                if (pinCode.isNullOrEmpty() || pinCode?.length!! < 6){
                    message = "Please enter a 6 digit pin"
                }else{
                    prefs.setPin(pinCode)
                    prefs.setNickName(nickName)
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }
            }else{
                message = "You must provide a nickname"
            }
        }
    }

    private fun fragmentBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::callback.isInitialized) {
            callback.isEnabled = false
            callback.remove()
        }
    }
}
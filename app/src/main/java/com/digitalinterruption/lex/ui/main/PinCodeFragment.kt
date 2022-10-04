package com.digitalinterruption.lex.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentPinCodeBinding

class PinCodeFragment : Fragment(){
    lateinit var binding: FragmentPinCodeBinding

    private lateinit var viewModel: PinCodeViewModel
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var countDown: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPinCodeBinding.inflate(inflater)
        val sharedPrefs = SharedPrefs(requireContext())
        viewModel = ViewModelProvider(this, PinCodeViewModelFactory(sharedPrefs))
            .get(PinCodeViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        return binding.root
    }

    private fun fragmentBackPress(){
        callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (exit){
                    requireActivity().finishAndRemoveTask()
                } else{
                    Toast.makeText(
                        context,
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exit = true
                    countDown = object : CountDownTimer(3000,1000){
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            exit = false
                        }
                    }
                }
            }
        }
    }

    companion object{
        fun newInstance() = PinCodeFragment()
    }
}
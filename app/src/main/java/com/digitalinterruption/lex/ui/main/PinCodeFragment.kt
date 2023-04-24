package com.digitalinterruption.lex.ui.main

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentPinCodeBinding
import com.digitalinterruption.lex.helpers.EventObserver


class PinCodeFragment : Fragment(){
    lateinit var binding: FragmentPinCodeBinding

    private lateinit var viewModel: PinCodeViewModel
    private lateinit var callback: OnBackPressedCallback
    private lateinit var navController: NavController
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        viewModel.navigateScreen.observe(requireActivity(), EventObserver{
            navController.navigate(it)
        })
        viewModel.snackboxMessage.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{
                Toast.makeText(requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        })
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
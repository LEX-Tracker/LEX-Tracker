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
        var isSecondaryPin = false
    }
/*
    private var _binding: FragmentLockBinding? = null
    private val binding get() = _binding!!
    private lateinit var callback: OnBackPressedCallback
    var exit: Boolean = false
    lateinit var countDown: CountDownTimer

    private var isPinCreated = false
    private var inPinConfirmView = false
    private var createAuthPin = ""
    var updatePin: Boolean = false
    var oldPinConfirmation: Boolean = false

    lateinit var preferences: SharedPrefs
    lateinit var EncryptSharedPreferences: EncryptSharedPreferences

    val pinCode = MutableLiveData<String>()
    val securedPinCode = MutableLiveData<String>()

    companion object {
        var isSecondaryPin = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = SharedPrefs(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentBackPress()

        // check the preferences value and then call the click method

        init()
        initListeners()
        //pinCodeListeners()


        binding.tvSignUp.setOnClickListener {
            signInUpClicked("SignUp")
            if (!binding.etName.text.isNullOrEmpty()) {
                if (!binding.etPassword.text.isNullOrEmpty() && binding.etPassword.text.length == 6) {
                    preferences.setPassPin(binding.etPassword.text.toString())
                    preferences.setPassCode(true)
                    Toast.makeText(context, "Signed up Successfully", Toast.LENGTH_SHORT).show()
                    signInUpClicked("SignIn")

                } else {

                    binding.etName.text.clear()
                    binding.etPassword.text.clear()
                    Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {

                binding.etName.text.clear()
                binding.etPassword.text.clear()
                Toast.makeText(context, "Enter a valid name to continue", Toast.LENGTH_SHORT).show()
            }

        }
        binding.tvSignUp2.setOnClickListener {
            signInUpClicked("SignUp")
        }
        binding.tvSignIn.setOnClickListener {
            signInUpClicked("SignIn")

        }
    }

    private fun init() {
        isPinCreated = preferences.getPassCode()
        if (isPinCreated) {
            signInUpClicked("SignIn")
        } else {
            signInUpClicked("SignUp")
        }
    }

    private fun initListeners() {
        binding.pinEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 6) {


                    if (preferences.getPassCode()) {
                        if (binding.pinEd.text.isNotEmpty()) {
                            val createdPin = preferences.getPassPin()
                            val secondCreatedPin = preferences.getSecondPassPin()
                            if (createdPin == binding.pinEd.text.toString()) {
                                moveToNextFragment("pinConfirmed")
                            } else if (secondCreatedPin == binding.pinEd.text.toString()) {
                                moveToNextFragment("secondPinConfirmed")
                                isSecondaryPin = true
                            } else {
                                Toast.makeText(context, "Incorrect Pin", Toast.LENGTH_SHORT)
                                    .show()
                                binding.pinEd.text.clear()
                            }
                        } else {
                            Toast.makeText(context, "Enter your 6 digit pin", Toast.LENGTH_SHORT)
                                .show()

                        }
                    }


                } else {

                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun moveToNextFragment(from: String) {

        if (from == "pinConfirmed") {
            if (findNavController().currentDestination?.id == R.id.lockFragment) {
                val action = LockFragmentDirections.actionLockFragmentToHomeFragment()
                findNavController().navigate(action)
            }
        } else if (from == "secondPinConfirmed") {
            if (findNavController().currentDestination?.id == R.id.lockFragment) {
                val action = LockFragmentDirections.actionLockFragmentToHomeFragment("secondPin")
                findNavController().navigate(action)
            }
        }


    }

    private fun signInUpClicked(btnClicked: String) {
        isPinCreated = preferences.getPassCode()
        if (btnClicked == "SignUp") {
            binding.pinEd.text.clear()
            if (isPinCreated) {
                Toast.makeText(context, "Already Signed up!", Toast.LENGTH_SHORT).show()
                binding.groupSignIn.isVisible = true
                binding.groupSignUp.isVisible = false
            } else {
                binding.tvPinDescription.text = getString(R.string.signUpDesc)
                binding.groupSignUp.isVisible = true
                binding.groupSignIn.isVisible = false
                binding.tvSignUp2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                binding.tvSignIn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                if (context != null) {
                    binding.tvSignUp2.setTextColor(
                        ContextCompat.getColor(
                            context!!,
                            R.color.white
                        )
                    );
                    binding.tvSignIn.setTextColor(ContextCompat.getColor(context!!, R.color.text_grey));
                }
            }

        } else if (btnClicked == "SignIn") {
            binding.pinEd.text.clear()
            if (isPinCreated) {
                binding.tvPinDescription.text = getString(R.string.signInDesc)
                binding.groupSignUp.isVisible = false
                binding.groupSignIn.isVisible = true
                binding.tvSignIn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30F)
                binding.tvSignUp2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                if (context != null) {
                    binding.tvSignUp2.setTextColor(ContextCompat.getColor(context!!, R.color.text_grey));
                    binding.tvSignIn.setTextColor(ContextCompat.getColor(context!!, R.color.white));
                }
            } else {
                binding.groupSignUp.isVisible = true
                binding.groupSignIn.isVisible = false
                Toast.makeText(context, "Sign Up to continue", Toast.LENGTH_SHORT).show()
            }

        }

    }
///*

    }*/



    private fun fragmentBackPress() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (exit) {
                    requireActivity().finishAndRemoveTask()
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.press_again_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                    exit = true
                    countDown = object : CountDownTimer(3000, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                        }

                        override fun onFinish() {
                            exit = false
                        }
                    }
                    countDown.start()

                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::callback.isInitialized) {
            callback.isEnabled = false
            callback.remove()
        }
    }
*/
}
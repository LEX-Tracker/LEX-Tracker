package com.digitalinterruption.lex.ui

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
import androidx.navigation.fragment.findNavController
import com.digitalinterruption.lex.R
import com.digitalinterruption.lex.SharedPrefs
import com.digitalinterruption.lex.databinding.FragmentLockBinding

class LockFragment : Fragment() {

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
//        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        fragmentBackPress()

        // check the perfernces value and then call the click method

        init()
        initListeners()
        pinCodeListeners()

        binding.tvSignUp.setOnClickListener {
            signInUpClicked("SignUp")
            if (!binding.etName.text.isNullOrEmpty()) {
                if (!binding.etPassword.text.isNullOrEmpty() && binding.etPassword.text.length == 6) {
                    preferences.setPassPin(binding.etPassword.text.toString())
                    preferences.setPassCode(true)
                    Toast.makeText(context, "Signed up Successfully", Toast.LENGTH_SHORT).show()
                    signInUpClicked("SignIn")

                } else {
                    vibrate()
                    binding.etName.text.clear()
                    binding.etPassword.text.clear()
                    Toast.makeText(context, "Enter 6 digit pin to continue", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                vibrate()
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

                    populateDots(count)

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
                                vibrate()
                                Toast.makeText(context, "Incorrect Pin", Toast.LENGTH_SHORT)
                                    .show()
                                binding.pinEd.text.clear()
                            }
                        } else {
                            Toast.makeText(context, "Enter your 6 digit pin", Toast.LENGTH_SHORT)
                                .show()
                            vibrate()
                        }
                    }

//                    if (!isPinCreated) {
//                        if (!inPinConfirmView) {
//                            createAuthPin = binding.pinEd.text.toString()
//                            binding.tvPinDescription.text = "Re-enter your 6 digit pin to confirm"
//                            inPinConfirmView = true
//                            binding.pinEd.text.clear()
//                        } else {
//                            if (createAuthPin == binding.pinEd.text.toString()) {
//                                Toast.makeText(context, "Lock Enabled", Toast.LENGTH_SHORT)
//                                    .show()
//                                preferences.setPassPin(createAuthPin)
//                                preferences.setPassCode(true)
//                                moveToNextFragment("lockEnabled")
//                            } else {
//                                vibrate()
//                                Toast.makeText(context, "Pin Not Matched", Toast.LENGTH_SHORT)
//                                    .show()
//                                binding.tvPinDescription.text = "Set your 6 digit pin to sign up"
//                                inPinConfirmView = false
//                                binding.pinEd.text.clear()
//                            }
//                        }
//
//                    } else {
//                        if (updatePin) {
//                            if (oldPinConfirmation) {
//                                if (!inPinConfirmView) {
//                                    createAuthPin = binding.pinEd.text.toString()
//                                    binding.tvPinDescription.text =
//                                        "Re-enter your 6 digit pin to confirm"
//                                    inPinConfirmView = true
//                                    binding.pinEd.text.clear()
//                                } else {
//                                    if (createAuthPin == binding.pinEd.text.toString()) {
//                                        Toast.makeText(context, "Pin Updated", Toast.LENGTH_SHORT)
//                                            .show()
//                                        preferences.setPassPin(createAuthPin)
//                                        preferences.setPassCode(true)
//                                        if (findNavController().currentDestination?.id == R.id.lockFragment) {
//                                            findNavController().popBackStack()
//                                        }
//
//                                    } else {
//                                        vibrate()
//                                        Toast.makeText(
//                                            context,
//                                            "Pin Not Matched",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                        binding.tvPinDescription.text =
//                                            "Set your 6 digit pin to sign up"
//                                        inPinConfirmView = false
//                                        binding.pinEd.text.clear()
//                                    }
//                                }
//                            } else {
//                                if (preferences.getPassPin() == binding.pinEd.text.toString()) {
//                                    oldPinConfirmation = true
//                                    binding.tvPinDescription.text = "Enter your new 6 digit pin"
//                                    binding.pinEd.text.clear()
//
//                                } else {
//                                    vibrate()
//                                    Toast.makeText(
//                                        context,
//                                        "Old Pin Not Matched",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                    binding.tvPinDescription.text =
//                                        "Enter your 6 digit pin to sign in"
//                                    inPinConfirmView = false
//                                    binding.pinEd.text.clear()
//                                }
//
//
//                            }
//
//
//                        } else {
//                            //  do if you want user to move automatically
//                            if (binding.pinEd.text.isNotEmpty()) {
//                                val createdPin = preferences.getPassPin()
//                                if (createdPin == binding.pinEd.text.toString()) {
//                                    moveToNextFragment("pinConfirmed")
//                                } else {
//                                    vibrate()
//                                    Toast.makeText(context, "Incorrect Pin", Toast.LENGTH_SHORT)
//                                        .show()
//                                    binding.pinEd.text.clear()
//                                }
//                            } else {
//                                Toast.makeText(context, "Enter pin", Toast.LENGTH_SHORT).show()
//                                vibrate()
//                            }
//                        }
//                    }

                } else {
                    populateDots(count)
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

    private fun pinCodeListeners() {
        binding.keyboard.number0.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}0")
        }
        binding.keyboard.number1.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}1")
        }
        binding.keyboard.number2.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}2")
        }
        binding.keyboard.number3.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}3")
        }
        binding.keyboard.number4.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}4")
        }
        binding.keyboard.number5.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}5")
        }
        binding.keyboard.number6.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}6")
        }
        binding.keyboard.number7.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}7")
        }
        binding.keyboard.number8.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}8")
        }
        binding.keyboard.number9.setOnClickListener {
            binding.pinEd.setText("${binding.pinEd.text}9")
        }
        binding.keyboard.ivDelete.setOnClickListener {
            if (binding.pinEd.text.toString().isNotEmpty()) {
                val length: Int = binding.pinEd.text.length
                if (length > 0) {
                    binding.pinEd.text.delete(length - 1, length)
                    populateDots(binding.pinEd.text.length)
                }
            } else {
                binding.pinEd.text.clear()
                vibrate()
            }
        }
    }

    private fun vibrate() {
        val v = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v!!.vibrate(500)
        }

    }

    private fun populateDots(count: Int) {
        when (count) {
            0 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_off)
                binding.dot2.setImageResource(R.drawable.ic_pin_off)
                binding.dot3.setImageResource(R.drawable.ic_pin_off)
                binding.dot4.setImageResource(R.drawable.ic_pin_off)
                binding.dot5.setImageResource(R.drawable.ic_pin_off)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            1 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_off)
                binding.dot3.setImageResource(R.drawable.ic_pin_off)
                binding.dot4.setImageResource(R.drawable.ic_pin_off)
                binding.dot5.setImageResource(R.drawable.ic_pin_off)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            2 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_on)
                binding.dot3.setImageResource(R.drawable.ic_pin_off)
                binding.dot4.setImageResource(R.drawable.ic_pin_off)
                binding.dot5.setImageResource(R.drawable.ic_pin_off)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            3 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_on)
                binding.dot3.setImageResource(R.drawable.ic_pin_on)
                binding.dot4.setImageResource(R.drawable.ic_pin_off)
                binding.dot5.setImageResource(R.drawable.ic_pin_off)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            4 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_on)
                binding.dot3.setImageResource(R.drawable.ic_pin_on)
                binding.dot4.setImageResource(R.drawable.ic_pin_on)
                binding.dot5.setImageResource(R.drawable.ic_pin_off)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            5 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_on)
                binding.dot3.setImageResource(R.drawable.ic_pin_on)
                binding.dot4.setImageResource(R.drawable.ic_pin_on)
                binding.dot5.setImageResource(R.drawable.ic_pin_on)
                binding.dot6.setImageResource(R.drawable.ic_pin_off)
            }
            6 -> {
                binding.dot1.setImageResource(R.drawable.ic_pin_on)
                binding.dot2.setImageResource(R.drawable.ic_pin_on)
                binding.dot3.setImageResource(R.drawable.ic_pin_on)
                binding.dot4.setImageResource(R.drawable.ic_pin_on)
                binding.dot5.setImageResource(R.drawable.ic_pin_on)
                binding.dot6.setImageResource(R.drawable.ic_pin_on)
            }
        }
    }

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

}
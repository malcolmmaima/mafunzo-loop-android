package com.mafunzo.loop.ui.auth.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.mafunzo.loop.utils.showProgress
import com.google.firebase.auth.*
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentOtpVerificationBinding
import com.mafunzo.loop.ui.auth.AccountDisabledActivity
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {
    val TAG = "OtpVerificationFragment"
    private lateinit var binding: FragmentOtpVerificationBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var tt: TimerTask? = null
    private var userEnabled = false

    // get storedVerificationId from the previous screen
    private val verificationId: String? by lazy {
        requireArguments().getString("storedVerificationId")
    }

    private val storedPhoneNumber: String? by lazy {
        requireArguments().getString("storedPhoneNumber")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyOtpCode(verificationId, storedPhoneNumber)
        initializeObservers()
    }

    private fun initializeObservers() {
        binding.tvOtpNotReceived.gone()
        binding.tvResend.gone()
        startTimer()

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            toggleLoading(isLoading)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isOTPVerified.collectLatest { isVerified ->
                    if (isVerified) {
                        Log.d(TAG, "isVerified: $isVerified")
                        checkUser()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.codeSent.collectLatest { codeSent ->
                    if (codeSent) {
                        binding.root.snackbar("Code sent to $storedPhoneNumber")
                        stopTimer()
                        startTimer()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.errorMessage.collectLatest { errorMessage ->
                    binding.root.snackbar(errorMessage)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.errorMessage.collectLatest { errorMessage ->
                    binding.root.snackbar(errorMessage)
                    binding.tvOtpNotReceived.visible()
                    binding.tvResend.visible()
                    binding.timertv.gone()
                    binding.squareField.text?.clear()
                    binding.verifyButton.hideProgress("VERIFY")
                }
            }
        }
    }

    private fun checkUser() {
        storedPhoneNumber?.let { phoneNumber ->
            Log.d(TAG, "checkUser: $phoneNumber")
            authViewModel.fetchUser(phoneNumber)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userEnabled.collectLatest { enabled ->
                    Log.d("SplashActivity", "User enabled: $userEnabled")
                    userEnabled = enabled
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                authViewModel.userExists.observe(viewLifecycleOwner) { exists ->
                    if (exists && userEnabled) {
                        Log.d(TAG, "User exists and is enabled")
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    else {
                        Log.d(TAG, "User does not exist")
                        findNavController().navigate(
                            R.id.action_otpVerificationFragment2_to_accountSetupFragment2,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.otpVerificationFragment2, false).build()
                        )
                    }
                }
            }
        }
    }

    private fun verifyOtpCode(verificationId: String?, storedPhoneNumber: String?) {
        binding.verifyButton.setOnClickListener {
            if (binding.squareField.text.isNullOrEmpty()) {
                toggleLoading(false)
                binding.root.snackbar("Please Enter OTP")
                return@setOnClickListener
            } else {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verificationId.toString(), binding.squareField.text.toString()
                )
                authViewModel.signInWithPhoneAuthCredential(credential)
            }
        }

        binding.tvResend.setOnClickListener {
            binding.squareField.text?.clear()
            authViewModel.initiateFirebaseCallbacks()
            authViewModel.sendVerificationCode(storedPhoneNumber.toString(), requireActivity())
        }

        //squareField text watcher, do something once the text length is 6
        binding.squareField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s?.length == 6) {
                    binding.root.hideKeyboard()
                    binding.verifyButton.showProgress()
                    toggleLoading(true)
                    val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        verificationId.toString(), binding.squareField.text.toString()
                    )
                    authViewModel.signInWithPhoneAuthCredential(credential)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length != 6) {
                    binding.verifyButton.visible()
                }
            }
        })
    }

    private fun toggleLoading(displayLoading: Boolean) {
        if (displayLoading) {
            binding.verifyButton.enable(false)
            binding.verifyButton.showProgress()
        } else {
            binding.verifyButton.enable(true)
            binding.verifyButton.hideProgress("VERIFY")
        }
    }

    fun startTimer() {
        binding.timertv.visible()
        var timer = Timer()

        tt = object : TimerTask() {
            var second = 60
            override fun run() {
                if (second <= 0) {
                    //run on UI thread
                    activity?.runOnUiThread {
                        binding.tvOtpNotReceived.visible()
                        binding.tvResend.visible()
                        binding.timertv.gone()
                        timer.cancel()
                    }
                } else {
                    activity?.runOnUiThread {
                        var seconds = second--
                        var formattedSeconds = if(seconds < 10) "0$seconds" else seconds.toString()
                        binding.timertv.text = "00:$formattedSeconds"
                    }
                }
            }
        }
        timer.schedule(tt, 0L, 1000)
    }

    private fun stopTimer() {
        tt?.cancel()
    }
}
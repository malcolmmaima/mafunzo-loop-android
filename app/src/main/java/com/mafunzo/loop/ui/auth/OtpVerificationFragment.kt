package com.mafunzo.loop.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.*
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentOtpVerificationBinding
import com.mafunzo.loop.utils.enable
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {
    private lateinit var binding: FragmentOtpVerificationBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var tt: TimerTask? = null

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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isLoading.collectLatest { isLoading ->
                    if (isLoading) {
                        toggleLoading(true)
                    } else {
                        toggleLoading(false)
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isOTPVerified.collect {isVerified ->
                    if (isVerified) {
                        findNavController().navigate(R.id.mainActivity)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.codeSent.collectLatest { codeSent ->
                    if(codeSent){
                        binding.root.snackbar("Code sent to $storedPhoneNumber")
                        stopTimer()
                        startTimer()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.errorMessage.collectLatest {errorMessage ->
                    binding.root.snackbar(errorMessage)
                    binding.tvOtpNotReceived.visible()
                    binding.tvResend.visible()
                    binding.timertv.gone()
                    binding.squareField.text?.clear()
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
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
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
    }

    private fun toggleLoading(displayLoading: Boolean) {
        if (displayLoading) {
            binding.pbOtpVerification.visibility = View.VISIBLE
            binding.verifyButton.enable(false)
        } else {
            binding.pbOtpVerification.visibility = View.GONE
            binding.verifyButton.enable(true)
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
                }
                else {
                    activity?.runOnUiThread {
                        binding.timertv.text = "00:" + second--
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
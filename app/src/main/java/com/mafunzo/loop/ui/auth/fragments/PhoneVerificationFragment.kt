package com.mafunzo.loop.ui.auth.fragments

import android.os.Bundle
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
import com.hbb20.CountryCodePicker
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentPhoneVerificationBinding
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhoneVerificationFragment : Fragment() {
    private lateinit var binding: FragmentPhoneVerificationBinding
    private lateinit var countryCodePicker: CountryCodePicker
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPhoneVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.initiateFirebaseCallbacks()
        validatePhoneNumber()
        initializeObservers()
    }

    private fun initializeObservers() {
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
                authViewModel.verificationId.observe(viewLifecycleOwner) { verificationId ->
                    if (verificationId.isNotEmpty()) {
                        findNavController().navigate(
                            R.id.action_phoneVerificationFragment2_to_passwordVerificationFragment2,
                            Bundle().apply {
                                putString("storedVerificationId", verificationId)
                                putString("storedPhoneNumber", "+${countryCodePicker.fullNumber}")
                            },
                            NavOptions.Builder().setPopUpTo(R.id.phoneVerificationFragment2, true)
                                .build()
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.codeSent.collectLatest { codeSent ->
                    if(codeSent){
                        binding.root.snackbar("Code sent to +${countryCodePicker.fullNumber}")
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
    }

    private fun toggleLoading(displayLoading: Boolean) {
        if (displayLoading) {
            binding.nextButton.enable(false)
            binding.nextButton.showProgress()
        } else {
            binding.nextButton.enable(true)
            binding.nextButton.hideProgress("LOGIN")
        }
    }

    private fun validatePhoneNumber() {
        countryCodePicker = binding.ccp
        countryCodePicker.registerCarrierNumberEditText(binding.etPhoneNumber)
        binding.etPhoneNumber.hideKeyboard()

        binding.nextButton.setOnClickListener {
            Log.d("PhoneVerification", "Phone number: ${countryCodePicker.fullNumber}")

            if (!countryCodePicker.isValidFullNumber) {
                binding.etPhoneNumber.error = "Invalid phone number"
                toggleLoading(false)
                return@setOnClickListener
            }
            //only KE is supported for now
            else if(countryCodePicker.selectedCountryCode != "254"){
                binding.root.snackbar("Only Kenya is supported for now")
                toggleLoading(false)
                return@setOnClickListener
            }
            else {
                toggleLoading(true)
                authViewModel.sendVerificationCode("+${countryCodePicker.fullNumber}", requireActivity())
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.etPhoneNumber.hideKeyboard()
    }

    override fun onResume() {
        super.onResume()
        binding.etPhoneNumber.hideKeyboard()
    }
}
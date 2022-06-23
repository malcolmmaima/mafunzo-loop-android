package com.mafunzo.loop.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.hbb20.CountryCodePicker
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentPhoneVerificationBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PhoneVerificationFragment : Fragment() {
    private lateinit var binding: FragmentPhoneVerificationBinding

    private lateinit var countryCodePicker: CountryCodePicker

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

        validatePhoneNumber()
    }

    private fun toggleLoading(displayLoading: Boolean) {
        if (displayLoading) {
            binding.pbPhoneVerification.visibility = View.VISIBLE
            binding.nextButton.isEnabled = false
        } else {
            binding.pbPhoneVerification.visibility = View.GONE
            binding.nextButton.isEnabled = true
        }
    }

    private fun validatePhoneNumber() {
        countryCodePicker = binding.ccp
        countryCodePicker.registerCarrierNumberEditText(binding.etPhoneNumber)

        binding.nextButton.setOnClickListener {
            toggleLoading(true)

            if (!countryCodePicker.isValidFullNumber) {
                binding.etPhoneNumber.error = "Invalid phone number"

                toggleLoading(false)
                return@setOnClickListener
            }

            toggleLoading(false)
            findNavController().navigate(R.id.action_phoneVerificationFragment2_to_passwordVerificationFragment2)

        }

    }
}
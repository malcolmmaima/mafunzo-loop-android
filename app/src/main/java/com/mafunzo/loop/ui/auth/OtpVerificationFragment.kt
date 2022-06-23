package com.mafunzo.loop.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentOtpVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpVerificationFragment : Fragment() {
    private lateinit var binding: FragmentOtpVerificationBinding

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

        verifyOtpCode()
    }

    private fun verifyOtpCode() {
        binding.verifyButton.setOnClickListener {

            binding.pbOtpVerification.visibility = View.VISIBLE
            binding.verifyButton.isEnabled = false

            if (binding.squareField.text.isNullOrEmpty()) {
                showMessage("Please Enter OTP")
                binding.pbOtpVerification.visibility = View.GONE
                binding.verifyButton.isEnabled = true
                return@setOnClickListener
            }

            toggleLoading(false)
            findNavController().navigate(R.id.mainActivity)

        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun toggleLoading(displayLoading: Boolean) {
        if (displayLoading) {
            binding.pbOtpVerification.visibility = View.VISIBLE
            binding.verifyButton.isEnabled = false
        } else {
            binding.pbOtpVerification.visibility = View.GONE
            binding.verifyButton.isEnabled = true
        }
    }

}
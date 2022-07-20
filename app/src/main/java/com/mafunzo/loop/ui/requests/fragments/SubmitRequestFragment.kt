package com.mafunzo.loop.ui.requests.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.requests.StandardRequest
import com.mafunzo.loop.databinding.FragmentSubmitRequestBinding
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.requests.viewmodels.SubmitRequestViewModel
import com.mafunzo.loop.utils.enable
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.showProgress
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubmitRequestFragment : Fragment() {
    private lateinit var binding: FragmentSubmitRequestBinding
    private val submitRequestViewModel: SubmitRequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmitRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        submitRequest()
        initializeObservers()
    }

    private fun initializeObservers() {
        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.submittedSuccessfully.observe(viewLifecycleOwner){
                    if(it){
                        clearFields()
                        Snackbar.make(binding.root, "Request submitted successfully", Snackbar.LENGTH_LONG).show()
                    } else {
                        Snackbar.make(binding.root, "Request failed to submit", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.requestTypes.observe(viewLifecycleOwner) { requestTypes ->
                    if (requestTypes.isNotEmpty()) {
                        //add default option to spinner
                        val reqTypes = arrayListOf<String>()
                        reqTypes.add("Select Request Type")
                        reqTypes.map {
                            reqTypes.add(it.lowercase())
                        }
                        binding.requestTypeSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            reqTypes
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.loading.observe(viewLifecycleOwner) {
                    if (it) {
                        binding.submitRequestBtn.gone()
                    } else {
                        binding.submitRequestBtn.visible()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.errorMessage.observe(viewLifecycleOwner) {error ->
                    if(error != null) {
                        Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun clearFields() {
        binding.editTextRequestSubject.text.clear()
        binding.editTextRequestDescription.text.clear()
        binding.requestTypeSpinner.setSelection(0)
    }

    private fun submitRequest() {
        submitRequestViewModel.getRequestTypes()
        binding.apply {
            submitRequestBtn.setOnClickListener {

                if (editTextRequestSubject.text.trim().isEmpty()) {
                    editTextRequestSubject.error = getString(R.string.first_name_required)
                    submitRequestBtn.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextRequestDescription.text.trim().length <= 2) {
                    editTextRequestDescription.error = getString(R.string.first_name_too_short)
                    submitRequestBtn.isEnabled = true
                    return@setOnClickListener
                }
                if (requestTypeSpinner.selectedItemPosition == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.request_type_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    submitRequestBtn.isEnabled = true
                    return@setOnClickListener
                }

                val message = editTextRequestDescription.text.trim().toString()
                val subject = requestTypeSpinner.selectedItem.toString().uppercase()
                val createdAt = System.currentTimeMillis()
                val status = getString(R.string.pending)

                this.submitRequestBtn.showProgress()
                this.submitRequestBtn.enable(false)
                val standardRequest = StandardRequest(
                    message,
                    subject,
                    createdAt,
                    status
                )
                submitRequestViewModel.submitRequest(standardRequest)
            }
        }
    }


    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Submit Request"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}
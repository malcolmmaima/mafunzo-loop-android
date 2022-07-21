package com.mafunzo.loop.ui.requests.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.UserRequestResponse
import com.mafunzo.loop.databinding.FragmentViewRequestBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.requests.viewmodel.ViewRequestViewModel
import com.mafunzo.loop.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ViewRequestFragment : Fragment() {

    private lateinit var binding: FragmentViewRequestBinding
    private val viewRequestViewModel: ViewRequestViewModel by viewModels()

    private val request: UserRequestResponse? by lazy {
        arguments?.getParcelable(Constants.REQUEST_STRING_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(request == null) {
            binding.root.snackbar(getString(R.string.no_request_found))
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        setUpView()
        initializeObservers()
    }

    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewRequestViewModel.requestWithdrawn.observe(viewLifecycleOwner){
                    if(it){
                        binding.withdrawRequestBtn.gone()
                        Toast.makeText(context, "Request withdrawn successfully", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewRequestViewModel.loading.observe(viewLifecycleOwner) {
                    if (it) {
                        binding.progressBar.visible()

                    } else {
                        binding.progressBar.gone()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewRequestViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
                    if(error != null) {
                        binding.root.snackbar(error)
                    }
                }
            }
        }
    }

    private fun setUpView() {
        binding.progressBar.gone()
        binding.requestTitleTV.text = request?.subject
        binding.requestTimeTV.text = request?.createdAt?.formatDateTime()
        binding.requestStatusTv.text = request?.status
        binding.requestBodyTV.text = request?.message

        //change binding.requestStatusTv text color
        when (request?.status) {
            Constants.REQUEST_STATUS_PENDING -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.grey))
            }
            Constants.REQUEST_STATUS_PROCESSING -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.colorSecondary))
            }
            Constants.REQUEST_STATUS_APPROVED -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.green))
            }
            Constants.REQUEST_STATUS_CANCELLED -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.red))
            }
            Constants.REQUEST_STATUS_REJECTED -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.red))
            }
            else -> {
                binding.requestStatusTv.setTextColor(binding.root.context.getColor(R.color.grey))
            }
        }

        if(request?.status == Constants.REQUEST_STATUS_PENDING) {
            binding.withdrawRequestBtn.visible()
            binding.withdrawRequestBtn.setOnClickListener {
                showWithdrawDialog()
            }
        } else {
            binding.withdrawRequestBtn.gone()
        }
    }


    fun showWithdrawDialog() {
        MaterialAlertDialogBuilder(binding.root.context)
            .setTitle("Withdraw Request")
            .setMessage("Are you sure you want to withdraw this request?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    viewRequestViewModel.withdrawRequest(request)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setUpToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as MainActivity).supportActionBar?.title = "Request Details"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }
}
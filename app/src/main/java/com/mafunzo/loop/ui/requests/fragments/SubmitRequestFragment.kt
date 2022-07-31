package com.mafunzo.loop.ui.requests.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.requests.StandardRequest
import com.mafunzo.loop.databinding.FragmentSubmitRequestBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.requests.adapters.RequestsAdapter
import com.mafunzo.loop.ui.requests.viewmodel.SubmitRequestViewModel
import com.mafunzo.loop.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubmitRequestFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentSubmitRequestBinding
    private val submitRequestViewModel: SubmitRequestViewModel by viewModels()
    private lateinit var requestsAdapter: RequestsAdapter
    private var submitFormVisible = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSubmitRequestBinding.inflate(inflater, container, false)

        binding.swipeContainer.setOnRefreshListener(this)
        binding.swipeContainer.setColorSchemeResources(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark
        )

        binding.swipeContainer.post {
            binding.swipeContainer.isRefreshing = true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        submitRequest()
        viewRequests()
        initializeObservers()
        setupRequestAdapter()
    }

    private fun viewRequests() {
        submitRequestViewModel.getRequests(5)
        binding.apply {
            tvViewAll.setOnClickListener {
                if(llRequestForm.isVisible){
                    llRequestForm.gone()
                    tvViewAll.text = getString(R.string.submit_new_request)
                } else {
                    llRequestForm.visible()
                    tvViewAll.text = getString(R.string.view_requests)
                }

                submitFormVisible = llRequestForm.isVisible
                submitRequestViewModel.getRequests(null)
            }
        }
    }

    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.submittedSuccessfully.collectLatest {
                    if(it){
                        clearFields()
                        binding.submitRequestBtn.hideProgress(getString(R.string.submit))
                        binding.root.snackbar("Request submitted successfully")
                    } else {
                        binding.root.snackbar("Request failed to submit")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.requestTypes.observe(viewLifecycleOwner) { requestTypes ->
                    if (requestTypes.isNotEmpty()) {
                        //add default option to spinner
                        val reqTypes = arrayListOf<String>()
                        reqTypes.add("Select Request Type")
                        requestTypes.map {
                            reqTypes.add(it.lowercase())
                        }
                        binding.requestTypeSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            reqTypes
                        )
                    } else {
                        binding.requestTypeSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            arrayListOf("No request types available")
                        )
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.loading.observe(viewLifecycleOwner) {
                    if (it) {
                        binding.swipeContainer.isRefreshing = true
                        binding.submitRequestBtn.enable(false)
                    } else {
                        binding.submitRequestBtn.enable(true)
                        binding.swipeContainer.isRefreshing = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.errorMessage.collectLatest {error ->
                    binding.root.snackbar(error)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitRequestViewModel.requests.observe(viewLifecycleOwner) {requests ->
                    if(requests.isNotEmpty()) {
                        binding.rvRequests.visible()
                        requestsAdapter.saveData(requests)
                        binding.rvRequests.scrollToPosition(0)
                        binding.tvViewAll.visible()
                        binding.footerPatternIV.gone()
                    } else {
                        binding.tvViewAll.gone()
                        binding.llRequestForm.visible()
                        binding.tvViewAll.gone()
                        binding.footerPatternIV.visible()
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
                    editTextRequestSubject.error = getString(R.string.subject_required)
                    submitRequestBtn.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextRequestDescription.text.trim().length <= 2) {
                    editTextRequestDescription.error = getString(R.string.description_required)
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

                if(requestTypeSpinner.size > 0) {
                    val message = editTextRequestDescription.text.trim().toString()
                    val subject = editTextRequestSubject.text.trim().toString()
                    val createdAt = System.currentTimeMillis()
                    val status = getString(R.string.pending)
                    val type = requestTypeSpinner.selectedItem.toString().uppercase()

                    this.submitRequestBtn.showProgress()
                    this.submitRequestBtn.enable(false)
                    val standardRequest = StandardRequest(
                        id = "",
                        message,
                        subject,
                        createdAt,
                        status,
                        type
                    )
                    submitRequestViewModel.submitRequest(standardRequest)
                }
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

    private fun setupRequestAdapter() {
        requestsAdapter = RequestsAdapter()
        requestsAdapter.onItemClick { userRequestResponse ->
            findNavController().navigate(R.id.action_requestsFragment_to_viewRequestFragment, Bundle().apply {
                putParcelable(Constants.REQUEST_STRING_KEY, userRequestResponse)
            })
        }

        binding.rvRequests.apply {
            adapter = requestsAdapter
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onRefresh() {
        if(submitFormVisible) {
            submitRequestViewModel.getRequests(5)
        } else {
            submitRequestViewModel.getRequests(null)
        }
    }

    override fun onResume() {
        super.onResume()
        if(submitFormVisible) {
            binding.llRequestForm.visible()
            submitRequestViewModel.getRequests(5)
        } else {
            binding.llRequestForm.gone()
            binding.tvViewAll.text = getString(R.string.submit_new_request)
            submitRequestViewModel.getRequests(null)
        }
    }
}
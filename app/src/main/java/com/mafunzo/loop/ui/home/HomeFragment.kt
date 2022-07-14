package com.mafunzo.loop.ui.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.databinding.FragmentHomeBinding
import com.mafunzo.loop.ui.auth.viewmodel.AuthViewModel
import com.mafunzo.loop.ui.home.viewmodels.HomeViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserDetails()
        getCurrentSchoolWorkSpace()
        initializeHomeWidgets()
        initializeObservers()
        setUpToolbar()
    }

    private fun getCurrentSchoolWorkSpace() {
        homeViewModel.getCurrentWorkspace()
    }

    private fun setUpToolbar() {
        (requireActivity() as MainActivity).setSupportActionBar(binding.mainToolBar)
        binding.mainToolBar.showOverflowMenu()
        (requireActivity() as MainActivity).supportActionBar?.title = ""
    }

    private fun initializeObservers() {
        Log.d("HomeFragment", "initializeObservers")

        //observe current workspace / school name fetched from firestore
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.schoolDetails.observe(viewLifecycleOwner) {schoolDetails ->
                if(schoolDetails != null) {
                    binding.currentWorkspaceText.text = schoolDetails.schoolName
                } else {
                    binding.currentWorkspaceText.text = "No school selected - Refresh"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userDetails.collectLatest {user ->
                    setWidgetValues(user)
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
                homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
                    binding.root.snackbar(errorMessage)
                }
            }
        }
    }

    private fun getUserDetails() {
        //fetch user details from firebase
        authViewModel.userPhoneNumber.let { phoneNumber ->
            if (phoneNumber != null) {
                authViewModel.fetchUser(phoneNumber)
            }
        }
    }

    private fun initializeHomeWidgets() {
        binding.cvAnnouncements.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAnnouncementsFragment())
        }

        binding.cvCalendar.setOnClickListener {
            Toast.makeText(context, "Calendar", Toast.LENGTH_SHORT).show()
        }

        binding.cvRequests.setOnClickListener {
            Toast.makeText(context, "Requests", Toast.LENGTH_SHORT).show()
        }

        binding.cvTeachers.setOnClickListener {
            Toast.makeText(context, "Teachers", Toast.LENGTH_SHORT).show()
        }

        binding.cvSchoolBus.setOnClickListener {
            Toast.makeText(context, "School Bus", Toast.LENGTH_SHORT).show()
        }

        binding.cvContact.setOnClickListener {
            Toast.makeText(context, "Contact", Toast.LENGTH_SHORT).show()
        }

        binding.currentWorkspace.setOnClickListener {
            homeViewModel.getCurrentWorkspace()
        }
    }

    private fun setWidgetValues(user: UserResponse) {
        binding.helloMessageTV.text = "Hi ${user.firstName},"
    }
}
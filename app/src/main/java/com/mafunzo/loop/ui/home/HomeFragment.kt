package com.mafunzo.loop.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.databinding.FragmentHomeBinding
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.ui.home.viewmodel.HomeViewModel
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.splash.SystemOfflineActivity
import com.mafunzo.loop.ui.splash.viewmodel.SplashViewModel
import com.mafunzo.loop.utils.enable
import com.mafunzo.loop.utils.gone
import com.mafunzo.loop.utils.snackbar
import com.mafunzo.loop.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()
    private var currentSchoolName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableAllModules(true)
        checkSystemStatus()
        getUserDetails()
        getCurrentSchoolWorkSpace()
        initializeHomeWidgets()
        initializeObservers()
        setUpToolbar()
    }

    private fun checkSystemStatus() {
        splashViewModel.getSystemSettings()
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
        //observe current workspace / school name fetched from firestore
        binding.currentWorkspaceStatus.gone()
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.schoolDetails.collectLatest {schoolDetails ->
                currentSchoolName = schoolDetails.schoolName.toString()
                binding.currentWorkspaceText.text = currentSchoolName
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.workSpacePresent.collectLatest { workspaceAvailable ->
                if(workspaceAvailable) {
                    disableAllModules(false)
                } else {
                    binding.currentWorkspaceText.text = "No school selected - Refresh"
                    lifecycleScope.launch {
                        delay(2000)
                        homeViewModel.getCurrentWorkspace()
                    }

                    //fetch user details again which saves new workspace id to local storage
                    authViewModel.userPhoneNumber?.let {phonenumber -> authViewModel.fetchUser(phonenumber) }
                    disableAllModules(true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userDetails.collectLatest {user ->
                    setWidgetValues(user)
                    when(user.accountType) {
                        "BUS_DRIVER" -> {
                            binding.cvTimetable.enable(false)
                        }
                        "TEACHER" -> {
                            binding.cvRequests.enable(false)
                        }
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
                homeViewModel.errorMessage.collectLatest { errorMessage ->
                    binding.root.snackbar(errorMessage)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homeViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
                    if(loading) {
                        disableAllModules(true)
                        binding.progressBar.visible()
                    } else {
                        binding.progressBar.gone()

                        lifecycleScope.launch {
                            homeViewModel.workSpaceEnabled.collectLatest { enabled ->
                                Log.d("HomeFragment", "workSpaceEnabled: $enabled")
                                disableAllModules(!enabled)
                                if(!enabled) {
                                    hideCards(true)
                                    binding.currentWorkspaceStatus.visible()
                                } else {
                                    hideCards(false)
                                    binding.currentWorkspaceStatus.gone()
                                }
                            }
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.systemSettings.collectLatest { systemSetts ->
                    val allowedMaintainer = authViewModel.userPhoneNumber?.let {
                        systemSetts.maintainers?.contains(
                            it
                        ) ?: false
                    }
                    if(systemSetts.offline == true && allowedMaintainer == false) {
                        loadMaintenance()
                    }
                }
            }
        }

    }

    private fun loadMaintenance() {
        val intent = Intent(requireActivity(), SystemOfflineActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        activity?.finish()
    }

    private fun disableAllModules(disable: Boolean) {
        binding.cvAnnouncements.isEnabled = !disable
        binding.cvCalendar.isEnabled = !disable
        binding.cvRequests.isEnabled = !disable
        binding.cvTeachers.isEnabled = !disable
        binding.cvSchoolBus.isEnabled = !disable
        binding.cvTimetable.isEnabled = !disable
    }

    private fun getUserDetails() {
        //hide gridlayout items until user details are fetched
        hideCards(true)
        //fetch user details from firebase
        authViewModel.userPhoneNumber.let { phoneNumber ->
            if (phoneNumber != null) {
                authViewModel.fetchUser(phoneNumber)
            }
        }
    }

    private fun hideCards(hide: Boolean) {
        if(hide) {
            binding.cvRequests.gone()
            binding.cvTeachers.gone()
            binding.cvSchoolBus.gone()
            binding.cvTimetable.gone()
        } else {
            binding.cvRequests.visible()
            binding.cvTeachers.visible()
            binding.cvSchoolBus.visible()
            binding.cvTimetable.visible()
        }
    }

    private fun initializeHomeWidgets() {
        binding.cvAnnouncements.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAnnouncementsFragment())
        }

        binding.cvCalendar.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCalendarFragment())
        }

        binding.cvRequests.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToRequestsFragment())
        }

        binding.cvTeachers.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTeachersFragment())
        }

        binding.cvSchoolBus.setOnClickListener {
            binding.root.snackbar(getString(R.string.coming_soon))
        }

        binding.cvTimetable.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTimeTableFragment())
        }

        binding.currentWorkspace.setOnClickListener {
            binding.progressBar.visible()
            homeViewModel.getCurrentWorkspace()
        }
    }

    private fun setWidgetValues(user: UserResponse) {
        binding.helloMessageTV.text = "Hi ${user.firstName},"
    }

    override fun onResume() {
        super.onResume()
        disableAllModules(true)
        homeViewModel.getCurrentWorkspace()
    }
}
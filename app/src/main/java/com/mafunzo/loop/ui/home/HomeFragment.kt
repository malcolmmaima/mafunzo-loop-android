package com.mafunzo.loop.ui.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mafunzo.loop.BuildConfig
import com.mafunzo.loop.R
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.databinding.FragmentHomeBinding
import com.mafunzo.loop.di.Constants
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
    private val userPrefs: AppDatasource by lazy { AppDatasource(requireContext()) }
    private var currentSchoolName = ""
    private var schoolsList: List<SchoolResponse>? = null
    private var myUserDetails: UserResponse? = null

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

        homeViewModel.workSpacePresent.observe(viewLifecycleOwner) { workspaceAvailable ->
            if(workspaceAvailable) {
                disableAllModules(false)
            } else {
                binding.currentWorkspaceText.text = "No school selected - Refresh"
                lifecycleScope.launch {
                    delay(2000)
                    homeViewModel.getCurrentWorkspace()
                }

                //fetch user details again which saves new workspace id to local storage
                //authViewModel.userPhoneNumber?.let {phonenumber -> authViewModel.fetchUser(phonenumber) }
                disableAllModules(true)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.schools.collectLatest { schools ->
                schoolsList = schools
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userDetails.collectLatest {user ->
                    setWidgetValues()

                    //update if firestore user details change
                    if(myUserDetails != user){
                        myUserDetails = user
                    }
                    getSchoolDetails(user.schools)
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
                    }
                }
            }
        }

        homeViewModel.workSpaceEnabled.observe(viewLifecycleOwner) { enabled ->
            disableAllModules(!enabled)
            if(!enabled) {
                hideCards(true)
                binding.currentWorkspaceStatus.visible()
            } else {
                hideCards(false)
                binding.currentWorkspaceStatus.gone()
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
                    if(systemSetts.currentVersionCode > BuildConfig.VERSION_CODE) {
                        forceUpdate(systemSetts.forceUpdate)
                    } else {
                        if(systemSetts.offline == true && allowedMaintainer == false) {
                            loadMaintenance()
                        }
                    }
                }
            }
        }
    }

    private fun getSchoolDetails(schools: HashMap<String, Boolean>?) {
        homeViewModel.mySchools(schools)
    }

    private fun forceUpdate(forceUpdate: Boolean) {
        if(forceUpdate) {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.update_dialog_title))
                .setMessage(getString(R.string.update_message))
                .setPositiveButton("Update") { _, _ ->
                    loadPlaystore()
                    lifecycleScope.launch {
                        authViewModel.signOutUser()
                    }
                }
                .setCancelable(false)
                .show()
        } else {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.update_dialog_title))
                .setMessage(getString(R.string.update_message))
                .setPositiveButton("Update") { _, _ ->
                    loadPlaystore()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    //do nothing
                }
                .setCancelable(true)
                .show()
        }
    }

    private fun loadPlaystore(){
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
                )
            )
        } catch (nfe: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
                )
            )
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
            if (phoneNumber != null && myUserDetails == null) {
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

            schoolsList?.let {
                val schools =
                    it.map { school ->
                        school.schoolName
                    }.toTypedArray()

                val schoolId = it.map { school ->
                    school.id
                }.toTypedArray()

                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Switch Workspace")
                    .setItems(schools) { _, which ->
                        lifecycleScope.launch {
                            myUserDetails?.let { userDetails ->
                                // get school hashmap of selected schoolId from userDetails.schools and check value is true
                                val workSpaceEnabled = userDetails.schools?.get(schoolId[which]) ?: false
                                schoolId[which]?.let { _schoolId -> userPrefs.saveCurrentWorkspace(_schoolId, workSpaceEnabled) }

                                //trigger refresh of home fragment
                                homeViewModel.getCurrentWorkspace()
                            }
                        }
                    }
                    .setCancelable(true)
                    .show()
            }
        }
    }

    private fun setWidgetValues() {
        viewLifecycleOwner.lifecycleScope.launch {
            myUserDetails?.let {
                binding.helloMessageTV.text = "Hi ${it.firstName},"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        disableAllModules(true)
        setWidgetValues()
        homeViewModel.getCurrentWorkspace()
    }
}
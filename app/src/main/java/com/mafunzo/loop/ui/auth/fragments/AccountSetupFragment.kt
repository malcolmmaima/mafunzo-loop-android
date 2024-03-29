package com.mafunzo.loop.ui.auth.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mafunzo.loop.data.models.requests.CreateUserRequest
import com.mafunzo.loop.databinding.FragmentAccountSetupBinding
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import com.mafunzo.loop.utils.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Intent
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.mafunzo.loop.R
import com.mafunzo.loop.data.local.preferences.AppDatasource
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.ui.auth.AuthActivity
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.main.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@AndroidEntryPoint
class AccountSetupFragment : Fragment() {

    private lateinit var binding: FragmentAccountSetupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()
    private val userPrefs: AppDatasource by lazy { AppDatasource(requireContext()) }

    private val schools = arrayListOf<SchoolResponse>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()
        setupAccount()
        initializeObservers()
    }

    private fun setUpToolbar() {
        (requireActivity() as AuthActivity).setSupportActionBar(binding.toolbarWelcome)
        binding.toolbarWelcome.showOverflowMenu()
        (requireActivity() as AuthActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AuthActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as AuthActivity).supportActionBar?.title = getString(R.string.setup_account)
        binding.toolbarWelcome.setNavigationOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupAccount() {
        mainViewModel.getAccountTypes()

        viewLifecycleOwner.lifecycleScope.launch {
            val userLocale = userPrefs.getCurrentUserLocale().first()?.trim()
            if (userLocale != null) {
                mainViewModel.getSchools(userLocale)
            } else {
                mainViewModel.getSchools("KE")
            }
        }

        binding.apply {
            setUpNextButton.setOnClickListener {

                if (editTextTextFirstName.text.trim().isEmpty()) {
                    editTextTextFirstName.error = getString(R.string.first_name_required)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextFirstName.text.trim().length <= 2) {
                    editTextTextFirstName.error = getString(R.string.first_name_too_short)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextSecondName.text.trim().isEmpty()) {
                    editTextTextSecondName.error = getString(R.string.second_name_required)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextSecondName.text.trim().length <= 2) {
                    editTextTextSecondName.error = getString(R.string.second_name_too_short)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextEmailAddress.text.trim().isEmpty()) {
                    editTextEmailAddress.error = getString(R.string.email_required)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (!validateEmail(editTextEmailAddress.text.trim().toString())) {
                    editTextEmailAddress.error = getString(R.string.email_invalid)
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }
                if (accountTypeSpinner.selectedItemPosition == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.account_type_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }

                if(schoolSpinner.selectedItemPosition == 0){
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.school_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    setUpNextButton.isEnabled = true
                    return@setOnClickListener
                }


                if(schools.size > 0) {
                    val firstName = editTextTextFirstName.text.trim().toString()
                    val secondName = editTextTextSecondName.text.trim().toString()
                    val email = editTextEmailAddress.text.trim().toString()
                    val accountType = accountTypeSpinner.selectedItem.toString().uppercase()
                    val school = schools[schoolSpinner.selectedItemId.toInt()]
                    Log.d("AccountSetup", "school: $school")

                    this.setUpNextButton.showProgress()
                    this.setUpNextButton.enable(false)
                    val userDetails = authViewModel.userPhoneNumber?.let { phoneNumber ->
                        CreateUserRequest(
                            phone = phoneNumber,
                            email = email,
                            firstName = firstName,
                            lastName = secondName,
                            profilePic = "",
                            dateCreated = getCurrentTimeInMillis(),
                            accountType = accountType,
                            enabled = true,
                            schools = HashMap<String, Boolean>().apply {
                                school.id?.let { schoolId -> put(schoolId, false) }
                            }
                        )
                    }
                    if (userDetails != null) {
                        registerUser(createUserRequest = userDetails)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.school_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    setUpNextButton.isEnabled = true
                }
            }
        }
    }

    private fun registerUser(createUserRequest: CreateUserRequest) {
        authViewModel.createUser(createUserRequest)
    }

    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userCreated.collectLatest { isCreated ->
                    if (isCreated) {
                        // load MainActivity and clear backstack
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        binding.setUpNextButton.enable(true)
                        binding.setUpNextButton.hideProgress("NEXT")
                        binding.root.snackbar(getString(R.string.error_creating_account))
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visible()
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.accountTypes.collectLatest { accountTypes ->
                    binding.progressBar.gone()
                    if (accountTypes.isNotEmpty()) {
                        //add default option to spinner
                        val accounts = arrayListOf<String>()
                        accounts.add("Select Account Type")
                        accountTypes.map {
                            accounts.add(it.lowercase())
                        }
                        Log.d("AccountSetup", "Account Types Loaded")
                        binding.accountTypeSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            accounts
                        )
                    } else {
                        binding.root.snackbar(getString(R.string.error_loading_account_types))

                        //empty spinner with option "No account types available"
                        val accounts = arrayListOf<String>()
                        accounts.add("No account types available")
                        binding.accountTypeSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            accounts
                        )
                        // wait 3 seconds then mainViewModel.getAccountTypes()
                        delay(3000)
                        mainViewModel.getAccountTypes()
                    }
                }
            }
        }

        mainViewModel.schools.observe(viewLifecycleOwner){ mafunzoSchools ->
            binding.progressBar.gone()
            Log.d("AccountSetup", "Schools Loaded")
            if (mafunzoSchools.isNotEmpty()) {
                schools.clear()
                schools.add(SchoolResponse(schoolName = "Select School"))
                mafunzoSchools.sortedBy { it.schoolName }.forEach {
                    schools.add(it)
                }
                Log.d("AccountSetup", "Schools: $schools")
                binding.schoolSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_spinner_layout,
                    schools.map { it.schoolName }
                )
            } else {
                binding.root.snackbar(getString(R.string.error_loading_schools))
                //empty spinner with option "No schools available"
                val schools = arrayListOf<String>()
                schools.add("No schools available")
                binding.schoolSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    R.layout.drop_down_spinner_layout,
                    schools
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.isLoading.collectLatest { isLoading ->
                    if (isLoading) {
                        binding.setUpNextButton.showProgress()
                        binding.setUpNextButton.enable(false)
                    } else {
                        binding.setUpNextButton.enable(true)
                        binding.setUpNextButton.hideProgress("NEXT")
                    }
                }
            }
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.setUpNextButton.showProgress()
                binding.setUpNextButton.enable(false)
            } else {
                binding.setUpNextButton.enable(true)
                binding.setUpNextButton.hideProgress("NEXT")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.errorMessage.collectLatest { errorMessage ->
                    if (errorMessage.isNotEmpty()) {
                        binding.root.snackbar(errorMessage)
                    }
                }
            }
        }

    }
}

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
import com.mafunzo.loop.data.models.responses.SchoolResponse
import com.mafunzo.loop.ui.auth.AuthActivity
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.ui.main.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountSetupFragment : Fragment() {

    private lateinit var binding: FragmentAccountSetupBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

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
        val deviceLocale = ConfigurationCompat.getLocales(Resources.getSystem().configuration)[0].country
        mainViewModel.getAccountTypes()
        mainViewModel.getSchools(deviceLocale)

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


                val firstName = editTextTextFirstName.text.trim().toString()
                val secondName = editTextTextSecondName.text.trim().toString()
                val email = editTextEmailAddress.text.trim().toString()
                val accountType = accountTypeSpinner.selectedItem.toString().uppercase()
                val school = getSchoolResponse(schoolSpinner.selectedItemId)
                Log.d("AccountSetup", "school: $school")

                this.setUpNextButton.showProgress()
                this.setUpNextButton.enable(false)
                val userDetails = CreateUserRequest(
                    email = email,
                    firstName = firstName,
                    lastName = secondName,
                    profilePic = "",
                    dateCreated = getCurrentTimeInMillis(),
                    accountType = accountType,
                    enabled = false,
                    schools = listOf(school.id)
                )
                registerUser(createUserRequest = userDetails)
            }
        }
    }

    private fun getSchoolResponse(selectedSchoolId: Long): SchoolResponse {
        return schools[selectedSchoolId.toInt()]
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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.accountTypes.collectLatest { accountTypes ->
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
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.schools.collectLatest { mafunzoSchools ->
                    if (mafunzoSchools.isNotEmpty()) {
                        schools.clear()
                        schools.add(SchoolResponse(schoolName = "Select School"))
                        mafunzoSchools.sortedBy { it.schoolName }.forEach {
                            schools.add(it)
                        }
                        binding.schoolSpinner.adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.drop_down_spinner_layout,
                            schools.map { it.schoolName }
                        )
                    }
                }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isLoading.collectLatest { isLoading ->
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

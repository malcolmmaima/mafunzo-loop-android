package com.mafunzo.loop.ui.settings.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.FragmentPersonalDetailsBinding
import com.mafunzo.loop.ui.main.viewmodel.MainViewModel
import com.mafunzo.loop.ui.settings.SettingsActivity
import com.mafunzo.loop.ui.settings.viewmodel.SettingsViewModel
import com.mafunzo.loop.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PersonalDetailsFragment : Fragment() {

    private var _binding: FragmentPersonalDetailsBinding? = null
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var accountType: String = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPersonalDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        initializeObservers()
        initializeListeners()
    }

    private fun initializeListeners() {
        settingsViewModel.fetchUserData()
        binding.saveDetailsButton.enable(false)

        binding.apply {
            saveDetailsButton.setOnClickListener {

                if (editTextTextFirstName.text.trim().isEmpty()) {
                    editTextTextFirstName.error = getString(R.string.first_name_required)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextFirstName.text.trim().length <= 2) {
                    editTextTextFirstName.error = getString(R.string.first_name_too_short)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextSecondName.text.trim().isEmpty()) {
                    editTextTextSecondName.error = getString(R.string.second_name_required)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextTextSecondName.text.trim().length <= 2) {
                    editTextTextSecondName.error = getString(R.string.second_name_too_short)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (editTextEmailAddress.text.trim().isEmpty()) {
                    editTextEmailAddress.error = getString(R.string.email_required)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (!validateEmail(editTextEmailAddress.text.trim().toString())) {
                    editTextEmailAddress.error = getString(R.string.email_invalid)
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }
                if (accountTypeSpinner.selectedItemPosition == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.account_type_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    saveDetailsButton.isEnabled = true
                    return@setOnClickListener
                }

                val firstName = editTextTextFirstName.text.trim().toString()
                val secondName = editTextTextSecondName.text.trim().toString()
                val email = editTextEmailAddress.text.trim().toString()
                val accountType = accountTypeSpinner.selectedItem.toString().uppercase()

                this.saveDetailsButton.showProgress()
                this.saveDetailsButton.enable(false)

                //update fields in firestore
                settingsViewModel.updateUserDetails(firstName, secondName, email, accountType)
            }
        }
    }

    private fun initializeObservers() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.userDetails.collectLatest { user ->
                    binding.editTextTextFirstName.setText(user.firstName)
                    binding.editTextTextSecondName.setText(user.lastName)
                    binding.editTextEmailAddress.setText(user.email)
                    accountType = user.accountType.toString()

                    mainViewModel.getAccountTypes()
                    binding.saveDetailsButton.enable(true)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.accountTypes.collectLatest { accountTypes ->
                    //disabled switching account types for now
                    binding.accountTypeSpinner.isEnabled = false
                    binding.accountTypeSpinner.isClickable = false

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

                        //set default selection
                        if(accountType.isNotEmpty()) {
                            binding.accountTypeSpinner.setSelection(accounts.indexOf(accountType.lowercase()))
                        }
                    } else {
                        binding.root.snackbar(getString(R.string.error_loading_account_types))
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.submittedSuccessfully.collectLatest { submitted ->
                    binding.saveDetailsButton.hideProgress("SAVE")
                    binding.saveDetailsButton.enable(true)
                    if (submitted) {
                        binding.root.snackbar("Details updated successfully")
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.errorMessage.collectLatest { errorMessage ->
                    binding.saveDetailsButton.hideProgress("SAVE")
                    binding.saveDetailsButton.enable(true)
                    if (errorMessage.isNotEmpty()) {
                        binding.root.snackbar(errorMessage)
                    }
                }
            }
        }

    }

    private fun setUpToolbar() {
        setHasOptionsMenu(true)
        (requireActivity() as SettingsActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as SettingsActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as SettingsActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        (requireActivity() as SettingsActivity).supportActionBar?.title = "Personal Details"
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as SettingsActivity).finish()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.fetchUserData()
    }
}
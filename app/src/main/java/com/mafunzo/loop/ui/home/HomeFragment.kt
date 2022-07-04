package com.mafunzo.loop.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mafunzo.loop.data.models.responses.UserResponse
import com.mafunzo.loop.databinding.FragmentHomeBinding
import com.mafunzo.loop.ui.auth.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getUserDetails()
        initializeHomeWidgets()
        initializeObservers()
    }


    private fun initializeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.userDetails.collectLatest {user ->
                    setWidgetValues(user)
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
    }

    private fun setWidgetValues(user: UserResponse) {
        binding.helloMessageTV.text = "Hi ${user.firstName},"
    }
}
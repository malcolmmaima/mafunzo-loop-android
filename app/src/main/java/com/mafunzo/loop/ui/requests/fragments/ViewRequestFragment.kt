package com.mafunzo.loop.ui.requests.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.data.models.responses.UserRequestResponse
import com.mafunzo.loop.databinding.FragmentViewRequestBinding
import com.mafunzo.loop.di.Constants
import com.mafunzo.loop.ui.main.MainActivity
import com.mafunzo.loop.utils.formatDateTime
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ViewRequestFragment : Fragment() {

    private lateinit var binding: FragmentViewRequestBinding
    private val request: UserRequestResponse? by lazy {
        arguments?.getParcelable(Constants.REQUEST_STRING_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(request == null) {
            Snackbar.make(binding.root, getString(R.string.no_request_found), Snackbar.LENGTH_LONG).show()
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
    }

    private fun setUpView() {
        binding.requestTitleTV.text = request?.subject
        binding.requestTimeTV.text = request?.createdAt?.formatDateTime()
        binding.requestBodyTV.text = request?.message
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
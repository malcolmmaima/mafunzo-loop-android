package com.mafunzo.loop.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.text.toSpannable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.ActivityMainBinding
import com.mafunzo.loop.ui.announcements.AnnouncementsFragment
import com.mafunzo.loop.ui.auth.fragments.PhoneVerificationFragment
import com.mafunzo.loop.ui.auth.viewmodel.AuthViewModel
import com.mafunzo.loop.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMain()
    }

    private fun initializeMain() {
        //authviewmodel observers
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.loggedOut.collectLatest { loggedOut ->
                    if (loggedOut) {
                        //clear backstack and navigate to login
                        supportFragmentManager.popBackStack(
                            null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                        )
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.mainFragment, PhoneVerificationFragment())
                            .commit()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)
        //menu item text color
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val span = item.title.toSpannable()
            span.setSpan(ForegroundColorSpan(resources.getColor(R.color.black, theme)), 0, span.length, 0)
            item.title = span
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_logout -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    authViewModel.signOutUser()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
package com.mafunzo.loop.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.text.toSpannable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.ActivityMainBinding
import com.mafunzo.loop.ui.auth.viewmodel.AuthViewModel
import com.mafunzo.loop.ui.settings.SettingsActivity
import com.mafunzo.loop.ui.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
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
                        //clear backstack and navigate SplashActivity
                        val intent = Intent(this@MainActivity, SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
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

    fun dialogLogout() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    authViewModel.signOutUser()
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.nav_logout -> {
                dialogLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
package com.mafunzo.loop.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.mafunzo.loop.R
import com.mafunzo.loop.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeMain()
    }

    private fun initializeMain() {
        supportFragmentManager.popBackStack(null,
            androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)

        Toast.makeText(this, "MainActivity", Toast.LENGTH_SHORT).show()
    }
}
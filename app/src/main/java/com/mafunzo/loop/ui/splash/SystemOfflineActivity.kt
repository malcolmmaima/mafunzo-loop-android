package com.mafunzo.loop.ui.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mafunzo.loop.databinding.ActivitySystemOfflineBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SystemOfflineActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySystemOfflineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySystemOfflineBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
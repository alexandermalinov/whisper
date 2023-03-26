package com.example.whisper.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.whisper.R
import com.example.whisper.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navigationController: NavController
    private lateinit var dataBinding: ActivityMainBinding

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDataBinding()
        initNavController()
    }

    override fun onSupportNavigateUp(): Boolean = navigationController.navigateUp()

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun initDataBinding() {
        dataBinding = DataBindingUtil.setContentView(
            this@MainActivity,
            R.layout.activity_main
        )
    }

    private fun initNavController() {
        val navHostController = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navigationController = navHostController.navController
    }
}
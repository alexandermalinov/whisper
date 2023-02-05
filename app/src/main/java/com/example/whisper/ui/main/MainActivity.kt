package com.example.whisper.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.whisper.R
import com.example.whisper.databinding.ActivityMainBinding
import com.example.whisper.utils.common.*
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
        initBottomNavigation()
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
        /*navigationController = Navigation.findNavController(
            this,
            R.id.nav_host_fragment
        )*/
    }

    private fun initBottomNavigation() {
        dataBinding
            .bottomNavigationMenu
            .setupWithNavController(navigationController)
        viewModel
            .setBottomNavigationVisibility(
                navigationController,
                dataBinding.bottomNavigationMenu
            )
    }
}
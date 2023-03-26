package com.example.whisper.ui.basecontacts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.whisper.R
import com.example.whisper.databinding.FragmentBaseContactsBinding
import com.example.whisper.ui.base.BaseFragment
import com.example.whisper.utils.common.collectState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BaseContactsFragment : BaseFragment<FragmentBaseContactsBinding>() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    private val viewModel: BaseContactsViewModel by viewModels()

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.presenter = viewModel
        initBottomNavigation()
        initViewPager()
        collectUiStates()
        observeNavigation(viewModel.navigationFlow)
    }

    override fun getLayoutId(): Int = R.layout.fragment_base_contacts

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private fun collectUiStates() {
        collectState {
            viewModel.uiModel.collect { uiModel ->
                dataBinding.model = uiModel
            }
        }
    }

    private fun initViewPager() {
        dataBinding.viewPager.apply {
            adapter = ViewPagerAdapter(this@BaseContactsFragment)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.setCurrentBottomNavigationItem(position)
                }
            })
        }
    }

    private fun initBottomNavigation() {
        dataBinding.bottomNavigationMenu.apply {
            setupWithNavController(findNavController())
            setOnItemSelectedListener {
                viewModel.setCurrentPage(it.itemId)
                return@setOnItemSelectedListener true
            }
            itemIconTintList = null
        }
    }
}
package com.example.whisper.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.example.whisper.navigation.Destination
import com.example.whisper.navigation.navigate
import com.example.whisper.ui.utils.dialogs.showDialog
import com.example.whisper.ui.utils.menu.MenuUiModel
import com.example.whisper.ui.utils.menu.showMenu
import com.example.whisper.utils.common.collectLatestFlow
import com.example.whisper.vo.dialogs.Dialog
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    /* --------------------------------------------------------------------------------------------
     * Properties
    ---------------------------------------------------------------------------------------------*/
    protected lateinit var dataBinding: T

    /* --------------------------------------------------------------------------------------------
     * Override
    ---------------------------------------------------------------------------------------------*/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataBinding = DataBindingUtil.inflate(
            inflater,
            getLayoutId(),
            container,
            false
        )
        return dataBinding.root
    }

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    abstract fun getLayoutId(): Int

    /* --------------------------------------------------------------------------------------------
     * Protected
    ---------------------------------------------------------------------------------------------*/
    protected fun collectNavigation(navigationFlow: SharedFlow<Destination>) {
        collectLatestFlow(navigationFlow) { destination ->
            navigate(destination)
        }
    }

    protected fun collectMenuFlow(
        menuFlow: SharedFlow<MenuUiModel>,
        menuIcon: View
    ) {
        collectLatestFlow(menuFlow) { menu ->
            requireActivity().showMenu(menu, menuIcon)
        }
    }

    protected fun collectDialogFlow(dialogFlow: SharedFlow<Dialog>) {
        collectLatestFlow(dialogFlow) { dialog ->
            showDialog(dialog)
        }
    }
}
package com.gigforce.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.navOptions
import com.gigforce.core.R
import com.gigforce.core.logger.GigforceLogger

abstract class BaseDialogFragment<V : ViewDataBinding>(
    private val fragmentName: String,
    @LayoutRes private val layoutId: Int
) : DialogFragment() {

    val logTag: String
        get() {
            return fragmentName
        }

    val logger: GigforceLogger = GigforceLogger()
    private lateinit var _viewDataBinding: V

    val viewBinding: V
        get() {
            return _viewDataBinding
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logger.d(fragmentName, "onCreateView()")
        _viewDataBinding = DataBindingUtil.inflate(
            inflater,
            layoutId,
            container,
            false
        )

        return _viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logger.d(fragmentName, "viewCreated()")
        viewCreated(_viewDataBinding, savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        logger.d(fragmentName, "onSaveInstanceState()")
    }

    override fun onResume() {
        super.onResume()
        logger.d(fragmentName, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        logger.d(fragmentName, "onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.d(fragmentName, "onDestroy()")
    }

    abstract fun viewCreated(
        viewBinding: V,
        savedInstanceState: Bundle?
    )

    fun getNavOptions() = navOptions {
        this.anim {
            this.enter = R.anim.nav_default_enter_anim
            this.exit = R.anim.nav_default_exit_anim
            this.popEnter = R.anim.nav_default_pop_enter_anim
            this.popExit = R.anim.nav_default_pop_exit_anim
        }
    }
}
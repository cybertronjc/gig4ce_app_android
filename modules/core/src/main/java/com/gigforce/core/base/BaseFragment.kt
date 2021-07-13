package com.gigforce.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.gigforce.core.logger.GigforceLogger

abstract class BaseFragment2<V : ViewDataBinding>(
    private val fragmentName: String,
    @LayoutRes private val layoutId: Int,
    @ColorRes private val statusBarColor: Int
) : Fragment() {

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
        viewCreated(_viewDataBinding)
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
        viewBinding: V
    )
}
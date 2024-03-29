package com.gigforce.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.navOptions
import com.gigforce.core.R
import com.gigforce.core.logger.GigforceLogger
import com.jaeger.library.StatusBarUtil

abstract class BaseFragment2<V : ViewDataBinding>(
    private val fragmentName: String,
    @LayoutRes private val layoutId: Int,
    @ColorRes private val statusBarColor: Int
) : Fragment() {

    companion object {
        const val INTENT_EXTRA_TOOLBAR_TITLE = "title"
    }

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

    private var _viewCreatedForTheFirstTime = false
    val viewCreatedForTheFirstTime get() = _viewCreatedForTheFirstTime

    private var toolbarTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            toolbarTitle = it.getString(INTENT_EXTRA_TOOLBAR_TITLE)
        } ?: savedInstanceState?.let {
            toolbarTitle = it.getString(INTENT_EXTRA_TOOLBAR_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        logger.d(fragmentName, "onCreateView()")

        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, statusBarColor, null)
        )

        if (shouldPreventViewRecreationOnNavigation()) {
            if (::_viewDataBinding.isInitialized.not()) {
                _viewCreatedForTheFirstTime = true

                _viewDataBinding = DataBindingUtil.inflate(
                    inflater,
                    layoutId,
                    container,
                    false
                )
            } else {
                _viewCreatedForTheFirstTime = false
            }
        } else {

            _viewDataBinding = DataBindingUtil.inflate(
                inflater,
                layoutId,
                container,
                false
            )
        }

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
        outState.putString(
            INTENT_EXTRA_TOOLBAR_TITLE,
            toolbarTitle
        )
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

    open fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return false
    }

    abstract fun viewCreated(
        viewBinding: V,
        savedInstanceState: Bundle?
    )

    fun getToolBarTitleReceivedFromPreviousScreen(): String? {
        return toolbarTitle
    }

    fun getNavOptions() = navOptions {
        this.anim {
            this.enter = R.anim.anim_enter_from_right
            this.exit = R.anim.anim_exit_to_left
            this.popEnter = R.anim.anim_enter_from_left
            this.popExit = R.anim.anim_exit_to_right
        }
    }
}
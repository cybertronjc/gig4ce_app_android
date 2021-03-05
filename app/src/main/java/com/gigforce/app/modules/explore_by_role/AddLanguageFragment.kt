package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.core.datamodels.profile.Language
import com.gigforce.common_ui.decors.AddLangugeRvItemDecorator
import com.gigforce.common_ui.StringConstants
import kotlinx.android.synthetic.main.layout_fragment_add_language.*

class AddLanguageFragment : BaseFragment(), AdapterAddLanguage.AdapterAddLanguageCallbacks {
    private lateinit var win: Window
    val addLanguageViewModel: AddLanguageViewModel by activityViewModels<AddLanguageViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_add_language, inflater, container)
    }

    override fun onBackPressed(): Boolean {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        return super.onBackPressed()

    }

    private var adapter: AdapterAddLanguage? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
        initObservers()
        initClicks()
    }

    private fun initClicks() {
        iv_close_add_bio.setOnClickListener {
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )
            popBackState()
        }
    }

    private fun initObservers() {
        addLanguageViewModel.observableSuccess.observe(viewLifecycleOwner, Observer {
            pb_add_language.gone()
            if (it == "true") {
                navFragmentsData?.setData(bundleOf(StringConstants.MOVE_TO_NEXT_STEP.value to true))

                popBackState()
            } else {
                showToast(it!!)
            }
        })
    }

    private fun setUpRecycler() {
        rv_add_language.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_language.addItemDecoration(
            AddLangugeRvItemDecorator(
                requireContext()
            )
        )
        adapter = AdapterAddLanguage()
        rv_add_language.adapter = adapter
        adapter?.addData(mutableListOf(Language()))
        adapter?.setCallbacks(this)


    }

    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(requireActivity().getColor(R.color.white))
        }
    }


    private fun restoreStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onStart() {
        super.onStart()
        makeStatusBarTransparent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreStatusBar()
    }

    override fun submitClicked(items: MutableList<Language>) {
        var submitLanguages = true
        for (i in 0 until items.size) {
            if (items[i].name.isEmpty()) {
                items[i].validateFields = true
                submitLanguages = false

            }
        }
        adapter?.notifyItemRangeChanged(0, items.size)

        if (submitLanguages) {
            pb_add_language.visible()
            addLanguageViewModel.addLanguages(items)
        }
    }

    override fun goBack() {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        popBackState()
    }

}
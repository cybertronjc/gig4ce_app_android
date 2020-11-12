package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_add_bio_fragment.*


class AddBioFragment : BaseFragment() {
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(AddBioViewModel(AddBioRepository()))
    }
    private val viewModel: AddBioViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(AddBioViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_bio_fragment, inflater, container)
    }

    private lateinit var win: Window
    var text: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        initObservers()
    }

    private fun initObservers() {
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        viewModel.observableAddBioResponse.observe(viewLifecycleOwner, Observer {
            pb_add_bio.gone()
            popBackState()
        })
    }

    private fun initClicks() {
        iv_close_add_bio.setOnClickListener {
            popBackState()
        }
        PushDownAnim.setPushDownAnimTo(tv_save_add_bio).setOnClickListener(View.OnClickListener {
            pb_add_bio.visible()
            viewModel.saveBio(et_add_bio.text.toString())
        })
        et_add_bio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                text = s.toString()


            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                tv_save_add_bio.alpha = if (text.isNotEmpty()) 1.0f else 0.4f
                tv_save_add_bio.isEnabled = text.isNotEmpty()


            }

            override fun afterTextChanged(s: Editable) {
                if (et_add_bio.lineCount > 4) {
                    et_add_bio.setText(text);
                    val length = et_add_bio.text.toString().length
                    et_add_bio.setSelection(length)
                }
            }
        })
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

}
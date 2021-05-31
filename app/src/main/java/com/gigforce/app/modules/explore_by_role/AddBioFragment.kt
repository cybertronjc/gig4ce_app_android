package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.layout_add_bio_fragment.*


class AddBioFragment : BaseFragment() {
    private var FROM_CLIENT_ACTIVATION: Boolean = false
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(
            AddBioViewModel(
                AddBioRepository()
            )
        )
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
        getDataFromIntents(savedInstanceState)
        initClicks()
        initObservers()
    }

    private fun initObservers() {
        if (FROM_CLIENT_ACTIVATION) {
            iv_close_add_bio.setImageResource(R.drawable.ic_arrow_back_24)
            iv_close_add_bio.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
            viewModel.observableProfileData.observe(viewLifecycleOwner, Observer {
                et_add_bio.setText(it?.aboutMe ?: "")
            })
            viewModel.getProfileData()
        }

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)
        })
        viewModel.observableAddBioResponse.observe(viewLifecycleOwner, Observer {
            pb_add_bio.gone()
            navFragmentsData?.setData(bundleOf(StringConstants.MOVE_TO_NEXT_STEP.value to true))
            popBackState()
        })
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
        PushDownAnim.setPushDownAnimTo(tv_save_add_bio).setOnClickListener(View.OnClickListener {
            pb_add_bio.visible()
            viewModel.saveBio(et_add_bio.text.toString())
        })
        PushDownAnim.setPushDownAnimTo(tv_cancel_add_bio).setOnClickListener(View.OnClickListener {
            navFragmentsData?.setData(
                    bundleOf(
                            StringConstants.BACK_PRESSED.value to true

                    )
            )
            popBackState()
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

    override fun onBackPressed(): Boolean {
//        if (FROM_CLIENT_ACTIVATION) {
            navFragmentsData?.setData(
                    bundleOf(
                            StringConstants.BACK_PRESSED.value to true

                    )
            )
//        }

        return super.onBackPressed()
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

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            FROM_CLIENT_ACTIVATION = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)


        }

        arguments?.let {
            FROM_CLIENT_ACTIVATION = it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)


        }
    }


}
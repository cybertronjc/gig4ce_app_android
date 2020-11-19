package com.gigforce.app.modules.client_activation

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.explore_by_role.AdapterPreferredLocation
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_fragment_client_activation.*
import kotlinx.android.synthetic.main.layout_role_description.view.*

class ClientActivationFragment : BaseFragment() {
    private lateinit var mWordOrderID: String
    private val viewModel: ClientActivationViewmodel by viewModels()
    private val adapterPreferredLocation: AdapterPreferredLocation by lazy {
        AdapterPreferredLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_client_activation, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        setupPreferredLocationRv()
        initObservers()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
        }
    }

    private fun initObservers() {
        viewModel.observableWorkOrder.observe(viewLifecycleOwner, Observer {
            tv_role_client_activation.text = it?.work_order_title;
            it?.locations?.map { item -> item.location }?.let { locations ->
                adapterPreferredLocation.addData(locations)
            }
            tv_earning_client_activation.text = it?.payoutNote
            val viewRoleDesc = layoutInflater.inflate(R.layout.layout_role_description, null)
            it?.queries?.forEach { element ->
                viewRoleDesc.tv_what_client_activation.text = element.query
                viewRoleDesc.tv_what_value_client_activation.text = element.answer
                GlideApp.with(requireContext())
                    .load(element.icon)
                    .placeholder(getCircularProgressDrawable())
                    .into(viewRoleDesc.iv_what)
                ll_role_desc.addView(viewRoleDesc)
            }
            tv_requirements_client_activation.setOnClickListener { view ->
                run {
                    setOnExpandListener(
                        it?.requirments?.map { it ->
                            var appString = ""
                            it.detail?.forEach { detail -> appString += ("<br>$detail") }
                            "<b>" + it.title + "</b>" + appString;
                        }, tl_requirements_role_details_client_activation,
                        tv_requirements_client_activation
                    )
                }

            }
            tv_responsibilities_client_activation.setOnClickListener { view ->
                run {
                    setOnExpandListener(
                        it?.responsibilties, tl_responsibilities_client_activation,
                        tv_responsibilities_client_activation
                    )
                }

            }
            tv_faqs_client_activation.setOnClickListener { view ->
                run {
                    setOnExpandListener(
                        it?.faqs?.questions?.map { elem ->
                            "<font color=\"#000000\">" + elem.question + "</font> <br> <font color=\"#888888\">" + elem.question + "</font> "

                        }, tl_faqs_client_activation,
                        tv_faqs_client_activation
                    )
                }

            }


        })
        viewModel.getWorkOrder(docID = mWordOrderID)

    }

    private fun setupPreferredLocationRv() {
        rv_preferred_locations_client_activation.adapter = adapterPreferredLocation
        rv_preferred_locations_client_activation.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rv_preferred_locations_client_activation.addItemDecoration(
            HorizontaltemDecoration(
                requireContext(),
                R.dimen.size_11
            )
        )

    }

    fun setOnExpandListener(role: List<String>?, layout: TableLayout, textView: TextView) {
        if (layout.childCount > 0) {
            layout.removeAllViews()
            textView.setCompoundDrawablesWithIntrinsicBounds(
                textView.compoundDrawables[0],
                null,
                resources.getDrawable(R.drawable.ic_keyboard_arrow_down_c7c7cc),
                null
            )

        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(
                textView.compoundDrawables[0],
                null,
                resources.getDrawable(R.drawable.ic_baseline_keyboard_arrow_up_c7c7c7),
                null
            )
            addBulletsTill(
                0,
                if (role?.size!! > 2) 1 else role.size!! - 1,
                layout,
                role,
                true
            )
            if (role?.size!! > 2) {
                val moreTextView = AppCompatTextView(requireContext())
                moreTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    14F
                )
                moreTextView.setTextColor(resources.getColor(R.color.lipstick))
                moreTextView.text = getString(R.string.plus_more)
                val face =
                    Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
                moreTextView.typeface = face
                moreTextView.setPadding(resources.getDimensionPixelSize(R.dimen.size_16), 0, 0, 0)

                layout.addView(moreTextView)
                moreTextView.setOnClickListener {
                    layout.removeViewInLayout(moreTextView)
                    addBulletsTill(
                        2,
                        role.size!! - 1,
                        layout,
                        role,
                        false
                    )
                }
            }
        }

    }

    fun addBulletsTill(
        from: Int,
        to: Int,
        layout: TableLayout,
        arr: List<String>?,
        removeAllViews: Boolean
    ) {
        if (removeAllViews)
            layout.removeAllViews()
        for (i in from..to) {

            val iv = ImageView(requireContext())
            val layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(
                0,
                resources.getDimensionPixelSize(R.dimen.font_9),
                resources.getDimensionPixelSize(R.dimen.size_8),
                0
            )
            iv.layoutParams = layoutParams
            iv.setImageResource(R.drawable.shape_circle_lipstick)
            val textView = TextView(requireContext())
            val face =
                Typeface.createFromAsset(requireActivity().assets, "fonts/Lato-Regular.ttf")
            textView.typeface = face
            textView.layoutParams = TableRow.LayoutParams(
                getScreenWidth(requireActivity()).width - (resources.getDimensionPixelSize(R.dimen.size_66)),
                TableRow.LayoutParams.WRAP_CONTENT
            )

            textView.setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                18F
            )
            textView.text = Html.fromHtml(arr?.get(i))

            textView.setTextColor(resources.getColor(R.color.black))
            val tr = TableRow(requireContext())


            tr.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

            tr.addView(iv)
            tr.addView(textView)
            layout.addView(
                tr,
                TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
            )

        }
    }
}
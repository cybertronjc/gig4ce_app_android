package com.gigforce.verification.mainverification.compliance.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.gigforce.common_ui.views.GigforceImageView
import com.gigforce.core.ICustomClickListener
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.recyclerView.ItemClickListener
import com.gigforce.verification.R
import com.gigforce.verification.databinding.ComplianceDocDetailsComponentBinding
import com.gigforce.verification.mainverification.compliance.models.ComplianceDocDetailsDM
import com.gigforce.verification.mainverification.compliance.models.DataListItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ComplianceDocDetailsComponent (context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder, ICustomClickListener, View.OnClickListener {

    @Inject
    lateinit var navigation: INavigation

    private var viewBinding: ComplianceDocDetailsComponentBinding


    init {
        viewBinding = ComplianceDocDetailsComponentBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
//        this.layoutParams =
//            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        view = LayoutInflater.from(context)
//            .inflate(R.layout.compliance_doc_details_component, this, true)
        attrs?.let {
            val styledAttributeSet =
                context.obtainStyledAttributes(it, R.styleable.SimpleCardComponent1, 0, 0)
            val title =
                styledAttributeSet.getString(R.styleable.SimpleCardComponent1_title_scc) ?: ""
        }
        //viewBinding.cardView.setOnClickListener(this)
    }



    override fun onClickListener(itemClickListener: ItemClickListener?) {
            //Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun bind(data: Any?) {
        if (data is ComplianceDocDetailsDM) {
            Log.d("ComplianceComp", "name: ${data.name}")
            viewBinding.docTitle.setText(data.name)
//            if (data.type == "form_2" || data.type == "form_11") {
//            } else if (data.type == "uan" || data.type == "esic" || data.type == "pf"){
//                viewBinding.copyIcon.visible()
//            } else if (data.type == "offer_letter"){
//                viewBinding.copyIcon.gone()
//                viewBinding.docTitle.gone()
//            }
        }
    }

    override fun onClick(p0: View?) {
        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
    }

}
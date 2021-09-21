package com.gigforce.common_ui.components.cells

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.gigforce.common_ui.R
import com.gigforce.common_ui.viewdatamodels.AssessmentCardDVM
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class AssessmentCardComponent(context: Context, attributeSet: AttributeSet?) : FrameLayout(context, attributeSet),
        IViewHolder {

    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.view_assessment_card_layout, this, true)

    }

    //Assessment
    val assessmentRootLayout = this.findViewById<MaterialCardView>(R.id.card_view)
    val assessmentTitle = this.findViewById<TextView>(R.id.title)
    val assessmentStatus = this.findViewById<TextView>(R.id.status)
    val asessmentSideStausStrip = this.findViewById<MaterialCardView>(R.id.side_bar_status)
    val assessmentEstTime = this.findViewById<TextView>(R.id.time)

    @Inject
    lateinit var navigation: INavigation

    override fun bind(data: Any?) {
        if (data is AssessmentCardDVM) {
            assessmentRootLayout.setOnClickListener(null)
            assessmentRootLayout.setOnClickListener {
                data.navPath?.let { navPath ->
                    navigation.navigateTo(
                            navPath, data.args
                    )
                }

            }
            assessmentTitle.text = data.title
            assessmentEstTime.text = data.videoLengthString

            if (data.completed) {
                assessmentStatus.text = "COMPLETED"
                assessmentStatus.setBackgroundResource(R.drawable.rect_assessment_status_completed)
                asessmentSideStausStrip.setCardBackgroundColor(
                        ResourcesCompat.getColor(
                                context.resources,
                                R.color.status_bg_completed,
                                null
                        )
                )
            } else {
                assessmentStatus.text = "PENDING"
                assessmentStatus.setBackgroundResource(R.drawable.rect_assessment_status_pending)
                asessmentSideStausStrip.setCardBackgroundColor(
                        ResourcesCompat.getColor(
                                context.resources,
                                R.color.status_bg_pending,
                                null
                        )
                )
            }
        }
    }


}
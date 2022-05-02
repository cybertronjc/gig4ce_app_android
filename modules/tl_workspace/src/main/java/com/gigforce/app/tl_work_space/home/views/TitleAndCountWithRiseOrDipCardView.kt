package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextSwitcher
import android.widget.TextView
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.android_common_utils.extensions.setTextSkipAnimationIfTextIsTheSame
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.CardviewTitleCountRiseDipBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceCardItem
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.extensions.capitalizeWords
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TitleAndCountWithRiseOrDipCardView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
) {

    private var onCardClickListener: (() -> Unit)? = null
    private val viewBinding: CardviewTitleCountRiseDipBinding

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)
        viewBinding = CardviewTitleCountRiseDipBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.textSwitcher.setFactory {
            inflate(
                context,
                R.layout.cardview_title_count_rise_dip_textswitcher_textview,
                null
            ) as TextView
        }
    }

    fun setOnClickListener(
        listener: () -> Unit
    ) {
        this.onCardClickListener = listener
    }

    fun bind(
        data: TLWorkspaceCardItem
    ) = viewBinding.apply {

        data.title.capitalizeWords()
        this.titleTextview.text = data.title.capitalizeFirstLetter()
        this.textSwitcher.setTextSkipAnimationIfTextIsTheSame(
            data.value.toString()
        )

        if (data.changeType == ValueChangeType.UNCHANGED) {
            this.valueRiseDipIndicator.gone()
        } else if (data.changeType == ValueChangeType.INCREMENT) {
            this.valueRiseDipIndicator.visible()
            this.valueRiseDipIndicator.showTextWithRiseIndicator(
                data.valueChangedBy.toString()
            )
        } else {
            this.valueRiseDipIndicator.visible()
            this.valueRiseDipIndicator.showTextWithDipIndicator(
                data.valueChangedBy.toString()
            )
        }
    }

}
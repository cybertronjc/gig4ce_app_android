package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceCardItem
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
    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)
    }

    private var onCardClickListener  : (() -> Unit)? = null

    fun setOnClickListener(
        listener : () -> Unit
    ){
      this.onCardClickListener = listener
    }

    fun bind(
        data : TLWorkspaceCardItem
    ){

    }
}
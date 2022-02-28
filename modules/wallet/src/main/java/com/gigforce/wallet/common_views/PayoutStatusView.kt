package com.gigforce.wallet.common_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.RecyclerRowMonthYearHeaderItemViewBinding
import com.gigforce.wallet.databinding.RecyclerRowPayoutItemBinding
import com.gigforce.wallet.models.PayoutListPresentationItemData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class PayoutStatusView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
) {

    private lateinit var textView: TextView

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowMonthYearHeaderItemViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        textView = findViewById(R.id.statusTextView)
    }

    fun bind(
        status: String,
        statusColorCode: String
    ) {
        textView.text = status

        var background = textView.background
        background = DrawableCompat.wrap(background)
        DrawableCompat.setTint(background,Color.parseColor(statusColorCode))
        textView.background = background
    }
}

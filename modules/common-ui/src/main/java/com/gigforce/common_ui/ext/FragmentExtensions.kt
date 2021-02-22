package com.gigforce.common_ui.ext

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.facebook.shimmer.ShimmerFrameLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.utils.dp2Px
import com.gigforce.core.extensions.visible

fun Fragment.showToast(string: String){
    Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
}

fun Fragment.startShimmer(ll_shimmer: LinearLayout, shimmerModel: ShimmerDataModel,shimmerId: Int){
    ll_shimmer.removeAllViews()
    ll_shimmer.visible()
    ll_shimmer.orientation = shimmerModel.orientation
    for (i in 0 until shimmerModel.itemsToBeDrawn) {
        val view = LayoutInflater.from(requireContext()).inflate(shimmerModel.cardRes, null)
        ll_shimmer.addView(view)
        val layoutParams: LinearLayout.LayoutParams =
            view.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = shimmerModel.minHeight.dp2Px
        layoutParams.width = shimmerModel.minWidth
        layoutParams.setMargins(resources.getDimensionPixelSize(shimmerModel.marginLeft),
            resources.getDimensionPixelSize(shimmerModel.marginTop), resources.getDimensionPixelSize(shimmerModel.marginRight),
            resources.getDimensionPixelSize(shimmerModel.marginBottom))
        view.layoutParams = layoutParams
        val shimmerLayout = view.findViewById<ShimmerFrameLayout>(shimmerId)
        shimmerLayout.startShimmerAnimation()
    }
}
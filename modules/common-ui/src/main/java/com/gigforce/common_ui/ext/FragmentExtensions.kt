package com.gigforce.common_ui.ext

import android.content.Context
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.utils.dp2Px
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

fun Fragment.showToast(string: String) {
    Toast.makeText(this.context, string, Toast.LENGTH_SHORT).show()
}

fun Fragment.startShimmer(
    ll_shimmer: LinearLayout,
    shimmerModel: ShimmerDataModel,
    shimmerId: Int
) {
    ll_shimmer.removeAllViews()
    ll_shimmer.visible()
    ll_shimmer.orientation = shimmerModel.orientation
    for (i in 0 until shimmerModel.itemsToBeDrawn) {
        val view = LayoutInflater.from(requireContext()).inflate(shimmerModel.cardRes, null)
        ll_shimmer.addView(view)
        val layoutParams: LinearLayout.LayoutParams =
            view.layoutParams as LinearLayout.LayoutParams
        layoutParams.height = shimmerModel.minHeight.dp2Px
        layoutParams.width = shimmerModel.minWidth.dp2Px
        layoutParams.setMargins(
            shimmerModel.marginLeft.dp2Px,
            shimmerModel.marginTop.dp2Px, shimmerModel.marginRight.dp2Px,
            shimmerModel.marginBottom.dp2Px
        )
        view.layoutParams = layoutParams
        val shimmerLayout = view.findViewById<ShimmerFrameLayout>(shimmerId)
        shimmerLayout?.startShimmer()//startShimmerAnimation()
    }
}

fun Fragment.stopShimmer(view: LinearLayout, shimmerId: Int) {

    for (i in 0 until view.childCount) {
        val nestedView = view.getChildAt(i)
        val shimmerLayout = nestedView.findViewById<ShimmerFrameLayout>(shimmerId)
        shimmerLayout.stopShimmer()//Animation()
    }
    view.removeAllViews()
    view.gone()
}


fun Fragment.getCircularProgressDrawable(): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(requireContext())
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 20f
    circularProgressDrawable.start()
    return circularProgressDrawable
}

fun Fragment.hideSoftKeyboard() {

    val activity = activity ?: return

    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()?.getWindowToken(), 0)
}
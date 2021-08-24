package com.gigforce.common_ui.ext

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.facebook.shimmer.ShimmerFrameLayout
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

fun Fragment.showToast(string: String) {
    Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
}

fun Fragment.startShimmer(
    shimmerContainerLayout: View,
    shimmerModel: ShimmerDataModel,
    shimmerId: Int
) {

    val shimmerItemsContainer : LinearLayout
    if(shimmerContainerLayout is LinearLayout){
        shimmerItemsContainer = shimmerContainerLayout
    } else {
        throw IllegalStateException("shimmerContainerLayout should be a LinearLayout")
    }


    shimmerItemsContainer.removeAllViews()
    shimmerItemsContainer.visible()
    shimmerItemsContainer.orientation = shimmerModel.orientation
    for (i in 0 until shimmerModel.itemsToBeDrawn) {
        val view = LayoutInflater.from(requireContext()).inflate(
                shimmerModel.cardRes,
                null
        )

        shimmerItemsContainer.addView(view)
        val layoutParams: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams

        layoutParams.height = if(shimmerModel.minHeight == LinearLayout.LayoutParams.MATCH_PARENT){
            LinearLayout.LayoutParams.MATCH_PARENT
        } else
            resources.getDimensionPixelSize(shimmerModel.minHeight)

        layoutParams.width = if(shimmerModel.minWidth == LinearLayout.LayoutParams.MATCH_PARENT){
            LinearLayout.LayoutParams.MATCH_PARENT
        } else
            resources.getDimensionPixelSize(shimmerModel.minWidth)

        layoutParams.setMargins(resources.getDimensionPixelSize(shimmerModel.marginLeft),
                resources.getDimensionPixelSize(shimmerModel.marginTop),
                resources.getDimensionPixelSize(shimmerModel.marginRight),
                resources.getDimensionPixelSize(shimmerModel.marginBottom)
        )

        view.layoutParams = layoutParams
        val shimmerLayout = view.findViewById<ShimmerFrameLayout>(shimmerId)
        shimmerLayout?.startShimmer()
    }
}

fun Fragment.stopShimmer(shimmerContainerLayout: View, shimmerId: Int) {

    val shimmerItemsContainer : LinearLayout
    if(shimmerContainerLayout is LinearLayout){
        shimmerItemsContainer = shimmerContainerLayout
    } else {
        throw IllegalStateException("shimmerContainerLayout should be a LinearLayout")
    }


    for (i in 0 until shimmerItemsContainer.childCount) {
        val nestedView = shimmerItemsContainer.getChildAt(i)
        val shimmerLayout = nestedView.findViewById<ShimmerFrameLayout>(shimmerId)
        shimmerLayout.stopShimmer()//Animation()
    }
    shimmerItemsContainer.removeAllViews()
    shimmerItemsContainer.gone()
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
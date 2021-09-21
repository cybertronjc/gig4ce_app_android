package com.gigforce.common_ui.datamodels

import android.widget.LinearLayout
import androidx.annotation.DimenRes
import androidx.annotation.LayoutRes
import com.gigforce.common_ui.R

data class ShimmerDataModel(
        @DimenRes val marginLeft: Int = R.dimen.size_16,
        @DimenRes val marginRight: Int = R.dimen.size_16,
        @DimenRes val marginTop: Int = R.dimen.size_16,
        @DimenRes val marginBottom: Int = R.dimen.size_16,
        @LayoutRes var cardRes: Int = R.layout.shimmer_data_layout,
        @DimenRes var minWidth: Int = R.dimen.size_300,
        @DimenRes var minHeight: Int = R.dimen.size_200,
        var itemsToBeDrawn: Int = 4,
        var orientation: Int = LinearLayout.VERTICAL
)



package com.gigforce.common_ui.datamodels

import android.widget.LinearLayout
import com.gigforce.common_ui.R
import com.gigforce.common_ui.utils.dp2Px

data class ShimmerDataModel(val marginLeft: Int = 16.dp2Px,
                            val marginRight: Int =  16.dp2Px,
                            val marginTop: Int =  16.dp2Px,
                            val marginBottom: Int =  16.dp2Px,
                            var cardRes: Int = R.layout.shimmer_data_layout,
                            var minWidth: Int = 300.dp2Px,
                            var minHeight: Int = 200.dp2Px,
                            var itemsToBeDrawn: Int = 4,
                            var orientation: Int = LinearLayout.VERTICAL)
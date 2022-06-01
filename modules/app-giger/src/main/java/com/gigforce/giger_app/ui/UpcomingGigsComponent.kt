package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.common_image_picker.image_capture_camerax.utils.decodeExifOrientation
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.dataviewmodel.UpcomingGigSectionDVM
import com.gigforce.giger_app.vm.UpcomingGigsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigsComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    init {
        this.setOrientationAndRows(0, 1)

    }

    private var viewModel: UpcomingGigsViewModel? = null
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()


        viewModel = ViewModelProvider(findViewTreeViewModelStoreOwner()!!).get()
        viewModel?.data?.observeForever {
                try {
                    if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                        super.bind(
                            FeatureLayoutDVM(
                                data?.imageUrl?:"",
                                data?.hi?.title ?: data?.title?:"",
                                it
                            )
                        )
                    } else {
                        super.bind(FeatureLayoutDVM(data?.imageUrl?:"", data?.title?:"", it))
                    }
                } catch (e: Exception) {
                }
            viewModel?.state?.let { parcelable ->
                this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onRestoreInstanceState(
                    parcelable
                )

            }
        }

    }

    var data: UpcomingGigSectionDVM? = null
    override fun bind(data1: Any?) {
        data = data1 as UpcomingGigSectionDVM
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.state =
            this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onSaveInstanceState()

    }
}
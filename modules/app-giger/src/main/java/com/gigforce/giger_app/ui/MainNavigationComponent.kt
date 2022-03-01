package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.giger_app.dataviewmodel.MainSectionDVM
import com.gigforce.giger_app.vm.MainNavigationViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainNavigationComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private var viewModel: MainNavigationViewModel? = null

    init {
        this.setOrientationAndRows(1, 4)
    }

    var data: MainSectionDVM? = null
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel = ViewModelProvider(ViewTreeViewModelStoreOwner.get(this.rootView)!!).get()
        viewModel?.requestData(sharedPreAndCommonUtilInterface.getCurrentVersionCode())
        super.bind(
            FeatureLayoutDVM(
                data?.imageUrl ?: "",
                data?.title ?: "",
                viewModel?.getDefaultData() ?: listOf()
            )
        )
        viewModel?.liveData?.observeForever {
            try {
                Log.e("flowtest","${it.size} working")
                val filteredData =
                    it.filter { true}//it.type == null || it.type == "" || it.type == "icon" || it.type == "folder" }
                if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                    super.bind(
                        FeatureLayoutDVM(
                            data?.imageUrl ?: "",
                            data?.hi?.title ?: data?.title ?: "",
                            filteredData
                        )
                    )
                } else {
                    super.bind(
                        FeatureLayoutDVM(
                            data?.imageUrl ?: "",
                            data?.title ?: "",
                            filteredData
                        )
                    )
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

    override fun bind(data: Any?) {
        this.data = data as MainSectionDVM
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.state =
            this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onSaveInstanceState()
        Log.e("recyclerview", "onDetachedFromWindow")
    }

}
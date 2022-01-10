package com.gigforce.giger_app.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import com.gigforce.giger_app.dataviewmodel.HelpVideosSectionDVM
import com.gigforce.giger_app.vm.HelpVideosViewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HelpVideoInfoComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {
    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface

    private var viewModel: HelpVideosViewModels? = null

    init {
        this.setOrientationAndRows(1, 1)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel = ViewModelProvider(ViewTreeViewModelStoreOwner.get(this)!!).get()
        data?.let { data ->
            if (data is HelpVideosSectionDVM) {
                viewModel?.requestData(data.showVideo.toLong())
            }
        }



        viewModel?.data?.observeForever {
            if (it.isNotEmpty()) {
                data?.let { data ->
                    if (data is HelpVideosSectionDVM) {
                        enableSeemoreButton()
                        if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                            super.bind(
                                FeatureLayoutDVM(
                                    data.imageUrl,
                                    data.hi?.title ?: data.title,
                                    it
                                )
                            )
                        } else {
                            super.bind(
                                FeatureLayoutDVM(
                                    data.imageUrl,
                                    data.title,
                                    it
                                )
                            )
                        }

                    }
                }
            } else {
                super.bind(FeatureLayoutDVM("", "", emptyList()))
            }
            viewModel?.state?.let { parcelable->
                this.findViewById<RecyclerView>(com.gigforce.client_activation.R.id.featured_rv)?.layoutManager?.onRestoreInstanceState(parcelable)

            }
        }
    }

    var data: Any? = null
    override fun bind(data: Any?) {
        this.data = data
        if (data is HelpVideosSectionDVM) {

            this.findViewById<TextView>(R.id.layout_title).setOnClickListener {
                data.navPath?.let {
                    navigation.navigateTo(it)
                }
            }

            this.findViewById<View>(R.id.see_more_btn).setOnClickListener {
                data.navPath?.let {
                    navigation.navigateTo(it)
                }
            }

        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.state =
            this.findViewById<RecyclerView>(com.gigforce.client_activation.R.id.featured_rv)?.layoutManager?.onSaveInstanceState()
        Log.e("recyclerview", "onDetachedFromWindow")
    }
}
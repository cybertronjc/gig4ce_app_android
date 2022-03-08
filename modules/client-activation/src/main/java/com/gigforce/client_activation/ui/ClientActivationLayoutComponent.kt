package com.gigforce.client_activation.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.dataviewmodel.ClientActivationLayoutDVM
import com.gigforce.client_activation.viewmodels.ClientActivationViewModels
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.common_ui.viewdatamodels.SeeMoreItemDVM
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClientActivationLayoutComponent(context: Context, attrs: AttributeSet?) :
    FeatureLayoutComponent(context, attrs) {

    private var viewModel : ClientActivationViewModels? = null

    @Inject
    lateinit var navigation: INavigation
    @Inject lateinit var sharedPreAndCommonUtilInterface: SharedPreAndCommonUtilInterface
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel = ViewModelProvider(ViewTreeViewModelStoreOwner.get(this.rootView)!!).get()

        data?.let { data->
            if(data is ClientActivationLayoutDVM ) {
                viewModel!!.requestLiveData(data.showItem.toLong())
            }

        }


        Log.e("recyclerview","onAttachedToWindow")

        viewModel?.liveData?.observeForever { it ->
            Log.e("recyclerview","liveData")
                if ( it.isNotEmpty()) {
                    data?.let { data ->
                        if (data is ClientActivationLayoutDVM) {
                            val list1 = it.toMutableList<Any>()
                            list1.add(SeeMoreItemDVM("", "", data.seeMoreNav))
                            if (sharedPreAndCommonUtilInterface.getAppLanguageCode() == "hi") {
                                super.bind(
                                    FeatureLayoutDVM(
                                        data.image,
                                        data.hi?.title ?: data.title,
                                        list1
                                    )
                                )
                            }else {
                                super.bind(FeatureLayoutDVM(data.image, data.title, list1))
                            }
                        }
                    }
                }else {
                    super.bind(FeatureLayoutDVM("", "", emptyList()))
                }
                viewModel?.state?.let { parcelable->
                    this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onRestoreInstanceState(parcelable)

                }

            }

        }

    var data : Any?=null
    override fun bind(data: Any?) {
        Log.e("recyclerview","bind function called")
        this.data = data
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.state = this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onSaveInstanceState()
        Log.e("recyclerview","onDetachedFromWindow")
    }

}
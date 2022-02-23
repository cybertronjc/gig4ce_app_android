package com.gigforce.learning.ui

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import com.example.learning.R
import com.gigforce.common_ui.components.cells.FeatureLayoutComponent
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.learning.dataviewmodels.LearningLayoutDVM
import com.gigforce.learning.viewmodels.LearningComponentViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LearningLayoutComponent(context: Context, attrs: AttributeSet?) :
        FeatureLayoutComponent(context, attrs) {
    private var viewModel : LearningComponentViewModel?=null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewModel = ViewModelProvider(ViewTreeViewModelStoreOwner.get(this)!!).get()
        viewModel?.requestLearningData()
        viewModel?.allCourses?.observeForever{
            data?.let { data->
                if(it.isEmpty())
                view.findViewById<ConstraintLayout>(com.gigforce.common_ui.R.id.top_cl).gone()
                else if (data is LearningLayoutDVM) {
                    super.bind(FeatureLayoutDVM(data.imageUrl, data.title, it))
                }
            }
            viewModel?.state?.let { parcelable->
                this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onRestoreInstanceState(parcelable)

            }
        }
    }
    var data : Any?=null
    override fun bind(data: Any?) {
        this.data = data
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.state = this.findViewById<RecyclerView>(R.id.featured_rv)?.layoutManager?.onSaveInstanceState()
        Log.e("recyclerview","onDetachedFromWindow")
    }
}
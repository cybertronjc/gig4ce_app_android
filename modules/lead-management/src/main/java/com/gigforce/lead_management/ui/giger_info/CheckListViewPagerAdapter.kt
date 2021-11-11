package com.gigforce.lead_management.ui.giger_info

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.utils.GlideApp
import com.gigforce.lead_management.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage

class CheckListViewPagerAdapter : RecyclerView.Adapter<CheckListViewPagerAdapter.ViewPagerViewHolder>() {

    private var list: List<String> = listOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(parent)
    }


    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setItem(list: List<String>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size


    inner class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        constructor(parent: ViewGroup) : this(
            LayoutInflater.from(parent.context).inflate(
                R.layout.checklist_viewpager_list_item_view,
                parent, false
            )
        )


        private var backgroundImage: ImageView = itemView.findViewById(R.id.imageBack)

        fun bind(image: String) {

                image?.let {
                    try {
                        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                        GlideApp.with(itemView.context)
                            .load(gsReference)
                            .placeholder(getCircularProgressDrawable(itemView.context))
                            .into(backgroundImage)
                    } catch (e: Exception) {
                        CrashlyticsLogger.d("Viewpager Checklist", "${e.message} $it")
                        FirebaseCrashlytics.getInstance().log("Exception : Viewpager Checklist ${e.message} $it")
                    }
                }
            }

        }
}

package com.gigforce.verification.mainverification.adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.gone
import com.gigforce.core.utils.GlideApp
import com.gigforce.verification.R
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception


class ViewPagerAdapter(private val itemClickListener: (View) -> (Unit)) : RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder>() {

    private var list: List<KYCImageModel> = listOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        return ViewPagerViewHolder(parent)
    }


    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    fun setItem(list: List<KYCImageModel>) {
        this.list = list
        notifyDataSetChanged()
    }
    fun updateData(position: Int, uri: Uri){
        if(position<list.size) {
            list[position].imageIcon = uri
            list[position].imagePath = ""
            list[position].imageUploaded = true
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = list.size

    private var setImageClickable = true
    fun setImageClickable(enable : Boolean){
        setImageClickable = enable
        notifyDataSetChanged()
    }
    private var imageDetectionStarted = false
    private var imageFound = false
    inner class ViewPagerViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView),
         View.OnClickListener {

        constructor(parent: ViewGroup) : this(
            LayoutInflater.from(parent.context).inflate(
                R.layout.kyc_image_card_view_item,
                parent, false
            )
        )
        init {
            itemView.setOnClickListener(this)
        }
        private var title: TextView = itemView.findViewById(R.id.title)
        private var backgroundImage: ImageView = itemView.findViewById(R.id.imageBack)
        private var plusIcon: ImageView = itemView.findViewById(R.id.plusIcon)



        fun bind(kYCImageModel: KYCImageModel) {
            imageDetectionStarted = true
            title.text = kYCImageModel.text
            if (kYCImageModel.imagePath.isNullOrBlank()) {
                GlideApp.with(itemView.context)
                    .load(kYCImageModel.imageIcon)
                    .into(backgroundImage)

            }
            else{
                kYCImageModel.imagePath?.let {
                    try {
                        val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                        GlideApp.with(itemView.context)
                            .load(gsReference)
                            .into(backgroundImage)
                    }catch (e: Exception){
                        CrashlyticsLogger.d("Viewpager KYC", "${e.message} $it")
                        FirebaseCrashlytics.getInstance().log("Exception : Viewpager KYC ${e.message} $it")
                    }

                }
            }
            if (kYCImageModel.imageUploaded) {
                title.gone()
                plusIcon.gone()
            }
            itemView.isClickable = setImageClickable
        }

        override fun onClick(v: View?) {
            v?.let { itemClickListener(it) }
        }

    }
}

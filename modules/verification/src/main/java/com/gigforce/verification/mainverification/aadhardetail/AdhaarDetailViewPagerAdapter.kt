package com.gigforce.verification.mainverification.aadhardetail

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.core.extensions.gone
import com.gigforce.core.utils.GlideApp
import com.gigforce.verification.R
import com.google.firebase.storage.FirebaseStorage

class AdhaarDetailViewPagerAdapter(private val itemClickListener: (View) -> (Unit)) : RecyclerView.Adapter<AdhaarDetailViewPagerAdapter.ViewPagerViewHolder>() {

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
        list.get(position).imagePath = ""
        list.get(position).imageIcon = uri
        list.get(position).imageUploaded = true
        notifyItemChanged(position)
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
                        R.layout.kyc_image_card_view,
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
                    val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(it)
                    GlideApp.with(itemView.context)
                            .load(gsReference)
                            .into(backgroundImage)
                }
            }
            if (kYCImageModel.imageUploaded) {
                title.gone()
                plusIcon.gone()
            }
            if(!setImageClickable)
                itemView.isClickable = false
        }

        override fun onClick(v: View?) {
            v?.let { itemClickListener(it) }
        }

    }
}
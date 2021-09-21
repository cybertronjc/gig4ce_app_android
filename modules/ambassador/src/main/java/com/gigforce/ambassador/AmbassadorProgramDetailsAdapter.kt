package com.gigforce.ambassador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.widgets.RoundCornerImageView
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage

class AmbassadorProgramDetailsAdapter(val context : Context) : RecyclerView.Adapter<AmbassadorProgramDetailsAdapter.APViewHolder>(){

    inner class APViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.title_)
        val subtitle = itemView.findViewById<TextView>(R.id.title)
        val comImg = itemView.findViewById<ImageView>(R.id.completed_iv)
        val img = itemView.findViewById<RoundCornerImageView>(R.id.learning_img)
        fun bind(course : Course){
            title.text = course.name
            subtitle.text = course.level
            comImg.isVisible = course?.completed

            if (!course!!.coverPicture.isNullOrBlank()) {
                if (course.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                        .load(course.coverPicture!!)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(img)
                } else {
                    FirebaseStorage.getInstance()
                        .getReference(AppConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(course.coverPicture!!)
                        .downloadUrl
                        .addOnSuccessListener { fileUri ->

                            GlideApp.with(context)
                                .load(fileUri)
                                .placeholder(getCircularProgressDrawable(context))
                                .error(R.drawable.ic_learning_default_back)
                                .into(img)
                        }
                }
            } else {
                GlideApp.with(context)
                    .load(R.drawable.ic_learning_default_back)
                    .into(img)
            }
        }
    }

    var data : List<Course> = ArrayList<Course>()
    var itemClickListener : ItemClickListener?= null
    fun setClickListener(itemClickListener : ItemClickListener){
            this.itemClickListener = itemClickListener
        }

    interface ItemClickListener{
        fun itemClickListener(view:View,position: Int, item : Course)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): APViewHolder {
        return APViewHolder(LayoutInflater.from(context).inflate(R.layout.learning_bs_item,parent,false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: APViewHolder, position: Int) {
        holder.bind(data.get(position))
    }
}
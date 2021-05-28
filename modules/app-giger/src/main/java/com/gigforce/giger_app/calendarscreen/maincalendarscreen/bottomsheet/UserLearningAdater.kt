package com.gigforce.giger_app.calendarscreen.maincalendarscreen.bottomsheet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.GlideApp
import com.gigforce.giger_app.R
import com.gigforce.learning.learning.LearningConstants
import com.google.firebase.storage.FirebaseStorage

class UserLearningAdater(val context: Context, val itemWidth : Int) :
    RecyclerView.Adapter<UserLearningAdater.CustomViewHolder>() {

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cardView = itemView.findViewById<View>(R.id.card_view)
        val title_ = itemView.findViewById<TextView>(R.id.title_)
        val title = itemView.findViewById<TextView>(R.id.title)
        val comImg = itemView.findViewById<ImageView>(R.id.completed_iv)
        val img = cardView.findViewById<ImageView>(R.id.learning_img)


        fun bindView(course: Course) {
//            val lp = cardView.layoutParams
//            lp.height = lp.height
//            lp.width = itemWidth    //need to implement later
//            cardView.layoutParams = lp
            title_.text = course.name
            title.text = course.level
            comImg.isVisible = course.completed



            if (!course.coverPicture.isNullOrBlank()) {
                if (course.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                        .load(course.coverPicture!!)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(img)
                } else {
                    FirebaseStorage.getInstance()
                        .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
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

    var data : List<Course>? = null

    var clickListener : AdapterClickListener<Course>? = null

    fun setOnclickListener(listener : AdapterClickListener<Course>){
        this.clickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.learning_bs_item, null)
        )
    }

    override fun getItemCount() = data?.size?:0


    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        data?.let {
            holder.bindView(it.get(position))
        }
    }

    override fun onViewAttachedToWindow(holder: UserLearningAdater.CustomViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.itemView.updateLayoutParams {
            width = itemWidth
        }
    }
}
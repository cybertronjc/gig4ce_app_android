package com.gigforce.landing_screen.landingscreen.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.AppConstants
import com.gigforce.core.datamodels.learning.Course
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.AdapterClickListener
import com.gigforce.core.utils.GlideApp
import com.gigforce.landing_screen.R
import com.google.firebase.storage.FirebaseStorage

class UserLearningCourseAdapter(
    private val context: Context, val itemWidth : Int
) : RecyclerView.Adapter<UserLearningCourseAdapter.UserLearningViewHolder>() {

    private var originalList: List<Course> = emptyList()
    var clickListener: AdapterClickListener<Course>? = null
    fun setOnclickListener(listener: AdapterClickListener<Course>) {
        this.clickListener = listener
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserLearningViewHolder {
        val view = LayoutInflater.from(
            context
        ).inflate(R.layout.learning_bs_item, parent, false)
        return UserLearningViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalList.size
    }


    override fun onBindViewHolder(holder: UserLearningViewHolder, position: Int) {
        holder.bindValues(originalList.get(position), position)
    }

    fun setData(contacts: List<Course>) {
        this.originalList = contacts
        notifyDataSetChanged()
    }


    inner class UserLearningViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val cardView = itemView.findViewById<View>(R.id.card_view)
        private var subtitle: TextView = itemView.findViewById(R.id.title)
        private var title: TextView = itemView.findViewById(R.id.title_)
        private var comImg: ImageView = itemView.findViewById(R.id.completed_iv)
        private var learningImg: ImageView = itemView.findViewById(R.id.learning_img)


        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(model: Course, position: Int) {

            val lp = cardView.layoutParams
            lp.height = lp.height
            lp.width = itemWidth
            cardView.layoutParams = lp

            title.text = model.name
            subtitle.text = model.level
            if (model.completed) comImg.visible() else comImg.gone()

            if (!model.coverPicture.isNullOrBlank()) {
                if (model.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                        .load(model.coverPicture!!)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(learningImg)
                } else {
                    val imageRef = FirebaseStorage.getInstance()
                        .getReference(AppConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(model.coverPicture!!)

                    GlideApp.with(context)
                        .load(imageRef)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(learningImg)
                }
            } else {

                GlideApp.with(context)
                    .load(R.drawable.ic_learning_default_back)
                    .into(learningImg)
            }
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            clickListener?.let {
                v?.let { it1 ->
                    it.onItemClick(it1, originalList.get(newPosition), newPosition)

                }
            }
        }
    }
}
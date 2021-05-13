package com.gigforce.app.modules.gigPage2.adapters

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.utils.GlideApp
import com.gigforce.learning.learning.LearningConstants
import com.gigforce.core.datamodels.learning.Course
import com.google.firebase.storage.FirebaseStorage

class GigDetailAdapter(val context: Context) :
    RecyclerView.Adapter<GigDetailAdapter.GigViewHolder>() {

    //    var arrCourse = emptyList<Course>()
    val displayMetrics = DisplayMetrics()
    var width = displayMetrics.widthPixels
    val itemWidth = ((width / 3) * 2).toInt()


    inner class GigViewHolder(val view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {
        private var title = view.findViewById<TextView>(R.id.title_)
        private var subTitle = view.findViewById<TextView>(R.id.title)
        private var comImg = view.findViewById<ImageView>(R.id.completed_iv)
        private var img = view.findViewById<ImageView>(R.id.learning_img)


        fun bindView(obj: Course) {
            view.setOnClickListener(this)
            title.text = obj.name
            subTitle.text = obj.level
            comImg.isVisible = obj.completed
            if (!obj.coverPicture.isNullOrBlank()) {
                if (obj.coverPicture!!.startsWith("http", true)) {

                    GlideApp.with(context)
                        .load(obj.coverPicture!!)
                        .placeholder(getCircularProgressDrawable(context))
                        .error(R.drawable.ic_learning_default_back)
                        .into(img)
                } else {
                    FirebaseStorage.getInstance()
                        .getReference(LearningConstants.LEARNING_IMAGES_FIREBASE_FOLDER)
                        .child(obj.coverPicture!!)
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

        override fun onClick(v: View?) {
            ionclickListener?.onclickListener(data.get(adapterPosition))

        }
    }

    interface IOnclickListener {
        fun onclickListener(course: Course)
    }

    var ionclickListener: IOnclickListener? = null

    fun setOnClickListener(ionclickListener: IOnclickListener) {
        this.ionclickListener = ionclickListener
    }

    var data: List<Course> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GigViewHolder {
        var view = LayoutInflater.from(context).inflate(R.layout.learning_bs_item, parent, false)
        val lp = view.layoutParams
        lp.height = lp.height
        lp.width = itemWidth
        view.layoutParams = lp
        return GigViewHolder(view)
    }

    override fun getItemCount() = data.size


    override fun onBindViewHolder(holder: GigViewHolder, position: Int) {
        holder.bindView(data.get(position))
    }
}
package com.gigforce.app.modules.ambassador_user_enrollment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.time.Duration
import java.time.LocalDate

class EnrolledUsersRecyclerAdapter constructor(
        private val applicationContext: Context,
        private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : RecyclerView.Adapter<EnrolledUsersRecyclerAdapter.EnrolledUserViewHolder>() {

    private var enrolledUsers: List<EnrolledUser> = emptyList()
    private lateinit var enrolledUsersRecyclerAdapterClickListener: EnrolledUsersRecyclerAdapterClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrolledUserViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item_enrolled_user, parent, false)
        return EnrolledUserViewHolder(view)
    }

    fun setListener(enrolledUsersRecyclerAdapterClickListener: EnrolledUsersRecyclerAdapterClickListener) {
        this.enrolledUsersRecyclerAdapterClickListener = enrolledUsersRecyclerAdapterClickListener
    }

    override fun getItemCount(): Int {
        return enrolledUsers.size
    }

    override fun onBindViewHolder(holder: EnrolledUserViewHolder, position: Int) {
        holder.bindValues(enrolledUsers[position])
    }

    fun setData(users: List<EnrolledUser>) {
        enrolledUsers = users
        notifyDataSetChanged()
    }

    inner class EnrolledUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {
        private val userImageIV: ImageView = itemView.findViewById(R.id.image_view)
        private val statusImageIV: ImageView = itemView.findViewById(R.id.status_iv)
        private val nameTv: TextView = itemView.findViewById(R.id.user_name_tv)
        private val userAddedTimeTV: TextView = itemView.findViewById(R.id.user_added_time)
        private val editProfileBtn: Button = itemView.findViewById(R.id.edit_profile_btn)


        init {
            itemView.setOnClickListener(this)
            editProfileBtn.setOnClickListener(this)
        }

        fun bindValues(user: EnrolledUser) {
            if (user.profilePic.isBlank()) {
                Glide.with(applicationContext).load(R.drawable.avatar).into(userImageIV)
            } else {

                if (user.profileAvatarThumbnail.isNotBlank()) {
                    val profilePicRef: StorageReference = firebaseStorage
                            .reference
                            .child("profile_pics")
                            .child(user.profileAvatarThumbnail)

                    GlideApp.with(applicationContext)
                            .load(profilePicRef)
                            .into(userImageIV)
                } else if (user.profilePic.isNotBlank()) {
                    val profilePicRef: StorageReference = firebaseStorage
                            .reference
                            .child("profile_pics")
                            .child(user.profilePic)

                    GlideApp.with(applicationContext)
                            .load(profilePicRef)
                            .into(userImageIV)
                }
            }

            nameTv.text = user.name
            val userEnrolledDate = user.enrolledOn.toLocalDate()
            if (userEnrolledDate.equals(LocalDate.now())) {
                // enrolled today
                userAddedTimeTV.text = "Added today"
            } else {
                //
                val daysDiff = Duration.between(
                        userEnrolledDate.atStartOfDay(),
                        LocalDate.now().atStartOfDay()
                ).toDays()
                userAddedTimeTV.text = "Added $daysDiff days ago"
            }

            if (user.enrollmentStepsCompleted.allStepsCompleted()) {
                statusImageIV.setImageResource(R.drawable.ic_applied)
            } else {
                statusImageIV.setImageResource(R.drawable.ic_pending_yellow_round)
            }
        }

        override fun onClick(v: View?) {
            val view = v ?: return

            if (view.id == R.id.edit_profile_btn) {
                enrolledUsersRecyclerAdapterClickListener.onUserEditButtonclicked(enrolledUsers[adapterPosition])
            } else {
                enrolledUsersRecyclerAdapterClickListener.onUserClicked(enrolledUsers[adapterPosition])
            }
        }
    }

    interface EnrolledUsersRecyclerAdapterClickListener {

        fun onUserClicked(enrolledUser: EnrolledUser)

        fun onUserEditButtonclicked(enrolledUser: EnrolledUser)
    }
}
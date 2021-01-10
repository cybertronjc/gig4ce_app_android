package com.gigforce.app.modules.ambassador_user_enrollment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.toLocalDate
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.utils.CustomTypeFaceSpan
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.lang.reflect.Field
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
        private val ivOptionsEnrollUser: ImageView =
            itemView.findViewById(R.id.iv_options_rv_enrolled_users)


        init {
            itemView.setOnClickListener(this)
            ivOptionsEnrollUser.setOnClickListener(this)
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
                userAddedTimeTV.text = itemView.resources.getString(R.string.added_today)
            } else {
                //
                val daysDiff = Duration.between(
                    userEnrolledDate.atStartOfDay(),
                    LocalDate.now().atStartOfDay()
                ).toDays()
                userAddedTimeTV.text =
                    "${itemView.resources.getString(R.string.added)} $daysDiff ${
                        itemView.resources.getString(
                            R.string.days_ago
                        )
                    }"
            }

            if (user.enrollmentStepsCompleted.allStepsCompleted()) {
                statusImageIV.setImageResource(R.drawable.ic_applied)
            } else {
                statusImageIV.setImageResource(R.drawable.ic_pending_yellow_round)
            }
        }

        private fun applyFontToMenuItem(mi: MenuItem) {
            val font = Typeface.createFromAsset(itemView.context.assets, "fonts/Lato-Regular.ttf")
            val mNewTitle = SpannableString(mi.getTitle())
            mNewTitle.setSpan(
                CustomTypeFaceSpan("", font, Color.BLACK),
                0,
                mNewTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            mi.setTitle(mNewTitle)
        }

        override fun onClick(v: View?) {
            val view = v ?: return

            if (view.id == R.id.iv_options_rv_enrolled_users) {
//                val wrapper: Context = ContextThemeWrapper(view.context, R.style.CustomPopupTheme)
                val popup = PopupMenu(view.context, view)

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_edit -> enrolledUsersRecyclerAdapterClickListener.onUserEditButtonclicked(
                            enrolledUsers[adapterPosition]
                        )
                        R.id.action_call -> {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.fromParts(
                                    "tel",
                                    enrolledUsers[adapterPosition].mobileNumber,
                                    null
                                )
                            )
                            itemView.context.startActivity(intent)
                        }
                        R.id.action_chat -> enrolledUsersRecyclerAdapterClickListener.openChat(
                            enrolledUsers[adapterPosition]
                        )


                    }
                    true

                }

                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_rv_enrolled_users, popup.menu)
                val menuHelper: Any
                val argTypes: Array<Class<*>?>
                try {
                    val fMenuHelper: Field = PopupMenu::class.java.getDeclaredField("mPopup")
                    fMenuHelper.isAccessible = true
                    menuHelper = fMenuHelper.get(popup)
                    argTypes = arrayOf(Boolean::class.javaPrimitiveType)
                    menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(
                        menuHelper,
                        true
                    )
                } catch (e: Exception) {
                }
                val menu: Menu = popup.menu
                for (i in 0 until menu.size()) {
                    val mi: MenuItem = menu.getItem(i)
                    applyFontToMenuItem(mi)
                }
                popup.show()

            } else {
                enrolledUsersRecyclerAdapterClickListener.onUserClicked(enrolledUsers[adapterPosition])
            }
        }
    }

    interface EnrolledUsersRecyclerAdapterClickListener {

        fun onUserClicked(enrolledUser: EnrolledUser)

        fun onUserEditButtonclicked(enrolledUser: EnrolledUser)

        fun openChat(enrollUser: EnrolledUser)
    }
}
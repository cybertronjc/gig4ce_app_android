package com.gigforce.ambassador

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.view.*
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.common_ui.IconPowerMenuItem
import com.gigforce.core.datamodels.ambassador.EnrolledUser
import com.gigforce.core.extensions.toLocalDate
import com.gigforce.core.utils.CustomTypeFaceSpan
import com.gigforce.core.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import java.time.Duration
import java.time.LocalDate


class EnrolledUsersRecyclerAdapter constructor(
    private val applicationContext: Context,
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : RecyclerView.Adapter<EnrolledUsersRecyclerAdapter.EnrolledUserViewHolder>(),
    Filterable {

    private var mFilteredUsersList: List<EnrolledUser> = emptyList()
    private var mOriginalUsersList: List<EnrolledUser> = emptyList()

    private lateinit var enrolledUsersRecyclerAdapterClickListener: EnrolledUsersRecyclerAdapterClickListener
    private val filter = EnrolledUserFilter()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnrolledUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_enrolled_user, parent, false)
        return EnrolledUserViewHolder(view)
    }

    fun setListener(enrolledUsersRecyclerAdapterClickListener: EnrolledUsersRecyclerAdapterClickListener) {
        this.enrolledUsersRecyclerAdapterClickListener = enrolledUsersRecyclerAdapterClickListener
    }

    override fun getItemCount(): Int {
        return mFilteredUsersList.size
    }

    override fun onBindViewHolder(holder: EnrolledUserViewHolder, position: Int) {
        holder.bindValues(mFilteredUsersList[position])
    }

    fun setData(users: List<EnrolledUser>) {
        mFilteredUsersList = users
        mOriginalUsersList = users
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filter
    }

    private inner class EnrolledUserFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                mFilteredUsersList = mOriginalUsersList
            } else {
                val filteredList: MutableList<EnrolledUser> = mutableListOf()
                for (user in mOriginalUsersList) {
                    if (user.name.contains(
                            charString,
                            true
                        ) || user.mobileNumber.contains(charString, true)
                    )
                        filteredList.add(user)
                }
                mFilteredUsersList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = mFilteredUsersList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mFilteredUsersList = results?.values as List<EnrolledUser>
            notifyDataSetChanged()
        }
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
            if (user.profilePic.isNullOrBlank()) {
                Glide.with(applicationContext).load(R.drawable.ic_user_2).into(userImageIV)
            } else {

                if (!user.profileAvatarThumbnail.isNullOrBlank()) {
                    val profilePicRef: StorageReference = firebaseStorage
                        .reference
                        .child("profile_pics")
                        .child(user.profileAvatarThumbnail!!)

                    GlideApp.with(applicationContext)
                        .load(profilePicRef)
                        .into(userImageIV)
                } else if (!user.profilePic.isNullOrBlank()) {
                    val profilePicRef: StorageReference = firebaseStorage
                        .reference
                        .child("profile_pics")
                        .child(user.profilePic!!)

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
            val mNewTitle = SpannableString(mi.title)
            mNewTitle.setSpan(
                CustomTypeFaceSpan("", font, Color.BLACK),
                0,
                mNewTitle.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            mi.title = mNewTitle
        }

        override fun onClick(v: View?) {
            val view = v ?: return

            if (view.id == R.id.iv_options_rv_enrolled_users) {
                val wrapper: ContextThemeWrapper = ContextThemeWrapper(
                    view.context,
                    R.style.CustomPopupTheme
                )
                val context = view.context
                var customPowerMenu: CustomPowerMenu<*, *>? = null
                customPowerMenu =
                    CustomPowerMenu.Builder(context, IconMenuAdapter())
                        .addItem(
                            IconPowerMenuItem(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.md_transparent
                                )!!, "Edit"
                            )
                        )
                        .addItem(
                            IconPowerMenuItem(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.md_transparent
                                )!!, "Call"
                            )
                        )
                        .addItem(
                            IconPowerMenuItem(
                                ContextCompat.getDrawable(
                                    context,
                                    R.drawable.md_transparent
                                )!!, "Chat"
                            )
                        ).setShowBackground(false)
                        .setOnMenuItemClickListener(object :
                            OnMenuItemClickListener<IconPowerMenuItem> {
                            override fun onItemClick(position: Int, item: IconPowerMenuItem?) {
                                when (position) {
                                    0 -> enrolledUsersRecyclerAdapterClickListener.onUserEditButtonclicked(
                                        mFilteredUsersList[adapterPosition]
                                    )
                                    1 -> {
                                        val intent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.fromParts(
                                                "tel",
                                                mFilteredUsersList[adapterPosition].mobileNumber,
                                                null
                                            )
                                        )
                                        itemView.context.startActivity(intent)
                                    }
                                    2 -> enrolledUsersRecyclerAdapterClickListener.openChat(
                                        mFilteredUsersList[adapterPosition]
                                    )


                                }
                                customPowerMenu?.dismiss()
                            }
                        })
                        .setAnimation(MenuAnimation.DROP_DOWN)
                        .setMenuRadius(
                            view.resources.getDimensionPixelSize(R.dimen.size_4).toFloat()
                        )
                        .setMenuShadow(
                            view.resources.getDimensionPixelSize(R.dimen.size_4).toFloat()
                        )

                        .build()
                customPowerMenu.showAsDropDown(
                    view,
                    -((customPowerMenu.contentViewWidth - view.resources.getDimensionPixelSize(
                        R.dimen.size_24
                    )
                            )),
                    -(view.resources.getDimensionPixelSize(
                        R.dimen.size_24
                    )
                            )
                )


//                popup.setOnMenuItemClickListener {
//
//                    true
//
//                }
//
//                val inflater = popup.menuInflater
//                inflater.inflate(R.menu.menu_rv_enrolled_users, popup.menu)
//                val menuHelper: Any
//                val argTypes: Array<Class<*>?>
//                try {
//                    val fMenuHelper: Field = PopupMenu::class.java.getDeclaredField("mPopup")
//                    fMenuHelper.isAccessible = true
//                    menuHelper = fMenuHelper.get(popup)
//                    argTypes = arrayOf(Boolean::class.javaPrimitiveType)
//                    menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes).invoke(
//                        menuHelper,
//                        true
//                    )
//                } catch (e: Exception) {
//                }
//                val menu: Menu = popup.menu
//                for (i in 0 until menu.size()) {
//                    val mi: MenuItem = menu.getItem(i)
//                    applyFontToMenuItem(mi)
//                }
//                popup.show()

            } else {
                enrolledUsersRecyclerAdapterClickListener.onUserClicked(mFilteredUsersList[adapterPosition])
            }
        }
    }

    interface EnrolledUsersRecyclerAdapterClickListener {

        fun onUserClicked(enrolledUser: EnrolledUser)

        fun onUserEditButtonclicked(enrolledUser: EnrolledUser)

        fun openChat(enrollUser: EnrolledUser)
    }
}
package com.gigforce.user_preferences

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class PreferencesFragmentAdapter : RecyclerView.Adapter<PreferencesFragmentAdapter.PreferencesViewHolder>() {

    companion object{
        const val DAY_TIME = 0
        const val LOCATION = 1
        const val EARNING = 2
        const val TITLE_OTHER = 3
        const val LANGUAGE = 4
        const val TITLE_SIGNOUT = 5
    }

    inner class PreferencesViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView), View.OnClickListener{
        val constraintView = itemView.findViewById<ConstraintLayout>(R.id.constraintLayout)
        val signOutView = itemView.findViewById<View>(R.id.signOutLayout)
        val signOutTV = itemView.findViewById<TextView>(R.id.signOutTitle)
        val signOutIV = itemView.findViewById<ImageView>(R.id.signOutIcon)

        val othersTV = itemView.findViewById<TextView>(R.id.others_and_signout)
        val title = itemView.findViewById<TextView>(R.id.item_title)
        val subTitle = itemView.findViewById<TextView>(R.id.item_subtitle)
        val imageView = itemView.findViewById<ImageView>(R.id.item_icon)

        fun bindView(prefScreen : PreferencesScreenItem, position: Int){
            itemView.setOnClickListener(this)
            if (position == PreferencesFragment.TITLE_OTHER) {
                signOutView.visibility = View.GONE
                visibleInvisibleMainItemView(constraintView, othersTV, false)
                setItemAsOther(othersTV, prefScreen)
            } else if (position == PreferencesFragment.TITLE_SIGNOUT) {
                signOutView.visibility = View.VISIBLE
                hideMainConstraintViewAndOthersViewInItemView(constraintView, othersTV)
                setItemAsSignOut(signOutTV, signOutIV, prefScreen)
            } else {
                signOutView.visibility = View.GONE
                visibleInvisibleMainItemView(constraintView, othersTV, true)
                setItems(imageView, title, subTitle, prefScreen)
            }
        }

        private fun visibleInvisibleMainItemView(
            constraintView: View,
            otherAndSignout: TextView,
            isVisible: Boolean
        ) {
            constraintView.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
            otherAndSignout.visibility = if (!isVisible) View.VISIBLE else View.INVISIBLE
        }

        private fun setItemAsSignOut(
            signOutTV: TextView,
            signOutIV: ImageView,
            obj: PreferencesScreenItem?
        ) {
            signOutTV.text = obj?.title
            signOutIV.setImageResource(obj!!.icon)
        }

        private fun setItems(
            imageView: ImageView,
            title: TextView,
            subTitle: TextView,
            obj: PreferencesScreenItem?
        ) {
            title.text = obj?.title
            subTitle.text = obj?.subtitle
            imageView.setImageResource(obj!!.icon)
        }

        private fun setItemAsOther(otherAndSignout: TextView, obj: PreferencesScreenItem?) {
            otherAndSignout.text = obj?.title
        }

        private fun hideMainConstraintViewAndOthersViewInItemView(
            constraintView: View,
            otherAndSignout: TextView
        ) {
            constraintView.visibility = View.INVISIBLE
            otherAndSignout.visibility = View.INVISIBLE
        }

        override fun onClick(v: View?) {
            clickListener?.onItemClickListener(adapterPosition)
        }

    }

    var data = ArrayList<PreferencesScreenItem>()
    var clickListener : ItemClickListener?=null

    fun setItemClickListener(itemClickListener : ItemClickListener){
        this.clickListener = itemClickListener
    }

    interface ItemClickListener{
        fun onItemClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferencesViewHolder {
        return PreferencesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.prefrences_item,null))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PreferencesViewHolder, position: Int) {
        holder.bindView(data.get(position),position)
    }
}
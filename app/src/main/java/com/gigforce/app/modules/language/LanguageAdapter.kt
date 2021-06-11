package com.gigforce.app.modules.language

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.app.R
import com.gigforce.profile.models.CityWithImage

class LanguageAdapter(
        private val context: Context
) : RecyclerView.Adapter<LanguageAdapter.OnboardingMajorCityViewHolder>(),
        Filterable {

    var originalLanguageList: List<Language> = emptyList()
    private var filteredLanguageList: List<Language> = emptyList()

    private val contactsFilter = CityFilter()

    private var selectedItemIndex: Int = 0
    private var onLanguageSelectedListener: LanguageAdapterClickListener? = null

    fun setOnCitySelectedListener(onCitySelectedListener: LanguageAdapterClickListener) {
        this.onLanguageSelectedListener = onCitySelectedListener
    }
    var allViewHolder = ArrayList<OnboardingMajorCityViewHolder>()
    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): OnboardingMajorCityViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(R.layout.recycler_item_language, parent, false)
        LanguageSelectFragment.languageCode?.let {
            if(it.equals("en")){
                selectedItemIndex = 0
            }
            else if(it.equals("hi")){
                selectedItemIndex = 1
            }
            else{
                selectedItemIndex = -1
            }
        }
        var viewHolder = OnboardingMajorCityViewHolder(view)
        allViewHolder.add(viewHolder)
        return viewHolder
    }

    fun getSelectedItemIndex(): Int {
        return selectedItemIndex
    }

    fun resetSelectedItem() {
        if (selectedItemIndex == -1)
            return

        val tempIndex = selectedItemIndex
        selectedItemIndex = -1
        notifyItemChanged(tempIndex)
    }

    override fun getItemCount(): Int {
        return filteredLanguageList.size
    }

    override fun onBindViewHolder(holder: OnboardingMajorCityViewHolder, position: Int) {
        holder.bindValues(filteredLanguageList.get(position), position)
    }

    fun setData(contacts: List<Language>) {

//        this.selectedItemIndex = -1
        this.originalLanguageList = contacts
        this.filteredLanguageList = contacts
        notifyDataSetChanged()
    }
    fun getSelectedLanguage():Language{

        if(selectedItemIndex == -1 ){
            return  Language(
                    languageCode = "en",
                    languageName = "English",
                    bigTextToDisplay = "Aa"
            )
        } else {
            return originalLanguageList.get(selectedItemIndex)
        }
    }
    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                filteredLanguageList = originalLanguageList
            } else {
                val filteredList: MutableList<Language> = mutableListOf()
                for (contact in originalLanguageList) {
                    if (contact.languageName.contains(
                                    charString,
                                    true
                            )
                    )
                        filteredList.add(contact)
                }
                filteredLanguageList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = filteredLanguageList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredLanguageList = results?.values as List<Language>
            notifyDataSetChanged()
        }
    }


    inner class OnboardingMajorCityViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        var languageNameTV: TextView = itemView.findViewById(R.id.language_name_tv)
        var languageNameBigTv: TextView = itemView.findViewById(R.id.langugage_text_big)
        var languageRootLayout: LinearLayout = itemView.findViewById(R.id.language_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(language : Language, position: Int) {
            languageNameTV.text = language.languageName
            languageNameBigTv.text = language.bigTextToDisplay

            if (selectedItemIndex == position) {
                languageNameTV.setTextColor(ResourcesCompat.getColor(context.resources,R.color.lipstick,null))
                languageNameBigTv.setTextColor(ResourcesCompat.getColor(context.resources,R.color.lipstick,null))
                languageRootLayout.setBackgroundResource(R.drawable.rectangle_round_light_pink)

            } else {
                languageNameTV.setTextColor(ResourcesCompat.getColor(context.resources,R.color.black,null))
                languageNameBigTv.setTextColor(ResourcesCompat.getColor(context.resources,R.color.black,null))
                languageRootLayout.setBackgroundResource(R.drawable.rectangle_round_light_blue)
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition

            if (selectedItemIndex != -1) {
                val tempIndex = selectedItemIndex
                selectedItemIndex = newPosition
//                notifyItemChanged(tempIndex)
//                notifyItemChanged(selectedItemIndex)
            } else {
                selectedItemIndex = newPosition
//                notifyItemChanged(selectedItemIndex)
            }
//            if (selectedItemIndex != -1) {
//                selectedItemIndex = -1
//            } else {
//                selectedItemIndex = adapterPosition
//            }
//            notifyDataSetChanged()
            notifyDataSetChanged()

            if(newPosition == -1)
                return

            val language = filteredLanguageList[newPosition]
            onLanguageSelectedListener?.onLanguageSelected(
                    language,allViewHolder.get(newPosition)
            )
        }

    }


    interface LanguageAdapterClickListener{

        fun onLanguageSelected(language : Language,viewHolder:OnboardingMajorCityViewHolder)
    }

}
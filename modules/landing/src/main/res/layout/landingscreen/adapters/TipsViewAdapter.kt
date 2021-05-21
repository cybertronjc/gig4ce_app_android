package com.gigforce.landing_screen.landingscreen.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.landing_screen.landingscreen.models.Tip

class TipsViewAdapter(
    private val context: Context
) : RecyclerView.Adapter<TipsViewAdapter.TipsViewHolder>(){

    private var originalList: List<Tip> = emptyList()
    private var onTipListener : OnTipListener? = null

    fun setOnTipListener(onTipListener: OnTipListener){
        this.onTipListener = onTipListener
    }
    private var onSkipListener : OnSkipListener? = null

    fun setOnSkipListener(onSkipListener: OnSkipListener){
        this.onSkipListener = onSkipListener
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TipsViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.gigforce_tips_item, parent, false)
        return TipsViewHolder(view)
    }


    override fun getItemCount(): Int {
        return originalList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == originalList.size - 1 ){
            return 2
        }
        else {
            return 1

        }
    }

    override fun onBindViewHolder(holder: TipsViewHolder, position: Int) {
        holder.bindValues(originalList.get(position), position)
    }

    fun setData(contacts: List<Tip>) {
        this.originalList = contacts
        notifyDataSetChanged()
    }


    inner class TipsViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var title: TextView = itemView.findViewById(R.id.gigtip_title)
        private var subtitle: TextView = itemView.findViewById(R.id.gigtip_subtitle)
        private var text102: ImageView = itemView.findViewById(R.id.textView102)
        private var skip: TextView = itemView.findViewById(R.id.skip)


        fun bindValues(model: Tip, position: Int) {
            title.text = model.title
            subtitle.text = model.subTitle

            text102.setOnClickListener {
                onTipListener?.onTipClicked(model = model)
            }

            skip.setOnClickListener {
                onSkipListener?.onSkipClicked(model = model, pos = position)
            }

        }

    }


    interface OnTipListener {

        fun onTipClicked(
            model: Tip
        )
    }

    interface OnSkipListener {

        fun onSkipClicked(
            model: Tip,
            pos: Int
        )
    }


}
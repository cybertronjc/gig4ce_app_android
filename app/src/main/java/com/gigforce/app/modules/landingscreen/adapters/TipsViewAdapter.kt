package com.gigforce.app.modules.landingscreen.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.landingscreen.models.Tip

class TipsViewAdapter(
    private val context: Context
) : RecyclerView.Adapter<TipsViewAdapter.TipsViewHolder>(){

    private var originalList: List<Tip> = emptyList()
    private var onCardSelectedListener : OnCardSelectedListener? = null

    fun setOnCardSelectedListener(onCardSelectedListener: OnCardSelectedListener){
        this.onCardSelectedListener = onCardSelectedListener
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
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var title: TextView = itemView.findViewById(R.id.gigtip_title)
        private var subtitle: TextView = itemView.findViewById(R.id.gigtip_subtitle)
        private var text102: ImageView = itemView.findViewById(R.id.textView102)
        private var skip: TextView = itemView.findViewById(R.id.skip)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(model: Tip, position: Int) {
            title.text = model.title
            subtitle.text = model.subTitle
        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = originalList[newPosition]
            onCardSelectedListener?.onCardSelected(jobProfile)
        }

    }


    interface OnCardSelectedListener {

        fun onCardSelected(
            model: Tip
        )
    }


}
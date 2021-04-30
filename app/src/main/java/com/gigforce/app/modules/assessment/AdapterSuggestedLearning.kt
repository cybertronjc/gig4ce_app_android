package com.gigforce.app.modules.assessment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R

class AdapterSuggestedLearning : RecyclerView.Adapter<AdapterSuggestedLearning.ViewHolder>() {
    private var callbacks: AdapterSuggestedLearningCallbacks? = null;
    fun setCallbacks(callbacks: AdapterSuggestedLearningCallbacks): AdapterSuggestedLearning {
        this.callbacks = callbacks
        return this
    }

    interface AdapterSuggestedLearningCallbacks {
        fun onClickSuggestedLearnings()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_suggested_learnings_assess_result, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.setOnClickListener {
            callbacks?.onClickSuggestedLearnings()
        }
    }
}
package com.gigforce.app.modules.assessment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import kotlinx.android.synthetic.main.layout_rv_answers_adapter.view.*

class AssessmentAnswersAdapter : RecyclerView.Adapter<AssessmentAnswersAdapter.ViewHolderAnswer>() {


    inner class ViewHolderAnswer(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderAnswer {
        return ViewHolderAnswer(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_answers_adapter, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: ViewHolderAnswer, position: Int) {
        holder.itemView.tv_number_rv_access_frag.text = (65 + position).toChar() + "."

    }

}
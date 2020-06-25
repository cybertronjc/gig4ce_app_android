package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.qa_row.view.*

class QArow (context: Context, attributeSet: AttributeSet): MaterialCardView(context, attributeSet) {

    init {
        View.inflate(context, R.layout.qa_row, this)
    }

    var ques: String = ""
        set(value) {
            field = value
            question.text = value
        }

    var ans: String = ""
        set(value) {
            field = value
        }

    var ans_summary: String = ""
        set(value) {
            field = value
            answer_summary.text = ans_summary
        }

}
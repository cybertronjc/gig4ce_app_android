package com.gigforce.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.wallet.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.qa_row.view.*

class QArow: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        View.inflate(context, R.layout.qa_row, this)
    }

    var ques: String = ""
        set(value) {
            field = value
            if (value.trim() != "")
                question.text = value
        }

    var ans: String = ""
        set(value) {
            field = value
        }

    var ans_summary: String = ""
        set(value) {
            field = value
            if (value.trim() != "")
                answer_summary.text = ans_summary
        }

}
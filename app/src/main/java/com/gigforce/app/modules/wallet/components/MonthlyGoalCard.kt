package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.monthly_goal_card.view.*

class MonthlyGoalCard: MaterialCardView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    init {
        View.inflate(context, R.layout.monthly_goal_card, this)
    }

    var avgMonthSalary: Int = 0
        set(value) {
            field = value
        }

    var currentMonthSalary: Int = 0
        set(value) {
            field = value
        }

    var isMonthlyGoalSet: Boolean = false
        set(value) {
            field = value
            setSummaryText()
        }

    var monthlyGoalAmount: Int = 0
        set(value) {
            field = value
        }

    private fun setSummaryText() {
        if (!isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text = "Click here to add monthly goal"
        } else if (!isMonthlyGoalSet && avgMonthSalary > 0){
            avg_earning.text = "Average earning per month is Rs XYZ. Click here to set monthly goal."
        } else if (isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text = "Monthly goal is Rs XYZ"
        } else {
            avg_earning.text = "Average earning per month is Rs XYZ"
        }
    }

    private fun setBonusText() {
        if (currentMonthSalary > monthlyGoalAmount/2)
            bonus_text.text = "You are doing really great"
        else if (!isMonthlyGoalSet)
            bonus_text.text = "You haven't set your monthly goal"
        else
            bonus_text.text = ""
    }
}
package com.gigforce.app.modules.wallet.components

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.View
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.monthly_goal_card.view.*
import kotlin.reflect.jvm.internal.impl.renderer.RenderingFormat

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
            setBonusText()
            if (monthlyGoalAmount > 0)
                progress_bar.progress = (currentMonthSalary*1.0F / monthlyGoalAmount)*100F
        }

    var monthlyGoalAmount: Int = 0
        set(value) {
            field = value
        }

    private fun setSummaryText() {
        if (!isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text = Html.fromHtml("<font color='#060606'><u>Click here</u></font> here to set monthly goal")
            progress_bar.progress = 5F

        } else if (!isMonthlyGoalSet && avgMonthSalary > 0){
            avg_earning.text = Html.fromHtml("Average earning per month is Rs $avgMonthSalary. <font color='#060606'><u>Click here</u></font> to set monthly goal.")
            progress_bar.progress = 5F
        } else if (isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text = "Monthly goal is Rs $monthlyGoalAmount"
        } else {
            avg_earning.text = "Average earning per month is Rs $avgMonthSalary"
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
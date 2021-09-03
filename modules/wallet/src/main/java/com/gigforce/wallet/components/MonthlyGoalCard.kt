package com.gigforce.wallet.components

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.View
import com.gigforce.wallet.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.monthly_goal_card.view.*

class MonthlyGoalCard : MaterialCardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        View.inflate(context, R.layout.monthly_goal_card, this)
    }

    var avgMonthSalary: Int = 0

    var currentMonthSalary: Int = 0

    var isMonthlyGoalSet: Boolean = false
        set(value) {
            field = value
            setSummaryText()
            setBonusText()
            if (monthlyGoalAmount > 0)
                progress_bar.progress = (currentMonthSalary * 1.0F / monthlyGoalAmount) * 100F
        }

    var monthlyGoalAmount: Int = 0

    private fun setSummaryText() {
        if (!isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text =
                Html.fromHtml("<font color='#a30674'><u>Click here</u></font> here to set monthly goal")
            progress_bar.progress = 5F

        } else if (!isMonthlyGoalSet && avgMonthSalary > 0) {
            avg_earning.text =
                Html.fromHtml("Average earning per month is Rs $avgMonthSalary. <font color='#060606'><u>Click here</u></font> to set monthly goal.")
            progress_bar.progress = 5F
        } else if (isMonthlyGoalSet && avgMonthSalary == 0) {
            avg_earning.text =
                Html.fromHtml("Monthly goal is <font color='#a30674'>Rs $monthlyGoalAmount</font>")
        } else {
            avg_earning.text =
                Html.fromHtml("Average earning per month is <font color='#a30674'>Rs $avgMonthSalary</font>")
        }
    }

    private fun setBonusText() {
        if (currentMonthSalary > monthlyGoalAmount / 2)
            bonus_text.text = context.getString(R.string.doing_really_great_wallet)
        else if (!isMonthlyGoalSet)
            bonus_text.text = context.getString(R.string.havent_set_monthly_goal_wallet)
        else
            bonus_text.text = "Plan your gigs to get maximum \nout of them"
    }
}
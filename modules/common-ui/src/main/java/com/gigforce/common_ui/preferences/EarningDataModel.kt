package com.gigforce.common_ui.preferences

import com.gigforce.core.fb.BaseFirestoreDataModel

class EarningDataModel : BaseFirestoreDataModel {
    constructor():super("earning")
    var preferredNoOfDays : String = ""
    var perDayGoal : Int = 0
    var perMonthGoal : Int = 0
    var monthlyContractRequired : Boolean = false
    var monthlyExpectation : Int = 0
}
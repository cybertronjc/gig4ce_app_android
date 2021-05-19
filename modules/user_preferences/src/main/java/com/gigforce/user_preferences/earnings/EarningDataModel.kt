package com.gigforce.user_preferences.earnings

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

class EarningDataModel : BaseFirestoreDataModel {
    constructor():super("earning")
    var preferredNoOfDays : String = ""
    var perDayGoal : Int = 0
    var perMonthGoal : Int = 0
    var monthlyContractRequired : Boolean = false
    var monthlyExpectation : Int = 0
}
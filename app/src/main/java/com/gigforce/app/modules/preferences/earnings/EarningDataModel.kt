package com.gigforce.app.modules.preferences.earnings

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

class EarningDataModel : BaseFirestoreDataModel {
    constructor():super("earning")
    var preferredNoOfDays : String = ""
    var perDayGoal : Int = 0
    var perMonthGoal : Int = 0
    var monthlyContractRequired : Boolean = false
    var monthlyExpectation : Int = 0
}
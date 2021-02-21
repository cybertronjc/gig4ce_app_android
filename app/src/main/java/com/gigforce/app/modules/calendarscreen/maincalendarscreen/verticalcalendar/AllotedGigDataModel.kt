package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.CalendarHomeScreenViewModel
import java.util.*
import kotlin.collections.ArrayList

class AllotedGigDataModel : BaseFirestoreDataModel {
    companion object{
        fun getGigData(gig: CalendarHomeScreenViewModel.GigData):AllotedGigDataModel{
            var calendarObj = Calendar.getInstance()
            calendarObj.time = gig.startDateTime
            var data = AllotedGigDataModel()
            data.date = calendarObj.get(Calendar.DATE)
            data.month = calendarObj.get(Calendar.MONTH)
            data.year = calendarObj.get(Calendar.YEAR)
            data.title = gig.title
            data.gigDetails = ArrayList<GigsDetail>()
            data.available = true
            return data
        }
    }
    constructor():super("all_gigs")
    var date:Int = -1
    var month:Int = -1
    var year:Int = -1
    var title:String = ""
    var gigDetails:ArrayList<GigsDetail> = ArrayList<GigsDetail>()
    var available :Boolean = true

}
class GigsDetail{
    var subTitle:String = ""
    var fromTime:String = ""
    var toTime:String = ""
    var gigCompleted:Boolean = false
    var available :Boolean = true
}
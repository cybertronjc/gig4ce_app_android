package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.CalendarHomeScreenViewModel

class AllotedGigDataModel : BaseFirestoreDataModel {
    companion object{
        fun getGigData(gig: CalendarHomeScreenViewModel.GigData):AllotedGigDataModel{
            var data = AllotedGigDataModel()
            data.date = gig.startDateTime.date
            data.month = gig.startDateTime.month
            data.year = gig.startDateTime.year
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
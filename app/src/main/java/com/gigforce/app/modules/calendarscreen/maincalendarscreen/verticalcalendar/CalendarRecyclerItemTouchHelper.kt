package com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter

class CalendarRecyclerItemTouchHelper(dragDirs:Int, swipeDirs:Int, listener:RecyclerItemTouchHelperListener):ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    private val listener:RecyclerItemTouchHelperListener
    init{
        this.listener = listener
    }
    override fun onMove(recyclerView:RecyclerView, viewHolder:RecyclerView.ViewHolder, target:RecyclerView.ViewHolder):Boolean {
        return true
    }

    fun getTopView(viewHolder:RecyclerView.ViewHolder):View{
        var view = (viewHolder as PFRecyclerViewAdapter<VerticalCalendarDataItemModel>.ViewHolder).getView(
            R.id.calendar_detail_item_cl)
//        view.findViewById<View>(R.id.daydatecard).visibility = View.GONE
        return view
    }

//    fun onSelectedChanged(viewHolder:RecyclerView.ViewHolder, actionState:Int) {
//        if (viewHolder != null)
//        {
//            val foregroundView = getTopView(viewHolder)
//            getDefaultUIUtil().onSelected(foregroundView)
//        }
//    }
    override fun onChildDrawOver(c:Canvas, recyclerView:RecyclerView,
                        viewHolder:RecyclerView.ViewHolder, dX:Float, dY:Float,
                        actionState:Int, isCurrentlyActive:Boolean) {
        val foregroundView = getTopView(viewHolder)
        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
    }
    override fun clearView(recyclerView:RecyclerView, viewHolder:RecyclerView.ViewHolder) {
        val foregroundView = getTopView(viewHolder)
        getDefaultUIUtil().clearView(foregroundView)
    }
    override fun onChildDraw(c:Canvas, recyclerView:RecyclerView,
                    viewHolder:RecyclerView.ViewHolder, dX:Float, dY:Float,
                    actionState:Int, isCurrentlyActive:Boolean) {
        val foregroundView = getTopView(viewHolder)
        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
            actionState, isCurrentlyActive)
    }
    override fun onSwiped(viewHolder:RecyclerView.ViewHolder, direction:Int) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition())
    }
    override fun convertToAbsoluteDirection(flags:Int, layoutDirection:Int):Int {
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }
    interface RecyclerItemTouchHelperListener {
        fun onSwiped(viewHolder:RecyclerView.ViewHolder, direction:Int, position:Int)
    }
}
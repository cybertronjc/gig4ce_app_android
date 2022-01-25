package com.gigforce.giger_gigs.attendance_tl

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders.AttendanceItemViewHolder
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class AttendanceSwipeHandler(
        private val attendanceSwipeHandlerListener : AttendanceSwipeHandlerListener
)  : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
    private var previousDx = 0f

    var attendanceSwipeControlsEnabled : Boolean = false
    var markPresentSwipeActionEnabled : Boolean = false
    var declineSwipeActionEnabled : Boolean = false

    override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder is AttendanceItemViewHolder) {

            try {
                val gigData = viewHolder.getData()
                return when {
                    "Present".equals(gigData.attendanceStatus, true) && declineSwipeActionEnabled-> ItemTouchHelper.LEFT
                    "Declined".equals(gigData.attendanceStatus, true) && markPresentSwipeActionEnabled -> ItemTouchHelper.RIGHT
                    "Absent".equals(gigData.attendanceStatus, true) && markPresentSwipeActionEnabled && declineSwipeActionEnabled -> ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                    "Absent".equals(gigData.attendanceStatus, true) && markPresentSwipeActionEnabled -> ItemTouchHelper.RIGHT
                    "Absent".equals(gigData.attendanceStatus, true) && declineSwipeActionEnabled -> ItemTouchHelper.LEFT
                    else -> 0 //Disabling swipe
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return super.getSwipeDirs(recyclerView, viewHolder)
            }
        } else {
            return 0 //Disabling swipe for view type that are not attendance items
        }
    }

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean = attendanceSwipeControlsEnabled

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (viewHolder is AttendanceItemViewHolder) {

            try {
                val gigData = viewHolder.getData()

                when (direction) {
                    ItemTouchHelper.LEFT -> attendanceSwipeHandlerListener.onLeftSwipedForDecliningAttendance(
                        viewHolder,
                        gigData
                    )
                    ItemTouchHelper.RIGHT -> attendanceSwipeHandlerListener.onRightSwipedForMarkingPresent(
                        viewHolder,
                        gigData
                    )
                    else -> { /*Do Nothing*/
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        previousDx = 0f
    }

    override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        if(viewHolder is AttendanceItemViewHolder){
            val foregroundView = viewHolder.getViewBinding().viewForeground
            getDefaultUIUtil().onDraw(c,
                    recyclerView,
                    foregroundView,
                    dX /2.5f,
                    dY,
                    actionState,
                    isCurrentlyActive
            )
        } else {
            super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX / 2.5f,
                    dY,
                    actionState,
                    isCurrentlyActive
            )
        }
        previousDx = dX
    }

    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        if(viewHolder is AttendanceItemViewHolder){
            val foregroundView = viewHolder.getViewBinding().viewForeground
            getDefaultUIUtil().onDrawOver(c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
            )
        } else {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX  , dY, actionState, isCurrentlyActive)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

        if(viewHolder is AttendanceItemViewHolder){
            val foregroundView = viewHolder.getViewBinding().viewForeground
            getDefaultUIUtil().clearView(foregroundView)
        } else {
            super.clearView(recyclerView, viewHolder)
        }
    }


    interface AttendanceSwipeHandlerListener {

        fun onRightSwipedForMarkingPresent(
                viewHolder : RecyclerView.ViewHolder,
                attendanceData : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        )

        fun onLeftSwipedForDecliningAttendance(
                viewHolder : RecyclerView.ViewHolder,
                attendanceData : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        )
    }
}
package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.core.CoreViewHolder
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.views.GigerAttendanceItemRecyclerItemView
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData

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
        if (viewHolder is CoreViewHolder &&  viewHolder.itemView is GigerAttendanceItemRecyclerItemView) {

            try {
                val gigData = (viewHolder.itemView as GigerAttendanceItemRecyclerItemView).getGigDataOrThrow()

                return when {
                    gigData.currentlyMarkingAttendanceForThisGig -> ItemTouchHelper.ACTION_STATE_IDLE // Disabling swipe
                    gigData.canTLMarkAbsent && declineSwipeActionEnabled && gigData.canTLMarkPresent && markPresentSwipeActionEnabled -> ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
                    gigData.hasTLMarkedAttendance.not() && markPresentSwipeActionEnabled && declineSwipeActionEnabled -> ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT

                    gigData.canTLMarkAbsent && declineSwipeActionEnabled-> ItemTouchHelper.LEFT
                    gigData.canTLMarkPresent && markPresentSwipeActionEnabled -> ItemTouchHelper.RIGHT
                    gigData.hasTLMarkedAttendance.not() && markPresentSwipeActionEnabled -> ItemTouchHelper.RIGHT
                    gigData.hasTLMarkedAttendance.not() && declineSwipeActionEnabled -> ItemTouchHelper.LEFT
                    else -> ItemTouchHelper.ACTION_STATE_IDLE // Disabling swipe
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return super.getSwipeDirs(recyclerView, viewHolder)
            }
        } else {
            return ItemTouchHelper.ACTION_STATE_IDLE //Disabling swipe for view type that are not attendance items
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
        if (viewHolder is CoreViewHolder &&  viewHolder.itemView is GigerAttendanceItemRecyclerItemView) {

            try {
                val gigData = (viewHolder.itemView as GigerAttendanceItemRecyclerItemView).getGigDataOrThrow()

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
        if(viewHolder is CoreViewHolder && viewHolder.itemView is GigerAttendanceItemRecyclerItemView){
            val foregroundView = (viewHolder.itemView as GigerAttendanceItemRecyclerItemView).viewForeground
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
        if(viewHolder is CoreViewHolder){
            val foregroundView = (viewHolder.itemView as GigerAttendanceItemRecyclerItemView).viewForeground
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

        if(viewHolder is CoreViewHolder){
            val foregroundView = (viewHolder.itemView as GigerAttendanceItemRecyclerItemView).viewForeground
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
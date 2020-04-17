package com.gigforce.app.modules.homescreen

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter.OnViewHolderClick
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter.ItemInterface
import com.riningan.widget.ExtendedBottomSheetBehavior
import com.riningan.widget.ExtendedBottomSheetBehavior.STATE_COLLAPSED
import kotlinx.android.synthetic.main.homescreen_1nsvbs.*
import kotlinx.android.synthetic.main.item_homescreen1.*


// UNused below can be removed
//class DemoBottomSheetFragment : SuperBottomSheetFragment() {
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.homescreen1_bs1, container, false)
//    }
//    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
//    override fun getStatusBarColor() = Color.RED
//}
//
//class ExtBottomSheetFragment : BaseFragment() {
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        super.onCreateView(inflater, container, savedInstanceState)
//        return inflater.inflate(R.layout.homescreen_1nsvbs, container, false)
//    }
//    //override fun getCornerRadius() = "16dp" //context!!.resources.getDimension(R.dimen.demo_sheet_rounded_corner)
//    //override fun getStatusBarColor() = Color.RED
//}

class HomeScreenNew : BaseFragment() {

    companion object {
        fun newInstance() = HomeScreenNew()
    }

    private var mExtendedBottomSheetBehavior: ExtendedBottomSheetBehavior<*>? = null
    private lateinit var viewModel: HomeScreenNewViewModel
    private val itemList: Array<String>
        get() = arrayOf("My Gig", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6", "Item 7", "Item 8", "Item 9", "Item 10", "Item 11", "Item 12")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.homescreen_1nsvbs, inflater, container)
    }

    class DataItem {
        var title:String = "Swiggy Deliveries";
        var subTitle:String = "+3 More";
        var date:String = "16";
        var comment:String = "Last gig complete 20 min ago";
        var month:String = "April";
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mExtendedBottomSheetBehavior = ExtendedBottomSheetBehavior.from(nsv)
        //val sheet = DemoBottomSheetFragment()
        //sheet.show(activity!!.supportFragmentManager, "bsFragment")
        mExtendedBottomSheetBehavior?.state = STATE_COLLAPSED
        val allowUserDragging:Boolean = true;
        mExtendedBottomSheetBehavior?.isAllowUserDragging = allowUserDragging;

        viewModel = ViewModelProviders.of(this).get(HomeScreenNewViewModel::class.java)
        var datalist: ArrayList<DataItem> = ArrayList<DataItem>()
//        var di = DataItem();
//        di.title = "Swiggy Deliveries"
//        di.subTitle = "+3 More"
//        di.comment = "Last gig complete 20 min ago"

        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())
        datalist.add(DataItem())

        val recyclerGenericAdapter : RecyclerGenericAdapter<DataItem> = RecyclerGenericAdapter<DataItem>(
            activity?.applicationContext,
            PFRecyclerViewAdapter.OnViewHolderClick <DataItem?> { view, position, item -> showToast("")},
            RecyclerGenericAdapter.ItemInterface<DataItem?> { obj, viewHolder, position ->
                val title:TextView = viewHolder.getView(R.id.hs1_title) as TextView
                title.text = obj?.title
                val subtitle:TextView = viewHolder.getView(R.id.hs1_subtitle) as TextView
                subtitle.text = obj?.subTitle
                val comment:TextView = viewHolder.getView(R.id.hs1_comment) as TextView
                comment.text = obj?.comment
                val month:TextView = viewHolder.getView(R.id.hs1_month) as TextView
                month.text = obj?.month
                val date:TextView = viewHolder.getView(R.id.hs1_date) as TextView
                date.text = obj?.date
            })!!

        recyclerGenericAdapter.list = datalist
        recyclerGenericAdapter.setLayout(R.layout.item_homescreen1)
        rv_.layoutManager = LinearLayoutManager(
                activity?.applicationContext,
                LinearLayoutManager.VERTICAL,
                false
        )
        rv_.adapter = recyclerGenericAdapter


        // layout manager for rv2 - make horizontal.
//        rv2_hs.layoutManager = LinearLayoutManager(
//            context,
//            LinearLayoutManager.HORIZONTAL,
//            false)
//
//        rv2_hs.adapter = recyclerGenericAdapter

        val gridview = view.findViewById<GridView>(R.id.gridView_hs1)
        val adapter = this.context?.let { FeaturesAdapter(it, R.layout.item_grid_features_hs1, itemList) }
        gridview.adapter = adapter
    }

    fun onClick(v: View) {
        when (v.id) {
//            R.id.btnHide -> mExtendedBottomSheetBehavior!!.setState(ExtendedBottomSheetBehavior.STATE_HIDDEN)
//            R.id.btnCollapse -> mExtendedBottomSheetBehavior!!.setState(STATE_COLLAPSED)
//            R.id.btnHalf -> mExtendedBottomSheetBehavior!!.setState(ExtendedBottomSheetBehavior.STATE_HALF)
//            R.id.btnExpand -> mExtendedBottomSheetBehavior!!.setState(ExtendedBottomSheetBehavior.STATE_EXPANDED)
//            R.id.btnDragging -> {
//                mExtendedBottomSheetBehavior!!.isAllowUserDragging =
//                    !mExtendedBottomSheetBehavior!!.isAllowUserDragging
//                if (mExtendedBottomSheetBehavior!!.isAllowUserDragging) {
//                    btnDragging.text = "Disable dragging"
//                } else {
//                    btnDragging.text = "Enable dragging"
//                }
//            }
        }
    }
}
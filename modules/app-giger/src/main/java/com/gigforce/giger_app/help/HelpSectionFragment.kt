package com.gigforce.giger_app.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.giger_app.R
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.help_section_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpSectionFragment : Fragment() {

    private val viewModel: HelpSectionViewModel by activityViewModels()
    @Inject
    lateinit var navigation : INavigation

    @Inject
    lateinit var eventTracker: IEventTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.help_section_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_OPEN, null))
        observer()
        listeners()
    }

    private fun listeners() {
        appBar2.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun observer() {
        viewModel.helpSectionLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Lce.Loading -> {

                }
                is Lce.Content -> {
                    if(it.content.isEmpty()){
                        showToast("Data not found")
                    }else{
                        helpMasterData.addAll(it.content)
                        recyclerGenericAdapter?.notifyDataSetChanged()
                    }
                }
                is Lce.Error ->{

                }
            }
        })
    }

    val helpMasterData = ArrayList<HelpSectionDM>()
    var recyclerGenericAdapter: RecyclerGenericAdapter<HelpSectionDM>?=null
    private fun setRecyclerView() {
        helpMasterData.clear()
//        helpMasterData.addAll(getData())
        recyclerGenericAdapter=
            RecyclerGenericAdapter<HelpSectionDM>(
                activity?.applicationContext,
                { view, position, item -> showToast("click listner") },
                { obj, viewHolder, position ->
                    val title: TextView = viewHolder.getView(R.id.textView17) as TextView
                    title.text = obj?.name
                    viewHolder.getView(R.id.top_layout).setOnClickListener{
                        try {

                            helpMasterData.get(position).questions?.let {
                                if(it.size>0) {
                                    navigation.navigateTo("HelpDetailSectionFragment", bundleOf("data" to helpMasterData.get(position)) )
                                    val map = mapOf("Section Title" to obj?.name.toString())
                                    eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_CATEGORY_SELECT, map))
                                } else showToast("Questions not found!!")
                            }
                        }catch (e:Exception){

                        }
                    }
                })

        recyclerGenericAdapter?.list = helpMasterData
        recyclerGenericAdapter?.setLayout(R.layout.help_section_cat_item)
        category_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        category_rv.adapter = recyclerGenericAdapter
    }

    fun getData(): ArrayList<HelpSectionDM> {
        val helpSectionDataArr = ArrayList<HelpSectionDM>()
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary1",
                id = "id",
                questions = getQuestions(1)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary2",
                id = "id2",
                questions = getQuestions(2)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary3",
                id = "id1",
                questions = getQuestions(3)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary4",
                id = "id1",
                questions = getQuestions(4)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary5",
                id = "id1",
                questions = getQuestions(5)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary6",
                id = "id1",
                questions = getQuestions(6)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary7",
                id = "id1",
                questions = getQuestions(7)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary8",
                id = "id1",
                questions = getQuestions(8)
            )
        )
        helpSectionDataArr.add(
            HelpSectionDM(
                name = "Login Summary9",
                id = "id1",
                questions = getQuestions(9)
            )
        )
        return helpSectionDataArr
    }

    private fun getQuestions(itemNum: Int): ArrayList<HelpDetailSectionDM> {
        val helpDetailSectionDMArr = ArrayList<HelpDetailSectionDM>()
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )
        helpDetailSectionDMArr.add(
            HelpDetailSectionDM(
                question = "I cannot see the icon of TL workspace in my APP $itemNum",
                answer = "TL workspace icon is visible once the offer letter is generated. If it is pending. Please email to the Gigforce HR team.$itemNum",
                helpful = "was this Helpful",
                thanksText = "THANK YOU! FOR YOUR RESPONSE",
                confirmLabel = "YES",
                notConfirmLabel = "NO"
            )
        )

        return helpDetailSectionDMArr
    }
}
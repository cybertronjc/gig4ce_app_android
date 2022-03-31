package com.gigforce.giger_app.help

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.help_section_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpDetailSectionFragment : Fragment() {

    @Inject
    lateinit var eventTracker: IEventTracker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_detail_section, container, false)
    }

    var helpSectionDMData: HelpSectionDM? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            try {
                helpSectionDMData = it.get("data") as HelpSectionDM
                initializeAll()
                setRecyclerView()
            } catch (e: Exception) {

            }
        }
        listeners()

    }

    private fun listeners() {
        appBar2.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun initializeAll() {
        helpSectionDMData?.let {
            textView16.text = it.name

        }
    }

    private fun setRecyclerView() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<HelpDetailSectionDM> =
            RecyclerGenericAdapter<HelpDetailSectionDM>(
                activity?.applicationContext,
                { view, position, item -> showToast("") },
                { obj, viewHolder, position ->
                    val question: TextView = viewHolder.getView(R.id.textView17) as TextView
                    question.text = obj?.question

                    val answer: TextView = viewHolder.getView(R.id.textView19) as TextView
                    answer.text = obj?.answer

                    if (obj.openCard == false) {
                        viewHolder.getView(R.id.textView19).gone()
                        viewHolder.getView(R.id.detail_section).gone()
                        (viewHolder.getView(R.id.imageView2) as ImageView).setImageResource(R.drawable.ic_arrow_up_24)


                    } else {
                        viewHolder.getView(R.id.textView19).visible()
                        viewHolder.getView(R.id.detail_section).visible()
                        (viewHolder.getView(R.id.imageView2) as ImageView).setImageResource(R.drawable.ic_arrow_down_24)
                    }
                    obj.viewstatus?.let {
                        if (it == -1) {
                            viewHolder.getView(R.id.textView19).gone()
                            viewHolder.getView(R.id.detail_section).gone()
                        } else if (it == 0) {
                            viewHolder.getView(R.id.textView19).visible()
                            viewHolder.getView(R.id.detail_section).visible()
                            viewHolder.getView(R.id.section1).visible()
                            viewHolder.getView(R.id.section2).gone()
                            viewHolder.getView(R.id.thanks_message).gone()
                        } else if (it == 1) {
                            viewHolder.getView(R.id.textView19).visible()
                            viewHolder.getView(R.id.detail_section).visible()
                            viewHolder.getView(R.id.section1).gone()
                            viewHolder.getView(R.id.section2).visible()
                            viewHolder.getView(R.id.thanks_message).gone()
                        } else if (it == 2) {
                            viewHolder.getView(R.id.textView19).visible()
                            viewHolder.getView(R.id.detail_section).visible()
                            viewHolder.getView(R.id.section1).gone()
                            viewHolder.getView(R.id.section2).gone()
                            viewHolder.getView(R.id.thanks_message).visible()
                        }
                    }

                    viewHolder.getView(R.id.top_layout).setOnClickListener {
                        if (viewHolder.getView(R.id.detail_section).visibility == View.VISIBLE) {
                            viewHolder.getView(R.id.detail_section).gone()
                            viewHolder.getView(R.id.textView19).gone()
                            obj.openCard = false
                            (viewHolder.getView(R.id.imageView2) as ImageView).setImageResource(R.drawable.ic_arrow_up_24)
                        } else {
                            viewHolder.getView(R.id.detail_section).visible()
                            viewHolder.getView(R.id.textView19).visible()
                            obj.openCard = true
                            (viewHolder.getView(R.id.imageView2) as ImageView).setImageResource(R.drawable.ic_arrow_down_24)
                            val map = mapOf("Question Title" to obj?.question.toString())
                            eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_QUESTION_SELECT, map))
                        }
                    }

                    viewHolder.getView(R.id.textView22).setOnClickListener {
                        viewHolder.getView(R.id.section1).gone()
                        viewHolder.getView(R.id.section2).visible()
                        viewHolder.getView(R.id.thanks_message).gone()
                        obj.viewstatus = 1
                        val map = mapOf("Question Title" to obj?.question.toString())
                        eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_NOT_USEFUL, map))
                    }
                    viewHolder.getView(R.id.textView20).setOnClickListener {
                        viewHolder.getView(R.id.section2).gone()
                        viewHolder.getView(R.id.section1).gone()
                        viewHolder.getView(R.id.thanks_message).visible()
                        obj.viewstatus = 2
                        val map = mapOf("Question Title" to obj?.question.toString())
                        eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_USEFUL, map))
                    }
                    viewHolder.getView(R.id.call).setOnClickListener {
                        obj?.helpLineNumber?.let {
                            if (it.length > 0) {
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts("tel", it, null)
                                )
                                context?.startActivity(intent)
                            }
                            val map = mapOf("Question Title" to obj.question.toString())
                            eventTracker.pushEvent(TrackingEventArgs(HelpSectionAnalyticsEvents.EVENT_HELP_CALL_HELPLINE, map))
                        }

                    }


                })

        recyclerGenericAdapter.list = helpSectionDMData?.questions
        recyclerGenericAdapter.setLayout(R.layout.help_section_detail_item)
        category_rv.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            LinearLayoutManager.VERTICAL,
            false
        )
        category_rv.adapter = recyclerGenericAdapter
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
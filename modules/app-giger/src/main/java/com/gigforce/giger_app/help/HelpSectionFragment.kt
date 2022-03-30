package com.gigforce.giger_app.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.genericadapter.RecyclerGenericAdapter
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.help_section_fragment.*
import javax.inject.Inject

@AndroidEntryPoint
class HelpSectionFragment : Fragment() {

    private val viewModel: HelpSectionViewModel by activityViewModels()
    @Inject
    lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.help_section_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val recyclerGenericAdapter: RecyclerGenericAdapter<HelpSectionDM> =
            RecyclerGenericAdapter<HelpSectionDM>(
                activity?.applicationContext,
                { view, position, item -> showToast("click listner") },
                { obj, viewHolder, position ->
                    val title: TextView = viewHolder.getView(R.id.textView17) as TextView
                    title.text = obj?.name
                    viewHolder.getView(R.id.top_layout).setOnClickListener{
                        navigation.navigateTo("HelpDetailSectionFragment")
                    }
                })

        recyclerGenericAdapter.list = getData()
        recyclerGenericAdapter.setLayout(R.layout.help_section_cat_item)
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
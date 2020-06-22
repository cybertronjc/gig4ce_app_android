package com.gigforce.app.modules.chat.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_help_chat.*


class HelpChatFragment : BaseFragment() {

    fun ScrollView.scrollToBottom() {
        val lastChild = getChildAt(childCount - 1)
        val bottom = lastChild.bottom + paddingBottom
        val delta = bottom - (scrollY + height)
        smoothScrollBy(0, delta)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_help_chat, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        helpChatBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        needHelpWithRG.setOnCheckedChangeListener { group, checkedId ->
            myCurrentGigMessage.visibility = View.VISIBLE
            currentGigLayout.visibility = View.VISIBLE
            concernIssuesLayout.visibility = View.VISIBLE

            helpChatScrollView.post {
                helpChatScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }

        concernIssuesRG.setOnCheckedChangeListener { group, checkedId ->
            iCantReachLocationLayout.visibility = View.VISIBLE
            okMihirLayout.visibility = View.VISIBLE
            challengeFacingLayout.visibility = View.VISIBLE

            helpChatScrollView.post {
                helpChatScrollView.fullScroll(View.FOCUS_DOWN)
            }

        }

        challengeFacingRG.setOnCheckedChangeListener { group, checkedId ->
            iAmAtOfficeMessage.visibility = View.VISIBLE
            contactSupervisorSuggestionLayout.visibility = View.VISIBLE
            mapWithDirectionLayout.visibility = View.VISIBLE
            solutionsDoneLayout.visibility = View.VISIBLE

            helpChatScrollView.post {
                helpChatScrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

}
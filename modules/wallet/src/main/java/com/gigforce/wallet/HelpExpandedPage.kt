package com.gigforce.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.wallet.components.QArow
import com.gigforce.wallet.models.QA
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bs_wallet_help_question.view.*
import kotlinx.android.synthetic.main.help_expanded_page.*
import kotlinx.android.synthetic.main.qa_row.view.question

class HelpExpandedPage : WalletBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.help_expanded_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListernere()
    }

    private fun initialize() {

    }

    private fun setListernere() {
        toggle_terminology.setOnClickListener {
            if (terminologies.visibility == View.VISIBLE) {
                terminologies.visibility = View.GONE
                bt_expand_terminology.icon =
                    resources.getDrawable(R.drawable.ic_baseline_forward_24)
            } else {
                terminologies.visibility = View.VISIBLE
                bt_expand_terminology.icon = resources.getDrawable(R.drawable.ic_baseline_up_24)
            }
        }

        back_button.setOnClickListener { requireActivity().onBackPressed() }

        val qas = ArrayList(
            listOf(
                QA(
                    question = getString(R.string.set_monthly_goal_wallet),
                    answer = getString(R.string.click_on_monthly_goal_wallet)
                ),
                QA(
                    question = getString(R.string.what_is_wallet_balance_wallet),
                    answer = getString(R.string.amount_in_wallet_wallet)
                ),
                QA(
                    question = getString(R.string.transfer_fund_to_account_wallet),
                    answer = getString(R.string.manual_transfer_wallet)
                )
            )
        )

        for (qa in qas) {
            val widget = QArow(requireContext())
            widget.ques = qa.question
            widget.ans = qa.answer

            help_topics.addView(widget)

            widget.setOnClickListener {
                showBottomHelpDialog(widget.ques, widget.ans)
            }

        }
    }

    private fun showBottomHelpDialog(question_text: String, answer_text: String) {
        val dialogView = layoutInflater.inflate(R.layout.bs_wallet_help_question, null)
        dialogView.question.text = question_text
        dialogView.answer.text = answer_text
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialogView.cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}

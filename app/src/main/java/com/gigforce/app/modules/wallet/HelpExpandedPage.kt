package com.gigforce.app.modules.wallet

import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.gigforce.app.R
import com.gigforce.app.modules.wallet.components.QArow
import com.gigforce.app.modules.wallet.models.QA
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.bs_wallet_help_question.view.*
import kotlinx.android.synthetic.main.help_expanded_page.*
import kotlinx.android.synthetic.main.qa_row.*
import kotlinx.android.synthetic.main.qa_row.view.*
import kotlinx.android.synthetic.main.qa_row.view.question

class HelpExpandedPage: WalletBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.help_expanded_page, inflater, container)
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
                bt_expand_terminology.icon = resources.getDrawable(R.drawable.ic_baseline_forward_24)
            }
            else {
                terminologies.visibility = View.VISIBLE
                bt_expand_terminology.icon = resources.getDrawable(R.drawable.ic_baseline_up_24)
            }
        }

        back_button.setOnClickListener { this.onBackPressed() }

        val qas = ArrayList(
            listOf(
                QA(
                    question = "How to set monthly goal?",
                    answer = "click on monthly goal card in wallet screen."
                ),
                QA(
                    question = "What is wallet balance?",
                    answer = "amount in your wallet"
                ),
                QA(
                    question = "How to transfer fund to my account?",
                    answer = "Manual transfer is not supported yet."
                )))

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

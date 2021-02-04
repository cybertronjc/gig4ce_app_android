package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import androidx.core.view.isVisible
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.tokenautocomplete.TokenCompleteTextView
import kotlinx.android.synthetic.main.layout_add_lang_profile_v2.*


class AddLanguageProfileV2 : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_lang_profile_v2, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        intLangChipGroup()
        initSearchAutoComplete()
    }

    private fun initSearchAutoComplete() {


        act_langs_add_lang_profile_v2.setAdapter_(
            ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.lang_array)
            )
        )
        act_langs_add_lang_profile_v2.threshold = 1

        act_langs_add_lang_profile_v2.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Select)
//        act_langs_add_lang_profile_v2.setAdapter(langAdapter)
//        act_langs_add_lang_profile_v2.setTokenizer(CommaTokenizer())
//        act_langs_add_lang_profile_v2.threshold = 1
//        act_langs_add_lang_profile_v2.onItemClickListener =
//                AdapterView.OnItemClickListener { _, _, position, _ ->
//                    createRecipientChip(langAdapter.getItem(position).toString());
//                    act_langs_add_lang_profile_v2.append(" ")
//
//                }
    }

    private fun intLangChipGroup() {
        chip_search_add_language.setOnCheckedChangeListener { buttonView, isChecked ->
            act_langs_add_lang_profile_v2.isVisible = isChecked
            tv_save_add_lang.isVisible = isChecked
            tv_cancel_add_lang.isVisible = isChecked
        }
        listOf("English", "Hindi", "Marathi", "Punjabi").forEach { element ->
            val chip = Chip(requireContext())
            val drawable = ChipDrawable.createFromAttributes(
                requireContext(),
                null,
                0,
                R.style.AppSingleChoiceChip2
            )
            chip.setChipDrawable(drawable)
            chip.text = element
            chip.chipStrokeWidth = 1f
            language_chip_group_add_language.addView(chip, 0)
        }

    }

//    private fun createRecipientChip(lang: String) {
//        val chipView: Chip = Chip(requireContext());
//        val chip = ChipDrawable.createFromResource(requireContext(), R.xml.standalone_chip)
//        chipView.setChipDrawable(chip)
//        val span = VerticalImageSpan(chip)
//        val cursorPosition: Int = act_langs_add_lang_profile_v2.selectionStart
//        val spanLength: Int = lang.length + 2
//        val text: Editable = act_langs_add_lang_profile_v2.text
//
//        chip.text = lang
//
//        chip.setBounds(0, 0, chip.intrinsicWidth, chip.intrinsicHeight)
//
//
//        text.setSpan(
//            span,
//            cursorPosition - spanLength,
//            cursorPosition,
//            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
//        )
//        chipView.text = text
//        chipView.elevation = resources.getDimensionPixelSize(R.dimen.size_20).toFloat()
//        chipView.setOnClickListener {
//            showToast(chipView.text.toString())
//        }
//
//
//    }

    class CommaTokenizer : MultiAutoCompleteTextView.Tokenizer {
        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor
            while (i > 0 && text[i - 1] != ',') {
                i--
            }
            while (i < cursor && text[i] == ' ') {
                i++
            }
            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length
            while (i < len) {
                if (text[i] == ',') {
                    return i
                } else {
                    i++
                }
            }
            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            var i = text.length
            while (i > 0 && text[i - 1] == ' ') {
                i--
            }
            return if (i > 0 && text[i - 1] == ',') {
                text
            } else {
                if (text is Spanned) {
                    val sp = SpannableString("$text, ")
                    TextUtils.copySpansFrom(
                        text, 0, text.length,
                        Any::class.java, sp, 0
                    )
                    sp
                } else {
                    "$text, "
                }
            }
        }
    }
}
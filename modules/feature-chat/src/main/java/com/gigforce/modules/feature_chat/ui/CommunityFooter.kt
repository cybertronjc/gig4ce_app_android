package com.gigforce.modules.feature_chat.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.linkedin.android.spyglass.suggestions.SuggestionsResult
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsResultListener
import com.linkedin.android.spyglass.suggestions.interfaces.SuggestionsVisibilityManager
import com.linkedin.android.spyglass.tokenization.QueryToken
import com.linkedin.android.spyglass.tokenization.impl.WordTokenizerConfig
import com.linkedin.android.spyglass.tokenization.interfaces.QueryTokenReceiver

class CommunityFooter(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs),
    QueryTokenReceiver,
    SuggestionsVisibilityManager,
    SuggestionsResultListener {

    companion object {
        private const val SUGGESTION_BUCKET = "names-suggestions"

        private val tokenizerConfig = WordTokenizerConfig.Builder()
            .setWordBreakChars(", ")
            .setExplicitChars("@")
//                .setExplicitChars("")
            .setMaxNumKeywords(2)
            .setThreshold(1)
            .build()
    }

    override fun onQueryReceived(queryToken: QueryToken): MutableList<String> {

    }

    override fun displaySuggestions(display: Boolean) {

    }

    override fun isDisplayingSuggestions(): Boolean {

    }

    override fun onReceiveSuggestionsResult(result: SuggestionsResult, bucket: String) {

    }
}
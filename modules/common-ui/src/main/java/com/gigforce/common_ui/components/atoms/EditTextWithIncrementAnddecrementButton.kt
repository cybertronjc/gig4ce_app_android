package com.gigforce.common_ui.components.atoms

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import com.gigforce.common_ui.R
import com.gigforce.core.extensions.onTextChanged

class EditTextWithIncrementAndDecrementButton (
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(context, attrs){

    private lateinit var incrementButton : View
    private lateinit var decrementButton : View
    private lateinit var actualEditText : EditText

    init {
        inflate()
    }

    private fun inflate() {
        val view = LayoutInflater.from(
            context
        ).inflate(
            R.layout.cell_edit_text_with_increment_decrement_button,
            this,
            true
        )
        initViews(view)
    }

    private fun initViews(view: View?) = view?.let{
        decrementButton = it.findViewById(R.id.minusIcon)
        incrementButton = it.findViewById(R.id.plusIcon)
        actualEditText = it.findViewById(R.id.edittext)

        incrementButton.setOnClickListener {

            var currentNo = actualEditText.text.toString().toIntOrNull() ?: 0
            ++currentNo
            actualEditText.setText(currentNo.toString())
        }

        decrementButton.setOnClickListener {

            var currentNo = actualEditText.text.toString().toIntOrNull() ?: 0

            if(currentNo <= 0){
                currentNo = 0
            } else {
                --currentNo
            }
            actualEditText.setText(currentNo.toString())
        }
    }

    fun setText(text  : String) {
        return actualEditText.setText(text)
    }

    fun getText() : String{
       return actualEditText.text.toString()
    }

    fun getTextAsInt() : Int{
        return actualEditText.text.toString().toIntOrNull() ?: 0
    }

    fun getTextAsIntOrNull() : Int?{
        return actualEditText.text.toString().toIntOrNull()
    }

    fun setOnTextChangeListener(onTextChange: (String) -> Unit){
        actualEditText.onTextChanged(onTextChange)
    }

    fun disbale(){
        incrementButton.isEnabled = true
        decrementButton.isEnabled = true
    }
}
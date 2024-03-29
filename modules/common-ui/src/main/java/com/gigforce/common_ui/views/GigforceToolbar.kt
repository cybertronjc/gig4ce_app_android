package com.gigforce.common_ui.views

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.google.android.material.card.MaterialCardView

class GigforceToolbar(
        context: Context,
        attrs: AttributeSet
) : FrameLayout(
        context,
        attrs
) {

    private lateinit var backButton: ImageView
    private lateinit var rootCardView: MaterialCardView
    private lateinit var toolbarImageView: GigforceImageView

    private lateinit var titleTV: TextView
    private lateinit var subTitleTV: TextView

    private lateinit var optionMenuImageView: ImageView
    private lateinit var searchLayout: View
    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView

    private var searchTextChangeListener: SearchTextChangeListener? = null
    private var optionMenuClickListener: PopupMenu.OnMenuItemClickListener? = null
    private var onBackClickListener: View.OnClickListener? = null

    private var subtitleEnabled = false
    private var searchListenerCalledFirstTime = false

    private lateinit var helpImageButton : View

    @MenuRes
    private var menu: Int = -1

    init {
        val view = LayoutInflater.from(context).inflate(
                R.layout.layout_toolbar,
                this,
                true
        )

        findViews(view)
    }

    private fun findViews(view: View?) = view?.let { nnView ->
//        rootCardView = nnView.findViewById(R.id.root_cardview)
//        rootCardView.shapeAppearanceModel = rootCardView.shapeAppearanceModel
//                .toBuilder()
//                .setTopLeftCornerSize(10.0f)
//                .setTopRightCornerSize(10.0f)
//                .setBottomLeftCorner(CornerFamily.ROUNDED,40.0f)
//                .setBottomRightCorner(CornerFamily.ROUNDED,40.0f)
//                .build()

        backButton = nnView.findViewById(R.id.back_arrow)
        helpImageButton = nnView.findViewById(R.id.helpImageButton)
        toolbarImageView = nnView.findViewById(R.id.toolbar_image_iv)
        titleTV = nnView.findViewById(R.id.titleTV)
        subTitleTV = nnView.findViewById(R.id.subTitleTV)

        searchIcon = nnView.findViewById(R.id.search_icon)
        optionMenuImageView = nnView.findViewById(R.id.option_menu_iv)

        searchLayout = nnView.findViewById(R.id.search_layout)
        searchEditText = nnView.findViewById(R.id.search_et)
    }

    fun setHelpImageButtonClickListener(listener: View.OnClickListener){
        helpImageButton.visible()
        helpImageButton.setOnClickListener{
            listener.onClick(it)
        }
    }

    val isSearchCurrentlyShown: Boolean get() = searchLayout.isVisible

    fun setBackButtonListener(listener: View.OnClickListener) {
        onBackClickListener = listener

        backButton.setOnClickListener {
            listener.onClick(it)

            if (isSearchCurrentlyShown) {
                hideSearchOption()
                searchEditText.setText("")
                titleTV.visible()

                if (subtitleEnabled) {
                    showSubtitle(null)
                } else {
                    hideSubTitle()
                }
            }
        }
    }


    fun showSearchOption(
            searchHint: String
    ) {
        searchIcon.visible()
        searchEditText.hint = searchHint
        searchIcon.setOnClickListener {

            if (searchLayout.isVisible) {

                onBackClickListener?.onClick(it)
                searchEditText.setText("")
                showTitle(null)

                if (subtitleEnabled)
                    showSubtitle(null)
                else
                    subTitleTV.gone()

                searchLayout.gone()


            } else {
                hideTitle()
                hideSubTitle()
                searchLayout.visible()

                searchEditText.requestFocus()
                openSoftKeyboard(searchEditText)
            }
        }

        searchEditText.onTextChanged {
            if (!searchListenerCalledFirstTime) {
                searchListenerCalledFirstTime = true
                return@onTextChanged
            }

            searchTextChangeListener?.onSearchTextChanged(it)
        }
    }

    fun hideSearchOption() {
        searchLayout.gone()

        titleTV.visible()
        subTitleTV.visible()


    }

    fun getOptionMenuViewForAnchor(): View {
        return optionMenuImageView
    }

    fun showActionMenuOption(
            @MenuRes menu: Int
    ) {
        this.menu = menu
        this.optionMenuImageView.visible()
        this.optionMenuImageView.setOnClickListener {

            val popUpMenu = PopupMenu(context, it)
            popUpMenu.setOnMenuItemClickListener(optionMenuClickListener)
            popUpMenu.inflate(menu)
            popUpMenu.show()
        }
    }

    fun setOnOpenActionMenuItemClickListener(listener: View.OnClickListener) {
        this.optionMenuImageView.setOnClickListener(listener)
    }

    fun showActionMenu(
            popUpMenu: PopupMenu
    ) {
        this.optionMenuImageView.visible()
        popUpMenu.setOnMenuItemClickListener(optionMenuClickListener)
        popUpMenu.show()
    }

    fun hideActionMenu() {
        this.optionMenuImageView.gone()
    }

    fun showImageBehindBackButton(
            image: String,
            @DrawableRes placeHolder: Int = -1,
            @DrawableRes errorImage: Int = -1
    ) {
        toolbarImageView.visibility = View.VISIBLE
        toolbarImageView.loadImageIfUrlElseTryFirebaseStorage(
                image,
                placeHolder,
                errorImage
        )
    }

    fun showImageBehindBackButton(
            @DrawableRes image: Int
    ) {
        toolbarImageView.visibility = View.VISIBLE
        toolbarImageView.loadImage(
                image
        )
    }

    fun hideImageBehindBackButton() {
        toolbarImageView.gone()
    }

    fun setImageClickListener(
            listener: View.OnClickListener
    ) {
        this.toolbarImageView.setOnClickListener(listener)
    }

    fun showTitle(
            title: String?
    ) {
        titleTV.visibility = View.VISIBLE

        if (title != null)
            titleTV.text = title
    }

    fun setTitleTypeface(
        style : Int
     ) {
        titleTV.setTypeface(titleTV.typeface,style)
    }

    fun hideTitle() {
        titleTV.visibility = View.GONE
    }

    fun showSubtitle(
            subTitle: String?
    ) {
        subtitleEnabled = true
        subTitleTV.visibility = View.VISIBLE

        if (subTitle != null)
            subTitleTV.text = subTitle
    }

    fun hideSubTitle() {
        subTitleTV.visibility = View.GONE
    }

    fun changeBackButtonDrawable(){
        backButton.setImageDrawable(resources.getDrawable(R.drawable.ic_chevron))
    }

    fun setBackButtonDrawable(
        @DrawableRes drawable : Int
    ){
        backButton.setImageDrawable(
            ResourcesCompat.getDrawable(resources,drawable,null)
        )
    }

    fun setTitleClickListener(
            listener: View.OnClickListener
    ) {
        titleTV.setOnClickListener(listener)
    }

    fun setSubtitleClickListener(
            listener: View.OnClickListener
    ) {
        subTitleTV.setOnClickListener(listener)
    }


    fun setOnMenuItemClickListener(
            menuItemClickListener: PopupMenu.OnMenuItemClickListener
    ) {
        this.optionMenuClickListener = menuItemClickListener
    }

    fun setOnSearchTextChangeListener(
            listener: SearchTextChangeListener
    ) {
        this.searchTextChangeListener = listener
    }

    fun getOnSearchTextChangeListener(): SearchTextChangeListener? {
        return this.searchTextChangeListener
    }

    fun openSoftKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethod.SHOW_FORCED,
                0
        )
    }

    fun getSearchTextChangeAsFlow() = this.searchEditText.getTextChangeAsStateFlow()


    interface SearchTextChangeListener {

        fun onSearchTextChanged(text: String)
    }
}
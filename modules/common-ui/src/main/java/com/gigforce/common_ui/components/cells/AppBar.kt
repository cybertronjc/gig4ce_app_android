package com.gigforce.common_ui.components.cells

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.listeners.AppBarClicks
import com.gigforce.core.AppConstants
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.onTextChanged
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class BackgroundType(val value: Int){


    Default(AppConstants.BACKGROUND_TYPE_DEFAULT),
    PinkBar(AppConstants.BACKGROUND_TYPE_PINKBAR),
    WhiteBar(AppConstants.BACKGROUND_TYPE_WHITEBAR),
    GreyBar(AppConstants.BACKGROUND_TYPE_GREYBAR);

    companion object {
        private val VALUES = values();
        fun getByValue(value: Int) = VALUES.first { it.value == value }
    }
}

@AndroidEntryPoint
class AppBar(context: Context, attributeSet: AttributeSet): FrameLayout(context, attributeSet),
        IViewHolder, AppBarClicks.OnSearchClickListener, AppBarClicks.OnMenuClickListener{

    @Inject
    lateinit var userinfo: UserInfoImp
    @Inject
    lateinit var navigation: INavigation


     var titleText: TextView
     var backImageButton: ImageButton
     var menuImageButton: ImageButton
     var searchImageButton: ImageButton
     var filterImageButton: ImageButton
     var filterDotImageButton: ImageButton
     var filterFrameLayout: FrameLayout
     var stepsTextView: TextView
     var search_item: EditText
     var profilePic: AppProfilePicComponent
     var onBackClickListener: View.OnClickListener? = null
     var searchTextChangeListener: SearchTextChangeListener? = null

    fun setOnSearchTextChangeListener(listener: SearchTextChangeListener) {
        this.searchTextChangeListener = listener
    }
     private var _backGroundType: BackgroundType = BackgroundType.Default
        var backGroundType:BackgroundType
            get() = _backGroundType
            set(value) {
                this._backGroundType = value
                val backgroundRes = when(value){
                    BackgroundType.Default -> R.drawable.white_app_bar_background
                    BackgroundType.PinkBar -> R.drawable.app_bar_background
                    BackgroundType.WhiteBar -> R.drawable.white_app_bar_background
                    BackgroundType.GreyBar -> R.drawable.grey_app_bar_bg
                    else -> R.drawable.white_app_bar_background
                }

                this.background = context.resources.getDrawable(backgroundRes)
            }

         var searchClickListener: AppBarClicks.OnSearchClickListener? = null
     fun setOnSearchClickListener(listener: AppBarClicks.OnSearchClickListener){
         this.searchClickListener = listener
     }
    var menuClickListener: AppBarClicks.OnMenuClickListener? = null
    fun setOnMenuClickListener(listener: AppBarClicks.OnMenuClickListener){
        this.menuClickListener = listener
    }

    var setProfilePicture: String
        get() = userinfo.getData().profilePicPath
        set(value) {
            value?.let {
                profilePic.setProfilePic(value)
            }

        }


    init {
        this.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.app_bar_layout, this, true)

        val styledAttributeSet = context.obtainStyledAttributes(attributeSet, R.styleable.AppBar, 0, 0)

        val app_title = styledAttributeSet.getText(R.styleable.AppBar_titleText)
        val isSearchVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isSearchVisible, false)
        val isMenuItemVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isMenuItemVisible, false)
        val isProfileVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isProfileVisible, false)
        val searchHint = styledAttributeSet.getString(R.styleable.AppBar_searchHint)
        val isFilterVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isFilterVisible, false)
        val isStepsVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isStepsVisible, false)
        this.backGroundType = BackgroundType.getByValue(styledAttributeSet.getInt(R.styleable.AppBar_backgroundType, 0))


        titleText = findViewById(R.id.textTitle)
        backImageButton = findViewById(R.id.backImageButton)
        menuImageButton = findViewById(R.id.menuImageButton)
        searchImageButton = findViewById(R.id.searchImageButton)
        filterImageButton = findViewById(R.id.filterImageButton)
        filterDotImageButton = findViewById(R.id.filterDot)
        filterFrameLayout = findViewById(R.id.filterImageFrame)
        search_item = findViewById(R.id.search_item)
        profilePic = findViewById(R.id.profilePicComp)
        stepsTextView = findViewById(R.id.steps)

        if (app_title.isNotEmpty()){
            titleText.visible()
            setAppBarTitle(app_title)
        }else {
            setAppBarTitle("")
            titleText.invisible()
        }
        makeSearchVisible(isSearchVisible)
        makeMenuItemVisible(isMenuItemVisible)
        makeProfileVisible(isProfileVisible)
        makeStepsVisible(isStepsVisible)
        makeFilterVisible(isFilterVisible)
        searchHint?.let { setHint(it) }
        userinfo.getData().profilePicPath?.let {
            setProfilePicture = it
        }

        searchImageButton.setOnClickListener {
            searchClickListener?.onSearchClick(it)
            onSearchClick(it)
        }
        menuImageButton.setOnClickListener {
            menuClickListener?.onMenuClick(it)
            onMenuClick(it)
        }
        setColorsOnViews(backGroundType)
        styledAttributeSet.recycle()

    }

    private fun makeFilterVisible(filterVisible: Boolean) {
        if (filterVisible){
            filterImageButton.visible()
            filterFrameLayout.visible()
        }  else{
            filterImageButton.gone()
            filterFrameLayout.gone()
        }
    }

    val isSearchCurrentlyShown: Boolean get() = search_item.isVisible
    fun setBackButtonListener(listener: View.OnClickListener) {
        onBackClickListener = listener

        backImageButton.setOnClickListener {
            listener.onClick(it)

            if (isSearchCurrentlyShown) {
                hideSearchOption()
                search_item.setText("")
                titleText.visible()
                hideKeyboard(search_item)
            }
        }
    }
    fun hideSearchOption() {
        search_item.setText("")
        search_item.gone()
        titleText.visible()
        searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
    }

    private fun setColorsOnViews(backgroundType: BackgroundType) {
        when(backgroundType.value){
            AppConstants.BACKGROUND_TYPE_PINKBAR -> {
                backImageButton.setColorFilter(context.resources.getColor(R.color.white))
                titleText.setTextColor(context.resources.getColor(R.color.white))
                searchImageButton.setColorFilter(context.resources.getColor(R.color.white))
                menuImageButton.setColorFilter(context.resources.getColor(R.color.white))
                search_item.setHintTextColor(context.resources.getColor(R.color.black))
                search_item.setTextColor(context.resources.getColor(R.color.black))

            }
            AppConstants.BACKGROUND_TYPE_WHITEBAR -> {
                backImageButton.setColorFilter(context.resources.getColor(R.color.black))
                titleText.setTextColor(context.resources.getColor(R.color.black))
                searchImageButton.setColorFilter(context.resources.getColor(R.color.black))
                menuImageButton.setColorFilter(context.resources.getColor(R.color.black))
                search_item.setHintTextColor(context.resources.getColor(R.color.black))
                search_item.setTextColor(context.resources.getColor(R.color.black))
            }
            AppConstants.BACKGROUND_TYPE_GREYBAR -> {
                backImageButton.setColorFilter(context.resources.getColor(R.color.black))
                titleText.setTextColor(context.resources.getColor(R.color.black))
                searchImageButton.setColorFilter(context.resources.getColor(R.color.black))
                menuImageButton.setColorFilter(context.resources.getColor(R.color.black))
                search_item.setHintTextColor(context.resources.getColor(R.color.black))
                search_item.setTextColor(context.resources.getColor(R.color.black))
            }
        }
    }


    fun setAppBarTitle(appTitle: CharSequence?) {
        titleText.setText(appTitle.toString())
    }
    fun makeTitleBold(){
        titleText.setTypeface(null, Typeface.BOLD)
    }
    fun makeBackgroundMoreRound(){
        this.background = context.resources.getDrawable(R.drawable.app_bar_background_more_rounded)
    }

    fun setSteps(step: String){
        stepsTextView.setText(step)
    }
    fun setHint(text: String){
        search_item.setHint(text)
    }
    fun makeProfileVisible(visible: Boolean){
        if (visible) profilePic.visible() else profilePic.gone()
    }

    fun makeStepsVisible(visible: Boolean){
        if (visible) stepsTextView.visible() else stepsTextView.gone()
    }


    fun makeSearchVisible(visible: Boolean){
         if (visible) searchImageButton.visible() else searchImageButton.invisible()
    }
    fun makeMenuItemVisible(visible: Boolean){
        if (visible) menuImageButton.visible() else menuImageButton.invisible()
    }

    fun changeBackButtonDrawable(){
        backImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_chevron))
    }

    override fun bind(data: Any?) {

    }

    fun setBackButtonDrawable(
        @DrawableRes drawable : Int
    ){
        backImageButton.setImageDrawable(
            ResourcesCompat.getDrawable(resources,drawable,null)
        )
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethodManager.HIDE_NOT_ALWAYS,
                0
        )
    }

    fun openSoftKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
                view.applicationWindowToken,
                InputMethod.SHOW_FORCED,
                0
        )
    }

    override fun onSearchClick(v: View) {
        Log.d("Click", "Search")
        if (search_item.isVisible){
            search_item.gone()
            titleText.visible()
            searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
            search_item.setText("")
            hideKeyboard(search_item)
            search_item.clearFocus()

        }
        else{
            search_item.visible()
            titleText.gone()
            search_item.requestFocus()
            search_item.onTextChanged {
                searchTextChangeListener?.onSearchTextChanged(it)
            }
            openSoftKeyboard(search_item)

            searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_close_24))
        }
    }

    override fun onMenuClick(v: View) {
        Log.d("Click", "Menu")
    }



}

interface SearchTextChangeListener {

    fun onSearchTextChanged(text: String)
}

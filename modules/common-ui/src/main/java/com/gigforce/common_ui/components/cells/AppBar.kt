package com.gigforce.common_ui.components.cells

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethod
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.gigforce.common_ui.R
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.databinding.AppBarLayoutBinding
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
        IViewHolder, AppBarClicks.OnSearchClickListener{

    @Inject
    lateinit var userinfo: UserInfoImp
    @Inject
    lateinit var navigation: INavigation

    private lateinit var viewBinding: AppBarLayoutBinding

     var titleText: TextView
//     var subTitleText: TextView
     var backImageButton: ImageButton
//     var menuImageButton: ImageButton
     var searchImageButton: ImageButton
     var filterImageButton: ImageButton
     var refreshImageButton: ImageButton
     var filterDotImageButton: ImageButton
     var filterFrameLayout: FrameLayout
//     var stepsTextView: TextView
     var search_item: EditText
//     var profilePic: AppProfilePicComponent
//     var mainImageView: GigforceImageView
//     var onlineImage: ImageView

     var onBackClickListener: View.OnClickListener? = null
     var searchTextChangeListener: SearchTextChangeListener? = null
     private lateinit var subTitleTV: TextView
     private var optionMenuClickListener: PopupMenu.OnMenuItemClickListener? = null
     @MenuRes
     private var menu: Int = -1
     private var subtitleEnabled = false

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
                viewBinding.profilePicComp.setProfilePic(value)
            }

        }


    init {
//        this.layoutParams =
//                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        LayoutInflater.from(context).inflate(R.layout.app_bar_layout, this, true)

        viewBinding = AppBarLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        val styledAttributeSet = context.obtainStyledAttributes(attributeSet, R.styleable.AppBar, 0, 0)

        val app_title = styledAttributeSet.getText(R.styleable.AppBar_titleText)
        val isSearchVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isSearchVisible, false)
        val isMenuItemVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isMenuItemVisible, false)
        val isRefreshVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isRefreshVisible, false)
        val isProfileVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isProfileVisible, false)
        val searchHint = styledAttributeSet.getString(R.styleable.AppBar_searchHint)
        val isFilterVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isFilterVisible, false)
        val isStepsVisible = styledAttributeSet.getBoolean(R.styleable.AppBar_isStepsVisible, false)
        this.backGroundType = BackgroundType.getByValue(styledAttributeSet.getInt(R.styleable.AppBar_backgroundType, 0))


//        titleText = findViewById(R.id.textTitle)
//        subTitleText = findViewById(R.id.subTitleTV)
//        backImageButton = findViewById(R.id.backImageButton)
//        menuImageButton = findViewById(R.id.menuImageButton)
//        searchImageButton = findViewById(R.id.searchImageButton)
//        filterImageButton = findViewById(R.id.filterImageButton)
//        refreshImageButton = findViewById(R.id.refreshImageButton)
//        filterDotImageButton = findViewById(R.id.filterDot)
//        filterFrameLayout = findViewById(R.id.filterImageFrame)
//        search_item = findViewById(R.id.search_item)
//        profilePic = findViewById(R.id.profilePicComp)
//        stepsTextView = findViewById(R.id.steps)
//        mainImageView = findViewById(R.id.iv_profile)
//        onlineImage = findViewById(R.id.user_online_iv)
          titleText = viewBinding.textTitle
          search_item = viewBinding.searchItem
          backImageButton = viewBinding.backImageButton
          filterFrameLayout = viewBinding.filterImageFrame
          filterDotImageButton = viewBinding.filterDot
          filterImageButton = viewBinding.filterImageButton
          refreshImageButton = viewBinding.refreshImageButton
          searchImageButton = viewBinding.searchImageButton

        if (app_title.isNotEmpty()){
            viewBinding.textTitle.visible()
            setAppBarTitle(app_title)
        }else {
            setAppBarTitle("")
            viewBinding.textTitle.invisible()
        }
        makeSearchVisible(isSearchVisible)
        makeMenuItemVisible(isMenuItemVisible)
        makeProfileVisible(isProfileVisible)
        makeStepsVisible(isStepsVisible)
        makeRefreshVisible(isRefreshVisible)
        makeFilterVisible(isFilterVisible)
        searchHint?.let { setHint(it) }
        userinfo.getData().profilePicPath?.let {
            setProfilePicture = it
        }

        viewBinding.searchImageButton.setOnClickListener {
            searchClickListener?.onSearchClick(it)
            onSearchClick(it)
        }
//        menuImageButton.setOnClickListener {
//            menuClickListener?.onMenuClick(it)
//            onMenuClick(it)
//        }
//        viewBinding.chatBackButton.setOnClickListener {
//            makeChatOptionsVisible(false, false, false)
//        }
        setColorsOnViews(backGroundType)
        styledAttributeSet.recycle()

    }

    private fun makeFilterVisible(filterVisible: Boolean) {
        if (filterVisible){
            viewBinding.filterImageButton.visible()
            viewBinding.filterImageFrame.visible()
        }  else{
            viewBinding.filterImageButton.gone()
            viewBinding.filterImageFrame.gone()
        }
    }

    val isSearchCurrentlyShown: Boolean get() = viewBinding.searchItem.isVisible
    fun setBackButtonListener(listener: View.OnClickListener) {
        onBackClickListener = listener

        viewBinding.backImageButton.setOnClickListener {
            listener.onClick(it)

            if (isSearchCurrentlyShown) {
                hideSearchOption()
                viewBinding.searchItem.setText("")
                viewBinding.textTitle.visible()
                hideKeyboard(viewBinding.searchItem)
            }
        }
    }
    fun hideSearchOption() {
        viewBinding.searchItem.setText("")
        viewBinding.searchItem.gone()
        viewBinding.textTitle.visible()
        viewBinding.searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
    }

    private fun setColorsOnViews(backgroundType: BackgroundType) {
        when(backgroundType.value){
            AppConstants.BACKGROUND_TYPE_PINKBAR -> {
                viewBinding.backImageButton.setColorFilter(context.resources.getColor(R.color.white))
                viewBinding.textTitle.setTextColor(context.resources.getColor(R.color.white))
                viewBinding.searchImageButton.setColorFilter(context.resources.getColor(R.color.white))
                viewBinding.menuImageButton.setColorFilter(context.resources.getColor(R.color.white))
                viewBinding.searchItem.setHintTextColor(context.resources.getColor(R.color.black))
                viewBinding.searchItem.setTextColor(context.resources.getColor(R.color.black))

            }
            AppConstants.BACKGROUND_TYPE_WHITEBAR -> {
                viewBinding.backImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.textTitle.setTextColor(context.resources.getColor(R.color.black))
                viewBinding.searchImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.menuImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.searchItem.setHintTextColor(context.resources.getColor(R.color.black))
                viewBinding.searchItem.setTextColor(context.resources.getColor(R.color.black))
            }
            AppConstants.BACKGROUND_TYPE_GREYBAR -> {
                viewBinding.backImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.textTitle.setTextColor(context.resources.getColor(R.color.black))
                viewBinding.searchImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.menuImageButton.setColorFilter(context.resources.getColor(R.color.black))
                viewBinding.searchItem.setHintTextColor(context.resources.getColor(R.color.black))
                viewBinding.searchItem.setTextColor(context.resources.getColor(R.color.black))
            }
        }
    }


    fun setAppBarTitle(appTitle: CharSequence?) {
        viewBinding.textTitle.setText(appTitle.toString())
    }
    fun makeTitleBold(){
        viewBinding.textTitle.setTypeface(null, Typeface.BOLD)
    }

    fun showSubtitle(
        subTitle: String?
    ) {
        subtitleEnabled = true
        viewBinding.subTitleTV.visibility = View.VISIBLE

        if (subTitle != null)
            viewBinding.subTitleTV.text = subTitle
    }

    fun hideSubTitle() {
        viewBinding.subTitleTV.visibility = View.GONE
    }

    fun makeBackgroundMoreRound(){
        this.background = context.resources.getDrawable(R.drawable.app_bar_background_more_rounded)
    }

    fun setSteps(step: String){
        viewBinding.steps.setText(step)
    }
    fun setHint(text: String){
        viewBinding.searchItem.setHint(text)
    }
    fun makeProfileVisible(visible: Boolean){
        if (visible) viewBinding.profilePicComp.visible() else viewBinding.profilePicComp.gone()
    }

    fun makeOnlineImageVisible(visible: Boolean){
        if (visible) viewBinding.userOnlineIv.visible() else viewBinding.userOnlineIv.gone()
    }

    fun makeRefreshVisible(visible: Boolean){
        if (visible) viewBinding.refreshImageButton.visible() else viewBinding.refreshImageButton.gone()
    }

    fun makeStepsVisible(visible: Boolean){
        if (visible) viewBinding.steps.visible() else viewBinding.steps.gone()
    }


    fun makeSearchVisible(visible: Boolean){
         if (visible) viewBinding.searchImageButton.visible() else viewBinding.searchImageButton.invisible()
    }
    fun makeMenuItemVisible(visible: Boolean){
        if (visible) viewBinding.menuImageButton.visible() else viewBinding.menuImageButton.gone()
    }


    fun setOnOpenActionMenuItemClickListener(listener: View.OnClickListener) {
        this.viewBinding.menuImageButton.setOnClickListener(listener)
    }


    fun changeBackButtonDrawable(){
        viewBinding.backImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_chevron))
    }

    fun setOnMenuItemClickListener(
        menuItemClickListener: PopupMenu.OnMenuItemClickListener
    ) {
        this.optionMenuClickListener = menuItemClickListener
    }

    fun showMainImageView(
        image: String,
        @DrawableRes placeHolder: Int = -1,
        @DrawableRes errorImage: Int = -1
    ) {
        viewBinding.ivProfile.visibility = View.VISIBLE
        viewBinding.ivProfile.loadImageIfUrlElseTryFirebaseStorage(
            image,
            placeHolder,
            errorImage
        )
    }

    fun showMainImageView(
        @DrawableRes image: Int
    ) {
        viewBinding.ivProfile.visibility = View.VISIBLE
        viewBinding.ivProfile.loadImage(
            image
        )
    }

    fun hideImageBehindBackButton() {
        viewBinding.ivProfile.gone()
    }

    fun makeChatOptionsVisible(visible: Boolean, copyEnable: Boolean, deleteEnable: Boolean, infoEnable: Boolean, downloadEnable: Boolean){
        if (visible){
            viewBinding.mainLayout.gone()
            viewBinding.chatOptionsLayout.visible()
        } else{
            viewBinding.mainLayout.visible()
            viewBinding.chatOptionsLayout.gone()
        }

        viewBinding.copyButton.isVisible = copyEnable
        viewBinding.deleteButton.isVisible = deleteEnable
        viewBinding.infoButton.isVisible = infoEnable
        viewBinding.downloadButton.isVisible = downloadEnable
    }

    override fun bind(data: Any?) {

    }

    fun getOptionMenuViewForAnchor(): View {
        return viewBinding.menuImageButton
    }

    fun setBackButtonDrawable(
        @DrawableRes drawable : Int
    ){
        viewBinding.backImageButton.setImageDrawable(
            ResourcesCompat.getDrawable(resources,drawable,null)
        )
    }

    fun setDeleteClickListener(
        listener: OnClickListener
    ) {
        this.viewBinding.deleteButton.setOnClickListener(listener)
    }

    fun setCopyClickListener(
        listener: OnClickListener
    ) {
        this.viewBinding.copyButton.setOnClickListener(listener)
    }

    fun setImageClickListener(
        listener: View.OnClickListener
    ) {
        this.viewBinding.ivProfile.setOnClickListener(listener)
    }

    fun setTitleClickListener(
        listener: OnClickListener
    ) {
        viewBinding.textTitle.setOnClickListener(listener)
    }

    fun setSubtitleClickListener(
        listener: View.OnClickListener
    ) {
        viewBinding.subTitleTV.setOnClickListener(listener)
    }

    fun setForwardClickListener(
        listener: OnClickListener
    ) {
        viewBinding.forwardButton.setOnClickListener(listener)
    }
    fun setInfoClickListener(
        listener: OnClickListener
    ) {
        viewBinding.infoButton.setOnClickListener(listener)
    }

    fun setReplyClickListener(
        listener: OnClickListener
    ) {
        viewBinding.replyButton.setOnClickListener(listener)
    }

    fun setChatOptionsCancelListener(
        listener: OnClickListener
    ) {
        viewBinding.chatBackButton.setOnClickListener(listener)
    }

    fun hideKeyboard(view: View) {
        context?.let {
            val imm: InputMethodManager =
                it.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun openSoftKeyboard(view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
                InputMethod.SHOW_FORCED,
                0
        )
    }

    override fun onSearchClick(v: View) {
        Log.d("Click", "Search")
        if (viewBinding.searchItem.isVisible){
            viewBinding.searchItem.gone()
            viewBinding.textTitle.visible()
            viewBinding.searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_search_24))
            viewBinding.searchItem.setText("")
            hideKeyboard(viewBinding.searchItem)
            viewBinding.searchItem.clearFocus()

        }
        else{
            viewBinding.searchItem.visible()
            viewBinding.textTitle.gone()
            viewBinding.searchItem.requestFocus()
            viewBinding.searchItem.onTextChanged {
                searchTextChangeListener?.onSearchTextChanged(it)
            }
            openSoftKeyboard(viewBinding.searchItem)

            viewBinding.searchImageButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_close_24))
        }
    }


    fun setSubTitle(
        subTitle : String
    ){
        viewBinding.subTitleTV.visible()
        viewBinding.subTitleTV.text = subTitle
    }



}

interface SearchTextChangeListener {

    fun onSearchTextChanged(text: String)
}

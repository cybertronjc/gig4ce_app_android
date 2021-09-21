package com.gigforce.app.modules.profile_.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.user_profile.components.ClickHandler

data class ContentCardData(@get:DrawableRes var topIcon: Int? = null, @get:StringRes var topLabel: Int? = null,
                           @get:DrawableRes var contentIllustration: Int? = null, @get:StringRes var contentTitle: Int? = null,
                           @get:StringRes var contentText: Int? = null, @get:StringRes var actionText: Int? = null, var clickHandler: com.app.user_profile.components.ClickHandler? = null)
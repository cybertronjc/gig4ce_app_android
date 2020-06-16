package com.gigforce.app.utils

object AppConstants {

    const val ON_BOARDING_COMPLETED = "on_boarding_completed"
    // shared preferences keys
    const val APP_LANGUAGE = "app_lang";
    const val APP_LANGUAGE_NAME = "app_lang_name";
    const val DEVICE_LANGUAGE = "device_language"
    const val  INTRO_COMPLETE = "intro_complete"
    const val  ALL_MOBILE_NUMBERS_USED = "all_mobile_no_used"



    // Temporary For Chat
    //https://stackoverflow.com/questions/31788678/android-toolbar-back-arrow-with-icon-like-whatsapp
//https://www.reddit.com/r/androiddev/comments/92htyu/how_to_layout_views_in_constraint_layout/
//https://stackoverflow.com/questions/60412216/android-drawable-importer-plugin-not-working-in-android-studio-3-6
    val DEFAULT_SEARCH_CATEGORIES = arrayOf<String>(
        "Help",
        "Aftab",
        "Amit",
        "Ankita",
        "Ashu",
        "Bedo",
        "Bhusan",
        "Guddn",
        "Nirbhay",
        "Nitesh",
        "Rashmi",
        "Shivam"
    )

    val DEFAULT_SEARCH_CATEGORY_IMAGES = arrayOf(
        "giglogo",
        "aftab",
        "amit",
        "ankita",
        "ashu",
        "bedo",
        "bhusan",
        "guddn",
        "nirbhay",
        "nitesh",
        "rashmi",
        "shivam"
    )

    const val IMAGE_URL = "imageUrl"
    const val CONTACT_NAME = "contactName"

    const val FROM_CONTACT_MSG_1 = "What is Lorem Ipsum?"
    const val TO_CONTACT_MSG_1 =
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
                "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type " +
                "specimen book."
    const val FROM_CONTACT_MSG_2 = "Nice nice."
    const val TO_CONTACT_MSG_2 = "Now you tell, Why do we use it?"

    const val FROM_CONTACT_MSG_3 =
        "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout."

}
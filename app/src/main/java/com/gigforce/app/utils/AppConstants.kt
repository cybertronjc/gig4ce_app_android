package com.gigforce.app.utils

object AppConstants {
    const val UNLOCK_FEATURE = false
    const val ON_BOARDING_COMPLETED = "on_boarding_completed"
    // shared preferences keys
    const val APP_LANGUAGE_CODE = "app_lang_code";
    const val APP_LANGUAGE_NAME = "app_lang_name";
    const val DEVICE_LANGUAGE = "device_language"
    const val  INTRO_COMPLETE = "intro_complete"
    const val  ALL_MOBILE_NUMBERS_USED = "all_mobile_no_used"

    const val PINCODE_URL = "https://api.postalpincode.in/pincode/"



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
        "Hello sir. A coffee machine in the box has a broken handle. What should I do?"
    const val FROM_CONTACT_MSG_2 = "Nice nice."
    const val TO_CONTACT_MSG_2 = "Sir I just opened the box and it was broken. "

    const val FROM_CONTACT_MSG_3 =
        "Alright, we’ll return it. Can you keep it in the storage section in the damage goods section and make a note in the register?"

    const val TO_CONTACT_MSG_3 = "Okay sir, will do that. Thank you."
    const val FROM_CONTACT_MSG_4 ="Okay, let me know when done. Next time onwards also keep any damaged goods there and make a note in the register."
    const val TO_CONTACT_MSG_4 = "Okay sir"

}
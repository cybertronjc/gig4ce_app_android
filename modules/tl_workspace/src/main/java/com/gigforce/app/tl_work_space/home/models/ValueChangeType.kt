package com.gigforce.app.tl_work_space.home.models

enum class ValueChangeType constructor(
    private val changeString: String
) {
    INCREMENT("increment"),
    DECREMENT("decrement"),
    UNCHANGED("unchanged");

    companion object {

        fun fromChangeString(
            changeString: String?
        ) = when (changeString?.trim()?.lowercase()) {
            INCREMENT.changeString -> INCREMENT
            DECREMENT.changeString -> DECREMENT
            else -> UNCHANGED
        }
    }
}
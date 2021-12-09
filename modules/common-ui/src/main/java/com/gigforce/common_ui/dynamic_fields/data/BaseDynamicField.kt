package com.gigforce.common_ui.dynamic_fields.data


interface BaseDynamicField {

    /**
     * Id for fields
     */
    val id: String?

    /**
     * Title to show above view
     */
    val title: String?

    /**
     * Is field mandatory, user can't process if he doesn't enter or select this field
     */
    val mandatory: Boolean

    /**
     * Type of field to inflate, currently allowed options
     *
     * [FieldTypes.TEXT_FIELD]
     * [FieldTypes.DROP_DOWN]
     * [FieldTypes.DATE_PICKER]
     * [FieldTypes.RADIO_BUTTON]
     * [FieldTypes.SIGNATURE_DRAWER]
     * [FieldTypes.SIGNATURE_DRAWER_2]
     * [FieldTypes.AADHAAR_VERIFICATION_VIEW]
     * [FieldTypes.BANK_VERIFICATION_VIEW]
     * [FieldTypes.DL_VERIFICATION_VIEW]
     * [FieldTypes.PAN_VERIFICATION_VIEW]
     */
    val fieldType: String?

    /**
     * Screen to this field in currently can be Joining forms
     */
    val screenIdToShowIn: String

    /**
     * Text to prefill, works for
     * [FieldTypes.TEXT_FIELD],
     * [FieldTypes.SIGNATURE_DRAWER]
     * [FieldTypes.SIGNATURE_DRAWER_2]
     * [FieldTypes.AADHAAR_VERIFICATION_VIEW]
     * [FieldTypes.BANK_VERIFICATION_VIEW]
     * [FieldTypes.DL_VERIFICATION_VIEW]
     * [FieldTypes.PAN_VERIFICATION_VIEW]
     */
    val prefillText: String?

}

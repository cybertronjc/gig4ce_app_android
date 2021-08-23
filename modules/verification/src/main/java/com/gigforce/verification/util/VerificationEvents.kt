package com.gigforce.verification.util

object VerificationEvents {
    const val PAN_OCR_STARTED = "PAN_OCR_started"

    const val PAN_OCR_SUCCESS = "PAN_OCR_success"

    const val PAN_OCR_FAILED = "PAN_OCR_failed"

    const val PAN_DETAIL_SUBMITTED = "PAN_detail_submitted"

    const val PAN_VERIFIED = "PAN_verified"       //backend side event

    const val PAN_REJECTED = "PAN_rejected"      //backend side event

    const val PAN_VERIFICATION_PENDING = "PAN_verification_pending"     //backend side event

    const val DL_OCR_STARTED = "DL_OCR_started"

    const val DL_OCR_SUCCESS = "DL_OCR_success"

    const val DL_OCR_FAILED = "DL_OCR_failed"

    const val DL_DETAIL_SUBMITTED = "DL_detail_submitted"

    const val DL_VERIFIED = "DL_verified"    //backend side event

    const val DL_REJECTED = "DL_rejected"    //backend side event

    const val DL_VERIFICATION_PENDING = "DL_verification_pending"   //backend side event

    const val BANK_OCR_STARTED = "Bank_OCR_started"

    const val BANK_OCR_SUCCESS = "Bank_OCR_success"

    const val BANK_OCR_FAILED = "Bank_OCR_failed"

    const val BANK_DETAIL_SUBMITTED = "Bank_detail_submitted"

    const val BANK_VERIFIED = "Bank_verified"

    const val BANK_REJECTED = "Bank_rejected"  //backend side event

    const val BANK_MISMATCH = "Bank_mismatch"

    const val BANK_VERIFICATION_PENDING = "Bank_verification_pending"  //backend side event

    const val AADHAAR_VERIFICATION_STARTED = "Aadhaar_verification_started"

    const val AADHAAR_VERIFIED = "Aadhaar_verified"  //backend side event

    const val Aadhaar_REJECTED = "Aadhaar_rejected"     //backend side event
}
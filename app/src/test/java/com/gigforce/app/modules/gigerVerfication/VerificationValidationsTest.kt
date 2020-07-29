package com.gigforce.app.modules.gigerVerfication

import org.junit.Test

import org.junit.Assert.*

class VerificationValidationsTest {

    @Test
    fun isPanCardValid() {

        var pan = ""
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "1234567890" //Al numbers
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDEFGHIJ" //Al Letters
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1174KK" // More than 10
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "1BCDE1174K" // Number in first 5 letters -1
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCD91174K" // Number in first 5 letters - 2
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "AB3DE1174K" //Number in first 5 letters - 3
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDEA174K" //Letter in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1A74K" //Letter in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE123AK" //Letter in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE12347" //End with digit
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1234A" // Correct Aadhar
        assertTrue(VerificationValidations.isPanCardValid(pan))
    }


    @Test
    fun isDlValid() {

        var dl = ""
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "13333" //Less numbe - all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "AAAAA" //Less numbe - all digits
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "133337676767676" //More numbe - all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "1333376767676" // 13 but all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "ABCDEFGHIJKLM" // 13 but all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "AAAAAAAAAAAAAAA" //More numbe - all digits
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK0133434343334" // Correct Format but more numbers than 13
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "1K01334343434" // Starts With Number In State
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "U101334343334" // Ends With Number In State
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UKA1334343334" // Letter in 3rd Place
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK1133A343334" // Letter in MID Place
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK1133344333A" // Ends With Number In State
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK12345678901" // Valid
        assertTrue(VerificationValidations.isDLNumberValid(dl))
    }
}
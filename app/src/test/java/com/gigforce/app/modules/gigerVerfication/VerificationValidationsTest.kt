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

        pan = "ABCDEFGHIJ" //All Alphabet
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1174KK" // More than 10
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "1BCDE1174K" // Number in first 5 Alphabet -1
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCD91174K" // Number in first 5 Alphabet - 2
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "AB3DE1174K" //Number in first 5 Alphabet - 3
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDEA174K" //Alphabet in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1A74K" //Alphabet in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE123AK" //Alphabet in Mid
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE12347" //End with digit
        assertFalse(VerificationValidations.isPanCardValid(pan))

        pan = "ABCDE1234A" // Correct PAN
        assertTrue(VerificationValidations.isPanCardValid(pan))
    }


    @Test
    fun isDlValid() {

        var dl = ""
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "13333" //Less numbe - all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "AAAAA" //Less numbe - all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "13333767456767676" //More numbers - all letters
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "133337676767654" // 15 but all digit
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "ABCDEFGHIJKLMAD" // 15 but all Alphabets
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "AAAAAAAAAAAAAAA" //More number - all Alphabet
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK013343434332334" // Correct Format but more numbers than 15
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "1K0133434343434" // Starts With Number In State
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "U10133434343334" // Ends With Number In State
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UKA133434343334" // Alpha in 3rd Place
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK1133A34334334" // Alpha in MID
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK113334344333A" // Ends With alphabet
        assertFalse(VerificationValidations.isDLNumberValid(dl))

        dl = "UK1234567890123" // Valid
        assertTrue(VerificationValidations.isDLNumberValid(dl))
    }
}
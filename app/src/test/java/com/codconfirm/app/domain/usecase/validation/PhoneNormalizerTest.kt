package com.codconfirm.app.domain.usecase.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNormalizerTest {
    @Test
    fun acceptsSupportedPakistaniMobileFormats() {
        val validInputs = listOf(
            "03001234567",
            "0300 1234567",
            "0300-1234567",
            "+92 300 1234567",
            "923001234567",
            "3001234567",
            "(0300) 1234567",
            "03-001234567",
        )

        validInputs.forEach { input ->
            assertEquals(input, "+923001234567", PhoneNormalizer.normalizePkPhone(input)?.e164)
        }
    }

    @Test
    fun rejectsInvalidOrNonMobileNumbers() {
        val invalidInputs = listOf(
            "",
            "123",
            "02112345678",
            "04212345678",
            "0300123456",
            "030012345678",
            "+1 300 1234567",
            "92300123456",
            "9230012345678",
            "02001234567",
            "03001234abc",
        )

        invalidInputs.forEach { input ->
            assertNull("$input should be rejected", PhoneNormalizer.normalizePkPhone(input))
        }
    }
}

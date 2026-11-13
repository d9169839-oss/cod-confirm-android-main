package com.codconfirm.app.domain.usecase.validation

import com.codconfirm.app.domain.model.PhoneNumber

object PhoneNormalizer {
    fun normalizePkPhone(raw: String): PhoneNumber? {
        val digits = raw.filter(Char::isDigit)
        val national = when {
            digits.startsWith("92") && digits.length == 12 -> digits.drop(2)
            digits.startsWith("0") && digits.length == 11 -> digits.drop(1)
            digits.length == 10 -> digits
            else -> return null
        }

        if (!national.startsWith("3") || national.length != 10) return null
        return PhoneNumber.fromNormalized("+92$national")
    }
}

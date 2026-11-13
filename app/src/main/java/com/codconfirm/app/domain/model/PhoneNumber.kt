package com.codconfirm.app.domain.model

@JvmInline
value class PhoneNumber private constructor(val e164: String) {
    companion object {
        fun fromNormalized(value: String): PhoneNumber? =
            value.takeIf { it.matches(Regex("^\\+923[0-9]{9}$")) }?.let(::PhoneNumber)
    }

    override fun toString(): String = e164
}

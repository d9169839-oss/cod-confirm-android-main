package com.codconfirm.app.domain.model

enum class OrderStatus {
    PENDING_CONFIRMATION,
    CONFIRMED,
    DISPATCHED,
    DELIVERED,
    REFUSED,
    CANCELLED;

    val allowedNext: Set<OrderStatus>
        get() = when (this) {
            PENDING_CONFIRMATION -> setOf(CONFIRMED, CANCELLED)
            CONFIRMED -> setOf(DISPATCHED, CANCELLED)
            DISPATCHED -> setOf(DELIVERED, REFUSED)
            DELIVERED, REFUSED, CANCELLED -> emptySet()
        }
}

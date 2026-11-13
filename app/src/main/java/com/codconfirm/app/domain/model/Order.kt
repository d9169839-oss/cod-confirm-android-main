package com.codconfirm.app.domain.model

import java.time.Instant

/**
 * A seller-owned COD order. Phone numbers are normalized before they enter this model.
 */
data class Order(
    val id: String,
    val customerName: String,
    val phone: PhoneNumber,
    val address: String,
    val city: String,
    val productNote: String,
    val amountPkr: Long,
    val courier: String? = null,
    val trackingId: String? = null,
    val status: OrderStatus = OrderStatus.PENDING_CONFIRMATION,
    val refusalReason: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val confirmedAt: Instant? = null,
    val dispatchedAt: Instant? = null,
    val closedAt: Instant? = null,
    val isDeleted: Boolean = false,
)

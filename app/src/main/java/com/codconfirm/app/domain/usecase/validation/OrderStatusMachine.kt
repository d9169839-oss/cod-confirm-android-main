package com.codconfirm.app.domain.usecase.validation

import com.codconfirm.app.domain.model.OrderStatus

/**
 * Pure domain policy for validating order status transitions.
 */
object OrderStatusMachine {
    fun canTransition(from: OrderStatus, to: OrderStatus): Boolean = to in from.allowedNext

    fun allowedNext(from: OrderStatus): Set<OrderStatus> = from.allowedNext
}

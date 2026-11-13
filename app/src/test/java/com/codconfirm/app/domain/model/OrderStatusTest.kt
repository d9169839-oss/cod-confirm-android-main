package com.codconfirm.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderStatusTest {
    @Test
    fun pendingConfirmationCanBeConfirmedOrCancelled() {
        assertEquals(
            setOf(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.PENDING_CONFIRMATION.allowedNext,
        )
    }

    @Test
    fun confirmedCanBeDispatchedOrCancelled() {
        assertEquals(
            setOf(OrderStatus.DISPATCHED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED.allowedNext,
        )
    }

    @Test
    fun dispatchedCanBeDeliveredOrRefused() {
        assertEquals(
            setOf(OrderStatus.DELIVERED, OrderStatus.REFUSED),
            OrderStatus.DISPATCHED.allowedNext,
        )
    }

    @Test
    fun terminalStatusesCannotAdvance() {
        listOf(OrderStatus.DELIVERED, OrderStatus.REFUSED, OrderStatus.CANCELLED).forEach {
            assertEquals(emptySet<OrderStatus>(), it.allowedNext)
        }
    }
}

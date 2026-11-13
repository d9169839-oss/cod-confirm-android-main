package com.codconfirm.app.domain.usecase.validation

import com.codconfirm.app.domain.model.OrderStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusMachineTest {
    @Test
    fun acceptsOnlyDocumentedTransitions() {
        assertTrue(
            OrderStatusMachine.canTransition(
                OrderStatus.PENDING_CONFIRMATION,
                OrderStatus.CONFIRMED,
            ),
        )
        assertTrue(
            OrderStatusMachine.canTransition(
                OrderStatus.DISPATCHED,
                OrderStatus.REFUSED,
            ),
        )
        assertFalse(
            OrderStatusMachine.canTransition(
                OrderStatus.PENDING_CONFIRMATION,
                OrderStatus.DISPATCHED,
            ),
        )
        assertFalse(
            OrderStatusMachine.canTransition(
                OrderStatus.DELIVERED,
                OrderStatus.CONFIRMED,
            ),
        )
    }
}

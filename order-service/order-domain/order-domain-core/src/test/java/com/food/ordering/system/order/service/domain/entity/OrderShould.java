package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.valueobject.OrderId;
import com.food.ordering.system.domain.entity.valueobject.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class OrderShould {

    private UUID uuid;

    @BeforeEach
    void setUp() {
        uuid = mock(UUID.class);
        mockStatic(UUID.class);
        given(UUID.randomUUID()).willReturn(uuid);
        given(uuid.toString()).willReturn(
            "e246a687-661d-408c-9a70-72370bc439b8",
            "662768d4-5f94-4833-b524-55edf721e9b8"
        );
    }

    @org.junit.jupiter.api.Test
    void initializeOrder() {

        Order order = new Order(
                Order.Builder.builder()
                        .items(List.of(
                                new OrderItem(OrderItem.Builder.builder()),
                                new OrderItem(OrderItem.Builder.builder())
                        ))
        );

        order.initializeOrder();

        Assertions.assertEquals(order.getId().getValue().toString(), "e246a687-661d-408c-9a70-72370bc439b8");
        Assertions.assertEquals(order.getTrackingId().getValue().toString(), "662768d4-5f94-4833-b524-55edf721e9b8");
        Assertions.assertEquals(order.getOrderStatus(), OrderStatus.PENDING);
        Assertions.assertEquals(order.getItems().get(0).getId().getValue(), 1);
        Assertions.assertEquals(order.getItems().get(1).getId().getValue(), 2);
    }
}
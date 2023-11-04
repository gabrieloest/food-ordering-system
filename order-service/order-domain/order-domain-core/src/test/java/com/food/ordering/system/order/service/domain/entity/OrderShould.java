package com.food.ordering.system.order.service.domain.entity;

import com.food.ordering.system.domain.entity.valueobject.Money;
import com.food.ordering.system.domain.entity.valueobject.OrderId;
import com.food.ordering.system.domain.entity.valueobject.OrderStatus;
import com.food.ordering.system.domain.entity.valueobject.ProductId;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static com.food.ordering.system.domain.entity.valueobject.OrderStatus.*;
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

	@AfterEach
	void tearDown() {
		clearAllCaches();
	}

	@Test
	void setInitialState_onInitializeOrder() {

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
		Assertions.assertEquals(order.getOrderStatus(), PENDING);
		Assertions.assertEquals(order.getItems().get(0).getId().getValue(), 1);
		Assertions.assertEquals(order.getItems().get(1).getId().getValue(), 2);
	}

	@Test
	void throwException_onValidateOrderValidateInitialOrder_withOrderId() {
		Order order = new Order(
			Order.Builder.builder()
				.orderId(new OrderId(UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8")))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);

		Assertions.assertEquals("Order is not in correct state for initialization!", exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateInitialOrder_withOrderStatus() {
		Order order = new Order(
			Order.Builder.builder()
				.orderStatus(PAID)
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);

		Assertions.assertEquals("Order is not in correct state for initialization!", exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateTotalPrice_withNoPrice() {
		Order order = new Order(
			Order.Builder.builder().price(null)
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);

		Assertions.assertEquals("Total price must be greater than zero!", exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateTotalPrice_withPriceZero() {
		Order order = new Order(
			Order.Builder.builder().price(new Money(BigDecimal.ZERO))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);

		Assertions.assertEquals("Total price must be greater than zero!", exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateItemsPrice_withWrongOrderPrice() {
		Order order = new Order(
			Order.Builder.builder()
				.price(new Money(new BigDecimal(15)))
				.items(List.of(
					new OrderItem(OrderItem.Builder.builder()
						.price(new Money(new BigDecimal(5)))
						.quantity(2)
						.subTotal(new Money(new BigDecimal(10).setScale(2, RoundingMode.HALF_EVEN)))
						.product(new Product(new ProductId(UUID.randomUUID()), "Product 1", new Money(new BigDecimal(5))))),
					new OrderItem(OrderItem.Builder.builder()
						.price(new Money(new BigDecimal(5)))
						.quantity(3)
						.subTotal(new Money(new BigDecimal(15).setScale(2, RoundingMode.HALF_EVEN)))
						.product(new Product(new ProductId(UUID.randomUUID()), "Product 2", new Money(new BigDecimal(5)))))
				))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);
		Assertions.assertEquals("Total price: 15 is not equal to Order items total: 25.00!",
			exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateItemsPrice_withOrderItemPriceDifferentFromProductPrice() {
		Order order = new Order(
			Order.Builder.builder()
				.price(new Money(new BigDecimal(15)))
				.items(List.of(
					new OrderItem(OrderItem.Builder.builder()
						.price(new Money(new BigDecimal(5)))
						.quantity(2)
						.subTotal(new Money(new BigDecimal(10).setScale(2, RoundingMode.HALF_EVEN)))
						.product(new Product(new ProductId(UUID.randomUUID()), "Product 1", new Money(new BigDecimal(15)))))
				))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);
		Assertions.assertEquals("Order item price: 5 is not valid for product e246a687-661d-408c-9a70-72370bc439b8",
			exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateItemsPrice_withOrderItemPriceZero() {
		Order order = new Order(
			Order.Builder.builder()
				.price(new Money(new BigDecimal(15)))
				.items(List.of(
					new OrderItem(OrderItem.Builder.builder()
						.price(Money.ZERO)
						.quantity(2)
						.subTotal(new Money(new BigDecimal(10).setScale(2, RoundingMode.HALF_EVEN)))
						.product(new Product(new ProductId(UUID.randomUUID()), "Product 1", new Money(new BigDecimal(15)))))
				))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);
		Assertions.assertEquals("Order item price: 0 is not valid for product e246a687-661d-408c-9a70-72370bc439b8",
			exception.getMessage());
	}

	@Test
	void throwException_onValidateOrderValidateItemsPrice_withOrderItemWrongSubtotal() {
		Order order = new Order(
			Order.Builder.builder()
				.price(new Money(new BigDecimal(15)))
				.items(List.of(
					new OrderItem(OrderItem.Builder.builder()
						.price(new Money(new BigDecimal(5)))
						.quantity(2)
						.subTotal(new Money(new BigDecimal(15).setScale(2, RoundingMode.HALF_EVEN)))
						.product(new Product(new ProductId(UUID.randomUUID()), "Product 1", new Money(new BigDecimal(15)))))
				))
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::validateOrder);
		Assertions.assertEquals("Order item price: 5 is not valid for product e246a687-661d-408c-9a70-72370bc439b8",
			exception.getMessage());
	}

	@Test
	void throwException_onPay_withOrderStatusNotPending() {
		Order order = new Order(
			Order.Builder.builder()
				.orderStatus(OrderStatus.CANCELLED)
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::pay);

		Assertions.assertEquals("Order is not in correct state for pay operation!", exception.getMessage());
	}

	@Test
	void updateOrderStatusSuccessfully_onPay_withOrderStatusPending() {
		Order order = new Order(
			Order.Builder.builder()
				.orderStatus(PENDING)
		);

		order.pay();

		Assertions.assertEquals(PAID, order.getOrderStatus());
	}

	@Test
	void throwException_onApprove_withOrderStatusNotPaid() {
		Order order = new Order(
			Order.Builder.builder()
				.orderStatus(PENDING)
		);

		Exception exception = Assertions.assertThrows(OrderDomainException.class, order::approve);

		Assertions.assertEquals("Order is not in correct state for approve operation!", exception.getMessage());
	}

	@Test
	void updateOrderStatusSuccessfully_onApprove_withOrderStatusPaid() {
		Order order = new Order(
			Order.Builder.builder()
				.orderStatus(PAID)
		);

		order.approve();

		Assertions.assertEquals(APPROVED, order.getOrderStatus());
	}
}
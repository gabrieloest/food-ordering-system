package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.entity.valueobject.*;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCanceleldEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class OrderDomainServiceImplShould {

	OrderDomainServiceImpl orderDomainService;
	ZonedDateTime zonedDateTimeMock;

	private UUID uuid;

	@BeforeEach
	void setUp() {
		uuid = mock(UUID.class);
		mockStatic(UUID.class);
		given(UUID.randomUUID()).willReturn(uuid);
		given(uuid.toString()).willReturn(
			"e246a687-661d-408c-9a70-72370bc439b8",
			"662768d4-5f94-4833-b524-55edf721e9b8",
			"1ea675bf-331c-43ea-bfe3-02d1073d6716",
			"a61c28d8-9fa2-4084-b42a-59565ce36d74"
		);

		String instantExpected = "2023-01-01T10:00:00Z";
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(instantExpected);
		ZoneId utc = ZoneId.of("UTC");

		zonedDateTimeMock = mock(ZonedDateTime.class);
		mockStatic(ZonedDateTime.class);
		given(ZonedDateTime.now(utc)).willReturn(zonedDateTime);

		orderDomainService = new OrderDomainServiceImpl();
	}

	@AfterEach
	void tearDown() {
		clearAllCaches();
	}

	@Test
	void throwException_onValidateAndInitiateOrder_whenRestaurantNotValid() {
		Order order = new Order(Order.Builder.builder());
		Restaurant restaurant = new Restaurant(Restaurant.Builder.builder()
			.active(false)
			.restaurantId(new RestaurantId(uuid)));

		Exception exception = Assertions.assertThrows(OrderDomainException.class,
			() -> orderDomainService.validateAndInitiateOrder(order, restaurant));

		Assertions.assertEquals("Restaurant with id e246a687-661d-408c-9a70-72370bc439b8 is currently not active!", exception.getMessage());
	}

	@Test
	void createOrderCreatedEvent_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInInitialState(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);

		Assertions.assertEquals("662768d4-5f94-4833-b524-55edf721e9b8", orderCreatedEvent.getOrder().getId().getValue().toString());
		Assertions.assertEquals("2023-01-01T10:00Z", orderCreatedEvent.getCreatedAt().toString());
	}

	@Test
	void setOrderProductInformation_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInInitialState(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		orderDomainService.validateAndInitiateOrder(order, restaurant);

		Assertions.assertEquals("Product 1", order.getItems().get(0).getProduct().getName());
		Assertions.assertEquals(10L, order.getItems().get(0).getProduct().getPrice().getAmount().longValue());
	}

	@Test
	void assertSuccess_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInInitialState(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		assertDoesNotThrow(() -> {
			orderDomainService.validateAndInitiateOrder(order, restaurant);
		});
	}

	@Test
	void createOrderPaidEvent_onPayOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPendingState(product1Id);

		OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order);

		Assertions.assertEquals(new OrderId(UUID.fromString("662768d4-5f94-4833-b524-55edf721e9b8")), orderPaidEvent.getOrder().getId());
		Assertions.assertEquals("2023-01-01T10:00Z", orderPaidEvent.getCreatedAt().toString());
	}

	@Test
	void setOrderStatusToPaid_onPayOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPendingState(product1Id);

		orderDomainService.payOrder(order);

		Assertions.assertEquals(OrderStatus.PAID, order.getOrderStatus());
	}

	@Test
	void assertSuccess_onPayOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPendingState(product1Id);

		assertDoesNotThrow(() -> {
			orderDomainService.payOrder(order);
		});
	}

	@Test
	void assertSuccess_onApproveOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPaidState(product1Id);

		assertDoesNotThrow(() -> {
			orderDomainService.approveOrder(order);
		});
	}

	@Test
	void createOrderCanceledEvent_onCancelOrderPaymentOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPaidState(product1Id);

		OrderCanceleldEvent orderCanceleldEvent = orderDomainService.cancelOrderPayment(order, List.of());

		Assertions.assertEquals(new OrderId(UUID.fromString("662768d4-5f94-4833-b524-55edf721e9b8")), orderCanceleldEvent.getOrder().getId());
		Assertions.assertEquals("2023-01-01T10:00Z", orderCanceleldEvent.getCreatedAt().toString());
	}

	@Test
	void setOrderStatusToCancelling_onCancelOrderPaymentOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPaidState(product1Id);

		orderDomainService.cancelOrderPayment(order, List.of());

		Assertions.assertEquals(OrderStatus.CANCELLING, order.getOrderStatus());
	}

	@Test
	void assertSuccess_onCancelOrderPaymentOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrderInPaidState(product1Id);

		assertDoesNotThrow(() -> {
			orderDomainService.cancelOrderPayment(order, List.of());
		});
	}

	@Test
	void approveOrder() {
	}

	@Test
	void cancelOrderPayment() {
	}

	@Test
	void cancelOrder() {
	}

	private static Order givenAValidOrderInInitialState(UUID product1Id) {
		Order order = new Order(Order.Builder.builder()
			.items(List.of(new OrderItem(
				OrderItem.Builder.builder()
					.product(
						new Product(new ProductId(product1Id), null, null)
					)
					.quantity(1)
					.price(new Money(new BigDecimal("10.00")))
					.subTotal(new Money(new BigDecimal("10.00")))
			)))
			.price(new Money(new BigDecimal("10.00")))
		);
		return order;
	}

	private static Order givenAValidOrderInPendingState(UUID product1Id) {
		Order order = new Order(Order.Builder.builder()
			.orderId(new OrderId(UUID.fromString("662768d4-5f94-4833-b524-55edf721e9b8")))
			.orderStatus(OrderStatus.PENDING)
			.items(List.of(new OrderItem(
				OrderItem.Builder.builder()
					.product(
						new Product(new ProductId(product1Id), null, null)
					)
					.quantity(1)
					.price(new Money(new BigDecimal("10.00")))
					.subTotal(new Money(new BigDecimal("10.00")))
			)))
			.price(new Money(new BigDecimal("10.00")))
		);
		return order;
	}

	private static Order givenAValidOrderInPaidState(UUID product1Id) {
		Order order = new Order(Order.Builder.builder()
			.orderId(new OrderId(UUID.fromString("662768d4-5f94-4833-b524-55edf721e9b8")))
			.orderStatus(OrderStatus.PAID)
			.items(List.of(new OrderItem(
				OrderItem.Builder.builder()
					.product(
						new Product(new ProductId(product1Id), null, null)
					)
					.quantity(1)
					.price(new Money(new BigDecimal("10.00")))
					.subTotal(new Money(new BigDecimal("10.00")))
			)))
			.price(new Money(new BigDecimal("10.00")))
		);
		return order;
	}

	private static Restaurant givenAValidRestaurant(UUID product1Id) {
		Restaurant restaurant = new Restaurant(Restaurant.Builder.builder()
			.restaurantId(new RestaurantId(product1Id))
			.active(true)
			.products(List.of(
				new Product(
					new ProductId(UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8")),
					"Product 1",
					new Money(new BigDecimal("10.00"))
				)
			))
		);
		return restaurant;
	}
}
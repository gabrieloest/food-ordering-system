package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.entity.valueobject.Money;
import com.food.ordering.system.domain.entity.valueobject.ProductId;
import com.food.ordering.system.domain.entity.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

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
			"662768d4-5f94-4833-b524-55edf721e9b8"
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
	}

	@Test
	void throwException_onValidateAndInitiateOrder_whenRestaurantNotValid() {
		Order order = new Order(Order.Builder.builder());
		Restaurant restaurant = new Restaurant(Restaurant.Builder.builder()
			.active(false)
			.restaurantId(new RestaurantId(UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8"))));

		Exception exception = Assertions.assertThrows(OrderDomainException.class,
			() -> orderDomainService.validateAndInitiateOrder(order, restaurant));

		Assertions.assertEquals("Restaurant with id e246a687-661d-408c-9a70-72370bc439b8 is currently not active!", exception.getMessage());
	}

	@Test
	void createOrderCreatedEvent_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrder(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		OrderCreatedEvent orderCreatedEvent = orderDomainService.validateAndInitiateOrder(order, restaurant);

		Assertions.assertEquals("662768d4-5f94-4833-b524-55edf721e9b8", orderCreatedEvent.getOrder().getId().getValue().toString());
		Assertions.assertEquals("2023-01-01T10:00Z", orderCreatedEvent.getCreatedAt().toString());
	}

	@Test
	void setOrderProductInformation_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrder(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		orderDomainService.validateAndInitiateOrder(order, restaurant);

		Assertions.assertEquals("Product 1", order.getItems().get(0).getProduct().getName());
		Assertions.assertEquals(10L, order.getItems().get(0).getProduct().getPrice().getAmount().longValue());
	}

	@Test
	void assertSuccess_onValidateAndInitiateOrder_whenInformationValid() {
		UUID product1Id = UUID.fromString("e246a687-661d-408c-9a70-72370bc439b8");

		Order order = givenAValidOrder(product1Id);

		Restaurant restaurant = givenAValidRestaurant(product1Id);

		assertDoesNotThrow(() -> {
			orderDomainService.validateAndInitiateOrder(order, restaurant);
		});
	}

	@Test
	void payOrder() {
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

	private static Order givenAValidOrder(UUID product1Id) {
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
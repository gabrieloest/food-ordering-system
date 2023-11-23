package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.entity.valueobject.*;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OderAddress;
import com.food.ordering.system.order.service.domain.dto.create.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Customer;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.exception.OrderDomainException;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.ports.input.service.OrderApplicationService;
import com.food.ordering.system.order.service.domain.ports.output.repository.CustomerRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.order.service.domain.ports.output.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = OrderTestConfiguration.class)
public class OrderApplicationServiceShould {

	@Autowired
	private OrderApplicationService orderApplicationService;

	@Autowired
	private OrderDataMapper orderDataMapper;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;

	private CreateOrderCommand createOrderCommand;
	private CreateOrderCommand createOrderCommandWrongPrice;
	private CreateOrderCommand createOrderCommandWrongProductPrice;
	private final UUID CUSTOMER_ID = UUID.fromString("1cfcee27-9b00-4f27-a0b0-d01f0a30feba");
	private final UUID RESTAURANT_ID = UUID.fromString("28841a9a-507a-4315-983b-b6fd8edbed59");
	private final UUID PRODUCT_ID = UUID.fromString("bb2313ce-f0ab-4519-973a-7fdb81ab281d");
	private final UUID ORDER_ID = UUID.fromString("8f73caba-6d13-422c-8a90-b653bee27a1a");
	private final BigDecimal PRICE = new BigDecimal("200.00");

	@BeforeEach
	public void init() {
		givenAValidCreateOrderCommand();

		Customer customer = new Customer();
		customer.setId(new CustomerId(CUSTOMER_ID));

		Restaurant restaurantResponse = Restaurant.Builder.builder()
			.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
			.products(List.of(
				new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
				new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
			))
			.active(true)
			.build();

		Order order = orderDataMapper.createOrderCommandToOrder(createOrderCommand);
		order.setId(new OrderId(ORDER_ID));

		when(customerRepository.findCustomer(CUSTOMER_ID)).thenReturn(Optional.of(customer));
		when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
			.thenReturn(Optional.of(restaurantResponse));
		when(orderRepository.save(any(Order.class))).thenReturn(order);
	}

	@Test
	public void createOrder_whenValidDataIsPassed() {
		CreateOrderResponse createOrderResponse = orderApplicationService.createOrder(createOrderCommand);
		assertEquals(OrderStatus.PENDING, createOrderResponse.getOrderStatus());
		assertEquals("Order created successfully", createOrderResponse.getMessage());
		assertNotNull(createOrderResponse.getOrderTrackingId());
	}

	@Test
	public void throwException_whenCreateOrderWrongTotalPrice() {
		givenACreateOrderCommandWithWrongTotalPrice();

		OrderDomainException orderDomainException =
			assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongPrice));
		assertEquals("Total price: 250.00 is not equal to Order items total: 200.00!",
			orderDomainException.getMessage());
	}

	@Test
	public void throwException_whenCreateOrderWrongProductPrice() {
		givenACreateOrderCommandWithWrongProductPrice();

		OrderDomainException orderDomainException =
			assertThrows(OrderDomainException.class,
				() -> orderApplicationService.createOrder(createOrderCommandWrongProductPrice));
		assertEquals("Order item price: 60.00 is not valid for product " + PRODUCT_ID,
			orderDomainException.getMessage());
	}

	@Test
	public void throwException_whenCreateOrderWithPassiveRestaurant() {
		Restaurant restaurant = Restaurant.Builder.builder()
			.restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
			.products(List.of(
				new Product(new ProductId(PRODUCT_ID), "product-1", new Money(new BigDecimal("50.00"))),
				new Product(new ProductId(PRODUCT_ID), "product-2", new Money(new BigDecimal("50.00")))
			))
			.active(false)
			.build();

		when(restaurantRepository.findRestaurantInformation(orderDataMapper.createOrderCommandToRestaurant(createOrderCommand)))
			.thenReturn(Optional.of(restaurant));

		OrderDomainException orderDomainException = assertThrows(OrderDomainException.class,
			() -> orderApplicationService.createOrder(createOrderCommand));

		assertEquals("Restaurant with id " + RESTAURANT_ID + " is currently not active!",
			orderDomainException.getMessage());
	}

	private void givenAValidCreateOrderCommand() {
		createOrderCommand = CreateOrderCommand.builder()
			.customerId(CUSTOMER_ID)
			.restaurantId(RESTAURANT_ID)
			.address(OderAddress.builder()
				.street("street_1")
				.postalCode("100AB")
				.city("Paris")
				.build())
			.price(PRICE)
			.items(List.of(
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(1)
					.price(new BigDecimal("50.00"))
					.subTotal(new BigDecimal("50.00"))
					.build(),
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(3)
					.price(new BigDecimal("50.00"))
					.subTotal(new BigDecimal("150.00"))
					.build()
			))
			.build();
	}

	private void givenACreateOrderCommandWithWrongTotalPrice() {
		createOrderCommandWrongPrice = CreateOrderCommand.builder()
			.customerId(CUSTOMER_ID)
			.restaurantId(RESTAURANT_ID)
			.address(OderAddress.builder()
				.street("street_1")
				.postalCode("100AB")
				.city("Paris")
				.build())
			.price(new BigDecimal("250.00"))
			.items(List.of(
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(1)
					.price(new BigDecimal("50.00"))
					.subTotal(new BigDecimal("50.00"))
					.build(),
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(3)
					.price(new BigDecimal("50.00"))
					.subTotal(new BigDecimal("150.00"))
					.build()
			))
			.build();
	}

	private void givenACreateOrderCommandWithWrongProductPrice() {
		createOrderCommandWrongProductPrice = CreateOrderCommand.builder()
			.customerId(CUSTOMER_ID)
			.restaurantId(RESTAURANT_ID)
			.address(OderAddress.builder()
				.street("street_1")
				.postalCode("100AB")
				.city("Paris")
				.build())
			.price(new BigDecimal("210.00"))
			.items(List.of(
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(1)
					.price(new BigDecimal("60.00"))
					.subTotal(new BigDecimal("60.00"))
					.build(),
				OrderItem.builder()
					.productId(PRODUCT_ID)
					.quantity(3)
					.price(new BigDecimal("50.00"))
					.subTotal(new BigDecimal("150.00"))
					.build()
			))
			.build();
	}
}

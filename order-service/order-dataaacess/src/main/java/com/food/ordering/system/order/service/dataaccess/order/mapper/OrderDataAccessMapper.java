package com.food.ordering.system.order.service.dataaccess.order.mapper;

import com.food.ordering.system.domain.entity.valueobject.*;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Component
public class OrderDataAccessMapper {

	public OrderEntity orderToOrderEntity(Order order) {
		OrderEntity orderEntity = OrderEntity.builder()
			.id(order.getId().getValue())
			.customerId(order.getCustomerId().getValue())
			.restaurantId(order.getRestaurantId().getValue())
			.trackingId(order.getTrackingId().getValue())
			.address(deliveryAddressToAddressEntity(order.getDeliveryAddress()))
			.price(order.getPrice().getAmount())
			.items(orderItemsToOrderItemEntities(order.getItems()))
			.orderStatus(order.getOrderStatus())
			.failureMessages(order.getFailureMessages() != null ?
				String.join(FAILURE_MESSAGE_DELIMITER, order.getFailureMessages()) : "")
			.build();

		orderEntity.getAddress().setOrder(orderEntity);
		orderEntity.getItems().forEach(it -> it.setOrder(orderEntity));

		return orderEntity;
	}

	public Order orderEntityToOrder(OrderEntity orderEntity) {
		return Order.Builder.builder()
			.orderId(new OrderId(orderEntity.getId()))
			.customerId(new CustomerId(orderEntity.getCustomerId()))
			.restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
			.deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
			.price(new Money(orderEntity.getPrice()))
			.items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
			.trackingId(new TrackingId(orderEntity.getTrackingId()))
			.orderStatus(orderEntity.getOrderStatus())
			.failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>() :
				new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages().split(FAILURE_MESSAGE_DELIMITER))))
			.build();
	}

	private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress deliveryAddress) {
		return OrderAddressEntity.builder()
			.id(deliveryAddress.getId())
			.street(deliveryAddress.getStreet())
			.postalCode(deliveryAddress.getPostalCode())
			.city(deliveryAddress.getCity())
			.build();
	}

	private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
		return items
			.stream()
			.map(it -> OrderItemEntity.builder()
				.id(it.getId().getValue())
				.productId(it.getProduct().getId().getValue())
				.price(it.getPrice().getAmount())
				.quantity(it.getQuantity())
				.subtotal(it.getSubTotal().getAmount())
				.build())
			.collect(Collectors.toList());
	}

	private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
		return new StreetAddress(address.getId(), address.getStreet(), address.getPostalCode(), address.getCity());
	}

	private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
		return items.stream()
			.map(it -> OrderItem.Builder.builder()
				.orderItemId(new OrderItemId(it.getId()))
				.product(new Product(new ProductId(it.getProductId())))
				.price(new Money(it.getPrice()))
				.quantity(it.getQuantity())
				.subTotal(new Money(it.getSubtotal()))
				.build())
			.collect(Collectors.toList());
	}
}

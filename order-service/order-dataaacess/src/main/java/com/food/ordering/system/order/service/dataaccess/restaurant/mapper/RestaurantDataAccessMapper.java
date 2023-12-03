package com.food.ordering.system.order.service.dataaccess.restaurant.mapper;

import com.food.ordering.system.domain.entity.valueobject.Money;
import com.food.ordering.system.domain.entity.valueobject.ProductId;
import com.food.ordering.system.domain.entity.valueobject.RestaurantId;
import com.food.ordering.system.order.service.dataaccess.restaurant.entity.RestaurantEntity;
import com.food.ordering.system.order.service.dataaccess.restaurant.exception.RestaurantDataAccessException;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantDataAccessMapper {

	public List<UUID> restaurantToRestaurantProducts(Restaurant restaurant) {
		return restaurant.getProducts().stream()
			.map(it -> it.getId().getValue())
			.collect(Collectors.toList());
	}

	public Restaurant restaurantEntityToRestaurant(List<RestaurantEntity> restaurantEntities) {
		RestaurantEntity restaurantEntity =
			restaurantEntities.stream().findFirst().orElseThrow(() ->
				new RestaurantDataAccessException("Restaurant could not be found!"));

		List<Product> restaurantProducts = restaurantEntities.stream().map(it ->
			new Product(new ProductId(it.getProductId()), it.getProductName(), new Money(it.getProductPrice())))
			.collect(Collectors.toList());

		return Restaurant.Builder.builder()
			.restaurantId(new RestaurantId(restaurantEntity.getRestaurantId()))
			.products(restaurantProducts)
			.active(restaurantEntity.getRestaurantActive())
			.build();
	}
}
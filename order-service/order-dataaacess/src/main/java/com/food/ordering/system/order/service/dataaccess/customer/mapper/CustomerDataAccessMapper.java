package com.food.ordering.system.order.service.dataaccess.customer.mapper;

import com.food.ordering.system.domain.entity.valueobject.CustomerId;
import com.food.ordering.system.order.service.dataaccess.customer.entity.CustomerEntity;
import com.food.ordering.system.order.service.domain.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataAccessMapper {

	public Customer customerEntityToEntity(CustomerEntity customerEntity) {
		return new Customer(new CustomerId(customerEntity.getId()));
	}
}

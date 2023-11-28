package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import com.food.ordering.system.order.service.domain.config.OrderServiceConfigData;
import com.food.ordering.system.order.service.domain.event.OrderCancelledEvent;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.payment.OrderCancelledPaymentRequestMessagePublisher;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class CancelOrderKafkaMessagePublisher implements OrderCancelledPaymentRequestMessagePublisher {

	private final OrderMessagingDataMapper orderMessagingDataMapper;
	private final OrderServiceConfigData orderServiceConfigData;
	private final KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer;

	public CancelOrderKafkaMessagePublisher(OrderMessagingDataMapper orderMessagingDataMapper,
											OrderServiceConfigData orderServiceConfigData,
											KafkaProducer<String, PaymentRequestAvroModel> kafkaProducer) {
		this.orderMessagingDataMapper = orderMessagingDataMapper;
		this.orderServiceConfigData = orderServiceConfigData;
		this.kafkaProducer = kafkaProducer;
	}

	@Override
	public void publish(OrderCancelledEvent domainEvent) {
		{
			String orderId = domainEvent.getOrder().getId().getValue().toString();
			log.info("Received OrderCancelledEvent for order id: {}", orderId);

			try {
				PaymentRequestAvroModel paymentRequestAvroModel =
					orderMessagingDataMapper.orderCreatedEventToPaymentRequestAvroModel(domainEvent);

				kafkaProducer.send(orderServiceConfigData.getPaymentRequestTopicName(),
					orderId,
					paymentRequestAvroModel,
					getKafkaCallback(orderServiceConfigData.getPaymentResponseTopicName(), paymentRequestAvroModel)
				);

				log.info("PaymentRequestAvroModel sent to Kafka for order id: {}", paymentRequestAvroModel.getOrderId());
			} catch (Exception e) {
				log.error("Error while sending PaymentRequestAvroModel message to Kafka with order id: {}, " +
					"error: {}", orderId, e.getMessage());
			}
		}
	}

	private ListenableFutureCallback<SendResult<String, PaymentRequestAvroModel>> getKafkaCallback(
		String paymentResponseTopicName, PaymentRequestAvroModel paymentRequestAvroModel) {

		return new ListenableFutureCallback<SendResult<String, PaymentRequestAvroModel>>() {
			@Override
			public void onFailure(Throwable ex) {
				log.error("Error while sending PaymentRequestAvroModel " +
					"message {} to topic {}", paymentRequestAvroModel.toString(), paymentResponseTopicName, ex);
			}

			@Override
			public void onSuccess(SendResult<String, PaymentRequestAvroModel> result) {
				RecordMetadata recordMetadata = result.getRecordMetadata();
				log.info("Received successful response from Kafka for order id: {} " +
						"Topic: {} Partition: {} Offset: {} Timestamp: {}",
					paymentRequestAvroModel.getOrderId(),
					recordMetadata.topic(),
					recordMetadata.partition(),
					recordMetadata.offset(),
					recordMetadata.timestamp());
			}
		};
	}
}

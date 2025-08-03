package ec.com.nttdata.accounts_movements_service.producer.customer;

import static ec.com.nttdata.accounts_movements_service.producer.topics.Topic.TOPIC_MOVEMENT_EVENT;

import ec.com.nttdata.accounts_movements_service.producer.KafkaDispatcher;
import ec.com.nttdata.accounts_movements_service.producer.customer.dto.MovementCustomerRequest;
import ec.com.nttdata.accounts_movements_service.producer.topics.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor
public class EventAccountCustomerPublisher {
    private final KafkaDispatcher kafkaDispatcher;

    public void sendTransactionEvent(MovementCustomerRequest dto) {
        kafkaDispatcher.sendMessage(dto, TOPIC_MOVEMENT_EVENT, "create");
    }
}
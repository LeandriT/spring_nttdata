package ec.com.nttdata.accounts_movements_service.producer.customer;

import static ec.com.nttdata.accounts_movements_service.producer.topics.Topic.TOPIC_MOVEMENT_EVENT;

import ec.com.nttdata.accounts_movements_service.producer.KafkaDispatcher;
import ec.com.nttdata.accounts_movements_service.producer.customer.dto.MovementCustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventAccountCustomerPublisher {
    private final KafkaDispatcher kafkaDispatcher;

    public void sendTransactionEvent(MovementCustomerRequest dto) {
        log.info("** Enviando kafka message ** {}", dto.toString());
        kafkaDispatcher.sendMessage(dto, TOPIC_MOVEMENT_EVENT, "create");
    }
}
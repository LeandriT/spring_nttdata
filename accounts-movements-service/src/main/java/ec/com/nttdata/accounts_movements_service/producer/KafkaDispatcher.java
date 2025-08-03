package ec.com.nttdata.accounts_movements_service.producer;


import ec.com.nttdata.accounts_movements_service.config.kafka.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("KafkaDispatcher.v1")
public class KafkaDispatcher extends KafkaMessage {

    private final KafkaTemplate<String, KafkaMessage> producer;
    private static final Logger log = LoggerFactory.getLogger(KafkaDispatcher.class);

    public KafkaDispatcher(KafkaTemplate<String, KafkaMessage> producer) {
        this.producer = producer;
    }

    public void sendMessage(Object data, String topic, String action) {
        KafkaMessage message = buildMessage(data, action);
        try {

            Thread.sleep(5000); // esperar para evitar LEADER_NOT_AVAILABLE
            String response = producer.send(topic, message).get().toString();
            log.trace(response);
        } catch (Exception e) {
            log.error("Failed to send Kafka message", e);
        }
    }

    private KafkaMessage buildMessage(Object data, String action) {
        this.setData(data);
        this.setAction(action);
        return this;
    }
}
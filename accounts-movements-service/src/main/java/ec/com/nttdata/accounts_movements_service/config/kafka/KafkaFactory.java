package ec.com.nttdata.accounts_movements_service.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaFactory {

    private final ObjectMapper objectMapper;

    public KafkaFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "config.kafka.consumerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> consumerFactory(KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, KafkaMessage> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        ErrorHandlingDeserializer<KafkaMessage> errorHandlingDeserializer =
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(KafkaMessage.class, objectMapper));

        DefaultKafkaConsumerFactory<String, KafkaMessage> consumerFactory =
                new DefaultKafkaConsumerFactory<>(
                        KafkaConfig.getConfig(kafkaProperties),
                        new StringDeserializer(),
                        errorHandlingDeserializer
                );

        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory;
    }
}
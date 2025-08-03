package ec.com.nttdata.customer_service.config.kafka;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaMessage {

    private String action;
    private Object data;

    // Constructor sin argumentos
    public KafkaMessage() {
    }

    // Getters y Setters para los campos

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
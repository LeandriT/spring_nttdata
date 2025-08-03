package ec.com.nttdata.customer_service.dto.response;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CustomerResponse {
    private Long id;
    private String name;
    private String gender;
    private int age;
    private String dni;
    private String address;
    private String phone;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
package ec.com.nttdata.customer_service.dto.request;

import lombok.Data;

@Data
public class CustomerRequest {
    private String name;
    private String gender;
    private int age;
    private String dni;
    private String address;
    private String phone;
    private String password;
    private boolean isActive;
}
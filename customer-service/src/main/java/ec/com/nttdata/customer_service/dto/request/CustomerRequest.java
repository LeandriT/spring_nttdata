package ec.com.nttdata.customer_service.dto.request;

import ec.com.nttdata.customer_service.dto.retentions.OnCreate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "name cannot be blank", groups = {OnCreate.class})
    private String name;

    @NotBlank(message = "gender cannot be blank", groups = {OnCreate.class})
    private String gender;

    @NotNull(message = "age cannot be null", groups = {OnCreate.class})
    @Min(value = 0, message = "age must be positive", groups = {OnCreate.class})
    private Integer age;

    @NotBlank(message = "dni cannot be blank", groups = {OnCreate.class})
    @Size(max = 20, message = "dni must be less than 20 characters", groups = {OnCreate.class})
    private String dni;

    @NotBlank(message = "address cannot be blank", groups = {OnCreate.class})
    private String address;

    @NotBlank(message = "phone cannot be blank", groups = {OnCreate.class})
    private String phone;

    @NotBlank(message = "password cannot be blank", groups = {OnCreate.class})
    @Size(min = 5, message = "password must have at least 5 characters", groups = {OnCreate.class})
    private String password;

    @NotNull(message = "isActive cannot be null", groups = {OnCreate.class})
    private Boolean isActive;
}
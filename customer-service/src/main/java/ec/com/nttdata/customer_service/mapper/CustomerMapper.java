package ec.com.nttdata.customer_service.mapper;

import ec.com.nttdata.customer_service.dto.request.CustomerRequest;
import ec.com.nttdata.customer_service.dto.response.CustomerResponse;
import ec.com.nttdata.customer_service.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CustomerMapper {

    Customer toModel(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    void updateModel(CustomerRequest request, @MappingTarget Customer customer);
}
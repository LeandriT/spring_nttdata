package ec.com.nttdata.accounts_movements_service.mapper;

import ec.com.nttdata.accounts_movements_service.dto.account.request.AccountRequest;
import ec.com.nttdata.accounts_movements_service.dto.account.response.AccountResponse;
import ec.com.nttdata.accounts_movements_service.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface AccountMapper {
    Account toModel(AccountRequest request);

    AccountResponse toResponse(Account customer);

    @Mapping(target = "customerId", ignore = true)
    void updateModel(AccountRequest request, @MappingTarget Account entity);
}
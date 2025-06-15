package ebanking_backend.ebanking_backend.mappers;

import ebanking_backend.ebanking_backend.dtos.AccountOperationDto;
import ebanking_backend.ebanking_backend.entities.AccountOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AccountOperationMapper {

    public AccountOperationDto fromAccountOperation(AccountOperation accountOperation) {
        AccountOperationDto dto = new AccountOperationDto();
        BeanUtils.copyProperties(accountOperation, dto);
        return dto;
    }
}
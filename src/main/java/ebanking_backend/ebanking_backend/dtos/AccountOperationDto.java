package ebanking_backend.ebanking_backend.dtos;

import ebanking_backend.ebanking_backend.enums.OperationType;
import lombok.Data;

import java.util.Date;

@Data
public class AccountOperationDto {
    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
}

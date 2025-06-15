package ebanking_backend.ebanking_backend.dtos;


import lombok.Data;
import ebanking_backend.ebanking_backend.enums.AccountStatus;

import java.util.Date;

@Data
public class SavingBankAccountDto extends BankAccountDto{
    private String id;
    private double balance;
    private Date createdAt;
    private AccountStatus status;
    private CustomerDto customerDto;
    private double interestRate;
}
package ebanking_backend.ebanking_backend.mappers;


import ebanking_backend.ebanking_backend.dtos.CurrentBankAccountDto;
import ebanking_backend.ebanking_backend.dtos.SavingBankAccountDto;
import ebanking_backend.ebanking_backend.entities.CurrentAccount;
import ebanking_backend.ebanking_backend.entities.SavingAccount;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BankAccountMapper {
    private CustomerMapper customerMapper;

    public SavingBankAccountDto fromSavingBankAccount(SavingAccount savingBankAccount) {
        SavingBankAccountDto dto = new SavingBankAccountDto();
        BeanUtils.copyProperties(savingBankAccount, dto);
        dto.setCustomerDto(customerMapper.fromCustomer(savingBankAccount.getCustomer()));
        dto.setType(savingBankAccount.getClass().getSimpleName());
        return dto;
    }

    public SavingAccount fromSavingBankAccountDto(SavingBankAccountDto dto) {
        SavingAccount savingBankAccount = new SavingAccount();
        BeanUtils.copyProperties(dto, savingBankAccount);
        savingBankAccount.setCustomer(customerMapper.fromCustomerDto(dto.getCustomerDto()));
        return savingBankAccount;
    }

    public CurrentBankAccountDto fromCurrentBankAccount(CurrentAccount currentAccount) {
        CurrentBankAccountDto dto = new CurrentBankAccountDto();
        BeanUtils.copyProperties(currentAccount, dto);
        dto.setCustomerDto(customerMapper.fromCustomer(currentAccount.getCustomer()));
        dto.setType(currentAccount.getClass().getSimpleName());
        return dto;
    }

    public CurrentAccount fromCurrentBankAccountDto(CurrentBankAccountDto dto) {
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(dto, currentAccount);
        currentAccount.setCustomer(customerMapper.fromCustomerDto(dto.getCustomerDto()));
        return currentAccount;
    }
}

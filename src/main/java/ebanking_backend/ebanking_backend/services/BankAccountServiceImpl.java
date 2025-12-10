package ebanking_backend.ebanking_backend.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import ebanking_backend.ebanking_backend.dtos.*;
import ebanking_backend.ebanking_backend.entities.*;
import ebanking_backend.ebanking_backend.enums.AccountStatus;
import ebanking_backend.ebanking_backend.enums.OperationType;
import ebanking_backend.ebanking_backend.exceptions.BalanceNotSufficientException;
import ebanking_backend.ebanking_backend.exceptions.BankAccountNotFoundException;
import ebanking_backend.ebanking_backend.exceptions.CustomerNotFoundException;
import ebanking_backend.ebanking_backend.mappers.AccountOperationMapper;
import ebanking_backend.ebanking_backend.mappers.BankAccountMapper;
import ebanking_backend.ebanking_backend.mappers.CustomerMapper;
import ebanking_backend.ebanking_backend.repositories.AccountOperationRepository;
import ebanking_backend.ebanking_backend.repositories.BankAccountRepository;
import ebanking_backend.ebanking_backend.repositories.CustomerRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements IBankAccountService {

    private static final String CUSTOMER_NOT_FOUND = "Customer not found with ID: ";
    private static final String ACCOUNT_NOT_FOUND = "Bank account not found with ID: ";

    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    private final CustomerMapper customerMapper;
    private final BankAccountMapper bankAccountMapper;
    private final AccountOperationMapper accountOperationMapper;

    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        Customer customer = customerMapper.fromCustomerDto(customerDto);
        Customer save = customerRepository.save(customer);
        return customerMapper.fromCustomer(save);
    }

    @Override
    public CurrentBankAccountDto saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(CUSTOMMARY_NOT_FOUND + customerId)
        );

        CurrentAccount bankAccount = new CurrentAccount();
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setBalance(initialBalance);
        bankAccount.setCreatedAt(new Date());
        bankAccount.setCustomer(customer);
        bankAccount.setStatus(AccountStatus.CREATED);
        bankAccount.setOverDraft(overDraft);

        CurrentAccount save = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.fromCurrentBankAccount(save);
    }

    @Override
    public SavingBankAccountDto saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND + customerId)
        );

        SavingAccount bankAccount = new SavingAccount();
        bankAccount.setId(UUID.randomUUID().toString());
        bankAccount.setBalance(initialBalance);
        bankAccount.setCreatedAt(new Date());
        bankAccount.setCustomer(customer);
        bankAccount.setStatus(AccountStatus.CREATED);
        bankAccount.setInterestRate(interestRate);

        SavingAccount save = bankAccountRepository.save(bankAccount);
        return bankAccountMapper.fromSavingBankAccount(save);
    }

    @Override
    public List<CustomerDto> listCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::fromCustomer)
                .toList();
    }

    @Override
    public BankAccountDto getBankAccount(String accountId) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException(ACCOUNT_NOT_FOUND + accountId)
        );

        if (bankAccount instanceof CurrentAccount currentAccount) {
            return bankAccountMapper.fromCurrentBankAccount(currentAccount);
        } else if (bankAccount instanceof SavingAccount savingAccount) {
            return bankAccountMapper.fromSavingBankAccount(savingAccount);
        } else {
            throw new IllegalArgumentException("Unknown account type");
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException(ACCOUNT_NOT_FOUND + accountId)
        );

        if (bankAccount.getBalance() < amount) {
            throw new BalanceNotSufficientException("Insufficient funds");
        }

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void credit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException(ACCOUNT_NOT_FOUND + accountId)
        );

        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setAmount(amount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);

        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) {
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);
    }

    @Override
    public List<BankAccountDto> bankAccountList() {
        return bankAccountRepository.findAll().stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof CurrentAccount currentAccount) {
                        return bankAccountMapper.fromCurrentBankAccount(currentAccount);
                    } else if (bankAccount instanceof SavingAccount savingAccount) {
                        return bankAccountMapper.fromSavingBankAccount(savingAccount);
                    } else {
                        throw new IllegalArgumentException("Unknown account type");
                    }
                })
                .toList();
    }

    @Override
    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND + id)
        );
        return customerMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        Customer customer = customerRepository.findById(customerDto.getId()).orElseThrow(
                () -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND + customerDto.getId())
        );
        customer.setName(customerDto.getName());
        customer.setEmail(customerDto.getEmail());
        customer = customerRepository.save(customer);
        return customerMapper.fromCustomer(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new CustomerNotFoundException(CUSTOMER_NOT_FOUND + id)
        );
        customerRepository.delete(customer);
    }

    @Override
    public List<AccountOperationDto> accountHistory(String accountId) {
        return accountOperationRepository.findByBankAccountId(accountId).stream()
                .map(accountOperationMapper::fromAccountOperation)
                .toList();
    }

    @Override
    public AccountHistoryDto getAccountHistory(String accountId, int page, int size) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException(ACCOUNT_NOT_FOUND + accountId)
        );

        Page<AccountOperation> accountOperations =
                accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));

        AccountHistoryDto dto = new AccountHistoryDto();
        dto.setAccountOperationDtos(
                accountOperations.getContent().stream()
                        .map(accountOperationMapper::fromAccountOperation)
                        .toList()
        );
        dto.setAccountId(bankAccount.getId());
        dto.setBalance(bankAccount.getBalance());
        dto.setPageSize(size);
        dto.setCurrentPage(page);
        dto.setTotalPages(accountOperations.getTotalPages());

        return dto;
    }

    @Override
    public List<CustomerDto> searchCustomers(String keyword) {
        return customerRepository.findByNameContaining(keyword).stream()
                .map(customerMapper::fromCustomer)
                .toList();
    }
}

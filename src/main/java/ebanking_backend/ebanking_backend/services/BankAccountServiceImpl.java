package ebanking_backend.ebanking_backend.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import ebanking_backend.ebanking_backend.dtos.BankAccountDto;
import ebanking_backend.ebanking_backend.dtos.CurrentBankAccountDto;
import ebanking_backend.ebanking_backend.dtos.CustomerDto;
import ebanking_backend.ebanking_backend.dtos.SavingBankAccountDto;
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

import ebanking_backend.ebanking_backend.dtos.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import static java.util.stream.Collectors.toList;


@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements IBankAccountService {

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;

    private CustomerMapper customerMapper;
    private BankAccountMapper bankAccountMapper;
    private AccountOperationMapper accountOperationMapper;


    @Override
    public CustomerDto saveCustomer(CustomerDto customerDto) {
        Customer customer = customerMapper.fromCustomerDto(customerDto);
        Customer save = customerRepository.save(customer);
        return customerMapper.fromCustomer(save);
    }

    @Override
    public CurrentBankAccountDto saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) {

        Customer customer = customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException("Customer not found with ID: " + customerId)
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
                () -> new CustomerNotFoundException("Customer not found with ID: " + customerId)
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
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customerMapper::fromCustomer)
                .toList();
    }

    @Override
    public BankAccountDto getBankAccount(String accountId) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId)
        );
        if (bankAccount instanceof CurrentAccount) {
            return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        } else if (bankAccount instanceof SavingAccount) {
            return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            throw new IllegalArgumentException("Unknown account type");
        }
    }

    @Override
    public void debit(String accountId, double amount, String description) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId)
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
                () -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId)
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
        List<BankAccount> all = bankAccountRepository.findAll();
        return all.stream()
                .map(bankAccount -> {
                    if (bankAccount instanceof CurrentAccount) {
                        return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
                    } else if (bankAccount instanceof SavingAccount) {
                        return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
                    } else {
                        throw new IllegalArgumentException("Unknown account type");
                    }
                })
                .toList();
    }

    @Override
    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new CustomerNotFoundException("Customer not found with ID: " + id)
        );
        return customerMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        Customer customer = customerRepository.findById(customerDto.getId()).orElseThrow(
                () -> new CustomerNotFoundException("Customer not found with ID: " + customerDto.getId())
        );
        customer.setName(customerDto.getName());
        customer.setEmail(customerDto.getEmail());
        customer = customerRepository.save(customer);
        return customerMapper.fromCustomer(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(
                () -> new CustomerNotFoundException("Customer not found with ID: " + id)
        );
        customerRepository.delete(customer);
    }

    @Override
    public List<AccountOperationDto> accountHistory(String accountId) {
        List<AccountOperation> byBankAccountId = accountOperationRepository.findByBankAccountId(accountId);
        return byBankAccountId.stream()
                .map(accountOperationMapper::fromAccountOperation)
                .toList();
    }
    @Override
    public AccountHistoryDto getAccountHistory(String accountId, int page, int size) {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElseThrow(
                () -> new BankAccountNotFoundException("Bank account not found with ID: " + accountId)
        );
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDto accountHistoryDto = new AccountHistoryDto();
        List<AccountOperationDto> accountOperationDtos = accountOperations.getContent().stream().map(operation ->
                accountOperationMapper.fromAccountOperation(operation)
        ).toList();
        accountHistoryDto.setAccountOperationDtos(accountOperationDtos);
        accountHistoryDto.setAccountId(bankAccount.getId());
        accountHistoryDto.setBalance(bankAccount.getBalance());
        accountHistoryDto.setPageSize(size);
        accountHistoryDto.setCurrentPage(page);
        accountHistoryDto.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDto;
    }

    @Override
    public List<CustomerDto> searchCustomers(String keyword) {
        return customerRepository.findByNameContaining(keyword).stream()
                .map(customerMapper::fromCustomer)
                .collect(toList());
    }

}
package ebanking_backend.ebanking_backend;

import ebanking_backend.ebanking_backend.entities.*;
import ebanking_backend.ebanking_backend.enums.AccountStatus;
import ebanking_backend.ebanking_backend.enums.OperationType;
import ebanking_backend.ebanking_backend.repositories.AccountOperationRepository;
import ebanking_backend.ebanking_backend.repositories.BankAccountRepository;
import ebanking_backend.ebanking_backend.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@Slf4j
public class EbankingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EbankingBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(BankAccountRepository bankAccountRepository) {
        return args -> {
            BankAccount bankAccount1 = bankAccountRepository
                    .findById("21b4cab6-f844-43ab-85ba-e40f49ef28bc")
                    .orElse(null);

            if (bankAccount1 != null) {

                log.info("Account ID: {}", bankAccount1.getId());
                log.info("Balance: {}", bankAccount1.getBalance());
                log.info("Status: {}", bankAccount1.getStatus());
                log.info("Created At: {}", bankAccount1.getCreatedAt());
                log.info("Customer: {}", bankAccount1.getCustomer().getName());
                log.info("Account Type: {}", bankAccount1.getClass().getSimpleName());

                if (bankAccount1 instanceof CurrentAccount currentAccount) {
                    log.info("Over Draft => {}", currentAccount.getOverDraft());
                } else if (bankAccount1 instanceof SavingAccount savingAccount) {
                    log.info("Rate => {}", savingAccount.getInterestRate());
                }

                bankAccount1.getAccountOperations().forEach(op ->
                        log.info("Operation: {} \t {} \t {}", 
                                op.getType(), op.getOperationDate(), op.getAmount())
                );
            }
        };
    }

    //@Bean
    CommandLineRunner start(CustomerRepository customerRepository,
                            BankAccountRepository bankAccountRepository,
                            AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("Hassan", "Yassine", "Aicha").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customerRepository.save(customer);
                log.info("Customer created: {}", name);
            });

            customerRepository.findAll().forEach(cust -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setBalance(Math.random() * 90000);
                currentAccount.setCreatedAt(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setCustomer(cust);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);
                log.info("Current account created for {}", cust.getName());

                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setBalance(Math.random() * 90000);
                savingAccount.setCreatedAt(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setCustomer(cust);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
                log.info("Saving account created for {}", cust.getName());
            });

            bankAccountRepository.findAll().forEach(acc -> {
                for (int i = 0; i < 10; i++) {
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }
                log.info("10 operations added for account {}", acc.getId());
            });
        };
    }
}

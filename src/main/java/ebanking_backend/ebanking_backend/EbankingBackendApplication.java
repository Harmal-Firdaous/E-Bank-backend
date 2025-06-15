package ebanking_backend.ebanking_backend;

import ebanking_backend.ebanking_backend.entities.*;
import ebanking_backend.ebanking_backend.enums.AccountStatus;
import ebanking_backend.ebanking_backend.enums.OperationType;
import ebanking_backend.ebanking_backend.repositories.AccountOperationRepository;
import ebanking_backend.ebanking_backend.repositories.BankAccountRepository;
import ebanking_backend.ebanking_backend.repositories.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.sql.SQLOutput;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}
	@Bean
	CommandLineRunner commandLineRunner(BankAccountRepository bankAccountRepository) {
		return args -> {
			BankAccount bankAccount1=bankAccountRepository.findById("21b4cab6-f844-43ab-85ba-e40f49ef28bc").orElse(null);
			if(bankAccount1!=null){

				System.out.println(bankAccount1.getId());
				System.out.println(bankAccount1.getBalance());
				System.out.println(bankAccount1.getStatus());
				System.out.println(bankAccount1.getCreatedAt());
				System.out.println(bankAccount1.getCustomer().getName());
				System.out.println(bankAccount1.getClass().getSimpleName());
				if(bankAccount1 instanceof CurrentAccount){
					System.out.println("Over Draft => "+((CurrentAccount) bankAccount1).getOverDraft());
				} else if(bankAccount1 instanceof SavingAccount){
					System.out.println("Rate => " + ((SavingAccount) bankAccount1).getInterestRate() );
				}
			}
			bankAccount1.getAccountOperations().forEach(op -> {
				System.out.println(op.getType() + "\t" + op.getOperationDate() + "\t" + op.getAmount());
			});
		};
	}
	//@Bean
	CommandLineRunner start(CustomerRepository customerRepository,
							BankAccountRepository bankAccountRepository,
							AccountOperationRepository accountOperationRepository){
		return args -> {
			Stream.of("Hassan","Yassine","Aicha").forEach(name->{
				Customer customer=new Customer();
				customer.setName(name);
				customer.setEmail(name+"@gmail.com");
				customerRepository.save(customer);
			});
			customerRepository.findAll().forEach(cust->{
				CurrentAccount currentAccount=new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random()*90000);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setStatus(AccountStatus.CREATED);
				currentAccount.setCustomer(cust);
				currentAccount.setOverDraft(9000);
				bankAccountRepository.save(currentAccount);

				SavingAccount savingAccount=new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random()*90000);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setStatus(AccountStatus.CREATED);
				savingAccount.setCustomer(cust);
				savingAccount.setInterestRate(5.5);
				bankAccountRepository.save(savingAccount);

			});
			bankAccountRepository.findAll().forEach(acc->{
				for (int i = 0; i <10 ; i++) {
					AccountOperation accountOperation=new AccountOperation();
					accountOperation.setOperationDate(new Date());
					accountOperation.setAmount(Math.random()*12000);
					accountOperation.setType(Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT);
					accountOperation.setBankAccount(acc);
					accountOperationRepository.save(accountOperation);
				}

			});
		};

	}
}

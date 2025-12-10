package ebanking_backend.ebanking_backend.repositories;

import ebanking_backend.ebanking_backend.entities.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
   }

package ebanking_backend.ebanking_backend.repositories;

import ebanking_backend.ebanking_backend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByNameContaining(String keyword);

}

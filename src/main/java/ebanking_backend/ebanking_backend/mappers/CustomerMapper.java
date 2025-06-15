package ebanking_backend.ebanking_backend.mappers;

import ebanking_backend.ebanking_backend.dtos.CustomerDto;
import ebanking_backend.ebanking_backend.entities.Customer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapper {

    public CustomerDto fromCustomer(Customer customer) {

        CustomerDto customerDto = new CustomerDto();
        BeanUtils.copyProperties(customer, customerDto);

//        customerDto.setId(customer.getId());
//        customerDto.setName(customer.getName());
//        customerDto.setEmail(customer.getEmail());
        return customerDto;
    }

    public Customer fromCustomerDto(CustomerDto customerDto) {

        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDto, customer);

//        customer.setId(customerDto.getId());
//        customer.setName(customerDto.getName());
//        customer.setEmail(customerDto.getEmail());
        return customer;
    }
}
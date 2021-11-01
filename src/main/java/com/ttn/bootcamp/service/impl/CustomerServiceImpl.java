package com.ttn.bootcamp.service.impl;

import com.ttn.bootcamp.util.Utility;
import com.ttn.bootcamp.domains.User.Customer;
import com.ttn.bootcamp.domains.User.Role;
import com.ttn.bootcamp.dto.User.CustomerDto;
import com.ttn.bootcamp.enums.UserRole;
import com.ttn.bootcamp.exceptions.GenericException;
import com.ttn.bootcamp.repository.CustomerRepository;
import com.ttn.bootcamp.repository.RoleRepository;
import com.ttn.bootcamp.service.CustomerService;
import com.ttn.bootcamp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    CustomerRepository customerRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserService userService;

    @Override
    public CustomerDto registerUser(CustomerDto customerDto) throws GenericException {
        if (!customerDto.getPassword().equals(customerDto.getConfirmPassword())) {
            throw new GenericException("Confirm password didn't matched", HttpStatus.BAD_REQUEST);
        }
        // throws exception if email already registered
        userService.checkForEmailExist(customerDto.getEmail());

        Customer customer = customerDto.toCustomerEntity();
        Role role = roleRepository.findByAuthority("ROLE_" + UserRole.CUSTOMER);
        customer.setRoleList(Collections.singletonList(role));
        customer.setPassword(Utility.encrypt(customer.getPassword()));
        customerDto = customerRepository.save(customer).toCustomerDto();

        // send account activation link
        userService.accountActivationHandler(customer);

        return customerDto;
    }

    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }
}

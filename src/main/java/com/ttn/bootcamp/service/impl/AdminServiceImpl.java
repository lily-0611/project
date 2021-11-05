package com.ttn.bootcamp.service.impl;

import com.ttn.bootcamp.domains.User.Customer;
import com.ttn.bootcamp.domains.User.Seller;
import com.ttn.bootcamp.exceptions.GenericException;
import com.ttn.bootcamp.service.AdminService;
import com.ttn.bootcamp.service.CustomerService;
import com.ttn.bootcamp.service.SellerService;
import com.ttn.bootcamp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminServiceImpl implements AdminService {

    private CustomerService customerService;
    private SellerService sellerService;
    private UserService userService;

    @Autowired
    public AdminServiceImpl(CustomerService customerService, SellerService sellerService, UserService userService) {
        this.customerService = customerService;
        this.sellerService = sellerService;
        this.userService = userService;
    }

    @Override
    public List<Customer> getAllCustomers() throws GenericException {
        return customerService.findAllCustomers();
    }

    @Override
    public List<Seller> getAllSellers() throws GenericException {
        return sellerService.findAllSellers();
    }

    @Override
    public String activateUser(Map<String, String> request) throws GenericException {
        Set<Map.Entry<String, String>> entry = request.entrySet();
        long id = Long.parseLong(entry.stream().iterator().next().getValue());
        return userService.activateUserAccountByAdmin(id);
    }

    @Override
    public String deActivateUser(Map<String, String> request) throws GenericException {
        Set<Map.Entry<String, String>> entry = request.entrySet();
        long id = Long.parseLong(entry.stream().iterator().next().getValue());
        return userService.deActivateUserAccountByAdmin(id);
    }
}

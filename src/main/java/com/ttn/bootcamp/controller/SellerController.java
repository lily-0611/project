package com.ttn.bootcamp.controller;

import com.ttn.bootcamp.dto.User.SellerDto;
import com.ttn.bootcamp.exceptions.GenericException;
import com.ttn.bootcamp.security.AppUser;
import com.ttn.bootcamp.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/sellers")
public class SellerController {
    @Autowired
    SellerService sellerService;

    @PostMapping("/register")
    public ResponseEntity<Object> userRegistration(@Valid @RequestBody SellerDto sellerDto) throws GenericException {
        SellerDto user = sellerService.registerUser(sellerDto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(value = "/profile")
    @ResponseBody
    public ResponseEntity<Object> currentUserName(Authentication authentication) throws GenericException {
        SellerDto sellerDto = sellerService.getSellerProfile((AppUser)authentication.getPrincipal());
        return new ResponseEntity<>(sellerDto, HttpStatus.OK);
    }
}

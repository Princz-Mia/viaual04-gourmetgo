package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
import com.princz_mia.viaual04_gourmetgo_backend.config.logging.LoggingUtils;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Confirmation;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Customer;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CartRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.ConfirmationRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CredentialRepository;
import com.princz_mia.viaual04_gourmetgo_backend.data.repository.CustomerRepository;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ServiceException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CreateCustomerRequest;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UpdateCustomerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService implements ICustomerService {

    private final CustomerRepository customerRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher publisher;
    private final CartRepository cartRepository;

    @Override
    public Customer getCustomerById(UUID id) {
        LoggingUtils.logMethodEntry(log, "getCustomerById", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            log.debug("Fetching customer with ID: {}", id);
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new ServiceException("Customer not found with ID: " + id, ErrorType.RESOURCE_NOT_FOUND));
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_FETCHED", "customerId", id, "customerEmail", LoggingUtils.maskSensitiveData(customer.getEmailAddress()));
            LoggingUtils.logPerformance(log, "getCustomerById", System.currentTimeMillis() - startTime);
            
            return customer;
        } catch (ServiceException e) {
            LoggingUtils.logError(log, "Customer not found", e, "customerId", id);
            throw e;
        }
    }

    @Override
    public Customer createCustomer(CreateCustomerRequest request) {
        LoggingUtils.logMethodEntry(log, "createCustomer", "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Creating new customer with email: {}", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            
            return Optional.of(request)
                    .filter(customer -> !customerRepository.existsByEmailAddress(request.getEmailAddress()))
                    .map(req -> {
                        Customer customer = new Customer();
                        customer.setEmailAddress(request.getEmailAddress());
                        customer.setFullName(request.getFullName());
                        customer.setLoginAttempts(0);
                        customer.setLastLogin(null);
                        customer.setCreatedAt(LocalDateTime.now());
                        customer.setAccountNonLocked(true);
                        customer.setEnabled(false);
                        customer = customerRepository.save(customer);

                        log.debug("Customer entity created with ID: {}", customer.getId());

                        String encodedPassword = bCryptPasswordEncoder.encode(req.getPassword());
                        Credential credential = new Credential(customer, encodedPassword);
                        credentialRepository.save(credential);
                        log.debug("Customer credentials saved");

                        Confirmation confirmation = new Confirmation(customer);
                        confirmationRepository.save(confirmation);
                        log.debug("Customer confirmation created");

                        publisher.publishEvent(new UserEvent(customer, EventType.REGISTRATION, Map.of("key", confirmation.getKey())));
                        log.debug("Registration event published");

                        LoggingUtils.logBusinessEvent(log, "CUSTOMER_CREATED", "customerId", customer.getId(), "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
                        LoggingUtils.logPerformance(log, "createCustomer", System.currentTimeMillis() - startTime);

                        return customer;
                    }).orElseThrow(() -> {
                        LoggingUtils.logError(log, "Customer creation failed - email already exists", null, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
                        return new ServiceException("Customer already exists with email: " + request.getEmailAddress(), ErrorType.ALREADY_EXISTS);
                    });
        } catch (Exception e) {
            LoggingUtils.logError(log, "Error creating customer", e, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            throw e;
        }
    }

    @Override
    @Transactional
    public void verifyAccountKey(String key) {
        LoggingUtils.logMethodEntry(log, "verifyAccountKey", "key", key);
        long startTime = System.currentTimeMillis();
        
        try {
            Confirmation confirmation = getCustomerConfirmation(key);
            Customer customer = getCustomerByEmail(confirmation.getUser().getEmailAddress());

            Cart cart = new Cart();
            cart.setCustomer(customer);
            cartRepository.save(cart);

            customer.setCart(cart);
            customer.setEnabled(true);
            customerRepository.save(customer);

            confirmationRepository.delete(confirmation);
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_ACCOUNT_VERIFIED", "customerId", customer.getId(), "email", LoggingUtils.maskSensitiveData(customer.getEmailAddress()));
            LoggingUtils.logPerformance(log, "verifyAccountKey", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to verify account", e, "key", key);
            throw e;
        }
    }

    @Override
    public List<Customer> getAllEnabledAndNonLockedCustomer() {
        LoggingUtils.logMethodEntry(log, "getAllEnabledAndNonLockedCustomer");
        long startTime = System.currentTimeMillis();
        
        List<Customer> customers = new ArrayList<>(customerRepository.findByIsEnabledAndIsAccountNonLocked(true, true));
        LoggingUtils.logBusinessEvent(log, "ENABLED_CUSTOMERS_RETRIEVED", "count", customers.size());
        LoggingUtils.logPerformance(log, "getAllEnabledAndNonLockedCustomer", System.currentTimeMillis() - startTime);
        
        return customers;
    }

    private Confirmation getCustomerConfirmation(String key) {
        return Optional.ofNullable(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new ServiceException("Invalid confirmation key: " + key, ErrorType.RESOURCE_NOT_FOUND));
    }

    @Override
    public Customer getCustomerByEmail(String emailAddress) {
        LoggingUtils.logMethodEntry(log, "getCustomerByEmail", "email", LoggingUtils.maskSensitiveData(emailAddress));
        return Optional.ofNullable(customerRepository.findByEmailAddressIgnoreCase(emailAddress))
                .orElseThrow(() -> new ServiceException("Customer not found with email: " + emailAddress, ErrorType.RESOURCE_NOT_FOUND));
    }

    @Override
    public Customer updateCustomer(UpdateCustomerRequest request, UUID id) {
        LoggingUtils.logMethodEntry(log, "updateCustomer", "id", id, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
        long startTime = System.currentTimeMillis();
        
        try {
            Customer updatedCustomer = customerRepository.findById(id).map(existingCustomer -> {
                existingCustomer.setFullName(request.getFullName());
                existingCustomer.setPhoneNumber(request.getPhoneNumber());
                existingCustomer.setEmailAddress(request.getEmailAddress());
                return customerRepository.save(existingCustomer);
            }).orElseThrow(() -> new ServiceException("Customer not found with ID: " + id, ErrorType.RESOURCE_NOT_FOUND));
            
            LoggingUtils.logBusinessEvent(log, "CUSTOMER_UPDATED", "customerId", id, "email", LoggingUtils.maskSensitiveData(request.getEmailAddress()));
            LoggingUtils.logPerformance(log, "updateCustomer", System.currentTimeMillis() - startTime);
            
            return updatedCustomer;
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to update customer", e, "customerId", id);
            throw e;
        }
    }

    @Override
    public void deleteCustomer(UUID id) {
        LoggingUtils.logMethodEntry(log, "deleteCustomer", "id", id);
        long startTime = System.currentTimeMillis();
        
        try {
            customerRepository.findById(id).ifPresentOrElse(customer -> {
                customerRepository.delete(customer);
                LoggingUtils.logBusinessEvent(log, "CUSTOMER_DELETED", "customerId", id, "email", LoggingUtils.maskSensitiveData(customer.getEmailAddress()));
            }, () -> {
                throw new ServiceException("Customer not found with ID: " + id, ErrorType.RESOURCE_NOT_FOUND);
            });
            
            LoggingUtils.logPerformance(log, "deleteCustomer", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            LoggingUtils.logError(log, "Failed to delete customer", e, "customerId", id);
            throw e;
        }
    }

    @Override
    public CustomerDto convertCustomerToDto(Customer customer) {
        return modelMapper.map(customer, CustomerDto.class);
    }

    @Override
    public Customer getAuthenticatedCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailAddress = authentication.getName();
        return customerRepository.findByEmailAddress(emailAddress);
    }
}
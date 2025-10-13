package com.princz_mia.viaual04_gourmetgo_backend.business.service.impl;

import com.princz_mia.viaual04_gourmetgo_backend.business.service.ICustomerService;
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
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CreateCustomerRequest;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.CustomerDto;
import com.princz_mia.viaual04_gourmetgo_backend.web.dto.UpdateCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerService implements ICustomerService
{

    private final CustomerRepository customerRepository;
    private final CredentialRepository credentialRepository;
    private final ConfirmationRepository confirmationRepository;

    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher publisher;
    private final CartRepository cartRepository;

    @Override
    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Override
    public Customer createCustomer(CreateCustomerRequest request) {
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

                    String encodedPassword = bCryptPasswordEncoder.encode(req.getPassword());
                    Credential credential = new Credential(customer, encodedPassword);
                    credentialRepository.save(credential);

                    Confirmation confirmation = new Confirmation(customer);
                    confirmationRepository.save(confirmation);

                    publisher.publishEvent(new UserEvent(customer, EventType.REGISTRATION, Map.of("key", confirmation.getKey())));

                    return customer;
                }).orElseThrow(() -> new AlreadyExistsException("Customer already exists with email: " + request.getEmailAddress()));
    }

    @Override
    @Transactional
    public void verifyAccountKey(String key) {
        Confirmation confirmation = getCustomerConfirmation(key);
        Customer customer = getCustomerByEmail(confirmation.getUser().getEmailAddress());

        Cart cart = new Cart();
        cart.setCustomer(customer);
        cartRepository.save(cart);

        customer.setCart(cart);
        customer.setEnabled(true);
        customerRepository.save(customer);

        confirmationRepository.delete(confirmation);
    }

    @Override
    public List<Customer> getAllEnabledAndNonLockedCustomer() {
        return new ArrayList<>(customerRepository.findByIsEnabledAndIsAccountNonLocked(true, true));
    }

    private Confirmation getCustomerConfirmation(String key) {
        return Optional.ofNullable(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid confirmation key: " + key));
    }

    private Customer getCustomerByEmail(String emailAddress) {
        return Optional.ofNullable(customerRepository.findByEmailAddressIgnoreCase(emailAddress))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + emailAddress));
    }

    @Override
    public Customer updateCustomer(UpdateCustomerRequest request, UUID id) {
        return customerRepository.findById(id).map(existingCustomer -> {
            existingCustomer.setFullName(request.getFullName());
            existingCustomer.setPhoneNumber(request.getPhoneNumber());
            existingCustomer.setEmailAddress(request.getEmailAddress());
            return customerRepository.save(existingCustomer);
        }).orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    @Override
    public void deleteCustomer(UUID id) {
        customerRepository.findById(id).ifPresentOrElse(customerRepository::delete, () -> {
            throw new ResourceNotFoundException("Customer not found with ID: " + id);
        });
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
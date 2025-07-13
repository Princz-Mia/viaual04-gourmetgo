package com.princz_mia.viaual04_gourmetgo_backend.customer;

import com.princz_mia.viaual04_gourmetgo_backend.cart.Cart;
import com.princz_mia.viaual04_gourmetgo_backend.cart.CartRepository;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.Confirmation;
import com.princz_mia.viaual04_gourmetgo_backend.confirmation.ConfirmationRepository;
import com.princz_mia.viaual04_gourmetgo_backend.credential.Credential;
import com.princz_mia.viaual04_gourmetgo_backend.credential.CredentialRepository;
import com.princz_mia.viaual04_gourmetgo_backend.events.EventType;
import com.princz_mia.viaual04_gourmetgo_backend.events.UserEvent;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AlreadyExistsException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ResourceNotFoundException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found", HttpStatus.NOT_FOUND));
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
                }).orElseThrow(() -> new AlreadyExistsException("Customer already exists!", HttpStatus.BAD_REQUEST));
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
        return Optional.of(confirmationRepository.findByKey(key))
                .orElseThrow(() -> new AppException("Confirmation key was not found in database", HttpStatus.NOT_FOUND));
    }

    private Customer getCustomerByEmail(String emailAddress) {
        return Optional.of(customerRepository.findByEmailAddressIgnoreCase(emailAddress))
                .orElseThrow(() -> new AppException("Customer is not found with matching Email address", HttpStatus.NOT_FOUND));
    }

    @Override
    public Customer updateCustomer(UpdateCustomerRequest request, UUID id) {
        return customerRepository.findById(id).map(existingCustomer -> {
            existingCustomer.setFullName(request.getFullName());
            existingCustomer.setPhoneNumber(request.getPhoneNumber());
            existingCustomer.setEmailAddress(request.getEmailAddress());
            return customerRepository.save(existingCustomer);
        }).orElseThrow(() -> new ResourceNotFoundException("Customer not found!", HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteCustomer(UUID id) {
        customerRepository.findById(id).ifPresentOrElse(customerRepository::delete, () -> {
            throw new ResourceNotFoundException("Customer not found", HttpStatus.NOT_FOUND);
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

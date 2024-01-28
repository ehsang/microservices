package com.example.customer;


import com.example.amqp.RabbitMQMessageProducer;
import com.example.clients.fraud.FraudClient;
import com.example.clients.fraud.FraudulentCheckResponse;
import com.example.clients.notification.NotificationClient;
import com.example.clients.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final FraudClient fraudClient;

    //private final NotificationClient notificationClient;

    private final RabbitMQMessageProducer rabbitMQMessageProducer;


    public void registerCustomer(CustomerRegistrationRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .build();

        customerRepository.saveAndFlush(customer);

        FraudulentCheckResponse fraudResponse = fraudClient.isFraudster(customer.getId());

        if (fraudResponse.isFraudster()) {
            throw new IllegalArgumentException("fraudster");
        }

        NotificationRequest notificationRequest = new NotificationRequest(customer.getId(),
                customer.getEmail(),
                String.format("Hi %s, welcome to Amigoscode...",
                        customer.getFirstName()));

        rabbitMQMessageProducer.publish(
                notificationRequest,
                "internal.exchange",
                "internal.notification.routing-key"
        );

        /*notificationClient.sendNotification(
                notificationRequest
        );*/
    }
}




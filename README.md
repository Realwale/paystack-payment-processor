# Paystack Payment Processor

A Spring Boot application that integrates with Paystack to process payments (deposits and withdrawals).

## Features

- Deposit Processing
- Withdrawal Processing
- Webhook Integration
- Transaction Management
- Swagger Documentation

## Prerequisites

- Java 11 or higher
- Maven
- PostgreSQL
- Paystack Account and API Key

## Configuration

1. Update `application.properties` with your database credentials and Paystack API key
2. Create a PostgreSQL database named `paystack_processor`

## Building and Running

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, you can access the Swagger documentation at:
`http://localhost:8080/swagger-ui/`

## API Endpoints

### 1. Initiate Deposit
```http
POST /api/v1/payments/deposit
Content-Type: application/json

{
    "amount": 1000.00,
    "email": "customer@example.com",
    "currency": "NGN",
    "callbackUrl": "https://hello.pstk.xyz/callback"
}
```

### 2. Initiate Withdrawal
```http
POST /api/v1/payments/withdraw
Content-Type: application/json

{
    "amount": 1000.00,
    "email": "customer@example.com",
    "currency": "NGN"
}
```

### 3. Webhook Endpoint
```http
POST /api/v1/payments/webhook
```

## Security Considerations

- The Paystack API key is stored in application.properties. In a production environment, use environment variables or a secure configuration management system.
- Implement proper authentication and authorization for the API endpoints.
- Validate webhook signatures from Paystack to ensure the requests are genuine.

## Error Handling

The application includes basic error handling for:
- Invalid requests
- Failed transactions
- API communication errors

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

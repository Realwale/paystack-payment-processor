# Paystack Payment Processor

A robust Java/Spring Boot application that integrates with Paystack's payment gateway to handle deposits and withdrawals.

## Features

- Deposit initialization and verification
- Withdrawal (transfer) processing
- Transfer recipient management
- Transaction tracking
- Bank listing
- Mock service for testing/development

## Prerequisites

- Java 17 or higher
- PostgreSQL database
- Maven
- Paystack Account (Test/Live)

## Setup

1. Clone the repository:
```bash
git clone https://github.com/Realwale/paystack-payment-processor.git
```

2. Configure database in `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/paystack_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. Add Paystack configuration in `application.properties`:
```properties
paystack.api.key=your_paystack_secret_key
paystack.use-mock-service=false  # Set to true for development
```

4. Run the application:
```bash
mvn spring-boot:run
```

## API Documentation

The API documentation is available via Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Key Endpoints

#### 1. Deposit
```http
POST /api/v1/payments/deposit/initialize
```
Request body:
```json
{
    "amount": 500.00,        
    "email": "customer@example.com",
    "currency": "NGN",
    "callbackUrl": "https://your-callback-url.com"
}
```

#### 2. Transfer Recipient Creation
```http
POST /api/v1/payments/recipients
```
Request body:
```json
{
    "type": "nuban",
    "name": "John Doe",
    "account_number": "0123456789",
    "bank_code": "058",
    "currency": "NGN"
}
```

#### 3. Withdrawal
```http
POST /api/v1/payments/withdrawal
```
Request body:
```json
{
    "amount": 500.00,        
    "recipient": "RCP_xxxxxxxxxxxx",
    "reason": "Withdrawal request",
    "email": "customer@example.com"
}
```

## Database Schema

### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    email VARCHAR(255) NOT NULL,
    recipient_code VARCHAR(255),
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Development Mode

The application includes a mock service for development and testing. To enable it:

1. Set in `application.properties`:
```properties
paystack.use-mock-service=true
```

2. Mock service will:
- Simulate successful transactions
- Not require Paystack account
- Not make actual API calls
- Return predictable responses

## Testing

Test the application using provided unit tests:
```bash
mvn test
```

## Error Handling

The application handles various scenarios:
- Invalid requests
- Failed transactions
- API timeouts
- Database constraints
- Paystack API errors

## Security Considerations

- API keys are stored in configuration, not in code
- Input validation on all endpoints
- Database transaction management
- Error message sanitization

## Known Limitations

1. Paystack Account Requirements:
    - Starter business accounts cannot initiate transfers
    - Business verification needed for full functionality

2. Currency Support:
    - Currently supports NGN only
    - Amount handling in Naira (converted to kobo for Paystack API)

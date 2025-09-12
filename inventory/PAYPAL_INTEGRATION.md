# PayPal Integration

This document describes the PayPal REST client implementation for the TechVVS Inventory system.

## Overview

The PayPal integration has been refactored to use Spring's RestTemplate instead of the PayPal SDK, making it more compatible with the existing codebase and easier to maintain.

## Components

### 1. PaypalClientConfig.java
- Configuration class that sets up the RestTemplate for PayPal API calls
- Configures timeouts and creates the PaypalRestClient bean
- Uses environment variables to determine sandbox vs production endpoints

### 2. PaypalRestClient.java
- Core REST client that handles all PayPal API communication
- Manages OAuth token authentication
- Provides methods for creating orders, capturing payments, and retrieving order details
- Includes all necessary DTOs for PayPal API requests and responses

### 3. PaypalCheckoutService.java
- Service layer that integrates with the existing CartVO model
- Converts cart data to PayPal order format
- Handles order creation, capture, and approval URL extraction
- Includes proper error handling and logging

### 4. PaypalController.java
- REST controller that exposes PayPal functionality via HTTP endpoints
- Integrates with existing CartRepo for cart management
- Provides endpoints for order creation, capture, and return/cancel handling
- Includes comprehensive validation and error handling

## Configuration

Add the following properties to your `application.properties` or `application.yml`:

```properties
# PayPal Configuration
paypal.clientId=your_paypal_client_id_here
paypal.clientSecret=your_paypal_client_secret_here
paypal.environment=SANDBOX
paypal.brandName=TechVVS Inventory
paypal.returnUrl=http://localhost:8080/api/paypal/return
paypal.cancelUrl=http://localhost:8080/api/paypal/cancel
```

## API Endpoints

### Create PayPal Order
```
POST /api/paypal/orders/{cartId}
```
Creates a PayPal order from an existing cart. Returns order ID and approval URL.

### Capture Payment
```
GET /api/paypal/capture?token={orderId}
```
Captures a PayPal payment after user approval.

### Get Order Details
```
GET /api/paypal/orders/{orderId}
```
Retrieves details of a PayPal order.

### Handle Return
```
GET /api/paypal/return?cartId={cartId}&token={orderId}
```
Handles user return from PayPal after payment approval.

### Handle Cancel
```
GET /api/paypal/cancel?cartId={cartId}
```
Handles user cancellation of PayPal payment.

## Usage Example

1. **Create a cart** with products using the existing cart functionality
2. **Create PayPal order**:
   ```bash
   POST /api/paypal/orders/123
   ```
3. **Redirect user** to the returned `approveUrl`
4. **Handle return** when user completes payment on PayPal
5. **Capture payment** automatically or manually as needed

## Error Handling

The implementation includes comprehensive error handling:
- Invalid cart validation
- PayPal API error responses
- Network timeouts
- Authentication failures
- Proper HTTP status codes and error messages

## Security

- OAuth token management with automatic refresh
- Secure credential storage via Spring properties
- Input validation and sanitization
- Proper error message handling (no sensitive data exposure)

## Testing

For testing, use PayPal's sandbox environment:
- Set `paypal.environment=SANDBOX`
- Use sandbox PayPal accounts for testing
- Test both successful and failed payment scenarios

## Production Deployment

For production:
1. Set `paypal.environment=PRODUCTION`
2. Update return and cancel URLs to your production domain
3. Use production PayPal API credentials
4. Ensure proper SSL/TLS configuration
5. Monitor PayPal webhook notifications for additional security












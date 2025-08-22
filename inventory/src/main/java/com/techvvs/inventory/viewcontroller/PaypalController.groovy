package com.techvvs.inventory.viewcontroller;

import com.techvvs.inventory.jparepo.CartRepo;
import com.techvvs.inventory.model.CartVO
import com.techvvs.inventory.service.paypal.PaypalCheckoutService;
import com.techvvs.inventory.service.paypal.PaypalRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/paypal")
public class PaypalController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaypalController.class);
    
    @Autowired
    PaypalCheckoutService paypalCheckoutService;
    
    @Autowired
    CartRepo cartRepo;


    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestParam Integer cartId, @RequestParam String shoppingtoken) {
        try {
            logger.info("Creating PayPal order for cart ID: {}", cartId);

            Optional<CartVO> cartOptional = cartRepo.findById(cartId);
            if (cartOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart not found with ID: " + cartId);
            }

            CartVO cart = cartOptional.get();

            // Validate cart has items
            if (cart.product_cart_list == null || cart.product_cart_list.isEmpty()) {
                return ResponseEntity.badRequest().body("Cart is empty");
            }

            // Validate cart has a total
            if (cart.total == null || cart.total <= 0) {
                return ResponseEntity.badRequest().body("Cart total is invalid");
            }

            PaypalRestClient.PaypalOrderResponse order = paypalCheckoutService.createOrderFromCart(cart);
            Optional<String> approveUrl = paypalCheckoutService.findApproveUrl(order);

            if (approveUrl.isEmpty()) {
                return ResponseEntity.badRequest().body("Failed to get PayPal approval URL");
            }

            CreateOrderResponse response = new CreateOrderResponse(order.id, approveUrl.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating PayPal order for cart {}: {}", cartId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error creating PayPal order: " + e.getMessage());
        }
    }
    
    @GetMapping("/capture")
    public ResponseEntity<?> captureOrder(@RequestParam("token") String orderId) {
        try {
            logger.info("Capturing PayPal order: {}", orderId);
            
            PaypalRestClient.PaypalOrderResponse capture = paypalCheckoutService.captureOrder(orderId);
            return ResponseEntity.ok(capture);
            
        } catch (Exception e) {
            logger.error("Error capturing PayPal order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error capturing PayPal order: " + e.getMessage());
        }
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        try {
            logger.info("Getting PayPal order: {}", orderId);
            
            PaypalRestClient.PaypalOrderResponse order = paypalCheckoutService.getOrder(orderId);
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            logger.error("Error getting PayPal order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error getting PayPal order: " + e.getMessage());
        }
    }
    
    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(@RequestParam("cartId") Integer cartId,
                                        @RequestParam("token") String orderId) {
        try {
            logger.info("Handling PayPal return for cart {} with order {}", cartId, orderId);
            
            // Get the order details
            PaypalRestClient.PaypalOrderResponse order = paypalCheckoutService.getOrder(orderId);
            
            // Check if order is approved
            if (!"APPROVED".equalsIgnoreCase(order.status)) {
                return ResponseEntity.badRequest().body("Order is not approved. Status: " + order.status);
            }
            
            // Capture the payment
            PaypalRestClient.PaypalOrderResponse capture = paypalCheckoutService.captureOrder(orderId);
            
            return ResponseEntity.ok(capture);
            
        } catch (Exception e) {
            logger.error("Error handling PayPal return for cart {}: {}", cartId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error handling PayPal return: " + e.getMessage());
        }
    }
    
    @GetMapping("/cancel")
    public ResponseEntity<?> handleCancel(@RequestParam("cartId") Integer cartId) {
        logger.info("PayPal payment cancelled for cart: {}", cartId);
        return ResponseEntity.ok("Payment cancelled");
    }

    /**
     * JS SDK calls this to create an Order from the server-side cart total.
     * Returns just the PayPal order ID (the JS SDK needs this).
     */
//    @PostMapping("/orders")
//    ResponseEntity<Map<String, String>> create(@RequestParam String cartId) {
//        CartVO cart = cartRepo.findById(Integer.valueOf(cartId)).get() // load latest totals server-side
//        def order = paypalCheckoutService.createOrderFromCart(cart)  // returns PaypalOrderResponse
//        return ResponseEntity.ok([orderId: order.id as String])
//    }

    /**
     * JS SDK calls this after onApprove to capture the payment.
     */
    @PostMapping("/orders/{orderId}/capture")
    ResponseEntity<?> capture(@PathVariable String orderId) {
        def result = paypalCheckoutService.captureOrder(orderId)
        // TODO: check result.status == "COMPLETED", update your DB, etc.
        return ResponseEntity.ok(result)
    }


    // Response DTO
    public static class CreateOrderResponse {
        private String orderId;
        private String approveUrl;
        
        public CreateOrderResponse(String orderId, String approveUrl) {
            this.orderId = orderId;
            this.approveUrl = approveUrl;
        }
        
        public String getOrderId() {
            return orderId;
        }
        
        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
        
        public String getApproveUrl() {
            return approveUrl;
        }
        
        public void setApproveUrl(String approveUrl) {
            this.approveUrl = approveUrl;
        }
    }
}

package com.techvvs.inventory.service.paypal;

import com.techvvs.inventory.model.CartVO;
import com.techvvs.inventory.model.ProductVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PaypalCheckoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaypalCheckoutService.class);
    
    @Autowired
    private PaypalRestClient paypalRestClient;
    
    @Value('${paypal.returnUrl:/payment/paypal/return}')
    private String returnUrl;
    
    @Value('${paypal.cancelUrl:/payment/paypal/cancel}')
    private String cancelUrl;
    
    @Value('${paypal.brandName:TechVVS Inventory}')
    private String brandName;

    @Value('${base.qr.domain:http://localhost:8080}')
    private String baseuri;

    
    public PaypalRestClient.PaypalOrderResponse createOrderFromCart(CartVO cart) throws Exception {
        logger.info("Creating PayPal order for cart ID: {}", cart.cartid);
        
        // Build purchase unit amount from cart
        PaypalRestClient.PaypalAmount amount = new PaypalRestClient.PaypalAmount();
        amount.currencyCode = "USD";
        amount.value = formatAmount(cart.total != null ? cart.total : 0.0);

        // Build items from cart products
        List<PaypalRestClient.PaypalItem> items = new ArrayList<>();
        for (ProductVO p : cart.product_cart_list) {
            PaypalRestClient.PaypalMoney money = new PaypalRestClient.PaypalMoney();
            money.currencyCode = "USD";
            money.value = formatAmount(p.price != null ? p.price : 0.0);

            PaypalRestClient.PaypalItem it = new PaypalRestClient.PaypalItem();
            it.name = (p.name != null ? p.name : "Product");
            it.quantity = String.valueOf(p.quantity != null ? p.quantity : 1);
            it.unitAmount = money;
            it.category = "PHYSICAL_GOODS"; // optional
            items.add(it);
        }
        
        // Build purchase unit
        PaypalRestClient.PaypalPurchaseUnit pu = new PaypalRestClient.PaypalPurchaseUnit();
        pu.referenceId = "CART-" + cart.cartid;
        pu.amount = amount;
        pu.items = items.toArray(new PaypalRestClient.PaypalItem[0]);

        // Build application context
        PaypalRestClient.PaypalApplicationContext appContext = new PaypalRestClient.PaypalApplicationContext();
        appContext.brandName = brandName;
        appContext.returnUrl = baseuri+returnUrl + "?cartId=" + cart.cartid;
        appContext.cancelUrl = baseuri+cancelUrl + "?cartId=" + cart.cartid;
        appContext.userAction = "PAY_NOW";
        
        // Build order request
        PaypalRestClient.PaypalOrderRequest reqBody = new PaypalRestClient.PaypalOrderRequest();
        reqBody.intent = "CAPTURE";
        reqBody.purchaseUnits = [pu] as PaypalRestClient.PaypalPurchaseUnit[]
        reqBody.applicationContext = appContext;
        return paypalRestClient.createOrder(reqBody);
    }
    
    public PaypalRestClient.PaypalOrderResponse captureOrder(String orderId) throws Exception {
        logger.info("Capturing PayPal order: {}", orderId);
        return paypalRestClient.captureOrder(orderId);
    }
    
    public PaypalRestClient.PaypalOrderResponse getOrder(String orderId) throws Exception {
        logger.info("Getting PayPal order: {}", orderId);
        return paypalRestClient.getOrder(orderId);
    }
    
    public Optional<String> findApproveUrl(PaypalRestClient.PaypalOrderResponse order) {
        if (order.links == null) {
            return Optional.empty();
        }
        
        for (PaypalRestClient.PaypalLink link : order.links) {
            if ("approve".equalsIgnoreCase(link.rel) || "payer-action".equalsIgnoreCase(link.rel)) {
                return Optional.of(link.href);
            }
        }
        
        return Optional.empty();
    }
    
    private String formatAmount(Double amount) {
        if (amount == null) {
            return "0.00";
        }
        return BigDecimal.valueOf(amount)
            .setScale(2, RoundingMode.HALF_UP)
            .toString();
    }
}

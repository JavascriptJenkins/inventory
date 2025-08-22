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
        logger.info("Creating PayPal order for cart ID: {}", cart.cartid)

        def currency = "USD"

        // ------ Build items & compute totals ------
        BigDecimal itemsTotal = 0G
        List<PaypalRestClient.PaypalItem> items = []

        cart?.product_cart_list?.each { ProductVO p ->
            BigDecimal unit = BigDecimal.valueOf(p?.price ?: 0.0).setScale(2, RoundingMode.HALF_UP)
            int qty = (p?.quantity ?: 1) as int
            itemsTotal = itemsTotal.add(unit.multiply(BigDecimal.valueOf(qty)))

            def unitMoney = new PaypalRestClient.PaypalMoney()
            unitMoney.currencyCode = currency
            unitMoney.value = unit.toPlainString()

            def it = new PaypalRestClient.PaypalItem()
            it.name = p?.name ?: "Product"
            it.quantity = String.valueOf(qty)           // PayPal expects a string
            it.unitAmount = unitMoney                   // { currency_code, value }
            it.category = "PHYSICAL_GOODS"              // optional
            items << it
        }

        // ------ Amount + breakdown ------
        def amount = new PaypalRestClient.PaypalAmount()
        amount.currencyCode = currency
        // If you don't have shipping/tax/discount fields yet, make total == items_total:
        amount.value = itemsTotal.setScale(2, RoundingMode.HALF_UP).toPlainString()

        def itemTotalMoney = new PaypalRestClient.PaypalMoney()
        itemTotalMoney.currencyCode = currency
        itemTotalMoney.value = amount.value

        def breakdown = new PaypalRestClient.PaypalAmountBreakdown()
        breakdown.itemTotal = itemTotalMoney
        // If/when you have these numbers, uncomment & set:
        // breakdown.shipping = money(currency, new BigDecimal("4.00"))
        // breakdown.taxTotal = money(currency, new BigDecimal("0.00"))
        // breakdown.discount = money(currency, new BigDecimal("0.00"))

        amount.breakdown = breakdown

        // ------ Purchase unit ------
        def pu = new PaypalRestClient.PaypalPurchaseUnit()
        pu.referenceId = "CART-${cart.cartid}"
        pu.amount = amount
        pu.items = (items ?: []) as PaypalRestClient.PaypalItem[]

        // ------ Application context ------
        def appContext = new PaypalRestClient.PaypalApplicationContext()
        appContext.brandName = brandName
        appContext.returnUrl = "${baseuri}${returnUrl}?cartId=${cart.cartid}"
        appContext.cancelUrl = "${baseuri}${cancelUrl}?cartId=${cart.cartid}"
        appContext.userAction = "PAY_NOW"

        // ------ Final request ------
        def reqBody = new PaypalRestClient.PaypalOrderRequest()
        reqBody.intent = "CAPTURE"
        reqBody.purchaseUnits = [pu] as PaypalRestClient.PaypalPurchaseUnit[]
        reqBody.applicationContext = appContext

        return paypalRestClient.createOrder(reqBody)
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

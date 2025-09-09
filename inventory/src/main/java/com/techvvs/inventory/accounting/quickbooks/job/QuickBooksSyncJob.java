package com.techvvs.inventory.accounting.quickbooks.job;

import com.techvvs.inventory.model.*;
import com.techvvs.inventory.service.LoggingService;
import com.techvvs.inventory.accounting.quickbooks.QuickbooksService;
import com.techvvs.inventory.jparepo.TransactionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Component
public class QuickBooksSyncJob {

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private QuickbooksService quickBooksService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepo transactionRepository;

    @Scheduled(fixedRate = 3600000) // Every hour
    public void syncTransactionsToQuickBooks() {
        JobLog jobLog = null;
        
        try {
            // Start job logging
            jobLog = loggingService.startJob(
                "QuickBooks Transaction Sync",
                "Sync completed transactions to QuickBooks",
                null, // tenantId - could be dynamic
                "SCHEDULED_TASK"
            );

            // Get unsynced transactions
            List<TransactionVO> unsyncedTransactions = findUnsyncedTransactions();
            
            Integer processed = 0;
            Integer succeeded = 0;
            Integer failed = 0;

            for (TransactionVO transaction : unsyncedTransactions) {
                try {
                    // Sync single transaction
                    syncTransactionToQB(transaction, jobLog);
                    succeeded++;
                } catch (Exception e) {
                    failed++;
                    // Log individual transaction error but continue processing
                    String currentError = jobLog.getErrorMessage();
                    jobLog.setErrorMessage((currentError != null ? currentError : "") + 
                        "\nFailed transaction " + transaction.getTransactionid() + ": " + e.getMessage());
                }
                
                processed++;
                
                // Update progress every 10 records
                if (processed % 10 == 0) {
                    loggingService.updateJobProgress(jobLog, processed, succeeded, failed);
                }
            }

            // Final update
            loggingService.updateJobProgress(jobLog, processed, succeeded, failed);

            if (failed == 0) {
                loggingService.completeJob(jobLog);
            } else {
                jobLog.markAsPartiallyCompleted("Some transactions failed to sync");
                // Note: You'll need to inject jobLogRepository or use loggingService to save
                // loggingService.jobLogRepository.save(jobLog);
            }

        } catch (Exception e) {
            // Job completely failed
            if (jobLog != null) {
                loggingService.failJob(jobLog, e.getMessage(), getStackTraceString(e));
            }
            throw e;
        }
    }

    private void syncTransactionToQB(TransactionVO transaction, JobLog jobLog) {
        long startTime = System.currentTimeMillis();
        
        // Prepare QB invoice data
        Map<String, Object> invoiceData = prepareInvoiceData(transaction);
        String requestBody = convertToJson(invoiceData);
        String correlationId = UUID.randomUUID().toString();

        // Log the outgoing API request
        ApiRequestLog requestLog = loggingService.logApiRequest(
            "POST",
            "https://sandbox-quickbooks.api.intuit.com/v3/company/123456/invoice",
            convertHeadersToJson(getQuickBooksHeaders()),
            requestBody,
            "QuickBooks",
            "createInvoice",
            null, // tenantId - not available in CustomerVO, could be added later
            jobLog,
            correlationId
        );

        try {
            // Make the actual API call
            // Note: You'll need to implement this method in QuickbooksService
            Object response = quickBooksService.createInvoice(invoiceData, correlationId);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log the API response
            loggingService.logApiResponse(
                requestLog,
                200, // status code
                "OK",
                convertHeadersToJson(new HashMap<>()), // You'll need to extract actual headers from response
                convertToJson(response),
                responseTime,
                null
            );

            // Update transaction with QB invoice ID
            // Note: You'll need to implement this based on your response structure
            // For now, we'll set a placeholder invoice ID
            transaction.setQuickbooksInvoiceId("QB-" + System.currentTimeMillis());
            transactionRepository.save(transaction);

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Log the failed API response
            loggingService.logApiResponse(
                requestLog,
                500, // or extract actual status from exception
                "Internal Server Error",
                null,
                e.getMessage(),
                responseTime,
                "QuickBooks API call failed: " + e.getMessage()
            );
            
            throw e;
        }
    }

    private Map<String, Object> prepareInvoiceData(TransactionVO transaction) {
        Map<String, Object> invoiceData = new HashMap<>();
        invoiceData.put("Name", "Invoice-" + transaction.getTransactionid());
        
        // Create line items from the transaction's product list
        List<Map<String, Object>> lineItems = new ArrayList<>();
        
        if (transaction.getProduct_list() != null) {
            for (ProductVO product : transaction.getProduct_list()) {
                Map<String, Object> lineItem = new HashMap<>();
                lineItem.put("Amount", product.getSalePrice() != null ? product.getSalePrice() : 0.0);
                lineItem.put("DetailType", "SalesItemLineDetail");
                
                Map<String, Object> salesItemLineDetail = new HashMap<>();
                Map<String, Object> itemRef = new HashMap<>();
                itemRef.put("value", product.getQuickbooksItemId() != null ? product.getQuickbooksItemId() : "1");
                salesItemLineDetail.put("ItemRef", itemRef);
                salesItemLineDetail.put("Qty", product.getQuantity() != null ? product.getQuantity() : 1);
                lineItem.put("SalesItemLineDetail", salesItemLineDetail);
                
                lineItems.add(lineItem);
            }
        }
        
        invoiceData.put("Line", lineItems);
        
        // Customer reference
        Map<String, Object> customerRef = new HashMap<>();
        customerRef.put("value", transaction.getCustomervo() != null ? 
            transaction.getCustomervo().getQuickbooksId() : "1");
        invoiceData.put("CustomerRef", customerRef);
        
        return invoiceData;
    }

    private List<TransactionVO> findUnsyncedTransactions() {
        // Find processed transactions that haven't been synced to QuickBooks yet
        // Note: This assumes you'll add a quickbooksInvoiceId field to TransactionVO
        // For now, we'll find all processed transactions
        return transactionRepository.findAll().stream()
            .filter(t -> t.getIsprocessed() != null && t.getIsprocessed() == 1)
            .filter(t -> t.getQuickbooksInvoiceId() == null) // This field needs to be added to TransactionVO
            .collect(java.util.stream.Collectors.toList());
    }

    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String convertHeadersToJson(Map<String, String> headers) {
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, String> getQuickBooksHeaders() {
        Map<String, String> headers = new HashMap<>();
        // Note: You'll need to get the actual access token from your QuickBooks config
        headers.put("Authorization", "Bearer " + getAccessToken());
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String getAccessToken() {
        // Note: You'll need to implement this to get the actual access token
        // from your QuickBooks configuration service
        return "your-access-token-here";
    }

    private String getStackTraceString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}

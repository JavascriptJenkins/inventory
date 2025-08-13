//package com.techvvs.inventory.batch.processor;
//
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.techvvs.inventory.metrc.constants.MetrcCallEnum;
//import com.techvvs.inventory.metrc.map.MetrcUriMapping;
//import com.techvvs.inventory.metrc.model.MetrcProductDto;
//import com.techvvs.inventory.metrc.model.MetrcTransferDto;
//import com.techvvs.inventory.metrc.service.MetrcService;
//import com.techvvs.inventory.model.batch.OutboundSubmissionVO;
//import com.techvvs.inventory.model.nonpersist.RequestMetaData;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//
//@Component
//public class OutboundProcessor implements ItemProcessor<OutboundSubmissionVO, OutboundSubmissionVO> {
//
//    @Autowired
//    private MetrcService metrcService;
//
//    @Autowired
//    private MetrcUriMapping metrcUriMapping;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Override
//    public OutboundSubmissionVO process(OutboundSubmissionVO submission) {
//        try {
//            MetrcCallEnum callType = MetrcCallEnum.valueOf(submission.getType());
//            RequestMetaData meta = metrcUriMapping.getMetadata(callType);
//
//            if (!"POST".equalsIgnoreCase(meta.getHttpMethod()) && !"PUT".equalsIgnoreCase(meta.getHttpMethod())) {
//                throw new IllegalArgumentException("OutboundProcessor only supports POST and PUT calls: " + callType);
//            }
//
//            String fullUrl = meta.getBaseUri() + meta.getUri();
//            submission.setUri(fullUrl);
//            submission.setHttpmethod(meta.getHttpMethod());
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            submission.setRequestheaders(headers.toSingleValueMap().toString());
//
//            // Serialize outgoing DTO for logging
//            if (submission.getOutgoingDTO() != null) {
//                String payloadJson = objectMapper.writeValueAsString(submission.getOutgoingDTO());
//                submission.setRequestbody(payloadJson);
//                submission.setPayload(payloadJson); // optional logging
//            }
//
//            // Call appropriate method using DTO
//            ResponseEntity<String> response = switch (callType) {
//                case CREATE_TRANSFER -> metrcService.createProduct((MetrcProductDto) submission.getOutgoingDTO());
//                case CREATE_TRANSFER -> metrcService.transferPackage((MetrcTransferDto) submission.getOutgoingDTO());
////                case CREATE_SALE -> metrcService.createSale((SaleDto) submission.getOutgoingDTO());
//                // Add more POST/PUT operations here
//                default -> throw new UnsupportedOperationException("Unsupported POST/PUT METRC call: " + callType);
//            };
//
//            submission.setResponsebody(response.getBody());
//            submission.setResponseheaders(response.getHeaders().toString());
//            submission.setStatuscode(String.valueOf(response.getStatusCodeValue()));
//            submission.setSubmitted(1);
//
//        } catch (Exception ex) {
//            submission.setSubmitted(0);
//            submission.setStatuscode("ERROR");
//            submission.setResponsebody(ex.getMessage());
//            submission.setResponseheaders("");
//        }
//
//        submission.setAttempts(submission.getAttempts() + 1);
//        submission.setUpdateTimeStamp(LocalDateTime.now());
//        return submission;
//    }
//}

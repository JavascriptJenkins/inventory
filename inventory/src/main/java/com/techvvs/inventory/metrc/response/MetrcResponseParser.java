package com.techvvs.inventory.metrc.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MetrcResponseParser {

    @Autowired
    private ObjectMapper objectMapper;

    public <T> T parse(String responseBody, Class<T> responseType) throws IOException {
        return objectMapper.readValue(responseBody, responseType);
    }

    // example of how to use the class
//    TransferResponseDto parsed = metrcResponseParser.parse(submission.getResponsebody(), TransferResponseDto.class);

}

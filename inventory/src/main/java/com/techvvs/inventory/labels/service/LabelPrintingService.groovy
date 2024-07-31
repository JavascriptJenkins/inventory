package com.techvvs.inventory.labels.service

import com.techvvs.inventory.labels.impl.LabelPrintingGenerator
import com.techvvs.inventory.model.BatchVO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class LabelPrintingService {

    @Autowired
    LabelPrintingGenerator labelPrintingGenerator


    void generate50StaticWeightLabels(BatchVO batchVO) {

        try {

            labelPrintingGenerator.generate50StaticWeightLabels();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }







}

package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.barcode.BarcodeGenerator;
import com.techvvs.inventory.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    BarcodeGenerator barcodeGenerator;

    @Autowired
    SimpleCache simpleCache;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");
        System.out.println("------- TechVVS Custom Cache Init ------");

        try {
            barcodeGenerator.generateBarcodes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        simpleCache.refreshCache();
    }

}

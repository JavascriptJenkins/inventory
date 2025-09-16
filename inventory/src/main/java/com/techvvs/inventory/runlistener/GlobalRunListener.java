package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.metrc.service.MetrcService;
import com.techvvs.inventory.model.ProductVO;
import com.techvvs.inventory.refdata.RefDataLoader;
import com.techvvs.inventory.service.webscrape.BrandStreetTokyoPoshmarkUpdater;
import com.techvvs.inventory.service.webscrape.BrandStreetTokyoScraperService;
import com.techvvs.inventory.util.SimpleCache;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    SimpleCache simpleCache;

    @Autowired
    RefDataLoader refDataLoader;

    @Autowired
    Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");


        // when the app starts up, perform this logic

        // check the database to see if the tenant.referenceDataFlag is 0 or 1

        // if the flag is 0, that means we need to load reference data for the app

        // use the tenant.referenceDataKey and load the reference data into the database

        // Load reference data based on configuration
        String loadRefData = environment.getProperty("load.ref.data", "no");
        if("yes".equals(loadRefData)){
            refDataLoader.loadRefData();
        }

        simpleCache.refreshCache();
    }



}

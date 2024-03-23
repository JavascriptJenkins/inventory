package com.techvvs.inventory.runlistener;

import com.techvvs.inventory.util.SimpleCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class GlobalRunListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    SimpleCache simpleCache;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("----- TechVVS Application has started ----");
        System.out.println("------- TechVVS Custom Cache Init ------");

        simpleCache.refreshCache();
    }

}

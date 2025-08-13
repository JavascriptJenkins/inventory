//package com.techvvs.inventory.batch.scheduler;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//public class OutboundJobScheduler {
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Autowired
//    private Job outboundSubmissionJob;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
////    @Scheduled(fixedRate = 60000)
//    @Scheduled(fixedRate = 10000)
//    public void checkAndRun() throws Exception {
//        Long count = jdbcTemplate.queryForObject(
//                "SELECT COUNT(*) FROM outboundsubmission WHERE submitted = 0", Long.class);
//
//        if (count != null && count > 0) {
//            JobParameters params = new JobParametersBuilder()
//                    .addLong("runTime", System.currentTimeMillis())
//                    .toJobParameters();
//
//            jobLauncher.run(outboundSubmissionJob, params);
//        }
//    }
//}
//

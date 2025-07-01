// Ubicación: src/main/java/com/mysql/demo/Controller/BatchJobController.java

package com.batch.demo.Controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("importUserJob")
    private Job importUserJob;


    @PostMapping("/import-users-from-csv")
    public ResponseEntity<String> importCsvToDBJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(importUserJob, jobParameters);
            return ResponseEntity.ok("¡Job de importación CSV iniciado exitosamente!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al iniciar el job de importación: " + e.getMessage());
        }
    }
}
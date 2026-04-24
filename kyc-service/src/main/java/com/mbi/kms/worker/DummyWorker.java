package com.mbi.kms.worker;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.api.worker.JobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DummyWorker {

    private static final Logger log = LoggerFactory.getLogger(DummyWorker.class);

    @JobWorker(type = "dummy-task", autoComplete = true)
    public void handle(final JobClient client, final ActivatedJob job) {
        log.info("✅ Dummy Service Task executed. JobKey={}", job.getKey());
    }
}
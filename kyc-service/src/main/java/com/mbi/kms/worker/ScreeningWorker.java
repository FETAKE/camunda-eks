package com.mbi.kms.worker;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.repository.KycApplicationRepository;
import com.mbi.kms.service.ScreeningService;
import io.camunda.client.annotation.JobWorker;
import io.camunda.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component

@RequiredArgsConstructor
@Slf4j
public class ScreeningWorker {

    private final KycApplicationRepository applicationRepository;
    private final ScreeningService screeningService;

    @JobWorker(type = "initiate-screening", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {

        log.info("Initiating sanctions & PEP screening for job: {}", job.getKey());

        Map<String, Object> variables = job.getVariablesAsMap();
        Long applicationId = Long.parseLong(variables.get("applicationId").toString());

        try {

            KycApplication application = applicationRepository.findById(applicationId)
                    .orElse(null);

            // If application not found → throw BPMN error
            if (application == null) {

                log.error("Application not found with ID: {}", applicationId);

                client.newThrowErrorCommand(job.getKey())
                        .errorCode("APPLICATION_NOT_FOUND")
                        .errorMessage("KYC application not found with ID: " + applicationId)
                        .send()
                        .join();

                return;
            }

            // Perform screening
            ScreeningService.ScreeningResult result =
                    screeningService.performScreening(application);

            applicationRepository.save(application);

            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "sanctionsPassed", result.isSanctionsPassed(),
                            "pepPassed", result.isPepPassed(),
                            "applicationId", applicationId
                    ))
                    .send()
                    .join();

            log.info("Screening completed for applicationId: {}", applicationId);

        } catch (Exception e) {

            log.error("Screening failed for job: {}", job.getKey(), e);

            client.newFailCommand(job.getKey())
                    .retries(Math.max(job.getRetries() - 1, 0))
                    .errorMessage(e.getMessage())
                    .send()
                    .join();
        }
    }
}
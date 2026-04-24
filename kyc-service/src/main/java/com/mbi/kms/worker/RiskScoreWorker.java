package com.mbi.kms.worker;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.repository.KycApplicationRepository;
import com.mbi.kms.service.RiskScoreService;
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
public class RiskScoreWorker {

    private final KycApplicationRepository applicationRepository;
    private final RiskScoreService riskScoreService;

    @JobWorker(type = "calculate-risk-score", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {

        log.info("Calculating risk score for job: {}", job.getKey());

        try {
            Map<String, Object> variables = job.getVariablesAsMap();
            Long applicationId = Long.parseLong(variables.get("applicationId").toString());

            KycApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));
            // Calculate risk score
            RiskScoreService.RiskAssessmentResult result =
                    riskScoreService.calculateRiskScore(application);
            applicationRepository.save(application);

            // Complete job manually
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "riskScore", result.getRiskScore(),
                            "riskLevel", result.getRiskLevel().toString(),
                            "applicationId", applicationId
                    ))
                    .send()
                    .join();

            log.info("Risk score calculated successfully for applicationId: {}", applicationId);

        } catch (Exception e) {

            log.error("Risk score calculation failed for job: {}", job.getKey(), e);

            client.newFailCommand(job.getKey())
                    .retries(Math.max(job.getRetries() - 1, 0))
                    .errorMessage("Risk score calculation failed: " + e.getMessage())
                    .send()
                    .join();
        }
    }
}
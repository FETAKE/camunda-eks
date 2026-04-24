package com.mbi.kms.worker;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.repository.KycApplicationRepository;
import com.mbi.kms.service.DocumentVerificationService;
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
public class DocumentVerificationWorker {

    private final KycApplicationRepository applicationRepository;
    private final DocumentVerificationService documentVerificationService;


//    @JobWorker(type = "verify-documents", autoComplete = false)
//    public void handle(JobClient client, ActivatedJob job) {
//
//        log.info("Performing identity & document verification for job: {}", job.getKey());
//
//        try {
//
//            Map<String, Object> variables = job.getVariablesAsMap();
//            Long applicationId = Long.parseLong(variables.get("applicationId").toString());
//
//            KycApplication application = applicationRepository.findById(applicationId)
//                    .orElseThrow(() -> new RuntimeException("Application not found"));
//
//            // Verify document using database
//            DocumentVerificationService.DocumentVerificationResult result =
//                    documentVerificationService.verifyDocument(application);
//
//            applicationRepository.save(application);
//
//            // Complete job manually
//            client.newCompleteCommand(job.getKey())
//                    .variables(Map.of(
//                            "documentVerified", result.isVerified(),
//                            "verificationMessage", result.getMessage(),
//                            "applicationId", applicationId
//                    ))
//                    .send()
//                    .join();
//
//            log.info("Document verification completed for applicationId: {}", applicationId);
//
//        } catch (Exception e) {
//
//            log.error("Document verification failed for job: {}", job.getKey(), e);
//
//            client.newFailCommand(job.getKey())
//                    .retries(job.getRetries() - 1)
//                    .errorMessage("Document verification failed: " + e.getMessage())
//                    .send()
//                    .join();
//        }
//    }



    @JobWorker(type = "verify-documents", autoComplete = false)
    public void handle(JobClient client, ActivatedJob job) {

        log.info("Performing identity & document verification for job: {}", job.getKey());

        try {

            Map<String, Object> variables = job.getVariablesAsMap();
            Long applicationId = Long.parseLong(variables.get("applicationId").toString());

            KycApplication application = applicationRepository.findById(applicationId)
                    .orElseThrow(() -> new RuntimeException("Application not found"));

            DocumentVerificationService.DocumentVerificationResult result =
                    documentVerificationService.verifyDocument(application);

            applicationRepository.save(application);

            // ❗ If document verification failed → throw BPMN error
            if (!result.isVerified()) {

                log.warn("Document verification failed for applicationId: {}", applicationId);

                client.newThrowErrorCommand(job.getKey())
                        .errorCode("DOCUMENT_NOT_FOUND")
                        .errorMessage(result.getMessage())
                        .send()
                        .join();

                return;
            }

            // Otherwise complete the job
            client.newCompleteCommand(job.getKey())
                    .variables(Map.of(
                            "documentVerified", true,
                            "verificationMessage", result.getMessage(),
                            "applicationId", applicationId
                    ))
                    .send()
                    .join();

            log.info("Document verification completed for applicationId: {}", applicationId);

        } catch (Exception e) {

            log.error("Document verification failed for job: {}", job.getKey(), e);

            client.newFailCommand(job.getKey())
                    .retries(Math.max(job.getRetries() - 1, 0))
                    .errorMessage("Document verification failed: " + e.getMessage())
                    .send()
                    .join();
        }
    }
}

package com.mbi.kms.worker;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.repository.KycApplicationRepository;
import com.mbi.kms.service.EmailService;
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
public class EmailWorkers {

    private final EmailService emailService;
    private final KycApplicationRepository applicationRepository;

//    @JobWorker(type = "send-rejection-notification")
@JobWorker(type = "send-rejection-notification", autoComplete = false)
public void sendRejectionNotification(JobClient client, ActivatedJob job) {

    log.info("Sending rejection notification for job: {}", job.getKey());

    try {

        Map<String, Object> variables = job.getVariablesAsMap();
        Long applicationId = Long.parseLong(variables.get("applicationId").toString());

        String rejectionReason = (String) variables.getOrDefault(
                "rejectionReason",
                "Your application did not meet our KYC requirements"
        );

        KycApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        // Send HTML email
        String htmlBody = emailService.buildRejectionEmail(
                application.getCustomerName(),
                rejectionReason,
                String.valueOf(applicationId)
        );

        emailService.sendHtmlEmail(
                application.getCustomerEmail(),
                "KYC Application Status - Rejected",
                htmlBody
        );

        // Send simple text email as backup
        String textBody = String.format(
                "Dear %s,\n\nYour KYC application (ID: %s) has been rejected.\nReason: %s\n\nThank you.\nKYC Team",
                application.getCustomerName(),
                applicationId,
                rejectionReason
        );

        emailService.sendSimpleEmail(
                application.getCustomerEmail(),
                "KYC Application Rejected",
                textBody
        );

        // Complete job manually
        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "rejectionEmailSent", true,
                        "rejectionEmailSentTo", application.getCustomerEmail(),
                        "rejectionReason", rejectionReason
                ))
                .send()
                .join();

        log.info("Rejection email sent successfully for applicationId: {}", applicationId);

    } catch (Exception e) {

        log.error("Failed to send rejection email for job: {}", job.getKey(), e);

        client.newFailCommand(job.getKey())
                .retries(Math.max(job.getRetries() - 1, 0))
                .errorMessage("Failed to send rejection notification: " + e.getMessage())
                .send()
                .join();
    }
}


//    @JobWorker(type = "send-approval-notification")
@JobWorker(type = "send-approval-notification", autoComplete = false)
public void sendApprovalNotification(JobClient client, ActivatedJob job) {

    log.info("Sending approval notification for job: {}", job.getKey());

    try {

        Map<String, Object> variables = job.getVariablesAsMap();
        Long applicationId = Long.parseLong(variables.get("applicationId").toString());

        KycApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        // Send HTML email
        String htmlBody = emailService.buildApprovalEmail(
                application.getCustomerName(),
                String.valueOf(applicationId)
        );

        emailService.sendHtmlEmail(
                application.getCustomerEmail(),
                "🎉 KYC Application Approved!",
                htmlBody
        );

        // Send simple text email as backup
        String textBody = String.format(
                "Dear %s,\n\nCongratulations! Your KYC application (ID: %s) has been APPROVED.\n\nYou can now access all features of our services.\n\nThank you.\nKYC Team",
                application.getCustomerName(),
                applicationId
        );

        emailService.sendSimpleEmail(
                application.getCustomerEmail(),
                "KYC Application Approved",
                textBody
        );

        // Complete job manually
        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "approvalEmailSent", true,
                        "approvalEmailSentTo", application.getCustomerEmail()
                ))
                .send()
                .join();

        log.info("Approval email sent successfully for applicationId: {}", applicationId);

    } catch (Exception e) {

        log.error("Failed to send approval email for job: {}", job.getKey(), e);

        client.newFailCommand(job.getKey())
                .retries(Math.max(job.getRetries() - 1, 0))
                .errorMessage("Failed to send approval notification: " + e.getMessage())
                .send()
                .join();
    }
}

//    @JobWorker(type = "send-under-review-notification")
@JobWorker(type = "send-under-review-notification", autoComplete = false)
public void sendUnderReviewNotification(JobClient client, ActivatedJob job) {

    log.info("Sending under review notification for job: {}", job.getKey());

    try {

        Map<String, Object> variables = job.getVariablesAsMap();
        Long applicationId = Long.parseLong(variables.get("applicationId").toString());

        KycApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        String htmlBody = emailService.buildUnderReviewEmail(
                application.getCustomerName(),
                String.valueOf(applicationId)
        );

        emailService.sendHtmlEmail(
                application.getCustomerEmail(),
                "KYC Application Under Review",
                htmlBody
        );

        // Complete job manually
        client.newCompleteCommand(job.getKey())
                .variables(Map.of(
                        "underReviewEmailSent", true,
                        "underReviewEmailSentTo", application.getCustomerEmail()
                ))
                .send()
                .join();

        log.info("Under review email sent for applicationId: {}", applicationId);

    } catch (Exception e) {

        log.error("Failed to send under review email for job: {}", job.getKey(), e);

        client.newFailCommand(job.getKey())
                .retries(Math.max(job.getRetries() - 1, 0))
                .errorMessage("Failed to send under review notification: " + e.getMessage())
                .send()
                .join();
    }
}

//    @JobWorker(type = "send-edd-notification")
//    public Map<String, Object> sendEddNotification(final ActivatedJob job) {
//        log.info("Sending enhanced due diligence notification for job: {}", job.getKey());
//
//        Map<String, Object> variables = job.getVariablesAsMap();
//        Long applicationId = Long.parseLong(variables.get("applicationId").toString());
//
//        KycApplication application = applicationRepository.findById(applicationId)
//                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));
//
//        String htmlBody = emailService.buildEDDEmail(
//                application.getCustomerName(),
//                String.valueOf(applicationId)
//        );
//
//        emailService.sendHtmlEmail(
//                application.getCustomerEmail(),
//                "Additional Information Required for KYC",
//                htmlBody
//        );
//
//        return Map.of(
//                "eddEmailSent", true,
//                "eddEmailSentTo", application.getCustomerEmail()
//        );
//    }
}

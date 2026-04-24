package com.mbi.kms.controller;


import com.mbi.kms.dto.KycFormRequest;
import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.entity.ApplicationStatus;
import com.mbi.kms.repository.KycApplicationRepository;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProcessController {

    private final ZeebeClient zeebeClient;
    private final KycApplicationRepository applicationRepository;

    /**
     * Start a new KYC process with form data
     */
    @PostMapping("/start-kyc")
    public ResponseEntity<Map<String, Object>> startKycProcess(@Valid @RequestBody KycFormRequest formRequest) {
        log.info("Starting KYC process for customer: {}", formRequest.getCustomerName());

        try {
            // 1. Save application to database first
            KycApplication application = new KycApplication();
            application.setCustomerName(formRequest.getCustomerName());
            application.setCustomerEmail(formRequest.getCustomerEmail());
            application.setCustomerPhone(formRequest.getCustomerPhone());
            application.setDocumentType(formRequest.getDocumentType());
            application.setDocumentNumber(formRequest.getDocumentNumber());
            application.setDocumentImageUrl(formRequest.getDocumentImageUrl());
            application.setSubmissionDate(LocalDateTime.now());
            application.setLastUpdatedDate(LocalDateTime.now());
            application.setStatus(ApplicationStatus.SUBMITTED);

            KycApplication savedApplication = applicationRepository.save(application);
            log.info("Application saved with ID: {}", savedApplication.getId());

            // 2. Prepare variables for Camunda process
            Map<String, Object> variables = new HashMap<>();
            variables.put("applicationId", savedApplication.getId());
            variables.put("customerName", formRequest.getCustomerName());
            variables.put("customerEmail", formRequest.getCustomerEmail());
            variables.put("customerPhone", formRequest.getCustomerPhone());
            variables.put("documentType", formRequest.getDocumentType());
            variables.put("documentNumber", formRequest.getDocumentNumber());
            variables.put("documentImageUrl", formRequest.getDocumentImageUrl());
            variables.put("country", formRequest.getCountry());
            variables.put("dateOfBirth", formRequest.getDateOfBirth());

            // Add default values for process flow
            variables.put("sanctionsPassed", false);
            variables.put("pepPassed", false);
            variables.put("documentVerified", false);
            variables.put("riskScore", 0);
            variables.put("riskLevel", "PENDING");
            variables.put("rejectionReason", "");

            // 3. Start Camunda process with variables
            ProcessInstanceEvent processInstance = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("bank_kyc_process")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();

            log.info("Process started with instance key: {}", processInstance.getProcessInstanceKey());

            // 4. Update application with process instance ID
            savedApplication.setProcessInstanceId(String.valueOf(processInstance.getProcessInstanceKey()));
            applicationRepository.save(savedApplication);

            // 5. Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "KYC process started successfully");
            response.put("applicationId", savedApplication.getId());
            response.put("processInstanceId", processInstance.getProcessInstanceKey());
            response.put("status", savedApplication.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error starting KYC process: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to start KYC process: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Start a simple process (your existing method)
     */
    @PostMapping("/start")
    public ResponseEntity<String> startProcess() {
        zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("bank_kyc_process")
                .latestVersion()
                .send()
                .join();

        return ResponseEntity.ok("Process started successfully");
    }

    /**
     * Get all active user tasks with more details
     */
    @GetMapping("/active-user-tasks")
    public ResponseEntity<List<Map<String, Object>>> getActiveUserTasks() {
        List<Map<String, Object>> taskList = new ArrayList<>();

        ActivateJobsResponse response = zeebeClient
                .newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(10)
                .send()
                .join();

        for (ActivatedJob job : response.getJobs()) {
            Map<String, Object> task = new HashMap<>();
            task.put("jobKey", job.getKey());
            task.put("type", job.getType());
            task.put("bpmnProcessId", job.getBpmnProcessId());
            task.put("elementId", job.getElementId());
            task.put("processInstanceKey", job.getProcessInstanceKey());
            task.put("variables", job.getVariablesAsMap());
            task.put("deadline", job.getDeadline());

            // Add custom task name based on elementId
            String taskName = getTaskName(job.getElementId());
            task.put("taskName", taskName);

            taskList.add(task);
        }

        return ResponseEntity.ok(taskList);
    }

    /**
     * Helper method to get user-friendly task names
     */
    private String getTaskName(String elementId) {
        switch (elementId) {
            case "Activity_Screening":
                return "Sanctions & PEP Screening";
            case "Activity_DocumentVerification":
                return "Identity & Document Verification";
            case "Activity_RiskAssessment":
                return "Risk Score Calculation";
            case "Activity_AnalystReview":
                return "KYC Analyst Review";
            case "Activity_EDD":
                return "Enhanced Due Diligence";
            default:
                return elementId;
        }
    }

    /**
     * Get tasks assigned to specific user/role
     */
    @GetMapping("/tasks/{assignee}")
    public ResponseEntity<List<Map<String, Object>>> getTasksByAssignee(@PathVariable String assignee) {
        // Note: This is a simplified version. In production, you'd use tasklist API
        List<Map<String, Object>> taskList = new ArrayList<>();

        ActivateJobsResponse response = zeebeClient
                .newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(10)
                .send()
                .join();

        for (ActivatedJob job : response.getJobs()) {
            Map<String, Object> variables = job.getVariablesAsMap();
            String taskAssignee = (String) variables.get("assignee");

            if (assignee.equals(taskAssignee)) {
                Map<String, Object> task = new HashMap<>();
                task.put("jobKey", job.getKey());
                task.put("elementId", job.getElementId());
                task.put("processInstanceKey", job.getProcessInstanceKey());
                task.put("variables", variables);
                taskList.add(task);
            }
        }

        return ResponseEntity.ok(taskList);
    }

    /**
     * Complete a task with variables (your existing method enhanced)
     */
    @PostMapping("/complete-task/{jobKey}")
    public ResponseEntity<Map<String, Object>> completeTask(
            @PathVariable long jobKey,
            @RequestBody(required = false) Map<String, Object> variables) {

        try {
            if (variables == null) variables = new HashMap<>();

            // Add completion timestamp
            variables.put("taskCompletedAt", LocalDateTime.now().toString());

            zeebeClient
                    .newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();

            // Fetch updated process instance to get latest variables
            // Note: This is simplified - you might want to query for updated application

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Task completed successfully: " + jobKey);
            response.put("variables", variables);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error completing task: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to complete task: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Fail a task (if something goes wrong)
     */
    @PostMapping("/fail-task/{jobKey}")
    public ResponseEntity<String> failTask(
            @PathVariable long jobKey,
            @RequestParam String errorMessage) {

        zeebeClient
                .newFailCommand(jobKey)
                .retries(0)
                .errorMessage(errorMessage)
                .send()
                .join();

        return ResponseEntity.ok("Task failed: " + jobKey);
    }

    /**
     * Get process instance details
     */
    @GetMapping("/instance/{processInstanceId}")
    public ResponseEntity<Map<String, Object>> getProcessInstance(@PathVariable String processInstanceId) {
        try {
            // Find application by process instance ID
            KycApplication application = applicationRepository.findByProcessInstanceId(processInstanceId);

            if (application == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> details = new HashMap<>();
            details.put("applicationId", application.getId());
            details.put("customerName", application.getCustomerName());
            details.put("status", application.getStatus());
            details.put("riskLevel", application.getRiskLevel());
            details.put("riskScore", application.getRiskScore());
            details.put("sanctionsPassed", application.getSanctionsCheckPassed());
            details.put("pepPassed", application.getPepCheckPassed());
            details.put("documentVerified", application.getDocumentVerificationPassed());

            return ResponseEntity.ok(details);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }




    @GetMapping("/active-user-tasks1")
    public ResponseEntity<List<Map<String, Object>>> getActiveUserTasks1() {

        List<Map<String, Object>> taskList = new ArrayList<>();

        ActivateJobsResponse response = zeebeClient
                .newActivateJobsCommand()
                .jobType("io.camunda.zeebe:userTask")
                .maxJobsToActivate(10)
                .timeout(Duration.ofSeconds(10))   // IMPORTANT
                .send()
                .join();

        for (ActivatedJob job : response.getJobs()) {

            Map<String, Object> task = new HashMap<>();
            task.put("jobKey", job.getKey());
            task.put("type", job.getType());
            task.put("bpmnProcessId", job.getBpmnProcessId());
            task.put("elementId", job.getElementId());
            task.put("processInstanceKey", job.getProcessInstanceKey());
            task.put("variables", job.getVariablesAsMap());
            task.put("deadline", job.getDeadline());

            String taskName = getTaskName(job.getElementId());
            task.put("taskName", taskName);

            taskList.add(task);
        }

        return ResponseEntity.ok(taskList);
    }
}
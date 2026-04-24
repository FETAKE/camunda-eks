package com.mbi.kms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KycFormRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number")
    private String customerPhone;

    @NotBlank(message = "Document type is required")
    private String documentType; // PASSPORT, DRIVING_LICENSE, NATIONAL_ID

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Document image is required")
    private String documentImageUrl; // Base64 encoded image or file path

    private String country;
    private String dateOfBirth;
}

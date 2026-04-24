package com.mbi.kms.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("umxsharma@gmail.com");

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {} - Error: {}", to, e.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML
            helper.setFrom("umxsharma@gmail.com");

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {} - Error: {}", to, e.getMessage());
        }
    }

    public String buildRejectionEmail(String customerName, String reason, String applicationId) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                        ".header { background-color: #dc3545; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { padding: 20px; }" +
                        ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                        ".reason { background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; padding: 10px; border-radius: 5px; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>KYC Application Update</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>We regret to inform you that your KYC application <strong>(ID: %s)</strong> has been rejected.</p>" +
                        "<div class='reason'>" +
                        "<p><strong> Check Document Number </strong></p>" +
                        "<p>%s</p>" +
                        "</div>" +
                        "<p>If you believe this is an error or would like to reapply with corrected information, please contact our support team.</p>" +
                        "<p>Thank you for your understanding.</p>" +
                        "<p>Best regards,<br/>KYC Team</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message. Please do not reply to this email.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                customerName, applicationId, reason
        );
    }

    public String buildApprovalEmail(String customerName, String applicationId) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                        ".header { background-color: #28a745; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { padding: 20px; }" +
                        ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                        ".success-icon { font-size: 48px; text-align: center; color: #28a745; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>KYC Application Approved!</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<div class='success-icon'>✓</div>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>We are pleased to inform you that your KYC application <strong>(ID: %s)</strong> has been <strong style='color: #28a745;'>APPROVED</strong>.</p>" +
                        "<p>You can now access all features of our services.</p>" +
                        "<p>Thank you for choosing our services.</p>" +
                        "<p>Best regards,<br/>KYC Team</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message. Please do not reply to this email.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                customerName, applicationId
        );
    }

    public String buildUnderReviewEmail(String customerName, String applicationId) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                        ".header { background-color: #ffc107; color: #333; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { padding: 20px; }" +
                        ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>KYC Application Under Review</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>Your KYC application <strong>(ID: %s)</strong> is currently under review by our analyst team.</p>" +
                        "<p>We will notify you once the review is complete. This process typically takes 1-2 business days.</p>" +
                        "<p>Thank you for your patience.</p>" +
                        "<p>Best regards,<br/>KYC Team</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message. Please do not reply to this email.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                customerName, applicationId
        );
    }

    public String buildEDDEmail(String customerName, String applicationId) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<style>" +
                        "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                        ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; }" +
                        ".header { background-color: #17a2b8; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }" +
                        ".content { padding: 20px; }" +
                        ".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h2>Additional Information Required</h2>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p>Dear <strong>%s</strong>,</p>" +
                        "<p>Your KYC application <strong>(ID: %s)</strong> requires additional verification.</p>" +
                        "<p>Please provide the following documents for Enhanced Due Diligence:</p>" +
                        "<ul>" +
                        "<li>Proof of income (last 3 months salary slips)</li>" +
                        "<li>Bank statements (last 6 months)</li>" +
                        "<li>Address verification (utility bill not older than 3 months)</li>" +
                        "</ul>" +
                        "<p>Please upload these documents through our portal at your earliest convenience.</p>" +
                        "<p>Best regards,<br/>KYC Team</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>This is an automated message. Please do not reply to this email.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                customerName, applicationId
        );
    }
}
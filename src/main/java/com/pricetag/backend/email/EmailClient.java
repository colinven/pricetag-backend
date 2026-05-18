package com.pricetag.backend.email;

import com.pricetag.backend.exception.EmailSendException;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailClient {

    private final Resend resend;

    @Value("${email.sender}")
    private String sender;

    /**
     * Sends an email with provided params via Resend API
     *
     * @param from name of company. displays next to FlowBid email address
     * @param to customer email address
     * @param replyTo company email address that will receive replies
     * @param subject main subject line of email
     * @param renderedEmail object containing HTML and plain text email strings with substituted vars
     */
    public void send(String from, String to, String replyTo, String subject, @NonNull RenderedEmail renderedEmail) {

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from + " <" + sender + ">")
                .to(to)
                .subject(subject)
                .html(renderedEmail.html())
                .text(renderedEmail.text())
                .replyTo(replyTo)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            System.out.println("Email sent. Id: " + response.getId());
        } catch (ResendException e) {
            throw new EmailSendException("Failed to send email", e);
        }
    }

}

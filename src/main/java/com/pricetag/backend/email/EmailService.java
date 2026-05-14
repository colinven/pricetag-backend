package com.pricetag.backend.service;

import com.pricetag.backend.entity.Quote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${frontend.domain}")
    private String frontendDomain;

    @Value("${email.sender}")
    private String emailSender;

    @Value("${keys.resendKey}")
    private String resendKey;


    //@Async
    public void sendLinkToQuoteEmail(Quote quote, String quoteToken) {

    }

    public void sendNewQuoteRequestEmail() {
        // email sent to company when a new quote request comes in
        // fires when quoteController.getQuote() returns
    }

    public void sendQuoteRequestReceivedEmail() {
        // confirmation email sent to customer after they submit a quote request
    }

    public void sendQuoteDecisionEmail() {
        // email sent to company when customer accepts/declines a finalized quote
    }

    public void sendCopyOfAcceptedQuoteEmail() {
        // email sent to customer after they accept a quote. (includes a pdf copy of the quote, next steps)
    }
}

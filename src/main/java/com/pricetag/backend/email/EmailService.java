package com.pricetag.backend.email;

import com.pricetag.backend.email.context.FinalQuoteReadyContext;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailTemplateLoader loader;
    private final EmailClient emailClient;

    @Async("emailExecutor")
    public void sendFinalQuoteReadyEmail(FinalQuoteReadyContext ctx) {
        Map<String, String> vars = ctx.toMap();
        RenderedEmail renderedEmail = loader.render("final-quote-ready", vars);
        emailClient.send(
                ctx.companyName(),
                ctx.customerEmail(),
                ctx.companyEmail(),
                "Your quote from " + ctx.companyName() + " is ready to view",
                renderedEmail
        );
    }

    public void sendNewQuoteRequestEmail() {
        // email sent to company when a new quote request comes in
        // fires when quoteController.submitQuoteRequest() returns
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

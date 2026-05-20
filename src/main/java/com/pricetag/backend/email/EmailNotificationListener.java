package com.pricetag.backend.email;

import com.pricetag.backend.email.context.FinalQuoteReadyContext;
import com.pricetag.backend.email.event.FinalQuoteReadyEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class EmailNotificationListener {

    @Value("${frontend.domain}")
    private String frontendDomain;

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onQuoteReviewed(FinalQuoteReadyEvent event) {
        String finalQuoteUrl = frontendDomain + "/q/" + event.quoteId() + "?token=" + event.quoteToken();
        var ctx = new FinalQuoteReadyContext(
                event.company().getName(),
                event.company().getPhone(),
                event.company().getEmail(),
                event.customer().getFirstName(),
                event.customer().getEmail(),
                event.property().getFullAddress(),
                Integer.toString(event.pricing().getQuoteExpiryDays()),
                finalQuoteUrl
        );
        emailService.sendFinalQuoteReadyEmail(ctx);
    }
}

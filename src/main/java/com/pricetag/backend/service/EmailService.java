package com.pricetag.backend.service;

import com.pricetag.backend.entity.Company;
import com.pricetag.backend.entity.Customer;
import com.pricetag.backend.entity.Quote;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${frontend.domain}")
    private String frontendDomain;

    @Value("${keys.sendgridKey}")
    private String sendgridKey;

    @Value("${sendgrid.sender}")
    private String sendgridSender;

    //@Async
    public void sendQuoteUrl(Quote quote, String quoteToken) throws IOException {

        String quoteUrl = frontendDomain + "/quotes/" + quote.getId() + "?token=" + quoteToken;
        System.out.println("Quote URL: " + quoteUrl);

//        Customer customer = quote.getCustomer();
//        Company company = quote.getCompany();
//
//        Email from = new Email(sendgridSender);
//        Email to = new Email(customer.getEmail());
//        String subject = "Your Quote From " + company.getName() + " is Ready to View!";
//        Content content = new Content("text/plain", "View your quote here: " + quoteUrl);
//        Mail mail = new Mail(from, subject, to, content);
//
//        SendGrid sendGrid = new SendGrid(sendgridKey);
//        Request request = new Request();
//
//        try{
//            request.setMethod(Method.POST);
//            request.setEndpoint("mail/send");
//            request.setBody(mail.build());
//            Response response = sendGrid.api(request);
//            System.out.println("SendGrid Response code: " +  response.getStatusCode());
//        } catch (IOException e) {
//            throw e;
//        }

    }
}

package com.pricetag.backend.service;

import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.entity.QuoteToken;
import com.pricetag.backend.exception.*;
import com.pricetag.backend.repository.QuoteTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteTokenService {

    private final QuoteTokenRepository quoteTokenRepository;

    @Transactional
    public String generateToken(Quote quote) {

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        String hashedToken = hashToken(rawToken);

        QuoteToken quoteToken = QuoteToken.builder()
                .quote(quote)
                .tokenHash(hashedToken)
                .expiresAt(LocalDateTime.now().plusDays(3))
                .build();

        // If token already exists for this quote, delete it before creating a new one
        QuoteToken existingToken = quoteTokenRepository.findByQuoteId(quote.getId()).orElse(null);
        if (existingToken != null) {
            quoteTokenRepository.delete(existingToken);
            quoteTokenRepository.flush();
        }
        quoteTokenRepository.save(quoteToken);

        return rawToken;
    }

    private String hashToken(String rawToken) {

        try {
            byte[] hashedBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public QuoteToken validateToken(UUID quoteId, String rawToken) {

        if (rawToken == null) throw new InvalidQuoteTokenException();
        QuoteToken quoteToken = quoteTokenRepository.findByQuoteId(quoteId)
                .orElseThrow(InvalidQuoteTokenException::new); // 404 NOT FOUND

        String hashedIncomingToken = hashToken(rawToken);

        if (!Objects.requireNonNull(hashedIncomingToken).equals(quoteToken.getTokenHash())) {
            throw new InvalidQuoteTokenException(); // 404 NOT FOUND
        }
        if (quoteToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new QuoteTokenExpiredException(); // 410 GONE
        }

        return quoteToken;
    }

    public void consumeToken(QuoteToken token) {
        token.setUsedAt(LocalDateTime.now());
        quoteTokenRepository.save(token);
    }

    public void checkIfTokenConsumed(QuoteToken token) {
        if (token.getUsedAt() != null ) {
            throw new QuoteTokenConsumedException();
        }
    }

}

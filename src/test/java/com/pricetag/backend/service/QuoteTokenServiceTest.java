package com.pricetag.backend.service;

import com.pricetag.backend.entity.Quote;
import com.pricetag.backend.entity.QuoteToken;
import com.pricetag.backend.exception.InvalidQuoteTokenException;
import com.pricetag.backend.exception.QuoteTokenConsumedException;
import com.pricetag.backend.exception.QuoteTokenExpiredException;
import com.pricetag.backend.repository.QuoteTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuoteTokenServiceTest {

    @Mock private QuoteTokenRepository quoteTokenRepository;

    @InjectMocks
    private QuoteTokenService quoteTokenService;

    // Helper: compute the same SHA-256 hash the service uses, so we can build matching QuoteToken stubs
    private String computeHash(String rawToken) throws NoSuchAlgorithmException {
        byte[] hashedBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    private Quote buildQuote() {
        return Quote.builder().id(UUID.randomUUID()).build();
    }

    // ── generateToken ────────────────────────────────────────────────────────

    @Test
    void givenQuote_whenGenerateToken_thenReturnsNonNullRawToken() {
        String rawToken = quoteTokenService.generateToken(buildQuote());
        assertThat(rawToken).isNotNull();
    }

    @Test
    void givenQuote_whenGenerateToken_thenSavedHashDiffersFromRawToken() {
        Quote quote = buildQuote();
        String rawToken = quoteTokenService.generateToken(quote);

        ArgumentCaptor<QuoteToken> captor = ArgumentCaptor.forClass(QuoteToken.class);
        verify(quoteTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isNotEqualTo(rawToken);
    }

    @Test
    void givenQuote_whenGenerateToken_thenExpiresAtIsApproximately72HoursFromNow() {
        quoteTokenService.generateToken(buildQuote());

        ArgumentCaptor<QuoteToken> captor = ArgumentCaptor.forClass(QuoteToken.class);
        verify(quoteTokenRepository).save(captor.capture());
        LocalDateTime expiresAt = captor.getValue().getExpiresAt();
        LocalDateTime expected = LocalDateTime.now().plusDays(3);
        assertThat(expiresAt).isAfter(expected.minusSeconds(5));
        assertThat(expiresAt).isBefore(expected.plusSeconds(5));
    }

    @Test
    void givenQuote_whenGenerateToken_thenSavedTokenHasNullUsedAt() {
        quoteTokenService.generateToken(buildQuote());

        ArgumentCaptor<QuoteToken> captor = ArgumentCaptor.forClass(QuoteToken.class);
        verify(quoteTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUsedAt()).isNull();
    }

    @Test
    void givenExistingTokenForQuote_whenGenerateToken_thenDeletesOldThenFlushThenSavesNew() {
        Quote quote = buildQuote();
        QuoteToken existing = QuoteToken.builder()
                .id(UUID.randomUUID())
                .quote(quote)
                .tokenHash("oldhash")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(quoteTokenRepository.findByQuoteId(quote.getId())).thenReturn(Optional.of(existing));

        quoteTokenService.generateToken(quote);

        InOrder inOrder = inOrder(quoteTokenRepository);
        inOrder.verify(quoteTokenRepository).delete(existing);
        inOrder.verify(quoteTokenRepository).flush();
        inOrder.verify(quoteTokenRepository).save(any(QuoteToken.class));
    }

    // ── validateToken ────────────────────────────────────────────────────────

    @Test
    void givenNullToken_whenValidateToken_thenThrowsInvalidQuoteTokenException() {
        assertThatThrownBy(() -> quoteTokenService.validateToken(UUID.randomUUID(), null))
                .isInstanceOf(InvalidQuoteTokenException.class);
    }

    @Test
    void givenNoTokenInDb_whenValidateToken_thenThrowsInvalidQuoteTokenException() {
        when(quoteTokenRepository.findByQuoteId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteTokenService.validateToken(UUID.randomUUID(), "sometoken"))
                .isInstanceOf(InvalidQuoteTokenException.class);
    }

    @Test
    void givenWrongToken_whenValidateToken_thenThrowsInvalidQuoteTokenException() {
        UUID quoteId = UUID.randomUUID();
        QuoteToken stored = QuoteToken.builder()
                .tokenHash("totallywronghash")
                .expiresAt(LocalDateTime.now().plusHours(72))
                .build();
        when(quoteTokenRepository.findByQuoteId(quoteId)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> quoteTokenService.validateToken(quoteId, "wrongtoken"))
                .isInstanceOf(InvalidQuoteTokenException.class);
    }

    @Test
    void givenExpiredToken_whenValidateToken_thenThrowsQuoteTokenExpiredException() throws NoSuchAlgorithmException {
        UUID quoteId = UUID.randomUUID();
        String rawToken = "testtoken123";
        QuoteToken stored = QuoteToken.builder()
                .tokenHash(computeHash(rawToken))
                .expiresAt(LocalDateTime.now().minusHours(1))  // already expired
                .build();
        when(quoteTokenRepository.findByQuoteId(quoteId)).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> quoteTokenService.validateToken(quoteId, rawToken))
                .isInstanceOf(QuoteTokenExpiredException.class);
    }

    @Test
    void givenValidToken_whenValidateToken_thenReturnsQuoteToken() throws NoSuchAlgorithmException {
        UUID quoteId = UUID.randomUUID();
        String rawToken = "testtoken123";
        QuoteToken stored = QuoteToken.builder()
                .tokenHash(computeHash(rawToken))
                .expiresAt(LocalDateTime.now().plusHours(72))
                .build();
        when(quoteTokenRepository.findByQuoteId(quoteId)).thenReturn(Optional.of(stored));

        QuoteToken result = quoteTokenService.validateToken(quoteId, rawToken);

        assertThat(result).isEqualTo(stored);
    }

    // ── consumeToken ─────────────────────────────────────────────────────────

    @Test
    void givenToken_whenConsumeToken_thenSetsUsedAtToNonNull() {
        QuoteToken token = QuoteToken.builder().build();
        quoteTokenService.consumeToken(token);
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    void givenToken_whenConsumeToken_thenSavesToken() {
        QuoteToken token = QuoteToken.builder().build();
        quoteTokenService.consumeToken(token);
        verify(quoteTokenRepository, times(1)).save(token);
    }

    // ── checkIfTokenConsumed ─────────────────────────────────────────────────

    @Test
    void givenTokenWithUsedAt_whenCheckIfConsumed_thenThrowsQuoteTokenConsumedException() {
        QuoteToken token = QuoteToken.builder().usedAt(LocalDateTime.now()).build();
        assertThatThrownBy(() -> quoteTokenService.checkIfTokenConsumed(token))
                .isInstanceOf(QuoteTokenConsumedException.class);
    }

    @Test
    void givenTokenWithNullUsedAt_whenCheckIfConsumed_thenDoesNotThrow() {
        QuoteToken token = QuoteToken.builder().usedAt(null).build();
        assertThatCode(() -> quoteTokenService.checkIfTokenConsumed(token))
                .doesNotThrowAnyException();
    }
}

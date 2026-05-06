package com.pricetag.backend.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FormatterTest {

    @Test
    public void givenPhoneNumberInVariousFormats_whenFormatPhoneNumber_thenResultIsPlain10DigitString() {
        String input1 = "(123)456-7890";
        String input2 = "123-456-7890";
        String input3 = "123 456 7890";
        String input4 = "1234567890";

        String output1 = Formatter.formatPhoneNumber(input1);
        String output2 = Formatter.formatPhoneNumber(input2);
        String output3 = Formatter.formatPhoneNumber(input3);
        String output4 = Formatter.formatPhoneNumber(input4);

        String expected = "1234567890";

        assertThat(output1).isEqualTo(expected);
        assertThat(output2).isEqualTo(expected);
        assertThat(output3).isEqualTo(expected);
        assertThat(output4).isEqualTo(expected);
    }
}

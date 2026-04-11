package com.pricetag.backend.util;

import com.pricetag.backend.dto.AddressInfo;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Formatter {

    private static final Map<String, String> SUFFIXES = Map.ofEntries(
            Map.entry("Drive", "Dr"),
            Map.entry("Street", "St"),
            Map.entry("Road", "Rd"),
            Map.entry("Boulevard", "Blvd"),
            Map.entry("Avenue", "Ave"),
            Map.entry("Lane", "Ln"),
            Map.entry("Court", "Ct"),
            Map.entry("Place", "Pl"),
            Map.entry("Terrace", "Ter"),
            Map.entry("Circle", "Cir"),
            Map.entry("Trail", "Trl"),
            Map.entry("Alley", "Aly"),
            Map.entry("Crossing", "Xing"),
            Map.entry("Highway", "Hwy"),
            Map.entry("Parkway", "Pkwy")
    );

    public static String formatAddress(AddressInfo address) {

        if (
                address.street().isBlank() ||
                address.city().isBlank() ||
                address.state().isBlank() ||
                address.zip().isBlank()
        ) return null;

        String street = address.street();
        for (String key : SUFFIXES.keySet()) {
            Pattern pattern = Pattern.compile("\\b" + key + "\\b$", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(address.street());
            if (matcher.find()) {
                street = matcher.replaceFirst(SUFFIXES.get(key));
                break;
            }
        }
        String streetAndCity = street + ", " + address.city();
        Matcher matcher = Pattern.compile("\\b\\w").matcher(streetAndCity.toLowerCase());
        String titleCase = matcher.replaceAll(m -> m.group().toUpperCase());
        return titleCase + ", " + address.state().toUpperCase() + " " + address.zip();

    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        Matcher matcher = Pattern.compile("\\b\\w").matcher(input.toLowerCase());
        return matcher.replaceAll(match -> match.group().toUpperCase());
    }
}

package com.analytics.influencer_dashboard.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextParserUtil {

    /*
     =====================================================
     MAIN METHOD TO PARSE INSTAGRAM TEXT
     =====================================================
    */
    public static Map<String, String> parseConsultationText(String rawText) {

        Map<String, String> data = new HashMap<>();

        data.put("name", extractField(rawText, "Name"));
        data.put("instagramId", extractField(rawText, "Instagram ID"));
        data.put("consultationType", extractField(rawText, "Consultation Type"));
        data.put("preferredSlot", extractField(rawText, "Preferred Slot"));

        return data;
    }

    /*
     =====================================================
     GENERIC FIELD EXTRACTOR
     =====================================================
    */
    private static String extractField(String text, String fieldName) {

        Pattern pattern = Pattern.compile(
                fieldName + "\\s*:\\s*(.*)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return "Not Provided";
    }
}
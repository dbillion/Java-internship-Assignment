package com.playtech.assignment.moduular.task.util;

import java.util.HashMap;
import java.util.Map;

public class CountryCodeConverter {
    private static final Map<String, String> alpha2ToAlpha3 = new HashMap<String, String>() {{
        put("US", "USA");
        put("GB", "GBR");
        put("DE", "DE");
        put("CA", "CA");
        put("FR", "FR");
        put("JP", "JP");
        put("JP", "JP");

    }};

    public static String toAlpha3(String alpha2Code) {
        return alpha2ToAlpha3.getOrDefault(alpha2Code, alpha2Code);
    }

    static String toAlpha2(String alpha3Code) {
        return alpha2ToAlpha3.entrySet().stream()
                .filter(entry -> entry.getValue().equals(alpha3Code))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(alpha3Code);
    }
}

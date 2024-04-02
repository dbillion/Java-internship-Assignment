package com.playtech.assignment.moduular.task.util;

import java.util.regex.Pattern;

public class IbanValidator {
    public static boolean isValidIban(String iban) {
        String regex = "^([A-Z]{2}[ \\-]?[0-9]{2})(?=(?:[ \\-]?[A-Za-z0-9]){9,30}$)((?:[ \\-]?[A-Za-z0-9]{3,5}){2,7})([ \\-]?[A-Za-z0-9]{1,3})?$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(iban).matches();
    }


}
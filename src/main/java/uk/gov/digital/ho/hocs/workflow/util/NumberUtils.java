package uk.gov.digital.ho.hocs.workflow.util;

import org.springframework.util.StringUtils;

public class NumberUtils {

    private NumberUtils() {

    }

    public static int parseInt(String input) {
        if (isNumeric(input)) {
            return Integer.parseInt(input);
        }
        throw new IllegalArgumentException(String.format("Value %s is not numeric", input));
    }

    public static boolean isNumeric(String str) {
        if (StringUtils.hasText(str)) {
            boolean firstChar = true;
            for (char c : str.toCharArray()) {
                if (!Character.isDigit(c)) {
                    // if first char is non digit but it is '-' and
                    // we have more chars to check then we can carry on
                    if (!(firstChar && c == '-' && str.length() > 1)) {
                        return false;
                    }
                }

                firstChar = false;
            }
            return true;
        }
        return false;
    }

}

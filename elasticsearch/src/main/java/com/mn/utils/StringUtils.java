package com.mn.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils
{

    private static Logger log = LoggerFactory.getLogger(StringUtils.class);

    public static boolean hasText(final String string) {
        return string != null && string.trim().length() != 0;
    }

    public static String trimEnd(final String str, final String trimStr) {
        String rtn = str;
        while (rtn.endsWith(trimStr)) {
            rtn = rtn.substring(0, rtn.length() - trimStr.length());
        }
        return rtn;
    }

    public static String getNotNullString(final String string) {
        return string == null ? "" : string;
    }

    public static Double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("", e);
            }
            return null;
        }
    }

    public static Integer parseInteger(String str) {
        try {
            return Integer.parseInt(str);
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("", e);
            }
            return null;
        }
    }

    public static boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }
}

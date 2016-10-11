package com.quancheng.saluki.serializer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

public final class JStringUtils {

    public static final String  EMPTY         = StringUtils.EMPTY;

    public static final String  WILD_CARD     = "*";

    public static final String  UNDERSCORE    = "_";

    public static final String  HYPHEN        = "-";

    public static final String  BACKSLASH     = "\\";

    public static final String  QUESTION_MARK = "?";

    public static final String  NEW_LINE      = "\n";

    public static final String  TAB           = "\t";

    public static final String  SHORT_NO      = "N";

    public static final String  SHORT_YES     = "Y";

    public static final String  PERCENT       = "%";

    public static final String  FORWARD_SLASH = "/";

    public static final String  PLUS          = "+";

    public static final String  COMMA         = ",";

    public static final String  PERIOD        = ".";

    public static final String  GET           = "get";

    public static final String  IS            = "is";

    public static final Charset UTF8_CHARSET  = Charset.forName("UTF-8");

    private JStringUtils(){
        // empty utility class
    }

    public static String safeTrim(String str) {
        if (str == null) {
            return EMPTY;
        }
        return str.trim();
    }

    public static String ltrim(String source) {
        return source.replaceAll("^\\s+", EMPTY);
    }

    public static String rtrim(String source) {
        return source.replaceAll("\\s+$", EMPTY);
    }

    public static boolean hasDigit(String str) {
        final Pattern pattern = Pattern.compile("[0-9]");
        final Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            return true;
        }
        return false;
    }

    public static String pad(String rawString, int length, char padChar) {
        String padded = rawString;
        String padFiller = null;

        if (padded.length() == length) {
            return padded;
        }

        if (padded.length() < length) {
            //
            // Keep a zero string that is big enough to pad the
            // largest field that we have, then to pad each field
            // we just append this string to the field and then
            // substr to the desired size.
            //
            StringBuffer buff = new StringBuffer(length);

            for (int i = buff.length(); i < length; i++) {
                buff.append(padChar);
            }
            padFiller = buff.toString();

            padded = rawString + padFiller;
            return padded.substring(0, length);

        } else {
            return padded.substring(0, length);
        }
    }

    public static String convertInputStreamToString(InputStream is, String charset) throws IOException {
        if (is == null) {
            return EMPTY;
        }

        final StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            if (charset != null) {
                reader = new BufferedReader(new InputStreamReader(is, charset));
            } else {
                reader = new BufferedReader(new InputStreamReader(is));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return sb.toString();
    }

    public static String upperCaseFirst(String str) {
        if (str == null || str.equals(EMPTY)) {
            return str;
        }

        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String join(Collection<?> collection, String separator) {
        return StringUtils.join(collection, separator);
    }

    public static List<String> split(String str, String separatorChars) {
        final String[] array = StringUtils.split(str, separatorChars);

        return Arrays.asList(array);
    }

    public static String decode(byte[] bytes, Charset charset) {
        return new String(bytes, charset);
    }

    public static byte[] encode(String string, Charset charset) {
        return string.getBytes(charset);
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        if (string == null || string.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean hasValue(String string) {
        return !isNullOrEmpty(string);
    }

    public static boolean compare(@Nullable String str1, @Nullable String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    public static String getNonNullValue(String string) {
        if (string == null) {
            return JStringUtils.EMPTY;
        }
        return string;
    }
}

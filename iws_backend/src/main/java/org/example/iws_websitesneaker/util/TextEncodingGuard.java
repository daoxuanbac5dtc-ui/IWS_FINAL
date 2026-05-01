package org.example.iws_websitesneaker.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class TextEncodingGuard {
    private static final Pattern CONTROL_OR_REPLACEMENT =
            Pattern.compile("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F\\u0080-\\u009F\\uFFFD]");
    private static final Pattern QUESTION_MARK_INSIDE_WORD =
            Pattern.compile("(?U)(\\p{L}\\?+\\p{L}|\\p{L}\\?+|\\?+\\p{L})");
    private static final Pattern MOJIBAKE_MARKER =
            Pattern.compile("(áº|á»|Ä.|Å.|Æ.|Ă[\\u00A0-\\u00FF]|â[\\u0080-\\u00BF]?.?)");

    private TextEncodingGuard() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
    }

    public static String normalizeAndRejectCorrupted(String fieldName, String value) {
        String normalized = normalize(value);
        if (hasEncodingIssue(normalized)) {
            throw new IllegalArgumentException(fieldName + " có dấu hiệu lỗi mã hóa tiếng Việt. Vui lòng nhập lại bằng UTF-8.");
        }
        return normalized;
    }

    public static boolean hasEncodingIssue(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return CONTROL_OR_REPLACEMENT.matcher(value).find()
                || QUESTION_MARK_INSIDE_WORD.matcher(value).find()
                || MOJIBAKE_MARKER.matcher(value).find();
    }
}

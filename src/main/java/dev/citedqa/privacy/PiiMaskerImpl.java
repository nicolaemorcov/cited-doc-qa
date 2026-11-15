package dev.citedqa.privacy;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * Masks personal data with regular expressions only (no NER library, no AI call).
 *
 * The patterns run in a fixed order: email, IBAN, phone, ID, name. Each one replaces
 * what it matches with a tag, and tags contain no digits or '@', so a later pattern
 * cannot match inside an earlier tag. Email runs first because addresses can contain
 * digit runs; IBAN runs before phone and ID because IBANs contain long digit runs.
 *
 * This class logs nothing: its input is question text.
 */
@Service
public class PiiMaskerImpl implements PiiMasker {

    private static final String EMAIL_TAG = "[EMAIL]";
    private static final String IBAN_TAG = "[IBAN]";
    private static final String PHONE_TAG = "[PHONE]";
    private static final String ID_TAG = "[ID]";
    private static final String NAME_TAG = "[NAME]";

    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    /** Country code, 2 check digits, then 12 to 30 letters or digits, compact or in groups of 4. */
    private static final Pattern IBAN = Pattern.compile("\\b[A-Z]{2}\\d{2}(?: ?[A-Z0-9]{4}){3,7}(?: ?[A-Z0-9]{1,3})?\\b");

    /** Anything phone-shaped: optional '+', digits mixed with spaces, dots, hyphens, brackets. Filtered by isPhoneNumber. */
    private static final Pattern PHONE_CANDIDATE = Pattern.compile("(?<![\\w+])\\+?\\(?\\d[\\d .()\\-]{5,}\\d(?!\\w)");

    /** The 13-digit IDNP and any other run of 9 or more digits. */
    private static final Pattern ID = Pattern.compile("(?<![\\w+])\\d{9,}(?!\\w)");

    /**
     * One or two capitalised words after "my name is" or Mr/Mrs/Ms/Dr. Group 1 is the cue,
     * kept in the output so the sentence still reads naturally: "Mr [NAME] asked ...".
     */
    private static final Pattern CUED_NAME = Pattern.compile(
            "(\\b(?i:my name is)\\s+|\\b(?:Mr|Mrs|Ms|Dr)\\.?\\s+)\\p{Lu}\\p{L}+(?:[ -]\\p{Lu}\\p{L}+)?");

    private static final int PHONE_MIN_DIGITS = 9;
    private static final int INTERNATIONAL_PHONE_MIN_DIGITS = 8;
    private static final int PHONE_MAX_DIGITS = 15;

    @Override
    public String mask(String text) {
        String masked = EMAIL.matcher(text).replaceAll(EMAIL_TAG);
        masked = IBAN.matcher(masked).replaceAll(IBAN_TAG);
        masked = PHONE_CANDIDATE.matcher(masked).replaceAll(this::phoneTagOrUnchanged);
        masked = ID.matcher(masked).replaceAll(ID_TAG);
        masked = CUED_NAME.matcher(masked).replaceAll("$1" + NAME_TAG);
        return masked;
    }

    private String phoneTagOrUnchanged(MatchResult matchResult) {
        return isPhoneNumber(matchResult.group()) ? PHONE_TAG : Matcher.quoteReplacement(matchResult.group());
    }

    /**
     * Decides whether a phone-shaped candidate is a phone number.
     * - It must start with '+' or contain a separator; a bare digit run is left for the ID rule.
     * - At most 15 digits (the E.164 maximum).
     * - At least 9 digits, or 8 with a leading '+'. Dates (14.11.2026, 2026-11-14) have 8 digits
     *   and no '+', so they are not phones; the cost is that short local numbers (123-4567) are missed.
     */
    private boolean isPhoneNumber(String candidate) {
        long digitCount = candidate.chars().filter(Character::isDigit).count();
        boolean international = candidate.startsWith("+");
        boolean separated = candidate.chars().anyMatch(character -> !Character.isDigit(character) && character != '+');
        if (digitCount > PHONE_MAX_DIGITS || (!international && !separated)) {
            return false;
        }
        return digitCount >= (international ? INTERNATIONAL_PHONE_MIN_DIGITS : PHONE_MIN_DIGITS);
    }
}

package dev.citedqa.privacy;

/**
 * Replaces personal data in a text with type tags such as [EMAIL] before the
 * text leaves the service.
 */
public interface PiiMasker {

    /**
     * @return the text with emails, phone numbers, IBANs, ID numbers and cued names
     *         replaced by [EMAIL], [PHONE], [IBAN], [ID] and [NAME]
     */
    String mask(String text);
}

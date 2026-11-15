package dev.citedqa.corpus;

/**
 * One document to load, with the parser rules that differ between documents.
 *
 * @param name            short name used in sources, for example "GDPR" or "AI Act"
 * @param resource        classpath location of the text file
 * @param startMarker     the exact line that ends the recitals; articles start after it
 * @param endMarkerPrefix start of the line after the last article; it and everything after it are skipped
 * @param divisionHeading regex for a whole chapter or section heading line, for example "CHAPTER [IVXLC]+|Section \\d+"
 */
public record DocumentSource(String name, String resource, String startMarker, String endMarkerPrefix,
                             String divisionHeading) {
}

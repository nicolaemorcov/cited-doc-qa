package dev.citedqa.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class CorpusLoaderImpl implements CorpusLoader {

    private static final Logger log = LoggerFactory.getLogger(CorpusLoaderImpl.class);

    private static final String DOCUMENT = "GDPR";
    private static final String RESOURCE_PATH = "corpus/gdpr-en.txt";
    private static final String START_MARKER = "HAVE ADOPTED THIS REGULATION:";
    private static final String END_MARKER_PREFIX = "This Regulation shall be binding in its entirety";
    private static final char NO_BREAK_SPACE = 0x00A0;
    private static final Pattern ARTICLE_HEADING = Pattern.compile("Article (\\d+)");
    private static final Pattern DIVISION_HEADING = Pattern.compile("CHAPTER [IVXLC]+|Section \\d+");

    @Override
    public List<Article> load() {
        List<String> lines = readLines();
        List<String> articleLines = extractArticleLines(lines);
        List<Article> articles = splitIntoArticles(articleLines);
        log.info("Loaded {} articles from {}", articles.size(), RESOURCE_PATH);
        return articles;
    }

    /**
     * Reads the resource line by line. BufferedReader.readLine() handles CRLF.
     * Each line has U+00A0 replaced by a normal space, then is stripped.
     * The replacement must come first: String.strip() does not treat U+00A0 as whitespace.
     */
    private List<String> readLines() {
        ClassPathResource classPathResource = new ClassPathResource(RESOURCE_PATH);
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(classPathResource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line.replace(NO_BREAK_SPACE, ' ').strip());
            }
        } catch (IOException ioException) {
            throw new UncheckedIOException("Cannot read corpus resource " + RESOURCE_PATH, ioException);
        }
        return lines;
    }

    /**
     * Returns the lines strictly between the start marker (end of the recitals)
     * and the end marker (start of the closing formula after Article 99).
     */
    private List<String> extractArticleLines(List<String> lines) {
        int startIndex = lines.indexOf(START_MARKER);
        if (startIndex < 0) {
            throw new IllegalStateException("Start marker not found in " + RESOURCE_PATH);
        }
        int endIndex = -1;
        for (int index = startIndex + 1; index < lines.size(); index++) {
            if (lines.get(index).startsWith(END_MARKER_PREFIX)) {
                endIndex = index;
                break;
            }
        }
        if (endIndex < 0) {
            throw new IllegalStateException("End marker not found in " + RESOURCE_PATH);
        }
        return List.copyOf(lines.subList(startIndex + 1, endIndex));
    }

    /**
     * Splits the article section of the document into Article records.
     *
     * Input: every line between the two markers, already stripped and with
     * U+00A0 replaced. Empty lines are still present.
     *
     * Rules:
     * - A heading is a line that is exactly "Article N". It starts a new article.
     * - The title is the next non-empty line after the heading.
     * - A "CHAPTER ..." or "Section ..." line, and the next non-empty line
     *   (its heading), sit between articles and belong to no article.
     * - Every other non-empty line belongs to the body of the current article.
     *
     * Throws IllegalStateException when the input breaks these rules,
     * for example a heading with no title line or body text before the first heading.
     */
    private List<Article> splitIntoArticles(List<String> articleLines) {
        List<Article> articles = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        ParserState parserState = ParserState.OUTSIDE_ARTICLE;
        int number = 0;
        String title = null;

        for (String line : articleLines) {
            if (line.isEmpty()) {
                continue;
            }
            Matcher matcher = ARTICLE_HEADING.matcher(line);
            boolean articleHeading = matcher.matches();
            boolean divisionHeading = DIVISION_HEADING.matcher(line).matches();

            parserState = switch (parserState) {
                case OUTSIDE_ARTICLE -> {
                    if (articleHeading) {
                        number = Integer.parseInt(matcher.group(1));
                        yield ParserState.EXPECT_TITLE;
                    }
                    if (divisionHeading) {
                        yield ParserState.SKIP_DIVISION_TITLE;
                    }
                    throw new IllegalStateException("Text outside any article in " + RESOURCE_PATH);
                }
                case EXPECT_TITLE -> {
                    if (articleHeading || divisionHeading) {
                        throw new IllegalStateException("Article " + number + " has no title in " + RESOURCE_PATH);
                    }
                    title = line;
                    yield ParserState.IN_ARTICLE;
                }
                case IN_ARTICLE -> {
                    if (!articleHeading && !divisionHeading) {
                        bodyLines.add(line);
                        yield ParserState.IN_ARTICLE;
                    }
                    articles.add(toArticle(number, title, bodyLines));
                    bodyLines.clear();
                    if (articleHeading) {
                        number = Integer.parseInt(matcher.group(1));
                        yield ParserState.EXPECT_TITLE;
                    }
                    yield ParserState.SKIP_DIVISION_TITLE;
                }
                case SKIP_DIVISION_TITLE -> ParserState.OUTSIDE_ARTICLE;
            };
        }

        if (parserState != ParserState.IN_ARTICLE) {
            throw new IllegalStateException("Article section ends in state " + parserState + " in " + RESOURCE_PATH);
        }
        articles.add(toArticle(number, title, bodyLines));
        return articles;
    }

    private Article toArticle(int number, String title, List<String> bodyLines) {
        return new Article(DOCUMENT, number, title, String.join("\n", bodyLines));
    }

    /**
     * Where the parser is, relative to the article structure.
     * OUTSIDE_ARTICLE: before the first article, or after a CHAPTER/Section heading.
     * EXPECT_TITLE: an "Article N" line was just read; the next line is its title.
     * IN_ARTICLE: lines are body text of the current article.
     * SKIP_DIVISION_TITLE: a CHAPTER/Section line was just read; the next line is its heading.
     */
    private enum ParserState {
        OUTSIDE_ARTICLE,
        EXPECT_TITLE,
        IN_ARTICLE,
        SKIP_DIVISION_TITLE
    }
}

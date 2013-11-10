package net.thornydev.simplecsv;

public class CsvParserBuilder {
  char separator = CsvParser.DEFAULT_SEPARATOR;
  char quoteChar = CsvParser.DEFAULT_QUOTE_CHAR;
  char escapeChar = CsvParser.DEFAULT_ESCAPE_CHAR;
  boolean strictQuotes = CsvParser.DEFAULT_STRICT_QUOTES;
  boolean trimWhitespace = CsvParser.DEFAULT_TRIM_WS;
  boolean allowUnbalancedQuotes = CsvParser.DEFAULT_ALLOW_UNBALANCED_QUOTES;
  boolean retainOuterQuotes = CsvParser.DEFAULT_RETAIN_OUTER_QUOTES;

  CsvParserBuilder separator(final char separator) {
    this.separator = separator;
    return this;
  }

  CsvParserBuilder quoteChar(final char quoteChar) {
    this.quoteChar = quoteChar;
    return this;
  }

  CsvParserBuilder escapeChar(final char escapeChar) {
    this.escapeChar = escapeChar;
    return this;
  }

  CsvParserBuilder strictQuotes(final boolean strictQuotes) {
    this.strictQuotes = strictQuotes;
    return this;
  }

  CsvParserBuilder trimWhitespace(final boolean trimWhitespace) {
    this.trimWhitespace = trimWhitespace;
    return this;
  }

  CsvParserBuilder allowUnbalancedQuotes(final boolean allowUnbalancedQuotes) {
    this.allowUnbalancedQuotes = allowUnbalancedQuotes;
    return this;
  }

  CsvParserBuilder retainQuotes(final boolean retainQuoteChars) {
    this.retainOuterQuotes = retainQuoteChars;
    return this;
  }

  /**
   * Constructs Parser2
   */
  CsvParser build() {
    return new CsvParser(
        separator,
        quoteChar,
        escapeChar,
        strictQuotes,
        trimWhitespace,
        allowUnbalancedQuotes,
        retainOuterQuotes);
  }
}

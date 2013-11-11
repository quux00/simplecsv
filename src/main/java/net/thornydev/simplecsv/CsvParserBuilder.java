package net.thornydev.simplecsv;

public class CsvParserBuilder {
  char separator = CsvParser.DEFAULT_SEPARATOR;
  char quoteChar = CsvParser.DEFAULT_QUOTE_CHAR;
  char escapeChar = CsvParser.DEFAULT_ESCAPE_CHAR;
  boolean strictQuotes = CsvParser.DEFAULT_STRICT_QUOTES;
  boolean trimWhitespace = CsvParser.DEFAULT_TRIM_WS;
  boolean allowUnbalancedQuotes = CsvParser.DEFAULT_ALLOW_UNBALANCED_QUOTES;
  boolean retainOuterQuotes = CsvParser.DEFAULT_RETAIN_OUTER_QUOTES;

  public CsvParserBuilder separator(final char separator) {
    this.separator = separator;
    return this;
  }

  public CsvParserBuilder quoteChar(final char quoteChar) {
    this.quoteChar = quoteChar;
    return this;
  }

  public CsvParserBuilder escapeChar(final char escapeChar) {
    this.escapeChar = escapeChar;
    return this;
  }

  public CsvParserBuilder strictQuotes() {
    this.strictQuotes = true;
    return this;
  }

  public CsvParserBuilder trimWhitespace() {
    this.trimWhitespace = true;
    return this;
  }

  public CsvParserBuilder allowUnbalancedQuotes() {
    this.allowUnbalancedQuotes = true;
    return this;
  }

  public CsvParserBuilder retainOuterQuotes() {
    this.retainOuterQuotes = true;
    return this;
  }

  /**
   * Constructs Parser2
   */
  public CsvParser build() {
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

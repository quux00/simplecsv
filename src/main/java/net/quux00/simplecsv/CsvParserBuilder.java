package net.quux00.simplecsv;

public class CsvParserBuilder {
  char separator = ParserUtil.DEFAULT_SEPARATOR;
  char quoteChar = ParserUtil.DEFAULT_QUOTE_CHAR;
  char escapeChar = ParserUtil.DEFAULT_ESCAPE_CHAR;
  boolean strictQuotes = ParserUtil.DEFAULT_STRICT_QUOTES;
  boolean trimWhitespace = ParserUtil.DEFAULT_TRIM_WS;
  boolean allowUnbalancedQuotes = ParserUtil.DEFAULT_ALLOW_UNBALANCED_QUOTES;
  boolean retainOuterQuotes = ParserUtil.DEFAULT_RETAIN_OUTER_QUOTES;
  boolean retainEscapeChars = ParserUtil.DEFAULT_RETAIN_ESCAPE_CHARS;
  boolean alwaysQuoteOutput = ParserUtil.DEFAULT_ALWAYS_QUOTE_OUTPUT;
  MultiLineStatus supportsMultiLine = MultiLineStatus.DEFAULT;
  boolean allowDoubleEscapedQuotes = false;
  boolean threadSafe = false;
  
  private enum MultiLineStatus {
    DEFAULT, REQUESTED_TRUE, REQUESTED_FALSE;
  }
  
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

  public CsvParserBuilder strictQuotes(boolean beStrict) {
    this.strictQuotes = beStrict;
    return this;
  }

  public CsvParserBuilder trimWhitespace(boolean trim) {
    this.trimWhitespace = trim;
    return this;
  }

  public CsvParserBuilder allowUnbalancedQuotes(boolean allow) {
    this.allowUnbalancedQuotes = allow;
    return this;
  }

  public CsvParserBuilder retainOuterQuotes(boolean retain) {
    this.retainOuterQuotes = retain;
    return this;
  }

  public CsvParserBuilder retainEscapeChars(boolean retain) {
    this.retainEscapeChars = retain;
    return this;
  }
  
  public CsvParserBuilder alwaysQuoteOutput(boolean alwaysQuote) {
    this.alwaysQuoteOutput = alwaysQuote;
    return this;
  }

  public CsvParserBuilder multiLine(boolean multi) {
    if (multi) {
      supportsMultiLine = MultiLineStatus.REQUESTED_TRUE;
    } else {
      supportsMultiLine = MultiLineStatus.REQUESTED_FALSE;
    }
    return this;
  }
  
  public CsvParserBuilder allowDoubleEscapedQuotes(boolean rfc4180) {
    allowDoubleEscapedQuotes = rfc4180;
    return this;
  }
  
  public CsvParserBuilder threadSafe(boolean safe) {
    threadSafe = safe;
    return this;
  }
  
  
  /**
   * Constructs Parser
   */
  public CsvParser build() {
    if (supportsMultiLine == MultiLineStatus.REQUESTED_FALSE && 
        (allowDoubleEscapedQuotes || threadSafe)) {
      throw new IllegalStateException("Request of 'allowDoubleEscapedQuotes' or 'threadSafe' requires MultiLineParser");
    }
    
    if (supportsMultiLine == MultiLineStatus.REQUESTED_TRUE || 
        allowDoubleEscapedQuotes || threadSafe) {
      return new MultiLineCsvParser(
          separator,
          quoteChar,
          escapeChar,
          strictQuotes,
          trimWhitespace,
          allowUnbalancedQuotes,
          retainOuterQuotes,
          retainEscapeChars,
          alwaysQuoteOutput,
          allowDoubleEscapedQuotes);
    }
    
    return new SimpleCsvParser(
        separator,
        quoteChar,
        escapeChar,
        strictQuotes,
        trimWhitespace,
        allowUnbalancedQuotes,
        retainOuterQuotes,
        retainEscapeChars,
        alwaysQuoteOutput);
  }
}

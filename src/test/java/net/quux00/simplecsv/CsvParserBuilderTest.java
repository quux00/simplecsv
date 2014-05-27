package net.quux00.simplecsv;

import static org.junit.Assert.*;

import org.junit.Test;

public class CsvParserBuilderTest {

  @Test
  public void testCreateStandardSimpleParser() {
    CsvParser p = new CsvParserBuilder().build();
    assertTrue(p instanceof SimpleCsvParser);

    p = new CsvParserBuilder().allowDoubleEscapedQuotes(false).build();
    assertTrue(p instanceof SimpleCsvParser);
    
    p = new CsvParserBuilder().threadSafe(false).build();
    assertTrue(p instanceof SimpleCsvParser);

    p = new CsvParserBuilder().trimWhitespace(true).build();
    assertTrue(p instanceof SimpleCsvParser);

    p = new CsvParserBuilder().
        retainEscapeChars(true).
        strictQuotes(true).
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        build();
    assertTrue(p instanceof SimpleCsvParser);
    
    p = new CsvParserBuilder().
        threadSafe(false).
        allowDoubleEscapedQuotes(false).
        multiLine(false).
        retainEscapeChars(true).
        strictQuotes(true).
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        build();
    assertTrue(p instanceof SimpleCsvParser);
  }
  
  @Test
  public void testCreateMultiLineParser() {
    CsvParser p = new CsvParserBuilder().allowDoubleEscapedQuotes(true).build();
    assertTrue(p instanceof MultiLineCsvParser);
    
    p = new CsvParserBuilder().threadSafe(true).build();
    assertTrue(p instanceof MultiLineCsvParser);
    
    p = new CsvParserBuilder().multiLine(true).build();
    assertTrue(p instanceof MultiLineCsvParser);

    p = new CsvParserBuilder().threadSafe(true).allowDoubleEscapedQuotes(true).build();
    assertTrue(p instanceof MultiLineCsvParser);

    p = new CsvParserBuilder().threadSafe(false).allowDoubleEscapedQuotes(true).build();
    assertTrue(p instanceof MultiLineCsvParser);

    p = new CsvParserBuilder().threadSafe(true).allowDoubleEscapedQuotes(false).build();
    assertTrue(p instanceof MultiLineCsvParser);
    
    p = new CsvParserBuilder().
        threadSafe(false).
        allowDoubleEscapedQuotes(true).
        retainEscapeChars(true).
        strictQuotes(true).
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        build();
    assertTrue(p instanceof MultiLineCsvParser);
    
    p = new CsvParserBuilder().
        threadSafe(false).
        allowDoubleEscapedQuotes(false).
        multiLine(true).
        retainEscapeChars(true).
        strictQuotes(true).
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        build();
    assertTrue(p instanceof MultiLineCsvParser);
  }

  @Test(expected=IllegalStateException.class)
  public void testRequestThreadSafeAndSettingMultiLineSetToFalseThrowsException() {
    new CsvParserBuilder().multiLine(false).threadSafe(true).build();
  }
  
  @Test(expected=IllegalStateException.class)
  public void testRequestAllowDoubleEscapedQuotestAndSettingMultiLineSetToFalseThrowsException() {
    new CsvParserBuilder().multiLine(false).allowDoubleEscapedQuotes(true).build();
  }
}

package net.thornydev.simplecsv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CsvParserInternalsTest {

  CsvParser parser = new CsvParser();
  
  @Test
  public void testTrimDefaultSettings() {
    // default setting is to not trim white space and not retain outer quotes
    // so the "pluckOuterQuotes" method will be called
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = parser.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\"");
    exp = "music";
    act = parser.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\" ");
    exp = "music ";
    act = parser.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\"\"music\"\"");
    exp = "\"music\"";
    act = parser.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t    \" music \" \t ");
    exp = "\t     music  \t ";
    act = parser.trim(input);
    assertEquals(exp, act);
  }

  @Test
  public void testTrimWithTrimWhiteSpaceSettings() {
    CsvParser p = new CsvParserBuilder().trimWhitespace().build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\"");
    exp = "music";
    act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\" ");
    exp = "music";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\"\"music\"\"");
    exp = "\"music\"";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t    \" music \" \t ");
    exp = "music";
    act = p.trim(input);
    assertEquals(exp, act);
  }

  @Test
  public void testTrimWithRetainOuterQuotesSettings() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes().build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\"");
    exp = "\"music\"";
    act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\" ");
    exp = "\"music\" ";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\"\"music\"\"");
    exp = "\"\"music\"\"";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t    \" music \" \t ");
    exp = "\t    \" music \" \t ";
    act = p.trim(input);
    assertEquals(exp, act);
  }

  
  @Test
  public void testTrimWithRetainOuterQuotesAndTrimWhitespaceSettings() {
    CsvParser p = new CsvParserBuilder().
        retainOuterQuotes().
        trimWhitespace().
        build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\"");
    exp = "\"music\"";
    act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("\"music\" ");
    exp = "\"music\"";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\"\"music\"\"");
    exp = "\"\"music\"\"";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t    \" music \" \t ");
    exp = "\" music \"";
    act = p.trim(input);
    assertEquals(exp, act);
  }
  
  
  @Test
  public void testTrimWithRetainOuterQuotesAndTrimWhitespaceNonDefaultQuoteCharSettings() {
    CsvParser p = new CsvParserBuilder().
        retainOuterQuotes().
        trimWhitespace().
        quoteChar('\'').
        build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("'music'");
    exp = "'music'";
    act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("'music 123' ");
    exp = "'music 123'";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("''music''");
    exp = "''music''";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t    ' music ' \t ");
    exp = "' music '";
    act = p.trim(input);
    assertEquals(exp, act);
  }  
  
  @Test
  public void testTrimWithAllowUnbalancedQuotesSettings() {
    CsvParser p = new CsvParserBuilder().
        allowUnbalancedQuotes().
        build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("music ");
    exp = "music ";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t     music  \t ");
    exp = "\t     music  \t ";
    act = p.trim(input);
    assertEquals(exp, act);
  }  

  @Test
  public void testTrimWithAllowUbalancedQuotesAndTrimWhiteSpace() {
    CsvParser p = new CsvParserBuilder().
        allowUnbalancedQuotes().
        trimWhitespace().
        build();
    
    StringBuilder input = new StringBuilder("music");
    String exp = input.toString();
    String act = p.trim(input);
    assertEquals(exp, act);
    
    input = new StringBuilder("music ");
    exp = "music";
    act = p.trim(input);
    assertEquals(exp, act);

    input = new StringBuilder("\t     music  \t ");
    exp = "music";
    act = p.trim(input);
    assertEquals(exp, act);
  }  
  
  
  @Test
  public void testHandleRegular() {

  }

  @Test
  public void testHandleEscape() {

  }

  @Test
  public void testHandleQuote() {

  }

  @Test
  public void testTrimIfQuotesPresent() {
    String input = "music";
    String exp = input;
    String act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = "\"music\"";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = "\"music\" ";
    exp = "\"music\"";
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = " \"music\"";
    exp = "\"music\"";
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);

    input = "123\"music\"456";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);

    input = "\"\"music\"\"";
    exp = "\"\"music\"\"";
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = "\t\"music\"\t";
    exp = "\"music\"";
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);

    input = "\t    \" music \" \t ";
    exp = "\" music \"";
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = "\t";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);
    
    input = " a ";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);   

    input = "z";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);   

    input = "";
    exp = input;
    act = parser.trimIfOuterQuotesPresent(input);
    assertEquals(exp, act);   
  }
  

  
  @Test
  public void testPluckOuterQuotes() {
    String input = "music";
    String exp = input;
    String act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);
    
    input = "\"music\"";
    exp = "music";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);
    
    input = "\"music\" ";
    exp = "music ";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);

    input = " \"music\"";
    exp = " music";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);

    input = "123\"music\"456";
    exp = input;
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);

    input = "\"\"music\"\"";
    exp = "\"music\"";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);

    input = "\t\"music\"\t";
    exp = "\tmusic\t";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);

    input = "\t    \" music \" \t ";
    exp = "\t     music  \t ";
    act = parser.pluckOuterQuotes(input);
    assertEquals(exp, act);
  }
  

  @Test
  public void testTrimEdgeQuotes() {
    String input = "music";
    String exp = input;
    String act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);
    
    input = "\"music\"";
    exp = "music";
    act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);
    
    input = "\"music\" ";
    exp = input;
    act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);

    input = " \"music\"";
    exp = input;
    act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);

    input = "123\"music\"456";
    exp = input;
    act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);

    input = "\"\"music\"\"";
    exp = "\"music\"";
    act = parser.trimEdgeQuotes(input);
    assertEquals(exp, act);
  }

  @Test
  public void testTrimQuotes() {

  }

}

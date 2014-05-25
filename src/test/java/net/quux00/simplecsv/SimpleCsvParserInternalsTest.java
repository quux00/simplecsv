package net.quux00.simplecsv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SimpleCsvParserInternalsTest {

  SimpleCsvParser parser = new SimpleCsvParser();
  
  @Test
  public void testTrimWithTrimWhiteSpaceSettings() {
  SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().trimWhitespace(true).build();
  
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
  SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().retainOuterQuotes(true).build();
  
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
    SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().
        retainOuterQuotes(true).
        trimWhitespace(true).
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
    SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().
        retainOuterQuotes(true).
        trimWhitespace(true).
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
    SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().
        allowUnbalancedQuotes(true).
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
    SimpleCsvParser p = (SimpleCsvParser) new CsvParserBuilder().
        allowUnbalancedQuotes(true).
        trimWhitespace(true).
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
  public void testTrimDefaultParser() {
    // default: will remove outer quotes, but not trim whitespace
    StringBuilder sb = new StringBuilder("\"standard\" ");
    assertEquals("standard ", parser.trim(sb));
  
    sb.setLength(0);
    sb.append(" standard ");
    assertEquals(" standard ", parser.trim(sb));

    sb.setLength(0);
    sb.append("\"\"standard\"\"");
    assertEquals("\"standard\"", parser.trim(sb));

    sb.setLength(0);
    sb.append("\"\"");
    assertEquals("", parser.trim(sb));

    sb.setLength(0);
    sb.append("");
    assertEquals("", parser.trim(sb));
  }

 
}

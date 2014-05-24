package net.quux00.simplecsv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SimpleCsvParserInternalsTest {

  SimpleCsvParser parser = new SimpleCsvParser();
  
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

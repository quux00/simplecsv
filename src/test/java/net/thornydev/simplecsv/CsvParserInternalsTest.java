package net.thornydev.simplecsv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
    // ??
  }

  
  ////////////// EXPERIMENTAL /////////////
  
  @Test
  public void testReadLeftAndWhiteSpace2_StringWithSpacesOnBothSides() {
    //                                    01 234567 89
    StringBuilder sb = new StringBuilder("\t \" mail\" ");
    
    int exp = 2;
    int act = parser.readLeftWhiteSpace2(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 8;
    act = parser.readRightWhiteSpace2(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 2;
    act = parser.readLeftWhiteSpace2(sb, 2, sb.length()-1);
    assertEquals(exp, act);

    exp = 8;
    act = parser.readRightWhiteSpace2(sb, 2, 8);
    assertEquals(exp, act);
  
    exp = 4;
    act = parser.readLeftWhiteSpace2(sb, 3, 8);
    assertEquals(exp, act);    
  }


  @Test
  public void testReadRightWhiteSpace2() {
    //                                                 1
    //                                    01 234567 8 90
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int exp = 9;
    int act = parser.readRightWhiteSpace2(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 9;
    act = parser.readRightWhiteSpace2(sb, 2, 9);
    assertEquals(exp, act);
  
    exp = 7;
    act = parser.readRightWhiteSpace2(sb, 3, 8);
    assertEquals(exp, act);    
  }

  @Test
  public void testReadLeftAndWhiteSpace2_Various() {
    StringBuilder sb1 = new StringBuilder().append("");
    StringBuilder sb2 = new StringBuilder().append("hi there");
    StringBuilder sb3 = new StringBuilder().append("1");
    StringBuilder sb4 = new StringBuilder().append(" ");

    // sb1: empty string
    try {
      parser.readLeftWhiteSpace2(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}
    
    try {
      parser.readRightWhiteSpace2(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}


    // sb2: string with internal spaces only
    int exp = 0;
    int act = parser.readLeftWhiteSpace2(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    exp = sb2.length()-1;
    act = parser.readRightWhiteSpace2(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    // sb3: string of size 1 (not a space)
    exp = 0;
    act = parser.readLeftWhiteSpace2(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);

    exp = sb3.length()-1;
    act = parser.readRightWhiteSpace2(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);
    
    // sb4: string of size 1, which is a space char
    exp = 0;
    act = parser.readLeftWhiteSpace2(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);

    exp = sb4.length()-1;
    act = parser.readRightWhiteSpace2(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);
  }
  
  @Test
  public void testPluckOuterQuotes2SpacesOnBothSides() {
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("   mail\t ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnLeftSide() {
    StringBuilder sb = new StringBuilder("\t\"hi there\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("\thi there", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnRightSide() {
    StringBuilder sb = new StringBuilder("\"hi there\" ");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi there ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnNeitherSide() {
    StringBuilder sb = new StringBuilder("\"hi\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2NoQuotes() {
    StringBuilder sb = new StringBuilder(" 1-2-3 ");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3 ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2UnbalancedQuotesShouldNotBePlucked() {
    StringBuilder sb = new StringBuilder(" 1-2-3\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes2(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3\"", sb.toString());
  }

}

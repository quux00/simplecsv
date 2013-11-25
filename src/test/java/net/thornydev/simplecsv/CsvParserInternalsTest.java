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
    CsvParser p = new CsvParserBuilder().trimWhitespace(true).build();
    
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
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).build();
    
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
    CsvParser p = new CsvParserBuilder().
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
    CsvParser p = new CsvParserBuilder().
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
    CsvParser p = new CsvParserBuilder().
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
  
//  @Test
//  public void testTrimIfQuotesPresent() {
//    String input = "music";
//    String exp = input;
//    String act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\"";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\" ";
//    exp = "\"music\"";
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = " \"music\"";
//    exp = "\"music\"";
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//
//    input = "123\"music\"456";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//
//    input = "\"\"music\"\"";
//    exp = "\"\"music\"\"";
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = "\t\"music\"\t";
//    exp = "\"music\"";
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//
//    input = "\t    \" music \" \t ";
//    exp = "\" music \"";
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = "\t";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);
//    
//    input = " a ";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);   
//
//    input = "z";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);   
//
//    input = "";
//    exp = input;
//    act = parser.trimIfOuterQuotesPresent(input);
//    assertEquals(exp, act);   
//  }
//  
//
//  
//  @Test
//  public void testPluckOuterQuotes() {
//    String input = "music";
//    String exp = input;
//    String act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\"";
//    exp = "music";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\" ";
//    exp = "music ";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//
//    input = " \"music\"";
//    exp = " music";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//
//    input = "123\"music\"456";
//    exp = input;
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//
//    input = "\"\"music\"\"";
//    exp = "\"music\"";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//
//    input = "\t\"music\"\t";
//    exp = "\tmusic\t";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//
//    input = "\t    \" music \" \t ";
//    exp = "\t     music  \t ";
//    act = parser.pluckOuterQuotes(input);
//    assertEquals(exp, act);
//  }
//  
//
//  @Test
//  public void testTrimEdgeQuotes() {
//    String input = "music";
//    String exp = input;
//    String act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\"";
//    exp = "music";
//    act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//    
//    input = "\"music\" ";
//    exp = input;
//    act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//
//    input = " \"music\"";
//    exp = input;
//    act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//
//    input = "123\"music\"456";
//    exp = input;
//    act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//
//    input = "\"\"music\"\"";
//    exp = "\"music\"";
//    act = parser.trimEdgeQuotes(input);
//    assertEquals(exp, act);
//  }

  ////////////// EXPERIMENTAL /////////////
  
  @Test
  public void testReadLeftAndWhiteSpace2_StringWithSpacesOnBothSides() {
    //                                    01 234567 89
    StringBuilder sb = new StringBuilder("\t \" mail\" ");
    
    int exp = 2;
    int act = parser.readLeftWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 8;
    act = parser.readRightWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 2;
    act = parser.readLeftWhiteSpace(sb, 2, sb.length()-1);
    assertEquals(exp, act);

    exp = 8;
    act = parser.readRightWhiteSpace(sb, 2, 8);
    assertEquals(exp, act);
  
    exp = 4;
    act = parser.readLeftWhiteSpace(sb, 3, 8);
    assertEquals(exp, act);    
  }


  @Test
  public void testReadRightWhiteSpace2() {
    //                                                 1
    //                                    01 234567 8 90
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int exp = 9;
    int act = parser.readRightWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 9;
    act = parser.readRightWhiteSpace(sb, 2, 9);
    assertEquals(exp, act);
  
    exp = 7;
    act = parser.readRightWhiteSpace(sb, 3, 8);
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
      parser.readLeftWhiteSpace(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}
    
    try {
      parser.readRightWhiteSpace(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}


    // sb2: string with internal spaces only
    int exp = 0;
    int act = parser.readLeftWhiteSpace(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    exp = sb2.length()-1;
    act = parser.readRightWhiteSpace(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    // sb3: string of size 1 (not a space)
    exp = 0;
    act = parser.readLeftWhiteSpace(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);

    exp = sb3.length()-1;
    act = parser.readRightWhiteSpace(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);
    
    // sb4: string of size 1, which is a space char
    exp = 0;
    act = parser.readLeftWhiteSpace(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);

    exp = sb4.length()-1;
    act = parser.readRightWhiteSpace(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);
  }
  
  @Test
  public void testPluckOuterQuotes2SpacesOnBothSides() {
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("   mail\t ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnLeftSide() {
    StringBuilder sb = new StringBuilder("\t\"hi there\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("\thi there", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnRightSide() {
    StringBuilder sb = new StringBuilder("\"hi there\" ");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi there ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2SpacesOnNeitherSide() {
    StringBuilder sb = new StringBuilder("\"hi\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2NoQuotes() {
    StringBuilder sb = new StringBuilder(" 1-2-3 ");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3 ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotes2UnbalancedQuotesShouldNotBePlucked() {
    StringBuilder sb = new StringBuilder(" 1-2-3\"");
    int lenBefore = sb.length();
    parser.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3\"", sb.toString());
  }
  
  
  // idxTrimSpaces
  
  @Test
  public void testIdxTrimSpacesSingleSpaceOnLeft() {
    //idxTrimSpaces
    StringBuilder sb = new StringBuilder(" 1-2-3\"");
    int[] indexes = parser.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(1, indexes[0]);
    assertEquals(sb.length()-1, indexes[1]);
  }

  @Test
  public void testIdxTrimSpacesMultipleSpacesBothSides() {
    //                                    0 1234567890
    StringBuilder sb = new StringBuilder(" \tone two 3   ");
    int[] indexes = parser.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(2, indexes[0]);
    assertEquals(10, indexes[1]);
    assertEquals("one two 3", sb.subSequence(indexes[0], indexes[1]+1));

    indexes = parser.idxTrimSpaces(sb, 2, 10);
    assertEquals(2, indexes[0]);
    assertEquals(10, indexes[1]);

    indexes = parser.idxTrimSpaces(sb, 3, 9);
    assertEquals(3, indexes[0]);
    assertEquals(8, indexes[1]);
    assertEquals("ne two", sb.subSequence(indexes[0], indexes[1]+1));    
  }

  @Test
  public void testIdxTrimSpacesNoSpaces() {
    //                                    0123456
    StringBuilder sb = new StringBuilder("one two");
    int[] indexes = parser.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(0, indexes[0]);
    assertEquals(6, indexes[1]);
    assertEquals("one two", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimSpacesEdgeCases() {
    StringBuilder sb = new StringBuilder("");
    int[] indexes = parser.idxTrimSpaces(sb, 0, 0);
    assertEquals(2, indexes.length);
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);

    sb.append(" ");
    indexes = parser.idxTrimSpaces(sb, 0, 0);
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);    
  }
  
  
  //// trimEdgeQuotes
  
  @Test
  public void testIdxTrimEdgeQuotes() {
    //                                     012345678 9
    StringBuilder sb = new StringBuilder("\"standard\"");
    int[] indexes = parser.idxTrimEdgeQuotes(sb, 0, 9);
    assertEquals(2, indexes.length);
    assertEquals(1, indexes[0]);
    assertEquals(8, indexes[1]);
    assertEquals("standard", sb.substring(indexes[0], indexes[1]+1));
  }
  
  @Test
  public void testIdxTrimEdgeQuotesOnlyOneEdgeQuote() {
    StringBuilder sb = new StringBuilder("\"standard\" ");
    int[] indexes = parser.idxTrimEdgeQuotes(sb, 0, sb.length()-1);
    assertEquals("\"standard\" ", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimEdgeQuotesZeroEdgeQuote() {
    StringBuilder sb = new StringBuilder("standard\"");
    int[] indexes = parser.idxTrimEdgeQuotes(sb, 0, sb.length()-1);
    assertEquals("standard\"", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimEdgeQuotesEdgeCases() {
    StringBuilder sb = new StringBuilder("");
    int[] indexes = parser.idxTrimEdgeQuotes(sb, 0, 0);
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);

    sb.append("\"");
    indexes = parser.idxTrimEdgeQuotes(sb, 0, 1);
    assertEquals(0, indexes[0]);
    assertEquals(1, indexes[1]);

    sb.append("\"");
    indexes = parser.idxTrimEdgeQuotes(sb, 0, 1);
    assertEquals(1, indexes[0]);
    assertEquals(0, indexes[1]);
    assertEquals("", sb.substring(indexes[0], indexes[1]+1));
  }

  
  @Test
  public void testTrim2DefaultParser() {
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

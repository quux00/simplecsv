package net.quux00.simplecsv;

import static org.junit.Assert.*;

import org.junit.Test;

public class ParserUtilTest {

  
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
  public void testReadLeftAndWhiteSpace_StringWithSpacesOnBothSides() {
    //                                    01 234567 89
    StringBuilder sb = new StringBuilder("\t \" mail\" ");
    
    int exp = 2;
    int act = ParserUtil.readLeftWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 8;
    act = ParserUtil.readRightWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 2;
    act = ParserUtil.readLeftWhiteSpace(sb, 2, sb.length()-1);
    assertEquals(exp, act);

    exp = 8;
    act = ParserUtil.readRightWhiteSpace(sb, 2, 8);
    assertEquals(exp, act);
  
    exp = 4;
    act = ParserUtil.readLeftWhiteSpace(sb, 3, 8);
    assertEquals(exp, act);    
  }


  @Test
  public void testReadRightWhiteSpace() {
    //                                                 1
    //                                    01 234567 8 90
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int exp = 9;
    int act = ParserUtil.readRightWhiteSpace(sb, 0, sb.length()-1);
    assertEquals(exp, act);
  
    exp = 9;
    act = ParserUtil.readRightWhiteSpace(sb, 2, 9);
    assertEquals(exp, act);
  
    exp = 7;
    act = ParserUtil.readRightWhiteSpace(sb, 3, 8);
    assertEquals(exp, act);    
  }

  @Test
  public void testReadLeftAndWhiteSpace_Various() {
    StringBuilder sb1 = new StringBuilder().append("");
    StringBuilder sb2 = new StringBuilder().append("hi there");
    StringBuilder sb3 = new StringBuilder().append("1");
    StringBuilder sb4 = new StringBuilder().append(" ");

    // sb1: empty string
    try {
      ParserUtil.readLeftWhiteSpace(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}
    
    try {
      ParserUtil.readRightWhiteSpace(sb1, 0, 0);
      fail("Should not get here");
    } catch (StringIndexOutOfBoundsException e) {}


    // sb2: string with internal spaces only
    int exp = 0;
    int act = ParserUtil.readLeftWhiteSpace(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    exp = sb2.length()-1;
    act = ParserUtil.readRightWhiteSpace(sb2, 0, sb2.length()-1);
    assertEquals(exp, act);

    // sb3: string of size 1 (not a space)
    exp = 0;
    act = ParserUtil.readLeftWhiteSpace(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);

    exp = sb3.length()-1;
    act = ParserUtil.readRightWhiteSpace(sb3, 0, sb3.length()-1);
    assertEquals(exp, act);
    
    // sb4: string of size 1, which is a space char
    exp = 0;
    act = ParserUtil.readLeftWhiteSpace(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);

    exp = sb4.length()-1;
    act = ParserUtil.readRightWhiteSpace(sb4, 0, sb4.length()-1);
    assertEquals(exp, act);
  }
  
  @Test
  public void testPluckOuterQuotesSpacesOnBothSides() {
    StringBuilder sb = new StringBuilder("  \" mail\t\" ");
    
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("   mail\t ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotesSpacesOnLeftSide() {
    StringBuilder sb = new StringBuilder("\t\"hi there\"");
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("\thi there", sb.toString());
  }

  @Test
  public void testPluckOuterQuotesSpacesOnRightSide() {
    StringBuilder sb = new StringBuilder("\"hi there\" ");
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi there ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotesSpacesOnNeitherSide() {
    StringBuilder sb = new StringBuilder("\"hi\"");
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should have been shortened", lenBefore > sb.length());
    assertEquals("hi", sb.toString());
  }

  @Test
  public void testPluckOuterQuotesNoQuotes() {
    StringBuilder sb = new StringBuilder(" 1-2-3 ");
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3 ", sb.toString());
  }

  @Test
  public void testPluckOuterQuotesUnbalancedQuotesShouldNotBePlucked() {
    StringBuilder sb = new StringBuilder(" 1-2-3\"");
    int lenBefore = sb.length();
    ParserUtil.pluckOuterQuotes(sb, 0, sb.length() - 1);
    assertTrue("length should NOT have been shortened", lenBefore == sb.length());
    assertEquals(" 1-2-3\"", sb.toString());
  }
  
  
  // idxTrimSpaces
  
  @Test
  public void testIdxTrimSpacesSingleSpaceOnLeft() {
    //idxTrimSpaces
    StringBuilder sb = new StringBuilder(" 1-2-3\"");
    int[] indexes = ParserUtil.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(1, indexes[0]);
    assertEquals(sb.length()-1, indexes[1]);
  }

  @Test
  public void testIdxTrimSpacesMultipleSpacesBothSides() {
    //                                    0 1234567890
    StringBuilder sb = new StringBuilder(" \tone two 3   ");
    int[] indexes = ParserUtil.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(2, indexes[0]);
    assertEquals(10, indexes[1]);
    assertEquals("one two 3", sb.subSequence(indexes[0], indexes[1]+1));

    indexes = ParserUtil.idxTrimSpaces(sb, 2, 10);
    assertEquals(2, indexes[0]);
    assertEquals(10, indexes[1]);

    indexes = ParserUtil.idxTrimSpaces(sb, 3, 9);
    assertEquals(3, indexes[0]);
    assertEquals(8, indexes[1]);
    assertEquals("ne two", sb.subSequence(indexes[0], indexes[1]+1));    
  }

  @Test
  public void testIdxTrimSpacesNoSpaces() {
    //                                    0123456
    StringBuilder sb = new StringBuilder("one two");
    int[] indexes = ParserUtil.idxTrimSpaces(sb, 0, sb.length()-1);
    assertEquals(2, indexes.length);
    assertEquals(0, indexes[0]);
    assertEquals(6, indexes[1]);
    assertEquals("one two", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimSpacesEdgeCases() {
    StringBuilder sb = new StringBuilder("");
    int[] indexes = ParserUtil.idxTrimSpaces(sb, 0, 0);
    assertEquals(2, indexes.length);
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);

    sb.append(" ");
    indexes = ParserUtil.idxTrimSpaces(sb, 0, 0);
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);    
  }  
  
  // ParserUtil tests
  @Test
  public void testEnsureQuotedNoQuotesNoSpacesOnEdge() {
    StringBuilder sb = new StringBuilder();
    sb.append("123456");
    
    String rt = ParserUtil.ensureQuoted(sb, 0, sb.length()-1, '"');
    assertEquals("\"123456\"", rt);
  }
  
  @Test
  public void testEnsureQuotedEmptyStringShouldNotBeQuoted() {
    StringBuilder sb = new StringBuilder(32);
    
    String rt = ParserUtil.ensureQuoted(sb, 0, sb.length()-1, '"');
    assertEquals("", rt);
  }

  @Test
  public void testEnsureQuotedNoQuotesSpacesOnEdge() {
    StringBuilder sb = new StringBuilder(32);
    sb.append("\t123456   ");
    
    String rt = ParserUtil.ensureQuoted(sb, 0, sb.length()-1, '"');
    assertEquals("\"\t123456   \"", rt);
  }

  @Test
  public void testEnsureQuotedNoQuotesSpacesOnEdgeInternalIndices() {
    StringBuilder sb = new StringBuilder(32);
    sb.append("\t123456   ");
    
    String rt = ParserUtil.ensureQuoted(sb, 1, 6, '"');
    assertEquals("\"123456\"", rt);
  }

  @Test
  public void testEnsureQuotedQuotesWithOuterSpaces() {
    StringBuilder sb = new StringBuilder();
    //          0 12345678 901
    sb.append("\t\"123456 \"  ");
    
    String rt = ParserUtil.ensureQuoted(sb, 0, sb.length()-1, '"');
    assertEquals("\t\"123456 \"  ", rt);
  }

  @Test
  public void testEnsureQuotedQuotesWithOuterSpacesRightSideOnly() {
    StringBuilder sb = new StringBuilder(32);
    //          0 12345678 901
    sb.append("\t\"123456 \"  ");
    
    String rt = ParserUtil.ensureQuoted(sb, 1, sb.length()-1, '"');
    assertEquals("\"123456 \"  ", rt);
  }
  
  @Test
  public void testEnsureQuotedQuotesWithOuterSpacesLeftSideOnly() {
    StringBuilder sb = new StringBuilder();
    //          0 12345678 901
    sb.append("\t\"123456 \"  ");
    
    String rt = ParserUtil.ensureQuoted(sb, 0, 9, '"');
    assertEquals("\t\"123456 \"", rt);
  }

  @Test
  public void testIdxTrimEdgeQuotes() {
    //                                     012345678 9
    StringBuilder sb = new StringBuilder("\"standard\"");
    int[] indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, 9, '"');
    assertEquals(2, indexes.length);
    assertEquals(1, indexes[0]);
    assertEquals(8, indexes[1]);
    assertEquals("standard", sb.substring(indexes[0], indexes[1]+1));
  }
  
  @Test
  public void testIdxTrimEdgeQuotesOnlyOneEdgeQuote() {
    StringBuilder sb = new StringBuilder("\"standard\" ");
    int[] indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, sb.length()-1, '"');
    assertEquals("\"standard\" ", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimEdgeQuotesZeroEdgeQuote() {
    StringBuilder sb = new StringBuilder("standard\"");
    int[] indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, sb.length()-1, '"');
    assertEquals("standard\"", sb.substring(indexes[0], indexes[1]+1));
  }

  @Test
  public void testIdxTrimEdgeQuotesEdgeCases() {
    StringBuilder sb = new StringBuilder("");
    int[] indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, 0, '"');
    assertEquals(0, indexes[0]);
    assertEquals(0, indexes[1]);

    sb.append("\"");
    indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, 1, '"');
    assertEquals(0, indexes[0]);
    assertEquals(1, indexes[1]);

    sb.append("\"");
    indexes = ParserUtil.idxTrimEdgeQuotes(sb, 0, 1, '"');
    assertEquals(1, indexes[0]);
    assertEquals(0, indexes[1]);
    assertEquals("", sb.substring(indexes[0], indexes[1]+1));
  }


}

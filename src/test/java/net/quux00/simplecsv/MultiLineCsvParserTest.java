package net.quux00.simplecsv;

import static net.quux00.simplecsv.ParserUtil.DEFAULT_QUOTE_CHAR;
import static net.quux00.simplecsv.ParserUtil.DEFAULT_SEPARATOR;
import static net.quux00.simplecsv.ParserUtil.NULL_CHARACTER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class MultiLineCsvParserTest {

  static final String longEntry1 = "aaaaaaaaaaaaaaaaaaaaaaaaaa bbbbbbbbbbbbbbbbbbbbbbbbbb cccccccccccccccccccccccccccccccccccccc ddddddddddddddddddddddddddddd efg 123456789012345678901234567890";
  static final String longEntry2 = "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.";
  static final String longLine = String.format("%s,, \"%s\"", longEntry1, longEntry2);
  static final String CR = "\r";
  static final String LF = "\n";
  static final String CRLF = CR + LF;

  CsvParser parser = null;

  @Before
  public void setUp() {
    parser = new MultiLineCsvParser();
  }

  /* --------------------------------- */
  /* ---[ Tests parser invariants ]--- */
  /* --------------------------------- */
  @Test(expected = UnsupportedOperationException.class)
  public void quoteAndEscapeCannotBeTheSameViaParserCtor() {
    boolean allowDoubleEscapedQUotes = false;
    new MultiLineCsvParser(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHAR, DEFAULT_QUOTE_CHAR,
        false, false, false, false, true, false, allowDoubleEscapedQUotes);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void quoteAndEscapeCannotBeTheSameViaBuilder() {
    new CsvParserBuilder().quoteChar(DEFAULT_QUOTE_CHAR).
    escapeChar(DEFAULT_QUOTE_CHAR).
    multiLine(true).
    build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void separatorCharacterCannotBeNull() {
    new CsvParserBuilder().multiLine(true).separator(NULL_CHARACTER).build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void separatorAndEscapeCannotBeTheSame() {
    new CsvParserBuilder().multiLine(true).separator(DEFAULT_SEPARATOR).escapeChar(DEFAULT_SEPARATOR).build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void separatorAndQuoteCannotBeTheSame() {
    new CsvParserBuilder().multiLine(true).separator(DEFAULT_SEPARATOR).quoteChar(DEFAULT_SEPARATOR).build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void nullQuoteCharAndAlwaysQuoteOutputAreIncompatible() {
    new CsvParserBuilder().
      multiLine(true).
      quoteChar(ParserUtil.NULL_CHARACTER).
      alwaysQuoteOutput(true).
      build();
  }

  /* -------------------------------------------- */
  /* ---[ Tests with Default Parser Settings ]--- */
  /* -------------------------------------------- */
  @Test
  public void testParseLine() {
    String toks[] = parser.parse("This, is, a, test.");
    assertEquals(4, toks.length);
    assertEquals("This", toks[0]);
    assertEquals(" is", toks[1]);
    assertEquals(" a", toks[2]);
    assertEquals(" test.", toks[3]);
  }

  @Test
  public void parseSimpleQuotedString() {
    String[] toks = parser.parse("\"a\",\"b\",\"c\"");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseSimpleQuotedStringWithSpaces() {
    String[] toks = parser.parse(" \"a\" , \"b\" , \"c\" ");
    assertEquals(3, toks.length);
    assertEquals(" a ", toks[0]);
    assertEquals(" b ", toks[1]);
    assertEquals(" c ", toks[2]);
  }

  @Test
  public void testParsedLineWithInternalQuota() {
    String[] toks = parser.parse("a,123\"4\"567,c");
    assertEquals(3, toks.length);
    assertEquals("123\"4\"567", toks[1]);
  }

  @Test
  public void parseQuotedStringWithCommas() {
    String[] toks = parser.parse("a,\"b,b,b\",c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b,b,b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseEmptyElements() {
    String[] toks = parser.parse(",,");
    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
  }

  // RFC4180 examples from: https://en.wikipedia.org/wiki/Comma-separated_values
  // This shows how simplecsv rejects the "quotes as escape chars" philosophy of that RFC
  @Test
  public void testRFC4180Examples() {
    String text = "1997,Ford,E350,\"Super, \"\"luxurious\"\" truck\"";
    String[] toks = parser.parse(text);

    assertEquals(4, toks.length);
    assertEquals("1997", toks[0]);
    assertEquals("Ford", toks[1]);
    assertEquals("E350", toks[2]);
    assertEquals("Super, \"\"luxurious\"\" truck", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElement() {
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = parser.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("test", toks[0]);
    assertEquals("this,test,is,good", toks[1]);
    assertEquals("\\\"test\\\"", toks[2]);
    assertEquals("\\\"quote\\\"", toks[3]);
  }

  @Test
  public void parseMultipleQuotes() {
    String[] toks = parser.parse("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
    assertEquals(2, toks.length);
    assertEquals("\"\"\"\"", toks[0]);  // check the tricky situation
    assertEquals("test", toks[1]);    // make sure we didn't ruin the next field..
  }

  /**
   * A carriage return at the end of an unquoted field is always a record
   * terminator
   *
   * @throws IOException
   */
  @Test
  public void parseTrickyString1() {
    String[] toks = parser.parse("\"a\nb\",b,\"\nd\",e\n");
    assertEquals(4, toks.length);
    assertEquals("a\nb", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("\nd", toks[2]);
    assertNotEquals("e\n", toks[3]);
  }

  /**
   * So then this is how it should be.
   *
   * @throws IOException
   */
  @Test
  public void parseTrickyString2() {
    String[] toks = parser.parse("\"a\nb\",b,\"\nd\",e\n");
    assertEquals(4, toks.length);
    assertEquals("a\nb", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("\nd", toks[2]);
    assertEquals("e", toks[3]);
  }

  @Test
  public void testAMultiLineInsideQuotes() {
    String text = "Small test,\"This is a test across \ntwo lines.\"";
    String[] toks = parser.parse(text);
    assertEquals(2, toks.length);
    assertEquals("Small test", toks[0]);
    assertEquals("This is a test across \ntwo lines.", toks[1]);
  }

  /**
   * Test issue 2726363
   * <p/>
   * Data given:
   * <p/>
   * "804503689","London",""London""shop","address","116.453182","39.918884"
   * "453074125","NewYork","brief","address"","121.514683","31.228511"
   */
  @Test
  public void testIssue2726363() {
    String[] toks = parser.parse("\"804503689\",\"London\",\"\"London\"shop\",\"address\",\"116.453182\",\"39.918884\"");

    assertEquals(6, toks.length);
    assertEquals("804503689", toks[0]);
    assertEquals("London", toks[1]);
    assertEquals("\"London\"shop", toks[2]);
    assertEquals("address", toks[3]);
    assertEquals("116.453182", toks[4]);
    assertEquals("39.918884", toks[5]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void anExceptionThrownifStringEndsInsideAQuotedString() {
    parser.parse("This,is a \"bad line to parse.");
  }

  @Test
  public void returnNullWhenNullPassedIn() {
    assertNull(parser.parse(null));
  }

  @Test
  public void testInternalQuotes() {
    String[] toks = parser.parse("a , \"b\",1000");
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals(" b", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void testInternalQuotedQuotes() {
    String[] toks = parser.parse("a , \"\\\"\",1000");  // a, "\"",1000
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals(" \\\"", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void testADoubleQuoteAsDataElement() {
    String[] toks = parser.parse("a,\"\"\"\",c");  // a,"""",c

    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\"\"", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testIssueThorny1a() {
    String[] toks = parser.parse("a , \"\",1000");  // a, "",1000

    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals(" ", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void testIssueThorny1b() {
    String[] toks = parser.parse("a , \"\" ,1000");  // a, "" ,1000
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals("  ", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void testIssueThorny1c() {
    String[] toks = parser.parse("a ,Mike \"The Situation\" Sorrentino,1000");
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals("Mike \"The Situation\" Sorrentino", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void testIssueThorny1d() {
    String[] toks = parser.parse("a,\" \"hello\" \",c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals(" \"hello\" ", toks[1]);
    assertEquals("c", toks[2]);

    CsvParser p = new CsvParserBuilder().multiLine(true).quoteChar('\'').build();
    toks = p.parse("a,' 'hello' ',c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals(" 'hello' ", toks[1]);
    assertEquals("c", toks[2]);

    p = new CsvParserBuilder().multiLine(true).quoteChar('\'').trimWhitespace(true).build();
    toks = p.parse("a,' 'hello' ',c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("'hello'", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void whitespaceBeforeEscape() {
    String[] toks = parser.parse("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    assertEquals("this", toks[0]);
    assertEquals(" is", toks[1]);
    assertEquals("a test", toks[2]);
  }

  @Test
  public void testFourSingleQuotes() {
    String[] toks = parser.parse("a,'\'\'', c ");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("''''", toks[1]);
    assertEquals(4, toks[1].length());
    assertEquals(" c ", toks[2]);
  }

  @Test
  public void testLongTokens() {
    String[] toks = parser.parse(longLine);
    assertEquals(3, toks.length);
    assertEquals(longEntry1, toks[0]);
    assertEquals("", toks[1]);
    assertEquals(" " + longEntry2, toks[2]);
  }

  /* -------------------------------------------- */
  /* ---[ Alternative Delims and Quote Chars ]--- */
  /* -------------------------------------------- */
  @Test
  public void testParseLinePipeDelimited() {
    CsvParser p = new CsvParserBuilder().multiLine(true).separator('|').build();
    String toks[] = p.parse("This|is|a|test.");
    assertEquals(4, toks.length);
    assertEquals("This", toks[0]);
    assertEquals("is", toks[1]);
    assertEquals("a", toks[2]);
    assertEquals("test.", toks[3]);
  }

  @Test
  public void parseQuotedStringWithDefinedSeperator() {
    CsvParser p = new CsvParserBuilder().multiLine(true).separator(':').build();

    String[] toks = p.parse("a:\"b:b:b\":c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b:b:b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseQuotedStringWithDefinedSeparatorAndQuote() {
    CsvParser p = new CsvParserBuilder().separator(':').quoteChar('\'').multiLine(true).build();

    String[] toks = p.parse("a:'b:b:b':c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b:b:b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test  // issue from the old opencsv sourceforge project
  public void testIssue2859181() {
    CsvParser p = new CsvParserBuilder().separator(';').multiLine(true).build();
    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("field1", toks[0]);
    assertEquals("\\=field2", toks[1]);
    assertEquals("\"\"field3\"\"", toks[2]);
  }

  @Test    // https://sourceforge.net/p/opencsv/bugs/93/
  public void testIssueSfBugs93() {
    CsvParser p = new CsvParserBuilder().separator(';').multiLine(true).build();

    String[] toks = p.parse("\"\";1");
    assertEquals(2, toks.length);
    assertEquals("", toks[0]);
    assertEquals("1", toks[1]);

    toks = p.parse("\"\";2");
    assertEquals(2, toks.length);
    assertEquals("", toks[0]);
    assertEquals("2", toks[1]);
  }

  @Test
  public void testFourSingleQuotesWithSingleQuoteAsQuoteChar() {
    CsvParser p = new CsvParserBuilder().
        quoteChar('\'').
        multiLine(true).
        build();

    String[] toks = p.parse("a,'\'\'',c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\'\'", toks[1]);
    assertEquals("c", toks[2]);
  }

  /**
   * This is an interesting issue where the data does not use quotes but IS
   * using a quote within the field as a inch symbol. So we want to keep that
   * quote as part of the field and not as the start or end of a field.
   *
   * Test data is as follows.
   *
   * RPO;2012;P; ; ; ;SDX;ACCESSORY WHEEL, 16", ALUMINUM, DESIGN 1 RPO;2012;P;
   * ; ; ;SDZ;ACCESSORY WHEEL - 17" - ALLOY - DESIGN 1
   */
  @Test
  public void testIssue3314579() {
    // difference from OpenCSV: cleaner soln is to set quotechar to NULL_CHAR
    CsvParser p = new CsvParserBuilder().
        separator(';').
        quoteChar(ParserUtil.NULL_CHARACTER).
        allowUnbalancedQuotes(true).
        multiLine(true).
        build();
    String testString = "RPO;2012;P; ; ; ;SDX;ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1";

    String[] toks = p.parse(testString);
    assertEquals(8, toks.length);
    assertEquals("RPO", toks[0]);
    assertEquals("2012", toks[1]);
    assertEquals("P", toks[2]);
    assertEquals(" ", toks[3]);
    assertEquals(" ", toks[4]);
    assertEquals(" ", toks[5]);
    assertEquals("SDX", toks[6]);
    assertEquals("ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1", toks[7]);

    // you don't need "allowUnbalancedQuotes" if you set quotechar to be the NULL_CHAR
    p = new CsvParserBuilder().
        separator(';').
        quoteChar(ParserUtil.NULL_CHARACTER).
        multiLine(true).
        build();
    testString = "RPO;2012;P; ; ; ;SDX;ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1";

    toks = p.parse(testString);
    assertEquals(8, toks.length);
    assertEquals("RPO", toks[0]);
    assertEquals("2012", toks[1]);
    assertEquals("P", toks[2]);
    assertEquals(" ", toks[3]);
    assertEquals(" ", toks[4]);
    assertEquals(" ", toks[5]);
    assertEquals("SDX", toks[6]);
    assertEquals("ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1", toks[7]);

    // this combination doesn't make any sense, but ensure it doesn't throw
    // NPEs or otherwise freak out
    p = new CsvParserBuilder().
        separator(';').
        quoteChar(ParserUtil.NULL_CHARACTER).
        retainOuterQuotes(true).
        multiLine(true).
        build();
    testString = "RPO;2012;P; ; ; ;SDX;ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1";

    toks = p.parse(testString);
    assertEquals(8, toks.length);
    assertEquals("RPO", toks[0]);
    assertEquals("2012", toks[1]);
    assertEquals("P", toks[2]);
    assertEquals(" ", toks[3]);
    assertEquals(" ", toks[4]);
    assertEquals(" ", toks[5]);
    assertEquals("SDX", toks[6]);
    assertEquals("ACCESSORY WHEEL, 16\", ALUMINUM, DESIGN 1", toks[7]);
  }

  /**
   * Test issue 2263439 where an escaped quote was causing the parse to fail.
   */
  @Test
  public void testIssue2263439() {
    CsvParser p = new CsvParserBuilder().
        quoteChar('\'').
        multiLine(true).
        build();

    String text = "865,0,'AmeriKKKa\\'s_Most_Wanted','',294,0,0,0.734338696798625,'20081002052147',242429208,18448";
    String[] toks = p.parse(text);

    assertEquals(11, toks.length);
    assertEquals("865", toks[0]);
    assertEquals("0", toks[1]);
    assertEquals("AmeriKKKa\\'s_Most_Wanted", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("18448", toks[10]);
  }

  // https://sourceforge.net/p/opencsv/bugs/100/
  @Test
  public void testOpenCsvIssue100() {
    CsvParser p = new CsvParserBuilder().
        escapeChar(NULL_CHARACTER).
        retainOuterQuotes(true).
        trimWhitespace(true).
        multiLine(true).
        build();

    String text = "\\x0\"\", two, \"three,four\"";
    String[] toks = p.parse(text);

    assertEquals(3, toks.length);
    assertEquals("\\x0\"\"", toks[0]);
    assertEquals("two", toks[1]);
    assertEquals("\"three,four\"", toks[2]);
  }

  /* ------------------------------- */
  /* ---[ Strict Quotes Setting ]--- */
  /* ------------------------------- */
  @Test
  public void parseSimpleQuotedStringWithSpacesWithstrictQuotes() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();

    String[] toks = p.parse(" \"a\" , \"b\" , \"c\" ");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testStrictQuoteSimple() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();
    String testString = "\"a\",\"b\",\"c\"";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testStrictQuoteWithSpacesAndTabs() {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String testString = " \t      \"a\",\"b\"      \t       ,   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void spacesAtEndOfQuotedStringDoNotCountIfStrictQuotesIsTrue() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();
    String[] toks = p.parse("\"Line with\", \"spaces at end\"  ");
    assertEquals(2, toks.length);
    assertEquals("Line with", toks[0]);
    assertEquals("spaces at end", toks[1]);
  }

  @Test
  public void testStrictQuoteWithGarbage() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();
    String testString = "abc',!@#\",\\\"\"   xyz,";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals(",\\\"", toks[1]);
    assertEquals("", toks[2]);
  }

  @Test
  public void testWhitespaceBeforeEscapeWithStrictQuotes() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();

    String[] toks = p.parse("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    assertEquals("this", toks[0]);
    assertEquals("is", toks[1]);
    assertEquals("a test", toks[2]);
  }

  @Test
  public void testSomeFieldsWithoutQuotesWithStrictQuotes() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();

    String[] toks = p.parse("this, \"is\",\"a test\" xyz");
    assertEquals("", toks[0]);
    assertEquals("is", toks[1]);
    assertEquals("a test", toks[2]);
  }

  /* ----------------------------------------- */
  /* ---[ Allow Unbalanced Quotes Setting ]--- */
  /* ----------------------------------------- */
  @Test
  public void parseSimpleQuotedStringAllowUnbalancedQuotes() {
    CsvParser p = new CsvParserBuilder().allowUnbalancedQuotes(true).multiLine(true).build();
    String[] toks = p.parse("\"\"a\"\",\"b\",\"c\"");
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testADoubleQuoteAsDataElementWithAllowUnbalancedQuotes() {
    CsvParser p = new CsvParserBuilder().allowUnbalancedQuotes(true).multiLine(true).build();
    String[] toks = p.parse("a,\"\"\"\",c");  // a,"""",c

    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\"\"", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testIssueThorny1WithAllowUnbalancedQuotes() {
    CsvParser p = new CsvParserBuilder().allowUnbalancedQuotes(true).multiLine(true).build();
    String[] toks = p.parse("a , \"\",1000");
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals(" ", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void whitespaceBeforeEscapeWithAllowUnbalancedQuotes() {
    CsvParser p = new CsvParserBuilder().allowUnbalancedQuotes(true).multiLine(true).build();

    String[] toks = p.parse("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    assertEquals("this", toks[0]);
    assertEquals(" is", toks[1]);
    assertEquals("a test", toks[2]);
  }

  /* ------------------------------------- */
  /* ---[ Retain Outer Quotes Setting ]--- */
  /* ------------------------------------- */
  @Test
  public void testParseLineWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String toks[] = p.parse("This, is,\"a\", test.");
    assertEquals(4, toks.length);
    assertEquals("This", toks[0]);
    assertEquals(" is", toks[1]);
    assertEquals("\"a\"", toks[2]);
    assertEquals(" test.", toks[3]);
  }

  @Test
  public void parseQuotedStringWithCommasWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String[] toks = p.parse("a,\"b,b,b\",c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\"b,b,b\"", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseQuotedStringWithDefinedSeperatorWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator(':').
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse("a:\"b:b:b\":c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\"b:b:b\"", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseQuotedStringWithDefinedSeperatorAndQuoteWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator(':').
        quoteChar('\'').
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse("a:'b:b:b':c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("'b:b:b'", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void parseEmptyElementsWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String[] toks = p.parse(",,");
    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
  }

  @Test
  public void testADoubleQuoteAsDataElementWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();

    String[] toks = p.parse("a,\"\"\"\",c");// a,"""",c

    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("\"\"\"\"", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();

    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\""); // "test","this,test,is,good","\"test\",\"quote\""

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\\\"test\\\"\"", toks[2]);
    assertEquals("\"\\\"quote\\\"\"", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementPipeDelimitedWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().separator('|').retainOuterQuotes(true).multiLine(true).build();
    //                            "test"|"this|test|is|good"|"\"test\"|\"quote\""
    String[] toks = p.parse("\"test\"|\"this,test,is,good\"|\"\\\"test\\\"\"|\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\\\"test\\\"\"", toks[2]);
    assertEquals("\"\\\"quote\\\"\"", toks[3]);
  }

  @Test
  public void parseMultipleQuotesWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String Q2 = "\"\"";
    String[] toks = p.parse(Q2 + Q2 + Q2 + ",\"test\"\n"); // """""","test"  representing:  "", test
    assertEquals("\"\"\"\"\"\"", toks[0]); // check the tricky situation
    assertEquals("\"test\"", toks[1]);   // make sure we didn't ruin the next field..
    assertEquals(2, toks.length);
  }

  @Test
  public void parseTrickyStringWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String[] toks = p.parse("\"a\nb\",b,\"\nd\",e\n");
    assertEquals(4, toks.length);
    assertEquals("\"a\nb\"", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("\"\nd\"", toks[2]);
    assertEquals("e", toks[3]);
  }

  @Test
  public void testAMultiLineInsideQuotesWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();

    String text = "Small test,\"This is a test across \ntwo lines.\"";
    String[] toks = p.parse(text);
    assertEquals(2, toks.length);
    assertEquals("Small test", toks[0]);
    assertEquals("\"This is a test across \ntwo lines.\"", toks[1]);
  }

  @Test
  public void testStrictQuoteSimpleWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        retainOuterQuotes(true).
        strictQuotes(true).
        multiLine(true).
        build();
    String testString = "\"a\",\"b\",\"c\"";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testIssue2859181WithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator(';').
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("field1", toks[0]);
    assertEquals("\\=field2", toks[1]);
    assertEquals("\"\"\"field3\"\"\"", toks[2]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void anExceptionThrownifStringEndsInsideAQuotedStringWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().multiLine(true).retainOuterQuotes(true).build();
    p.parse("This,is a \"bad line to parse.");
  }

  @Test
  public void testIssueThorny1WithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        retainOuterQuotes(true).
        multiLine(true).
        build();
    String[] toks = p.parse("a , \"\",1000");
    assertEquals(3, toks.length);
    assertEquals("a ", toks[0]);
    assertEquals(" \"\"", toks[1]);
    assertEquals("1000", toks[2]);
  }

  @Test
  public void whitespaceBeforeEscapeWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();

    String[] toks = p.parse("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    assertEquals("\"this\"", toks[0]);
    assertEquals(" \"is\"", toks[1]);
    assertEquals("\"a test\"", toks[2]);
  }

  @Test
  public void testFourSingleQuotesWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator(',').
        retainOuterQuotes(true).
        multiLine(true).
        build();
    String[] toks = p.parse("a,'\'\'',c");
    assertEquals(3, toks.length);
    assertEquals("a", toks[0]);
    assertEquals("''''", toks[1]);
    assertEquals("c", toks[2]);
  }

  @Test
  public void testLongTokensRetainOuterQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();

    String[] toks = p.parse(longLine);
    assertEquals(3, toks.length);
    assertEquals(longEntry1, toks[0]);
    assertEquals("", toks[1]);
    assertEquals(" \"" + longEntry2 + "\"", toks[2]);
  }

  /* ----------------------------------- */
  /* ---[ RetainEscapeChars = false ]--- */
  /* ----------------------------------- */
  @Test
  public void testEscapedDoubleQuoteAsDataElementWithRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().retainEscapeChars(false).multiLine(true).build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("test", toks[0]);
    assertEquals("this,test,is,good", toks[1]);
    assertEquals("\"test\"", toks[2]);
    assertEquals("\"quote\"", toks[3]);
  }

  @Test  // issue from the old opencsv sourceforge project
  public void testIssue2859181WithRetainEscapeCharsFalse() {
    MultiLineCsvParser p = (MultiLineCsvParser) new CsvParserBuilder().
        separator(';').
        retainEscapeChars(false).
        multiLine(true).
        build();
    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("field1", toks[0]);
    assertEquals("=field2", toks[1]);
    assertEquals("\"\"field3\"\"", toks[2]);
  }

  @Test
  public void testEscapesBeforeNewLinesEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().retainEscapeChars(false).multiLine(true).build();
    String[] toks = p.parse("\"a\\nb\",b,\"\\nd\",e\n");

    assertEquals(4, toks.length);
    assertEquals("a\nb", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("\nd", toks[2]);
    assertEquals("e", toks[3]);
  }

  /* ---------------------------------- */
  /* ---[ AlwaysQuoteOutput = true ]--- */
  /* ---------------------------------- */
  @Test
  public void testParseLineAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String toks[] = p.parse("This, is, a, test.");
    assertEquals(4, toks.length);
    assertEquals("\"This\"", toks[0]);
    assertEquals("\" is\"", toks[1]);
    assertEquals("\" a\"", toks[2]);
    assertEquals("\" test.\"", toks[3]);
  }

  @Test
  public void testParseLineAlwaysQuoteOutputWithTrimSpaces() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        multiLine(true).
        build();

    String toks[] = p.parse("This, is, a, test.");
    assertEquals(4, toks.length);
    assertEquals("\"This\"", toks[0]);
    assertEquals("\"is\"", toks[1]);
    assertEquals("\"a\"", toks[2]);
    assertEquals("\"test.\"", toks[3]);
  }

  @Test
  public void parseQuotedStringWithCommasAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String[] toks = p.parse("a,\"b,b,b\",c,, ,");
    assertEquals(6, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b,b,b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("\" \"", toks[4]);
    assertEquals("", toks[5]);
  }

  @Test
  public void parseQuotedStringWithCommasAndWhitespaceAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String[] toks = p.parse(" a ,   \"b,b,b\",c ,, ,");
    assertEquals(6, toks.length);
    assertEquals("\" a \"", toks[0]);
    assertEquals("   \"b,b,b\"", toks[1]);
    assertEquals("\"c \"", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("\" \"", toks[4]);
    assertEquals("", toks[5]);
  }

  @Test
  public void parseQuotedStringWithCommasAndWhitespaceAlwaysQuoteOutputAndTrimWhitespace() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        multiLine(true).
        build();

    String[] toks = p.parse(" a ,   \"b,b,b\",c ,, ,");
    assertEquals(6, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b,b,b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("\" \"", toks[4]);
    assertEquals("", toks[5]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\\\"test\\\"\"", toks[2]);
    assertEquals("\"\\\"quote\\\"\"", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementAlwaysQuoteOutputWithTrimWhitespace() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        multiLine(true).
        build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\\\"test\\\"\"", toks[2]);
    assertEquals("\"\\\"quote\\\"\"", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementAlwaysQuoteOutputRetainOuterQuotes() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\\\"test\\\"\"", toks[2]);
    assertEquals("\"\\\"quote\\\"\"", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementAlwaysQuoteOutputRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        retainEscapeChars(false).
        multiLine(true).
        build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\"test\"\"", toks[2]);
    assertEquals("\"\"quote\"\"", toks[3]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementAlwaysQuoteOutputAndStrictOutputAndRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        retainEscapeChars(false).
        strictQuotes(true).
        multiLine(true).
        build();
    //                                        "test","this,test,is,good","\"test\",\"quote\""
    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\"");

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\"test\"\"", toks[2]);
    assertEquals("\"\"quote\"\"", toks[3]);
  }

  @Test
  public void testIssue2859181WithAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().
        separator(';').
        alwaysQuoteOutput(true).
        multiLine(true).
        build();

    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("\"field1\"", toks[0]);
    assertEquals("\"\\=field2\"", toks[1]);
    assertEquals("\"\"\"field3\"\"\"", toks[2]);
  }

  @Test
  public void testIssue2859181WithAlwaysQuoteOutputAndStrictQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator(';').
        alwaysQuoteOutput(true).
        strictQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("\"field3\"", toks[2]);
  }

  @Test
  public void testIssueThorny1dAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();
    String[] toks = p.parse("a,\" \"hello\" \",c");
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\" \"hello\" \"", toks[1]);
    assertEquals("\"c\"", toks[2]);

    p = new CsvParserBuilder().quoteChar('\'').alwaysQuoteOutput(true).multiLine(true).build();
    toks = p.parse("a,' 'hello' ',c");
    assertEquals(3, toks.length);
    assertEquals("'a'", toks[0]);
    assertEquals("' 'hello' '", toks[1]);
    assertEquals("'c'", toks[2]);

    p = new CsvParserBuilder().
        quoteChar('\'').
        trimWhitespace(true).
        alwaysQuoteOutput(true).
        multiLine(true).
        build();
    toks = p.parse("a,' 'hello' ',c");
    assertEquals(3, toks.length);
    assertEquals("'a'", toks[0]);
    assertEquals("' 'hello' '", toks[1]);
    assertEquals("'c'", toks[2]);
  }

  @Test
  public void testADoubleQuoteAsDataElementAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String[] toks = p.parse("a,\"\"\"\",c");  // a,"""",c

    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"\"\"\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testIssue2263439AlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().
        quoteChar('\'').
        alwaysQuoteOutput(true).
        multiLine(true).
        build();

    String text = "865,0,'AmeriKKKa\\'s_Most_Wanted','',294,0,0,0.734338696798625,'20081002052147',242429208,18448";
    String[] toks = p.parse(text);

    assertEquals(11, toks.length);
    assertEquals("'865'", toks[0]);
    assertEquals("'0'", toks[1]);
    assertEquals("'AmeriKKKa\\'s_Most_Wanted'", toks[2]);
    assertEquals("''", toks[3]);
    assertEquals("'18448'", toks[10]);
  }

  @Test
  public void testIssue2726363AlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        multiLine(true).
        build();

    String[] toks = p.parse("\"804503689\",\"London\",\"\"London\"shop\",\"address\",\"116.453182\",\"39.918884\"");

    assertEquals(6, toks.length);
    assertEquals("\"804503689\"", toks[0]);
    assertEquals("\"London\"", toks[1]);
    assertEquals("\"\"London\"shop\"", toks[2]);
    assertEquals("\"address\"", toks[3]);
    assertEquals("\"116.453182\"", toks[4]);
    assertEquals("\"39.918884\"", toks[5]);
  }

  @Test    // https://sourceforge.net/p/opencsv/bugs/93/
  public void testIssueSfBugs93AlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().separator(';').alwaysQuoteOutput(true).multiLine(true).build();

    String[] toks = p.parse("\"\";1");
    assertEquals(2, toks.length);
    assertEquals("\"\"", toks[0]);
    assertEquals("\"1\"", toks[1]);

    toks = p.parse("\"\";2");
    assertEquals(2, toks.length);
    assertEquals("\"\"", toks[0]);
    assertEquals("\"2\"", toks[1]);
  }

  @Test
  public void testWithSpacesAndTabsAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String testString = " \t      \"a\",\"b\"      \t       ,   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals(" \t      \"a\"", toks[0]);
    assertEquals("\"b\"      \t       ", toks[1]);
    assertEquals("   \"c\"   ", toks[2]);
  }

  @Test
  public void testWithSpacesAndTabsAlwaysQuoteOutputWithTrimWhitespace() {
    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        alwaysQuoteOutput(true).
        multiLine(true).
        build();

    String testString = " \t      \"a\",\"b\"      \t       ,   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);

    // should get the same result with retainQuotes also thrown in
    p = new CsvParserBuilder().
        trimWhitespace(true).
        alwaysQuoteOutput(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();

    toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testWithSpacesAndTabsAlwaysQuoteOutputRetainOuterQuotes() {
    CsvParser p = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String testString = " \t      \"a\",\"b\"      \t       ,   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals(" \t      \"a\"", toks[0]);
    assertEquals("\"b\"      \t       ", toks[1]);
    assertEquals("   \"c\"   ", toks[2]);
  }

  @Test
  public void testStrictQuoteWithSpacesAndTabsAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().strictQuotes(true).alwaysQuoteOutput(true).multiLine(true).build();

    String testString = " \t      \"a\",\"b\"      \t       ,   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testStrictQuoteWithEverythingToggled() {
    MultiLineCsvParser p = (MultiLineCsvParser) new CsvParserBuilder().
        separator(':').
        strictQuotes(true).
        alwaysQuoteOutput(true).
        trimWhitespace(true).
        allowUnbalancedQuotes(true).
        retainEscapeChars(false).
        multiLine(true).
        build();

    String testString = " \t      \"a\":\"b\"      \\\t       :   \"c\"   ";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testLongTokensAlwaysQuoteOutput() {
    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).multiLine(true).build();

    String[] toks = p.parse(longLine);
    assertEquals(3, toks.length);
    assertEquals("\"" + longEntry1 + "\"", toks[0]);
    assertEquals("", toks[1]);
    assertEquals(" \"" + longEntry2 + "\"", toks[2]);
  }

  /* ------------------------------------- */
  /* ---[ Various Mixed Mode Settings ]--- */
  /* ------------------------------------- */
  @Test
  public void testCrLfAsWhiteSpace() throws IOException {
    MultiLineCsvParser p = (MultiLineCsvParser) new CsvParserBuilder().
        trimWhitespace(true).
        multiLine(true).
        build();

    String data = "a\n"
        + ",\r\n"
        + "b\n"
        + ",c\r";

    assertArrayEquals(new String[]{"a"}, p.parse(data));
    assertArrayEquals(new String[]{"", ""}, p.parseNthRecord(data, 2));
    assertArrayEquals(new String[]{"b"}, p.parseNthRecord(data, 3));
    assertArrayEquals(new String[]{"", "c"}, p.parseNthRecord(data, 4));
  }

  // TODO: this should be moved to the CsvReader unit test => why is this here?
  @Test
  public void testCrLfAndAlwaysQuoteOutput() throws IOException {
    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        alwaysQuoteOutput(true).
        multiLine(true).
        build();

    // all quoted of course
    CsvReader r = new CsvReader(new StringReader(
        "a" + LF
        + "," + CRLF
        + "b" + LF
        + ",c" + CR), p);

    // LINE 1 = 'a' + LF
    String[] result = r.readNext();
    assertArrayEquals(new String[]{"\"a\""}, result);

    // LINE 2 =  , + CRLF == _,_ (line starting with a comma and then a CR or CRLF is actually two emtpy fields
    result = r.readNext();
    assertArrayEquals(new String[]{"", ""}, result);

    // LINE 3 = 'b' + LF
    result = r.readNext();
    assertArrayEquals(new String[]{"\"b\""}, result);

    // LINE 4 =  , + 'c' + CR + EOF = empty + c
    result = r.readNext();
    assertArrayEquals(new String[]{"", "\"c\""}, result);
    
    r.close();
  }

  @Test
  public void testIssue2859181WithRetainQuotesAndRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        separator(';').
        retainOuterQuotes(true).
        retainEscapeChars(false).
        multiLine(true).
        build();

    String[] toks = p.parse("field1;\\=field2;\"\"\"field3\"\"\""); // field1;\=field2;"""field3"""

    assertEquals(3, toks.length);
    assertEquals("field1", toks[0]);
    assertEquals("=field2", toks[1]);
    assertEquals("\"\"\"field3\"\"\"", toks[2]);
  }

  @Test
  public void testEscapedDoubleQuoteAsDataElementWithRetainQuotesAndRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        retainOuterQuotes(true).
        retainEscapeChars(false).
        multiLine(true).
        build();

    String[] toks = p.parse("\"test\",\"this,test,is,good\",\"\\\"test\\\"\",\"\\\"quote\\\"\""); // "test","this,test,is,good","\"test\",\"quote\""

    assertEquals(4, toks.length);
    assertEquals("\"test\"", toks[0]);
    assertEquals("\"this,test,is,good\"", toks[1]);
    assertEquals("\"\"test\"\"", toks[2]);
    assertEquals("\"\"quote\"\"", toks[3]);
  }

  @Test
  public void parseSimpleQuotedStringWithSpacesWithRetainQuotesAndStrictQuotes() {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse(" \"a\" , \"b\" , \"c\" ");
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void parseSimpleQuotedStringWithSpacesPipeDelimitedWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        separator('|').
        strictQuotes(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();

    String[] toks = p.parse(" \"a\" | \"b\" | \"c\" ");
    assertEquals(3, toks.length);
    assertEquals("\"a\"", toks[0]);
    assertEquals("\"b\"", toks[1]);
    assertEquals("\"c\"", toks[2]);
  }

  @Test
  public void testParsedLineWithInternalQuotaWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    String[] toks = p.parse("a,123\"4\"567,c");

    assertEquals(3, toks.length);
    assertEquals("123\"4\"567", toks[1]);
  }

  // An opencsv issue: // https://issues.sonatype.org/browse/OSSRH-6159
  // The simplecsv code base leaves the trailing space if "trimWhiteSpace=true"
  // is not invoked, so the behavior is different from original opencsv
  @Test
  public void testTrailingSpace() {
    // trailing space
    String[] toks = parser.parse("\"1\" ,\"2\"");
    assertEquals(2, toks.length);
    assertEquals("1 ", toks[0]);
    assertEquals("2", toks[1]);

    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        multiLine(true).
        build();
    toks = p.parse("\"1\" ,\"2\"");
    assertEquals(2, toks.length);
    assertEquals("1", toks[0]);
    assertEquals("2", toks[1]);

    p = new CsvParserBuilder().
        retainOuterQuotes(true).
        trimWhitespace(true).
        multiLine(true).
        build();
    toks = p.parse("\"1\" ,\"2\"");
    assertEquals(2, toks.length);
    assertEquals("\"1\"", toks[0]);
    assertEquals("\"2\"", toks[1]);
  }

  @Test
  public void spacesAtEndOfQuotedStringDoNotCountIfStrictQuotesIsTrueWithRetainQuotes() {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        retainOuterQuotes(true).
        multiLine(true).
        build();
    String[] toks = p.parse("\"Line with\", \"spaces at end\"  ");
    assertEquals(2, toks.length);
    assertEquals("\"Line with\"", toks[0]);
    assertEquals("\"spaces at end\"", toks[1]);
  }

  @Test
  public void testStrictQuoteWithGarbageWithRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        retainEscapeChars(false).
        multiLine(true).
        build();
    String testString = "abc',!@#\",\\\"\"   xyz,";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals(",\"", toks[1]);
    assertEquals("", toks[2]);
  }

  @Test
  public void testStrictQuoteWithGarbageWithRetainEscapeCharsFalseAlwaysQuoteChars() {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        retainEscapeChars(false).
        alwaysQuoteOutput(true).
        multiLine(true).
        build();
    String testString = "abc',!@#\",\\\"\"   xyz,";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("", toks[0]);
    assertEquals("\",\"\"", toks[1]);
    assertEquals("", toks[2]);
  }

  @Test
  public void testWithGarbageWithRetainEscapeCharsFalseAndAlwaysQuoteOutput() {
    MultiLineCsvParser p = (MultiLineCsvParser) new CsvParserBuilder().
        alwaysQuoteOutput(true).
        retainEscapeChars(false).
        multiLine(true).
        build();
    String testString = "abc',!@#\",\\\"\"   xyz,";

    String[] toks = p.parse(testString);
    assertEquals(3, toks.length);
    assertEquals("\"abc'\"", toks[0]);
    assertEquals("\"!@#\",\"\"   xyz\"", toks[1]);
    assertEquals("", toks[2]);
  }

  @Test
  public void whitespaceBeforeEscapeWithAllowUnbalancedQuotesWithRetainEscapeCharsFalse() {
    CsvParser p = new CsvParserBuilder().
        allowUnbalancedQuotes(true).
        retainEscapeChars(false).
        multiLine(true).
        build();

    String[] toks = p.parse("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    assertEquals("this", toks[0]);
    assertEquals(" is", toks[1]);
    assertEquals("a test", toks[2]);
  }

  /* ---[ Table Examples in simplecsv documentation ]--- */
  @Test
  public void testDocTableExample1() {
    String text = "\"abc\"d\"efg\",1,\"2\", w\"x\"y\"\"z ";
    String[] toks = null;
    CsvParser p1 = new CsvParserBuilder().multiLine(true).strictQuotes(true).build();
    CsvParser p2 = new CsvParserBuilder().multiLine(true).retainOuterQuotes(true).build();
    CsvParser p3 = new CsvParserBuilder().multiLine(true).allowUnbalancedQuotes(true).build();
    CsvParser p4 = new CsvParserBuilder().multiLine(true).strictQuotes(true).retainOuterQuotes(true).build();
    CsvParser p5 = new CsvParserBuilder().multiLine(true).alwaysQuoteOutput(true).build();

    // default mode
    toks = parser.parse(text);  // [abc"d"efg, 1, 2,  w"x"y""z ]  // CORRECT
    String asList = Arrays.asList(toks).toString();
    String exp = "[abc\"d\"efg, 1, 2,  w\"x\"y\"\"z ]";
    assertEquals(exp, asList);

    toks = p1.parse(text);      // [abc"d"efg, 1, 2,  w"x"y""z ]  // WRONG=>[abcefg, , 2, x]
    asList = Arrays.asList(toks).toString();
    exp = "[abcefg, , 2, x]";
    assertEquals(exp, asList);

    toks = p2.parse(text);      // ["abc"d"efg", 1, "2",  w"x"y""z ]
    asList = Arrays.asList(toks).toString();
    exp = "[\"abc\"d\"efg\", 1, \"2\",  w\"x\"y\"\"z ]";
    assertEquals(exp, asList);

    toks = p3.parse(text);      // [abcdefg, 1, 2,  wxyz ]  // WRONG
    asList = Arrays.asList(toks).toString();
    exp = "[abc\"d\"efg, 1, 2,  w\"x\"y\"\"z ]";
    assertEquals(exp, asList);

    toks = p4.parse(text);      // ["abc"d"efg", 1, "2",  w"x"y""z ]
    asList = Arrays.asList(toks).toString();
    exp = "[\"abcefg\", , \"2\", \"x\"]";
    assertEquals(exp, asList);

    toks = p5.parse(text);      // ["abc"d"efg", 1, "2",  w"x"y""z ]
    asList = Arrays.asList(toks).toString();
    exp = "[\"abc\"d\"efg\", \"1\", \"2\", \" w\"x\"y\"\"z \"]";
    assertEquals(exp, asList);
  }

  @Test
  public void testDocTableExample2() {
    String text = "1,\"abc\\\"d\\\"efg\"";
    String[] toks = null;
    CsvParser p1 = new CsvParserBuilder().multiLine(true).strictQuotes(true).build();
    CsvParser p2 = new CsvParserBuilder().multiLine(true).retainOuterQuotes(true).build();
    CsvParser p3 = new CsvParserBuilder().multiLine(true).allowUnbalancedQuotes(true).build();
    CsvParser p4 = new CsvParserBuilder().multiLine(true).strictQuotes(true).retainOuterQuotes(true).build();
    CsvParser p5 = new CsvParserBuilder().multiLine(true).alwaysQuoteOutput(true).build();
    CsvParser p6 = new CsvParserBuilder().multiLine(true).strictQuotes(true).alwaysQuoteOutput(true).build();

    // default mode
    toks = parser.parse(text);
    String asList = Arrays.asList(toks).toString();
    String exp = "[1, abc\\\"d\\\"efg]";
    assertEquals(exp, asList);

    toks = p1.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, abc\\\"d\\\"efg]";
    assertEquals(exp, asList);

    toks = p2.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1, \"abc\\\"d\\\"efg\"]";
    assertEquals(exp, asList);

    toks = p3.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1, abc\\\"d\\\"efg]";
    assertEquals(exp, asList);

    toks = p4.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, \"abc\\\"d\\\"efg\"]";
    assertEquals(exp, asList);

    toks = p5.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[\"1\", \"abc\\\"d\\\"efg\"]";
    assertEquals(exp, asList);

    toks = p6.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, \"abc\\\"d\\\"efg\"]";
    assertEquals(exp, asList);
  }

  @Test
  public void testDocTableExample3() {
    String text = "1, \"abc\"def\"";
    String[] toks = null;
    CsvParser p1 = new CsvParserBuilder().strictQuotes(true).multiLine(true).build();
    CsvParser p2 = new CsvParserBuilder().retainOuterQuotes(true).multiLine(true).build();
    CsvParser p3 = new CsvParserBuilder().allowUnbalancedQuotes(true).multiLine(true).build();
    
    CsvParser p4 = new CsvParserBuilder().
        retainOuterQuotes(true).
        allowUnbalancedQuotes(true).
        multiLine(true).
        build();
    
    CsvParser p5 = new CsvParserBuilder().
        strictQuotes(true).
        retainOuterQuotes(true).
        allowUnbalancedQuotes(true).
        multiLine(true).
        build();
    
    CsvParser p6 = new CsvParserBuilder().
        strictQuotes(true).
        alwaysQuoteOutput(true).
        allowUnbalancedQuotes(true).
        multiLine(true).
        build();
    
    CsvParser p7 = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        allowUnbalancedQuotes(true).
        multiLine(true).
        build();
    
    CsvParser p8 = new CsvParserBuilder().
        alwaysQuoteOutput(true).
        allowUnbalancedQuotes(true).
        trimWhitespace(true).
        multiLine(true).
        build();

    // default mode
    try {
      parser.parse(text);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(IllegalArgumentException.class.getName()
          + ": Un-terminated quoted field at end of CSV record", e.toString());
    }

    try {
      p1.parse(text);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(IllegalArgumentException.class.getName()
          + ": Un-terminated quoted field at end of CSV record", e.toString());
    }

    try {
      p2.parse(text);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(IllegalArgumentException.class.getName()
          + ": Un-terminated quoted field at end of CSV record", e.toString());
    }

    toks = p3.parse(text);
    String asList = Arrays.asList(toks).toString();
    String exp = "[1,  abc\"def]";
    assertEquals(exp, asList);

    toks = p4.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1,  \"abc\"def\"]";
    assertEquals(exp, asList);

    toks = p5.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, \"abc\"]";
    assertEquals(exp, asList);

    toks = p6.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, \"abc\"]";
    assertEquals(exp, asList);

    toks = p7.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[\"1\",  \"abc\"def\"]";
    assertEquals(exp, asList);

    toks = p8.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[\"1\", \"abc\"def\"]";
    assertEquals(exp, asList);
  }

  @Test
  public void testDocTableExample4() {
    String text = "1,\"abc\\\"def\"";
    String[] toks;
    CsvParser p1 = new CsvParserBuilder().multiLine(true).strictQuotes(true).build();
    CsvParser p2 = new CsvParserBuilder().multiLine(true).retainOuterQuotes(true).build();
    CsvParser p3 = new CsvParserBuilder().multiLine(true).allowUnbalancedQuotes(true).build();
    CsvParser p4 = new CsvParserBuilder().multiLine(true).retainOuterQuotes(true).allowUnbalancedQuotes(true).build();
    CsvParser p5 = new CsvParserBuilder().multiLine(true).
        strictQuotes(true).
        retainOuterQuotes(true).
        allowUnbalancedQuotes(true).
        build();

    // default mode
    toks = parser.parse(text);
    String asList = Arrays.asList(toks).toString();
    String exp = "[1, abc\\\"def]";
    assertEquals(exp, asList);

    toks = p1.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, abc\\\"def]";
    assertEquals(exp, asList);

    toks = p2.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1, \"abc\\\"def\"]";
    assertEquals(exp, asList);

    toks = p3.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1, abc\\\"def]";
    assertEquals(exp, asList);

    toks = p4.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[1, \"abc\\\"def\"]";
    assertEquals(exp, asList);

    toks = p5.parse(text);
    asList = Arrays.asList(toks).toString();
    exp = "[, \"abc\\\"def\"]";
    assertEquals(exp, asList);
  }
}

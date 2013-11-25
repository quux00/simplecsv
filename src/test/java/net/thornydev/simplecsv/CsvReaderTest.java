package net.thornydev.simplecsv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class CsvReaderTest {

  CsvReader csvr;
  String lines;
  
  /**
   * Setup the test.
   */
  @Before
  public void setUp() throws Exception {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c").append("\n");   // standard case
    sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
    sb.append(",,").append("\n"); // empty elements
    sb.append("a,\"PO Box 123,\\nKippax,ACT. 2615.\\nAustralia\",d.\n");
    sb.append("\"Glen \"\"The Man\"\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
    sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  
    sb.append("\"a\\nb\",b,\"\\nd\",e\n");
    
    lines = sb.toString();
    csvr = new CsvReader(new StringReader(lines));
  }
  
  @Test
  public void canCloseReader() throws IOException {
    csvr.close();
  }

  @Test
  public void canCreateIteratorFromReader() {
    assertNotNull(csvr.iterator());
  }

  
  /**
   * Tests iterating over a reader.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testIteratorFunctionality() throws IOException {
    String[][] expectedResult = new String[7][];
    expectedResult[0] = new String[]{"a", "b", "c"};
    expectedResult[1] = new String[]{"a", "b,b,b", "c"};
    expectedResult[2] = new String[]{"", "", ""};
    expectedResult[3] = new String[]{"a", "PO Box 123,\\nKippax,ACT. 2615.\\nAustralia", "d."};
    expectedResult[4] = new String[]{"Glen \"\"The Man\"\" Smith", "Athlete", "Developer"};
    expectedResult[5] = new String[]{"\"\"\"\"", "test"};
    expectedResult[6] = new String[]{"a\\nb", "b", "\\nd", "e"};
    
    int idx = 0;
    for (String[] line : csvr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line);
    }
  }
  
  @Test
  public void testIteratorFunctionalityWithRetainEscapeCharsFalse() throws IOException {
    CsvParser parser = new CsvParserBuilder().retainEscapeChars(false).build();
    CsvReader cr = new CsvReader(new StringReader(lines), parser);
    
    String[][] expectedResult = new String[7][];
    expectedResult[0] = new String[]{"a", "b", "c"};
    expectedResult[1] = new String[]{"a", "b,b,b", "c"};
    expectedResult[2] = new String[]{"", "", ""};
    expectedResult[3] = new String[]{"a", "PO Box 123,\nKippax,ACT. 2615.\nAustralia", "d."};
    expectedResult[4] = new String[]{"Glen \"\"The Man\"\" Smith", "Athlete", "Developer"};
    expectedResult[5] = new String[]{"\"\"\"\"", "test"};
    expectedResult[6] = new String[]{"a\nb", "b", "\nd", "e"};
    int idx = 0;
    for (String[] line : cr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line);
    }
    
    cr.close();
  }
  
  @Test
  public void testIteratorFunctionalityWithRetainEscapeCharsFalseAndAlwaysQuoteOutput() throws IOException {
    CsvParser parser = new CsvParserBuilder().
        retainEscapeChars(false).
        alwaysQuoteOutput(true).
        build();
    CsvReader cr = new CsvReader(new StringReader(lines), parser);
    
    String[][] expectedResult = new String[7][];
    expectedResult[0] = new String[]{"\"a\"", "\"b\"", "\"c\""};
    expectedResult[1] = new String[]{"\"a\"", "\"b,b,b\"", "\"c\""};
    expectedResult[2] = new String[]{"", "", ""};
    expectedResult[3] = new String[]{"\"a\"", "\"PO Box 123,\nKippax,ACT. 2615.\nAustralia\"", "\"d.\""};
    expectedResult[4] = new String[]{"\"Glen \"\"The Man\"\" Smith\"", "\"Athlete\"", "\"Developer\""};
    expectedResult[5] = new String[]{"\"\"\"\"\"\"", "\"test\""};
    expectedResult[6] = new String[]{"\"a\nb\"", "\"b\"", "\"\nd\"", "\"e\""};

    int idx = 0;
    for (String[] line : cr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line);
    }
    
    cr.close();
  }
  
  
  @Test(expected = RuntimeException.class)
  public void creatingIteratorForReaderWithNullDataThrowsRuntimeException() throws IOException {
    Reader mockReader = mock(Reader.class);
    when(mockReader.read(Matchers.<CharBuffer>any())).thenThrow(new IOException("test io exception"));
    when(mockReader.read()).thenThrow(new IOException("test io exception"));
    when(mockReader.read((char[]) notNull())).thenThrow(new IOException("test io exception"));
    when(mockReader.read((char[]) notNull(), anyInt(), anyInt())).thenThrow(new IOException("test io exception"));
    CsvReader cr = new CsvReader(mockReader);
    cr.iterator();
    cr.close();
  }
  

  /* ---------------------------------------- */  
  /* ---[ Tests from data read from file ]--- */
  /* ---------------------------------------- */  
  
  @Test
  public void testRecordsFromFileDefaultParser() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    csvr = new CsvReader(fr, 1);
    
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("1", toks[0]);
    assertEquals(" abc", toks[1]);
    assertEquals(" Stan \"The Man\" Musial", toks[2]);
    assertEquals(" Mike \\\"The Situation\\\"", toks[3]);
    assertEquals(" I\\nlike\\nIke", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("2", toks[0]);
    assertEquals(" def", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("abc\"d\"efg ", toks[0]);
    assertEquals("Stan \"The Man\" Musial        ", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("2\\\\n", toks[0]);
    assertEquals("\\f", toks[1]);
    assertEquals("\\b", toks[2]);
    assertEquals("\\r\\n", toks[3]);
    assertEquals("\\t", toks[4]);
    assertEquals("\tlast", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithStrictQuotes() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().strictQuotes(true).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("Stan  Musial", toks[2]);
    assertEquals("Mike \\\"The Situation\\\"", toks[3]);
    assertEquals("I\\nlike\\nIke", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("abcefg", toks[0]);
    assertEquals("Stan  Musial", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("\\t", toks[4]);
    assertEquals("", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithTrimWhitespaceAndRetainEscapeCharFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().trimWhitespace(true).retainEscapeChars(false).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("1", toks[0]);
    assertEquals("abc", toks[1]);
    assertEquals("Stan \"The Man\" Musial", toks[2]);
    assertEquals("Mike \"The Situation\"", toks[3]);
    assertEquals("I\nlike\nIke", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("2", toks[0]);
    assertEquals("def", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("abc\"d\"efg", toks[0]);
    assertEquals("Stan \"The Man\" Musial", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("2n", toks[0]);
    assertEquals("\f", toks[1]);
    assertEquals("\b", toks[2]);
    assertEquals("\r\n", toks[3]);
    assertEquals("\t", toks[4]);
    assertEquals("last", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithStrictQuotesAndRetainEscapeCharsFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().strictQuotes(true).retainEscapeChars(false).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("Stan  Musial", toks[2]);
    assertEquals("Mike \"The Situation\"", toks[3]);
    assertEquals("I\nlike\nIke", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("abcefg", toks[0]);
    assertEquals("Stan  Musial", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("\t", toks[4]);
    assertEquals("", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithRetainOuterQuotesAndTrimWhitespaceAndRetainEscapeCharFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        retainEscapeChars(false).
        retainOuterQuotes(true).
        build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("1", toks[0]);
    assertEquals("abc", toks[1]);
    assertEquals("\"Stan \"The Man\" Musial\"", toks[2]);
    assertEquals("\"Mike \"The Situation\"\"", toks[3]);
    assertEquals("\"I\nlike\nIke\"", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("2", toks[0]);
    assertEquals("def", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("\"abc\"d\"efg\"", toks[0]);
    assertEquals("\"Stan \"The Man\" Musial\"", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("2n", toks[0]);
    assertEquals("\f", toks[1]);
    assertEquals("\b", toks[2]);
    assertEquals("\r\n", toks[3]);
    assertEquals("\"\t\"", toks[4]);
    assertEquals("last", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }

  @Test
  public void testRecordsFromFileWithAlwaysQuoteOutputAndTrimWhitespaceAndRetainEscapeCharFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        retainEscapeChars(false).
        alwaysQuoteOutput(true).
        build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    String[] toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("\"1\"", toks[0]);
    assertEquals("\"abc\"", toks[1]);
    assertEquals("\"Stan \"The Man\" Musial\"", toks[2]);
    assertEquals("\"Mike \"The Situation\"\"", toks[3]);
    assertEquals("\"I\nlike\nIke\"", toks[4]);

    toks = csvr.readNext();
    assertEquals(5, toks.length);
    assertEquals("\"2\"", toks[0]);
    assertEquals("\"def\"", toks[1]);
    assertEquals("", toks[2]);
    assertEquals("", toks[3]);
    assertEquals("", toks[4]);
    
    toks = csvr.readNext();
    assertEquals(2, toks.length);
    assertEquals("\"abc\"d\"efg\"", toks[0]);
    assertEquals("\"Stan \"The Man\" Musial\"", toks[1]);

    toks = csvr.readNext();
    assertEquals(1, toks.length);
    assertEquals("", toks[0]);
    
    toks = csvr.readNext();
    assertEquals(6, toks.length);
    assertEquals("\"2n\"", toks[0]);
    assertEquals("\"\f\"", toks[1]);
    assertEquals("\"\b\"", toks[2]);
    assertEquals("\"\r\n\"", toks[3]);
    assertEquals("\"\t\"", toks[4]);
    assertEquals("\"last\"", toks[5]);

    toks = csvr.readNext();
    assertNull(toks);
  }

  
  /* ---------------------------------- */  
  /* ---[ StringReader based tests ]--- */
  /* ---------------------------------- */  
  
  /**
   * Tests iterating over a reader.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testParseLine() throws IOException {

    // test normal case
    String[] toks = csvr.readNext();
    assertEquals("a", toks[0]);
    assertEquals("b", toks[1]);
    assertEquals("c", toks[2]);

    // test quoted commas
    toks = csvr.readNext();
    assertEquals("a", toks[0]);
    assertEquals("b,b,b", toks[1]);
    assertEquals("c", toks[2]);

    // test empty elements
    toks = csvr.readNext();
    assertEquals(3, toks.length);

    // test multiline quoted
    toks = csvr.readNext();
    assertEquals(3, toks.length);

    // test quoted quote chars
    toks = csvr.readNext();
    assertEquals("Glen \"\"The Man\"\" Smith", toks[0]);

    toks = csvr.readNext();
    assertEquals("\"\"\"\"", toks[0]); 
    assertEquals("test", toks[1]); // make sure we didn't ruin the next field..

    toks = csvr.readNext();
    assertEquals(4, toks.length);

    //test end of stream
    assertNull(csvr.readNext());
  }

  @Test
  public void testParseLineStrictQuote() throws IOException {

    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c").append("\n");   // standard case
    sb.append("a,\"b,b,b\",c").append("\n");  // quoted elements
    sb.append(",,").append("\n"); // empty elements
    sb.append("a,\"PO Box 123,\\nKippax,ACT. 2615.\\nAustralia\",d.\n");
    sb.append("\"Glen \\\"The Man\\\" Smith\",Athlete,Developer\n"); // Test quoted quote chars
    sb.append("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
    sb.append("\"a\\nb\",b,\"\\nd\",e\n");

    CsvParser parser = new CsvParserBuilder().strictQuotes(true).build();
    csvr = new CsvReader(new StringReader(sb.toString()), parser);

    // test normal case
    String[] toks = csvr.readNext();
    assertEquals("", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("", toks[2]);

    // test quoted commas
    toks = csvr.readNext();
    assertEquals("", toks[0]);
    assertEquals("b,b,b", toks[1]);
    assertEquals("", toks[2]);

    // test empty elements
    toks = csvr.readNext();
    assertEquals(3, toks.length);

    // test multiline quoted
    toks = csvr.readNext();
    assertEquals(3, toks.length);

    // test quoted quote chars
    toks = csvr.readNext();
    assertEquals("Glen \\\"The Man\\\" Smith", toks[0]);

    toks = csvr.readNext();
    assertEquals("", toks[0]);
    assertEquals("test", toks[1]);

    toks = csvr.readNext();
    assertEquals(4, toks.length);
    assertEquals("a\\nb", toks[0]);
    assertEquals("", toks[1]);
    assertEquals("\\nd", toks[2]);
    assertEquals("", toks[3]);

    //test end of stream
    assertNull(csvr.readNext());
  }


  /**
   * Test parsing to a list.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testParseAll() throws IOException {
    assertEquals(7, csvr.readAll().size());
  }

  /**
   * Tests constructors with optional delimiters and optional quote char.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testOptionalConstructors() throws IOException {

    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a\tb\tc").append("\n");   // tab separated case
    sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements
    
    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);

    nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    cr.close();
  }

  @Test
  public void parseQuotedStringWithDefinedSeperator() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a\tb\tc").append("\n");   // tab separated case

    CsvParser parser = new CsvParserBuilder().separator('\t').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);

    cr.close();
  }

  @Test
  public void parsePipeDelimitedString() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("[bar]|[baz]").append("\n");

    CsvParser parser = new CsvParserBuilder().separator('|').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(2, nextLine.length);
    assertEquals("[bar]", nextLine[0]);
    assertEquals("[baz]", nextLine[1]);

    cr.close();
  }

  /**
   * Tests option to skip the first few lines of a file.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testSkippingLines() throws IOException {

    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("Skip this line\t with tab").append("\n");   // should skip this
    sb.append("And this line too").append("\n");   // and this
    sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements

    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').build();
    CsvReader cr = new CsvReaderBuilder(new StringReader(sb.toString())).
        csvParser(parser).
        skipLines(2).
        build();
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);

    cr.close();
  }


  /**
   * Tests option to skip the first few lines of a file.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testSkippingLinesWithDifferentEscape() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("Skip this line?t with tab").append("\n");   // should skip this
    sb.append("And this line too").append("\n");   // and this
    sb.append("a\t'b\tb\tb'\t'c'").append("\n");  // single quoted elements

    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').escapeChar('?').build();
    CsvReader cr = new CsvReaderBuilder(new StringReader(sb.toString())).
        csvParser(parser).
        skipLines(2).
        build();
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);
    assertEquals("c", nextLine[2]);

    cr.close();
  }

  /**
   * Test a normal non quoted line with three elements
   *
   * @throws IOException
   */
  @Test
  public void testNormalParsedLine() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,1234567,c").append("\n");// a,1234,c

    CsvParser parser = new CsvParser();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);

    assertEquals("a", nextLine[0]);
    assertEquals("1234567", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }


  /**
   * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
   * single quote.
   *
   * @throws IOException
   */
  @Test
  public void testASingleQuoteAsDataElement() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);

    sb.append("a,'''',c").append("\n");// a,'',c

    CsvParser parser = new CsvParserBuilder().quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);
    assertEquals("''", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }

  /**
   * Same as testADoubleQuoteAsDataElement but I changed the quotechar to a
   * single quote.  Also the middle field is empty.
   *
   * @throws IOException
   */
  @Test
  public void testASingleQuoteAsDataElementWithEmptyField() throws IOException {

    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);

    sb.append("a,'',c").append("\n");// a,,c

    CsvParser parser = new CsvParserBuilder().quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);
    assertEquals(0, nextLine[1].length());
    assertEquals("", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }

  @Test
  public void testSpacesAtEndOfString() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("\"a\",\"b\",\"c\"   ");

    CsvParser parser = new CsvParserBuilder().strictQuotes(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);
    assertEquals("b", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }


  @Test
  public void testEscapedQuote() throws IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("a,\"123\\\"4567\",c").append("\n");// a,123"4",c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);

    assertEquals("123\\\"4567", nextLine[1]);
    cr.close();
  }

  @Test
  public void testEscapedEscape() throws IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("a,\"123\\\\4567\",c").append("\n");// a,"123\\4567",c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));
    
    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("123\\\\4567", nextLine[1]);
    cr.close();
  }


  /**
   * Test a line where one of the elements is two single quotes and the
   * quote character is the default double quote.  The expected result is two
   * single quotes.
   *
   * @throws IOException
   */
  @Test
  public void testSingleQuoteWhenDoubleQuoteIsQuoteChar() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,'',c").append("\n");// a,'',c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);
    assertEquals("a", nextLine[0]);
    assertEquals(2, nextLine[1].length());
    assertEquals("''", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }

  /**
   * Test a normal line with three elements and all elements are quoted
   *
   * @throws IOException
   */
  @Test
  public void testQuotedParsedLine() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("\"a\",\"1234567\",\"c\"").append("\n"); // "a","1234567","c"

    CsvParser parser = new CsvParserBuilder().strictQuotes(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    String[] nextLine = cr.readNext();
    assertEquals(3, nextLine.length);

    assertEquals("a", nextLine[0]);
    assertEquals(1, nextLine[0].length());

    assertEquals("1234567", nextLine[1]);
    assertEquals("c", nextLine[2]);
    cr.close();
  }

  @Test
  public void testIssue2992134OutOfPlaceQuotes() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));

    String[] nextLine = cr.readNext();
    assertEquals("a", nextLine[0]);
    assertEquals("b", nextLine[1]);
    assertEquals("c", nextLine[2]);
    assertEquals("ddd\\\"eee", nextLine[3]);

    nextLine = cr.readNext();
    assertEquals("f", nextLine[0]);
    assertEquals("g", nextLine[1]);
    assertEquals("h", nextLine[2]);
    assertEquals("iii,jjj", nextLine[3]);

    cr.close();
  }

  @Test
  public void testIssue2992134OutOfPlaceQuotesAlwaysQuoteOutput() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), p);

    String[] nextLine = cr.readNext();
    assertEquals("\"a\"", nextLine[0]);
    assertEquals("\"b\"", nextLine[1]);
    assertEquals("\"c\"", nextLine[2]);
    assertEquals("\"ddd\\\"eee\"", nextLine[3]);

    nextLine = cr.readNext();
    assertEquals("\"f\"", nextLine[0]);
    assertEquals("\"g\"", nextLine[1]);
    assertEquals("\"h\"", nextLine[2]);
    assertEquals("\"iii,jjj\"", nextLine[3]);

    cr.close();
  }
  
  @Test
  public void testASingleQuoteAsDataElementWithEmptyField2() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("\"\";1").append("\n");// ;1
    sb.append("\"\";2").append("\n");// ;2

    CsvParser parser = new CsvParserBuilder().separator(';').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    String[] nextLine = cr.readNext();
    assertEquals(2, nextLine.length);

    assertEquals(0, nextLine[0].length());
    assertEquals("1", nextLine[1]);

    nextLine = cr.readNext();
    assertEquals(2, nextLine.length);

    assertEquals("", nextLine[0]);
    assertEquals(0, nextLine[0].length());
    assertEquals("2", nextLine[1]);

    cr.close();
  }


  @Test(expected = UnsupportedOperationException.class)
  public void quoteAndEscapeMustBeDifferent() throws IOException {
    StringBuilder sb = new StringBuilder(CsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvParser parser = new CsvParserBuilder().
        quoteChar(CsvParser.DEFAULT_QUOTE_CHAR).
        escapeChar(CsvParser.DEFAULT_QUOTE_CHAR).
        build();
    CsvReader cr = null;
    try {
      cr = new CsvReader(new StringReader(sb.toString()), parser);
    } finally {
      if (cr != null) cr.close();
    }
  }
}

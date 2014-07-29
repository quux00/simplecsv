package net.quux00.simplecsv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.CsvReaderBuilder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class CsvReaderTest {

  public static final int INITIAL_READ_SIZE = 128;
  static final String CR = "\r";
  static final String LF = "\n";
  static final String CRLF = CR + LF;

  CsvReader csvr;
  String lines;
  
  /**
   * Setup the test.
   */
  @Before
  public void setUp() throws Exception {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
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
    for (List<String> line : csvr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line.toArray());
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
    for (List<String> line : cr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line.toArray());
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
    for (List<String> line : cr) {
      String[] expectedLine = expectedResult[idx++];
      assertArrayEquals(expectedLine, line.toArray());
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
    
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals(" abc", toks.get(1));
    assertEquals(" Stan \"The Man\" Musial", toks.get(2));
    assertEquals(" Mike \\\"The Situation\\\"", toks.get(3));
    assertEquals(" I\\nlike\\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("2", toks.get(0));
    assertEquals(" def", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("abc\"d\"efg ", toks.get(0));
    assertEquals("Stan \"The Man\" Musial        ", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2\\\\n", toks.get(0));
    assertEquals("\\f", toks.get(1));
    assertEquals("\\b", toks.get(2));
    assertEquals("\\r\\n", toks.get(3));
    assertEquals("\\t", toks.get(4));
    assertEquals("\tlast", toks.get(5));

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithStrictQuotes() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().strictQuotes(true).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("Stan  Musial", toks.get(2));
    assertEquals("Mike \\\"The Situation\\\"", toks.get(3));
    assertEquals("I\\nlike\\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("abcefg", toks.get(0));
    assertEquals("Stan  Musial", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("\\t", toks.get(4));
    assertEquals("", toks.get(5));

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithTrimWhitespaceAndRetainEscapeCharFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().trimWhitespace(true).retainEscapeChars(false).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals("abc", toks.get(1));
    assertEquals("Stan \"The Man\" Musial", toks.get(2));
    assertEquals("Mike \"The Situation\"", toks.get(3));
    assertEquals("I\nlike\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("2", toks.get(0));
    assertEquals("def", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("abc\"d\"efg", toks.get(0));
    assertEquals("Stan \"The Man\" Musial", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2n", toks.get(0));
    assertEquals("\f", toks.get(1));
    assertEquals("\b", toks.get(2));
    assertEquals("\r\n", toks.get(3));
    assertEquals("\t", toks.get(4));
    assertEquals("last", toks.get(5));

    toks = csvr.readNext();
    assertNull(toks);
  }
  
  
  @Test
  public void testRecordsFromFileWithStrictQuotesAndRetainEscapeCharsFalse() throws IOException {
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().strictQuotes(true).retainEscapeChars(false).build();
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
        
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("Stan  Musial", toks.get(2));
    assertEquals("Mike \"The Situation\"", toks.get(3));
    assertEquals("I\nlike\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("abcefg", toks.get(0));
    assertEquals("Stan  Musial", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("\t", toks.get(4));
    assertEquals("", toks.get(5));

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
        
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals("abc", toks.get(1));
    assertEquals("\"Stan \"The Man\" Musial\"", toks.get(2));
    assertEquals("\"Mike \"The Situation\"\"", toks.get(3));
    assertEquals("\"I\nlike\nIke\"", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("2", toks.get(0));
    assertEquals("def", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("\"abc\"d\"efg\"", toks.get(0));
    assertEquals("\"Stan \"The Man\" Musial\"", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2n", toks.get(0));
    assertEquals("\f", toks.get(1));
    assertEquals("\b", toks.get(2));
    assertEquals("\r\n", toks.get(3));
    assertEquals("\"\t\"", toks.get(4));
    assertEquals("last", toks.get(5));

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
        
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("\"1\"", toks.get(0));
    assertEquals("\"abc\"", toks.get(1));
    assertEquals("\"Stan \"The Man\" Musial\"", toks.get(2));
    assertEquals("\"Mike \"The Situation\"\"", toks.get(3));
    assertEquals("\"I\nlike\nIke\"", toks.get(4));

    toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("\"2\"", toks.get(0));
    assertEquals("\"def\"", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("", toks.get(4));
    
    toks = csvr.readNext();
    assertEquals(2, toks.size());
    assertEquals("\"abc\"d\"efg\"", toks.get(0));
    assertEquals("\"Stan \"The Man\" Musial\"", toks.get(1));

    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("\"2n\"", toks.get(0));
    assertEquals("\"\f\"", toks.get(1));
    assertEquals("\"\b\"", toks.get(2));
    assertEquals("\"\r\n\"", toks.get(3));
    assertEquals("\"\t\"", toks.get(4));
    assertEquals("\"last\"", toks.get(5));

    toks = csvr.readNext();
    assertNull(toks);
  }

  @Test(expected=IllegalArgumentException.class)
  public void testMultiLineInputWithSimpleCsvParser() throws IOException {
    FileReader fr = new FileReader("src/test/resources/quotednl.csv");
    csvr = new CsvReader(fr, 1);
    
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals(" abc", toks.get(1));
    assertEquals(" Stan \"\"The Man\"\" Musial", toks.get(2));
    assertEquals(" Mike \\\"The Situation\\\"", toks.get(3));
    assertEquals(" I\\nlike\\nIke", toks.get(4));

    csvr.readNext();
  }
  
  /* ---[ quotednl.csv tests with MultiLine Parser ]--- */
  
  @Test
  public void testMultiLineInputWithMultiLineParser() throws IOException {
    FileReader fr = new FileReader("src/test/resources/quotednl.csv");
    CsvParser p = new CsvParserBuilder().
        multiLine(true).
        build();
    assertTrue(p instanceof MultiLineCsvParser);
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
    
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals(" abc", toks.get(1));
    assertEquals(" Stan \"\"The Man\"\" Musial", toks.get(2));
    assertEquals(" Mike \\\"The Situation\\\"", toks.get(3));
    assertEquals(" I\\nlike\\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2", toks.get(0));
    assertEquals(" def", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("z\nabc\"d\"efg ", toks.get(4));
    assertEquals("Stan \"The Man\" Musial        ", toks.get(5));
    
    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2\\\\n", toks.get(0));
    assertEquals("\\f", toks.get(1));
    assertEquals("\\b", toks.get(2));
    assertEquals("\\r\\n", toks.get(3));
    assertEquals("\\t", toks.get(4));
    assertEquals("\tlast", toks.get(5));

    toks = csvr.readNext();
    assertNull(toks);
  }

  
  @Test
  public void testMultiLineInputWithMultiLineParserAndRfc4180Support() throws IOException {
    FileReader fr = new FileReader("src/test/resources/quotednl.csv");
    CsvParser p = new CsvParserBuilder().
        multiLine(true).
        supportRfc4180QuotedQuotes(true).
        build();
    assertTrue(p instanceof MultiLineCsvParser);
    csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
    
    List<String> toks = csvr.readNext();
    assertEquals(5, toks.size());
    assertEquals("1", toks.get(0));
    assertEquals(" abc", toks.get(1));
    // with RFC4180 turned on the double quotes in the input go to single quotes here
    assertEquals(" Stan \"The Man\" Musial", toks.get(2));  // key difference
    assertEquals(" Mike \\\"The Situation\\\"", toks.get(3));
    assertEquals(" I\\nlike\\nIke", toks.get(4));

    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2", toks.get(0));
    assertEquals(" def", toks.get(1));
    assertEquals("", toks.get(2));
    assertEquals("", toks.get(3));
    assertEquals("z\nabc\"d\"efg ", toks.get(4));
    assertEquals("Stan \"The Man\" Musial        ", toks.get(5));
    
    toks = csvr.readNext();
    assertEquals(1, toks.size());
    assertEquals("", toks.get(0));
    
    toks = csvr.readNext();
    assertEquals(6, toks.size());
    assertEquals("2\\\\n", toks.get(0));
    assertEquals("\\f", toks.get(1));
    assertEquals("\\b", toks.get(2));
    assertEquals("\\r\\n", toks.get(3));
    assertEquals("\\t", toks.get(4));
    assertEquals("\tlast", toks.get(5));

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
    List<String> toks = csvr.readNext();
    assertEquals("a", toks.get(0));
    assertEquals("b", toks.get(1));
    assertEquals("c", toks.get(2));

    // test quoted commas
    toks = csvr.readNext();
    assertEquals("a", toks.get(0));
    assertEquals("b,b,b", toks.get(1));
    assertEquals("c", toks.get(2));

    // test empty elements
    toks = csvr.readNext();
    assertEquals(3, toks.size());

    // test multiline quoted
    toks = csvr.readNext();
    assertEquals(3, toks.size());

    // test quoted quote chars
    toks = csvr.readNext();
    assertEquals("Glen \"\"The Man\"\" Smith", toks.get(0));

    toks = csvr.readNext();
    assertEquals("\"\"\"\"", toks.get(0)); 
    assertEquals("test", toks.get(1)); // make sure we didn't ruin the next field..

    toks = csvr.readNext();
    assertEquals(4, toks.size());

    //test end of stream
    assertNull(csvr.readNext());
  }

  @Test
  public void testParseLineStrictQuote() throws IOException {
    StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
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
    List<String> toks = csvr.readNext();
    assertEquals("", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("", toks.get(2));

    // test quoted commas
    toks = csvr.readNext();
    assertEquals("", toks.get(0));
    assertEquals("b,b,b", toks.get(1));
    assertEquals("", toks.get(2));

    // test empty elements
    toks = csvr.readNext();
    assertEquals(3, toks.size());

    // test multiline quoted
    toks = csvr.readNext();
    assertEquals(3, toks.size());

    // test quoted quote chars
    toks = csvr.readNext();
    assertEquals("Glen \\\"The Man\\\" Smith", toks.get(0));

    toks = csvr.readNext();
    assertEquals("", toks.get(0));
    assertEquals("test", toks.get(1));

    toks = csvr.readNext();
    assertEquals(4, toks.size());
    assertEquals("a\\nb", toks.get(0));
    assertEquals("", toks.get(1));
    assertEquals("\\nd", toks.get(2));
    assertEquals("", toks.get(3));

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

    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a\tb\tc").append("\n");   // tab separated case
    sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements
    
    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());

    nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    cr.close();
  }

  @Test
  public void parseQuotedStringWithDefinedSeperator() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a\tb\tc").append("\n");   // tab separated case

    CsvParser parser = new CsvParserBuilder().separator('\t').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());

    cr.close();
  }

  @Test
  public void parsePipeDelimitedString() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("[bar]|[baz]").append("\n");

    CsvParser parser = new CsvParserBuilder().separator('|').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(2, nextLine.size());
    assertEquals("[bar]", nextLine.get(0));
    assertEquals("[baz]", nextLine.get(1));

    cr.close();
  }

  /**
   * Tests option to skip the first few lines of a file.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testSkippingLines() throws IOException {

    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("Skip this line\t with tab").append("\n");   // should skip this
    sb.append("And this line too").append("\n");   // and this
    sb.append("a\t'b\tb\tb'\tc").append("\n");  // single quoted elements

    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').build();
    CsvReader cr = new CsvReaderBuilder(new StringReader(sb.toString())).
        csvParser(parser).
        skipLines(2).
        build();
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));

    cr.close();
  }


  /**
   * Tests option to skip the first few lines of a file.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testSkippingLinesWithDifferentEscape() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("Skip this line?t with tab").append("\n");   // should skip this
    sb.append("And this line too").append("\n");   // and this
    sb.append("a\t'b\tb\tb'\t'c'").append("\n");  // single quoted elements

    CsvParser parser = new CsvParserBuilder().separator('\t').quoteChar('\'').escapeChar('?').build();
    CsvReader cr = new CsvReaderBuilder(new StringReader(sb.toString())).
        csvParser(parser).
        skipLines(2).
        build();
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));
    assertEquals("c", nextLine.get(2));

    cr.close();
  }

  /**
   * Test a normal non quoted line with three elements
   *
   * @throws IOException
   */
  @Test
  public void testNormalParsedLine() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a,1234567,c").append("\n");// a,1234,c

    CsvParser parser = new SimpleCsvParser();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());

    assertEquals("a", nextLine.get(0));
    assertEquals("1234567", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
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
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);

    sb.append("a,'''',c").append("\n");// a,'',c

    CsvParser parser = new CsvParserBuilder().quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));
    assertEquals("''", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
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

    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);

    sb.append("a,'',c").append("\n");// a,,c

    CsvParser parser = new CsvParserBuilder().quoteChar('\'').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));
    assertEquals(0, nextLine.get(1).length());
    assertEquals("", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
    cr.close();
  }

  @Test
  public void testSpacesAtEndOfString() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("\"a\",\"b\",\"c\"   ");

    CsvParser parser = new CsvParserBuilder().strictQuotes(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));
    assertEquals("b", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
    cr.close();
  }


  @Test
  public void testEscapedQuote() throws IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("a,\"123\\\"4567\",c").append("\n");// a,123"4",c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());

    assertEquals("123\\\"4567", nextLine.get(1));
    cr.close();
  }

  @Test
  public void testEscapedEscape() throws IOException {
    StringBuffer sb = new StringBuffer();
    sb.append("a,\"123\\\\4567\",c").append("\n");// a,"123\\4567",c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));
    
    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("123\\\\4567", nextLine.get(1));
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
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a,'',c").append("\n");// a,'',c

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());
    assertEquals("a", nextLine.get(0));
    assertEquals(2, nextLine.get(1).length());
    assertEquals("''", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
    cr.close();
  }

  /**
   * Test a normal line with three elements and all elements are quoted
   *
   * @throws IOException
   */
  @Test
  public void testQuotedParsedLine() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("\"a\",\"1234567\",\"c\"").append("\n"); // "a","1234567","c"

    CsvParser parser = new CsvParserBuilder().strictQuotes(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);

    List<String> nextLine = cr.readNext();
    assertEquals(3, nextLine.size());

    assertEquals("a", nextLine.get(0));
    assertEquals(1, nextLine.get(0).length());

    assertEquals("1234567", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
    cr.close();
  }

  @Test
  public void testIssue2992134OutOfPlaceQuotes() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvReader cr = new CsvReader(new StringReader(sb.toString()));

    List<String> nextLine = cr.readNext();
    assertEquals("a", nextLine.get(0));
    assertEquals("b", nextLine.get(1));
    assertEquals("c", nextLine.get(2));
    assertEquals("ddd\\\"eee", nextLine.get(3));

    nextLine = cr.readNext();
    assertEquals("f", nextLine.get(0));
    assertEquals("g", nextLine.get(1));
    assertEquals("h", nextLine.get(2));
    assertEquals("iii,jjj", nextLine.get(3));

    cr.close();
  }

  @Test
  public void testIssue2992134OutOfPlaceQuotesAlwaysQuoteOutput() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvParser p = new CsvParserBuilder().alwaysQuoteOutput(true).build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), p);

    List<String> nextLine = cr.readNext();
    assertEquals("\"a\"", nextLine.get(0));
    assertEquals("\"b\"", nextLine.get(1));
    assertEquals("\"c\"", nextLine.get(2));
    assertEquals("\"ddd\\\"eee\"", nextLine.get(3));

    nextLine = cr.readNext();
    assertEquals("\"f\"", nextLine.get(0));
    assertEquals("\"g\"", nextLine.get(1));
    assertEquals("\"h\"", nextLine.get(2));
    assertEquals("\"iii,jjj\"", nextLine.get(3));

    cr.close();
  }
  
  @Test
  public void testASingleQuoteAsDataElementWithEmptyField2() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("\"\";1").append("\n");// ;1
    sb.append("\"\";2").append("\n");// ;2

    CsvParser parser = new CsvParserBuilder().separator(';').build();
    CsvReader cr = new CsvReader(new StringReader(sb.toString()), parser);
    
    List<String> nextLine = cr.readNext();
    assertEquals(2, nextLine.size());

    assertEquals(0, nextLine.get(0).length());
    assertEquals("1", nextLine.get(1));

    nextLine = cr.readNext();
    assertEquals(2, nextLine.size());

    assertEquals("", nextLine.get(0));
    assertEquals(0, nextLine.get(0).length());
    assertEquals("2", nextLine.get(1));

    cr.close();
  }


  @Test(expected = UnsupportedOperationException.class)
  public void quoteAndEscapeMustBeDifferent() throws IOException {
    StringBuilder sb = new StringBuilder(SimpleCsvParser.INITIAL_READ_SIZE);
    sb.append("a,b,c,ddd\\\"eee\nf,g,h,\"iii,jjj\"");

    CsvParser parser = new CsvParserBuilder().
        quoteChar(ParserUtil.DEFAULT_QUOTE_CHAR).
        escapeChar(ParserUtil.DEFAULT_QUOTE_CHAR).
        build();
    CsvReader cr = null;
    try {
      cr = new CsvReader(new StringReader(sb.toString()), parser);
    } finally {
      if (cr != null) cr.close();
    }
  }
  

  /* ---[ Test OpenCSV bug 97 ]--- */
  // https://sourceforge.net/p/opencsv/bugs/97/
  
  private List<List<String>> getTestData() {
    List<List<String>> list = new ArrayList<List<String>>();
    list.add(Arrays.asList("quote\"", "escape\\", "normal"));
    list.add(Arrays.asList("double \"quote\"", "middle \\escape", "regular"));
    list.add(Arrays.asList("typical", "end escape\\", "ordinary"));
    list.add(Arrays.asList("one", "two", "three"));
    return list;
}
  
  @Test
  public void defaultWriterDefaultReader() throws Exception {
      File file = new File("./tmptesting.csv");
      try {
        CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter(file)));
        writer.writeAll(getTestData());
        writer.close();
        CsvReader reader = new CsvReader(new FileReader(file));
        List<List<String>> list = reader.readAll();
        reader.close();
        assertEquals(4, list.size());
      } finally {
        file.delete();
      }
  }

  @Test
  public void customWriterDefaultReader() throws Exception {
      File file = new File("./tmptesting.csv");
      try {
        CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter(file)), ',', '"', '\\');
        writer.writeAll(getTestData());
        writer.close();
        CsvReader reader = new CsvReader(new FileReader(file));
        List<List<String>> list = reader.readAll();
        reader.close();
        assertEquals(4, list.size());
      
      } finally {
        file.delete();
      }
  }

  @Test
  public void defaultWriterCustomReader() throws Exception {
      File file = new File("./tmptesting.csv");
      try {
        CsvWriter writer = new CsvWriter(new BufferedWriter(new FileWriter(file)));
        writer.writeAll(getTestData());
        writer.close();
        CsvParser p = new CsvParserBuilder().
            escapeChar('\0').
            allowUnbalancedQuotes(true).
            build();
        CsvReader reader = new CsvReader(new FileReader(file), p);
        List<List<String>> list = reader.readAll();
        reader.close();
        assertEquals(4, list.size());
        
      } finally {
        file.delete();
      }
  }
  /* ---[ END Test OpenCSV bug 97 ]--- */
  
  
  
  // CsvReader tests using MultiLine parser
  // TODO: this should be moved to the CsvReader unit test => why is this here?
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
    List<String> result = r.readNext();
    assertArrayEquals(new String[]{"\"a\""}, result.toArray());

    // LINE 2 =  , + CRLF == _,_ (line starting with a comma and then a CR or CRLF is actually two emtpy fields
    result = r.readNext();
    assertArrayEquals(new String[]{"", ""}, result.toArray());

    // LINE 3 = 'b' + LF
    result = r.readNext();
    assertArrayEquals(new String[]{"\"b\""}, result.toArray());

    // LINE 4 =  , + 'c' + CR + EOF = empty + c
    result = r.readNext();
    assertArrayEquals(new String[]{"", "\"c\""}, result.toArray());
    
    r.close();
  }

  @Test
  public void rfc4180PlusChangeEscapeCharToDoubleQuote() throws IOException {
    CsvParser rfc4180 = new CsvParserBuilder().
        multiLine(true).
        supportRfc4180QuotedQuotes(true).
        escapeChar('"').
        quoteChar('\'').
        build();

    // all quoted of course
    CsvReader r = new CsvReader(new StringReader("'a\r\nb',b\\\b,'\\nd',e\n"), rfc4180);

    List<String> toks = r.readNext();
    assertEquals(4, toks.size());
    assertEquals("a\r\nb", toks.get(0));
    assertEquals("b\\\b", toks.get(1)); 
    assertEquals("\\nd", toks.get(2));
    assertEquals("e", toks.get(3));

    r = new CsvReader(new StringReader("'Stan \"\"The Man\"\"'"), rfc4180);
    toks = r.readNext();
    assertEquals(1, toks.size());
    assertEquals("Stan \"\"The Man\"\"", toks.get(0));
    
    r.close();
  }
}

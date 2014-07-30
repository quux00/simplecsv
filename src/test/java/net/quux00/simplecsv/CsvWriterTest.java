package net.quux00.simplecsv;
/**
Copyright 2005 Bytecode Pty Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.quux00.simplecsv.CsvWriter;
import net.quux00.simplecsv.CsvWriterBuilder;
import net.quux00.simplecsv.resultset.MockResultSetBuilder;
import net.quux00.simplecsv.resultset.ResultSetHelperService;

import org.junit.Test;


public class CsvWriterTest {

  /**
   * Test routine for converting output to a string.
   *
   * @param args the elements of a line of the cvs file
   * @return a String version
   * @throws IOException if there are problems writing
   */
  private String invokeWriter(List<String> args) throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).
        quoteChar('\'').
        build();

    csvw.writeNext(args);
    csvw.close();
    return sw.toString();
  }

  private String invokeNoEscapeWriter(List<String> args) throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).
        quoteChar('\'').
        escapeChar(CsvWriter.NO_ESCAPE_CHARACTER).
        build();
    csvw.writeNext(args);
    csvw.close();
    return sw.toString();
  }

  @Test
  public void correctlyParseNullString() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).quoteChar('\'').build();
    csvw.writeNext(null);
    assertEquals(0, sw.toString().length());
    csvw.close();
  }

  @Test
  public void correctlyParserNullObject() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).quoteChar('\'').build();
    csvw.writeNext(null, false);
    assertEquals(0, sw.toString().length());
    csvw.close();
  }

  /**
   * Tests parsing individual lines.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testParseLine() throws IOException {
    // test normal case
    List<String> normal = Arrays.asList("a", "b", "c");
    String output = invokeWriter(normal);
    assertEquals("'a','b','c'\n", output);

    // test quoted commas
    List<String> quoted = Arrays.asList("a", "b,b,b", "c");
    output = invokeWriter(quoted);
    assertEquals("'a','b,b,b','c'\n", output);

    // test empty elements
    List<String> empty = new ArrayList<String>();
    output = invokeWriter(empty);
    assertEquals("\n", output);

    // test multiline quoted
    List<String> multiline = Arrays.asList("This is a \n multiline entry", "so is \n this");
    output = invokeWriter(multiline);
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output);


    // test quoted line
    List<String> quoteLine = Arrays.asList("This is a \" multiline entry", "so is \n this");
    output = invokeWriter(quoteLine);
    assertEquals("'This is a \" multiline entry','so is \n this'\n", output);

  }

  @Test
  public void testSpecialCharacters() throws IOException {
    // test quoted line
    List<String> quoteLine = Arrays.asList("This is a \r multiline entry", "so is \n this");
    String output = invokeWriter(quoteLine);
    assertEquals("'This is a \r multiline entry','so is \n this'\n", output);
  }

  @Test
  public void parseLineWithBothEscapeAndQuoteChar() throws IOException {
    // test quoted line
    List<String> quoteLine = Arrays.asList("This is a 'multiline' entry", "so is \n this");
    String output = invokeWriter(quoteLine);
    assertEquals("'This is a \\'multiline\\' entry','so is \n this'\n", output);
  }

  /**
   * Tests parsing individual lines.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testParseLineWithNoEscapeChar() throws IOException {
    // test normal case
    List<String> normal = Arrays.asList("a", "b", "c");
    String output = invokeNoEscapeWriter(normal);
    assertEquals("'a','b','c'\n", output);

    // test quoted commas
    List<String> quoted = Arrays.asList("a", "b,b,b", "c");
    output = invokeNoEscapeWriter(quoted);
    assertEquals("'a','b,b,b','c'\n", output);

    // test empty elements
    List<String> empty = new ArrayList<String>();
    output = invokeNoEscapeWriter(empty);
    assertEquals("\n", output);

    // test multiline quoted
    List<String> multiline = Arrays.asList("This is a \n multiline entry", "so is \n this");
    output = invokeNoEscapeWriter(multiline);
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output);
  }

  @Test
  public void parseLineWithNoEscapeCharAndQuotes() throws IOException {
    List<String> quoteLine = Arrays.asList("This is a \" 'multiline' entry", "so is \n this");
    String output = invokeNoEscapeWriter(quoteLine);
    assertEquals("'This is a \" 'multiline' entry','so is \n this'\n", output);
  }


  /**
   * Test writing to a list.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testWriteAll() throws IOException {
    List<List<String>> allElements = new ArrayList<List<String>>();
    List<String> line1 = Arrays.asList("Name#Phone#Email".split("#"));
    List<String> line2 = Arrays.asList("Glen#1234#glen@abcd.com".split("#"));
    List<String> line3 = Arrays.asList("John#5678#john@efgh.com".split("#"));
    allElements.add(line1);
    allElements.add(line2);
    allElements.add(line3);

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.writeAll(allElements);

    String result = sw.toString();
    String[] lines = result.split("\n");

    assertEquals(3, lines.length);
    csvw.close();
  }

  /**
   * Test writing from a list.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testWriteAllObjects() throws IOException {
    List<List<String>> allElements = new ArrayList<List<String>>(3);
    List<String> line1 = Arrays.asList("Name#Phone#Email".split("#"));
    List<String> line2 = Arrays.asList("Glen#1234#glen@abcd.com".split("#"));
    List<String> line3 = Arrays.asList("John#5678#john@efgh.com".split("#"));
    allElements.add(line1);
    allElements.add(line2);
    allElements.add(line3);

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.writeAll(allElements, false);

    String result = sw.toString();
    String[] lines = result.split("\n");

    assertEquals(3, lines.length);

    String[] values = lines[1].split(",");
    assertEquals("1234", values[1]);
    csvw.close();
  }

  /**
   * Tests the option of having omitting quotes in the output stream.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testNoQuoteChars() throws IOException {
    List<String> line = Arrays.asList("Foo", "Bar", "Baz");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.NO_QUOTE_CHARACTER);
    csvw.writeNext(line);
    csvw.close();
    String result = sw.toString();

    assertEquals("Foo,Bar,Baz\n", result);
  }

  /**
   * Tests the option of having omitting quotes in the output stream.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testNoQuoteCharsAndNoEscapeChars() throws IOException {
    List<String> line = Arrays.asList("Foo", "Bar's", "Baz");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).
        quoteChar(CsvWriter.NO_QUOTE_CHARACTER).
        separator('\t').
        build();
    csvw.writeNext(line);
    csvw.close();
    String result = sw.toString();

    assertEquals("Foo\tBar's\tBaz\n", result);
  }

  /**
   * Tests the ability for the writer to apply quotes only where strings contain the separator, escape, quote or new line characters.
   * @throws IOException 
   */
  @Test
  public void testIntelligentQuotes() throws IOException {
    List<String> line = Arrays.asList("1", "Foo", "With,Separator", "Line\nBreak", "Hello \"Foo Bar\" World", "Bar");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.writeNext(line, false);
    csvw.close();
    
    String result = sw.toString();
    assertEquals("1,Foo,\"With,Separator\",\"Line\nBreak\",\"Hello \\\"Foo Bar\\\" World\",Bar\n", result);
  }


  /**
   * Test null values.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testNullValues() throws IOException {
    List<String> line = Arrays.asList("Foo", null, "Bar", "baz");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.writeNext(line);
    csvw.close();
    
    String result = sw.toString();
    assertEquals("\"Foo\",,\"Bar\",\"baz\"\n", result);
  }

  @Test
  public void testStreamFlushing() throws IOException {
    String WRITE_FILE = "myfile.csv";
    List<String> nextLine = Arrays.asList("aaaa", "bbbb", "cccc", "dddd");

    FileWriter fileWriter = new FileWriter(WRITE_FILE);
    CsvWriter writer = new CsvWriter(fileWriter);

    writer.writeNext(nextLine);

    // If this line is not executed, it is not written in the file.
    writer.close();
  }

  @Test(expected = IOException.class)
  public void flushWillThrowIOException() throws IOException {
    List<String> line = Arrays.asList("Foo", "bar's");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterExceptionThrower(sw);
    csvw.writeNext(line);
    csvw.flush();
    csvw.close();
  }

  @Test
  public void flushQuietlyWillNotThrowException() throws IOException {
    List<String> line = Arrays.asList("Foo", "bar's");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterExceptionThrower(sw);
    csvw.writeNext(line);
    csvw.flushQuietly();
    // this is here just to avoid compiler warnings of unclosed resources
    try {
      csvw.close();
    } catch (Exception e) {}
  }


  @Test
  public void testAlternateEscapeChar() throws IOException {
    List<String> line = Arrays.asList("Foo", "bar's");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).escapeChar('\'').build();
    csvw.writeNext(line);
    csvw.close();
    assertEquals("\"Foo\",\"bar''s\"\n", sw.toString());
  }

  @Test
  public void testNoQuotingNoEscaping() throws IOException {
    List<String> line = Arrays.asList("\"Foo\",\"Bar\"");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).
        escapeChar(CsvWriter.NO_ESCAPE_CHARACTER).
        quoteChar(CsvWriter.NO_QUOTE_CHARACTER).
        build();
    csvw.writeNext(line);
    csvw.close();
    assertEquals("\"Foo\",\"Bar\"\n", sw.toString());
  }

  @Test
  public void testNestedQuotes() {
    List<String> data = Arrays.asList("\"\"", "test");
    String oracle = new String("\"\\\"\\\"\",\"test\"\n");

    CsvWriter writer = null;
    File tempFile = null;
    FileWriter fwriter = null;

    try {
      tempFile = File.createTempFile("CsvWriterTest", ".csv");
      tempFile.deleteOnExit();
      fwriter = new FileWriter(tempFile);
      writer = new CsvWriter(fwriter);
    } catch (IOException e) {
      fail();
    }

    // write the test data:
    writer.writeNext(data);

    try {
      writer.close();
    } catch (IOException e) {
      fail();
    }

    try {
      // assert that the writer was also closed.
      fwriter.flush();
      fail();
    } catch (IOException e) {
      // we should go through here..
    }

    // read the data and compare.
    FileReader in = null;
    try {
      in = new FileReader(tempFile);
    } catch (FileNotFoundException e) {
      fail();
    }

    StringBuilder fileContents = new StringBuilder(CsvWriter.INITIAL_STRING_SIZE);
    try {
      int ch;
      while ((ch = in.read()) != -1) {
        fileContents.append((char) ch);
      }
      in.close();
    } catch (IOException e) {
      fail();
    }

    assertEquals(oracle, fileContents.toString());
  }
  

  @Test
  public void testAlternateLineFeeds() throws IOException {
    List<String> line = Arrays.asList("Foo", "Bar", "baz");
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).lineEnd("\r").build();
    csvw.writeNext(line);
    csvw.close();
    
    String result = sw.toString();
    assertTrue(result.endsWith("\r"));
  }

  @Test
  public void testResultSetWithHeaders() throws SQLException, IOException {
    String[] header = {"Foo", "Bar", "baz"};
    String[] value = {"v1", "v2", "v3"};

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.setResultService(new ResultSetHelperService());

    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

    csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
    csvw.close();
    assertFalse(csvw.checkError());
    String result = sw.toString();

    assertNotNull(result);
    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
  }

  @Test
  public void testMultiLineResultSetWithHeaders() throws SQLException, IOException {
    String[] header = {"Foo", "Bar", "baz"};
    String[] value = {"v1", "v2", "v3"};

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.setResultService(new ResultSetHelperService());

    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);

    csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
    assertFalse(csvw.checkError());
    String result = sw.toString();

    assertNotNull(result);
    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
    csvw.close();
  }

  @Test
  public void testResultSetWithoutHeaders() throws SQLException, IOException {
    String[] header = {"Foo", "Bar", "baz"};
    String[] value = {"v1", "v2", "v3"};

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.setResultService(new ResultSetHelperService());

    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

    csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.
    assertFalse(csvw.checkError());
    String result = sw.toString();

    assertNotNull(result);
    assertEquals("\"v1\",\"v2\",\"v3\"\n", result);
    csvw.close();
  }

  @Test
  public void testMultiLineResultSetWithoutHeaders() throws SQLException, IOException {
    String[] header = {"Foo", "Bar", "baz"};
    String[] value = {"v1", "v2", "v3"};

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.setResultService(new ResultSetHelperService());

    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);

    csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.

    assertFalse(csvw.checkError());
    String result = sw.toString();

    assertNotNull(result);
    assertEquals("\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
    csvw.close();
  }

  @Test
  public void testResultSetTrim() throws SQLException, IOException {
    String[] header = {"Foo", "Bar", "baz"};
    String[] value  = {"v1         ", "v2 ", "v3"};

    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw);
    csvw.setResultService(new ResultSetHelperService());

    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);

    csvw.writeAll(rs, true, true); // don't need a result set since I am mocking the result.
    assertFalse(csvw.checkError());
    String result = sw.toString();

    assertNotNull(result);
    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
    csvw.close();
  }
}

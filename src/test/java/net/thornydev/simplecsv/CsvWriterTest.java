package net.thornydev.simplecsv;
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

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class CsvWriterTest {

  /**
   * Test routine for converting output to a string.
   *
   * @param args the elements of a line of the cvs file
   * @return a String version
   * @throws IOException if there are problems writing
   */
  private String invokeWriter(String[] args) throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, ',', '\'');
    csvw.writeNext(args);
    csvw.close();
    return sw.toString();
  }

  private String invokeNoEscapeWriter(String[] args) throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, '\'', CsvWriter.NO_ESCAPE_CHARACTER);
    csvw.writeNext(args);
    csvw.close();
    return sw.toString();
  }

  @Test
  public void correctlyParseNullString() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, ',', '\'');
    csvw.writeNext(null);
    assertEquals(0, sw.toString().length());
    csvw.close();
  }

  @Test
  public void correctlyParserNullObject() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, ',', '\'');
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
    String[] normal = {"a", "b", "c"};
    String output = invokeWriter(normal);
    assertEquals("'a','b','c'\n", output);

    // test quoted commas
    String[] quoted = {"a", "b,b,b", "c"};
    output = invokeWriter(quoted);
    assertEquals("'a','b,b,b','c'\n", output);

    // test empty elements
    String[] empty = {,};
    output = invokeWriter(empty);
    assertEquals("\n", output);

    // test multiline quoted
    String[] multiline = {"This is a \n multiline entry", "so is \n this"};
    output = invokeWriter(multiline);
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output);


    // test quoted line
    String[] quoteLine = {"This is a \" multiline entry", "so is \n this"};
    output = invokeWriter(quoteLine);
    assertEquals("'This is a \"\" multiline entry','so is \n this'\n", output);

  }

  @Test
  public void testSpecialCharacters() throws IOException {
    // test quoted line
    String[] quoteLine = {"This is a \r multiline entry", "so is \n this"};
    String output = invokeWriter(quoteLine);
    assertEquals("'This is a \r multiline entry','so is \n this'\n", output);
  }

  @Test
  public void parseLineWithBothEscapeAndQuoteChar() throws IOException {
    // test quoted line
    String[] quoteLine = {"This is a 'multiline' entry", "so is \n this"};
    String output = invokeWriter(quoteLine);
    assertEquals("'This is a \"'multiline\"' entry','so is \n this'\n", output);
  }

  /**
   * Tests parsing individual lines.
   *
   * @throws IOException if the reader fails.
   */
  @Test
  public void testParseLineWithNoEscapeChar() throws IOException {
    // test normal case
    String[] normal = {"a", "b", "c"};
    String output = invokeNoEscapeWriter(normal);
    assertEquals("'a','b','c'\n", output);

    // test quoted commas
    String[] quoted = {"a", "b,b,b", "c"};
    output = invokeNoEscapeWriter(quoted);
    assertEquals("'a','b,b,b','c'\n", output);

    // test empty elements
    String[] empty = {,};
    output = invokeNoEscapeWriter(empty);
    assertEquals("\n", output);

    // test multiline quoted
    String[] multiline = {"This is a \n multiline entry", "so is \n this"};
    output = invokeNoEscapeWriter(multiline);
    assertEquals("'This is a \n multiline entry','so is \n this'\n", output);
  }

  @Test
  public void parseLineWithNoEscapeCharAndQuotes() throws IOException {
    String[] quoteLine = {"This is a \" 'multiline' entry", "so is \n this"};
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
    List<String[]> allElements = new ArrayList<String[]>();
    String[] line1 = "Name#Phone#Email".split("#");
    String[] line2 = "Glen#1234#glen@abcd.com".split("#");
    String[] line3 = "John#5678#john@efgh.com".split("#");
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
    List<String[]> allElements = new ArrayList<String[]>(3);
    String[] line1 = "Name#Phone#Email".split("#");
    String[] line2 = "Glen#1234#glen@abcd.com".split("#");
    String[] line3 = "John#5678#john@efgh.com".split("#");
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
    String[] line = {"Foo", "Bar", "Baz"};
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
    String[] line = {"Foo", "Bar", "Baz"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.NO_QUOTE_CHARACTER, CsvWriter.NO_ESCAPE_CHARACTER);
    csvw.writeNext(line);
    csvw.close();
    String result = sw.toString();

    assertEquals("Foo,Bar,Baz\n", result);
  }

  /**
   * Tests the ability for the writer to apply quotes only where strings contain the separator, escape, quote or new line characters.
   * @throws IOException 
   */
  @Test
  public void testIntelligentQuotes() throws IOException {
    String[] line = {"1", "Foo", "With,Separator", "Line\nBreak", "Hello \"Foo Bar\" World", "Bar"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.DEFAULT_QUOTE_CHARACTER, CsvWriter.DEFAULT_ESCAPE_CHARACTER);
    csvw.writeNext(line, false);
    csvw.close();
    
    String result = sw.toString();
    assertEquals("1,Foo,\"With,Separator\",\"Line\nBreak\",\"Hello \"\"Foo Bar\"\" World\",Bar\n", result);
  }


  /**
   * Test null values.
   *
   * @throws IOException if bad things happen
   */
  @Test
  public void testNullValues() throws IOException {
    String[] line = {"Foo", null, "Bar", "baz"};
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
    String[] nextLine = new String[]{"aaaa", "bbbb", "cccc", "dddd"};

    FileWriter fileWriter = new FileWriter(WRITE_FILE);
    CsvWriter writer = new CsvWriter(fileWriter);

    writer.writeNext(nextLine);

    // If this line is not executed, it is not written in the file.
    writer.close();
  }

  @Test(expected = IOException.class)
  public void flushWillThrowIOException() throws IOException {
    String[] line = {"Foo", "bar's"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterExceptionThrower(sw);
    csvw.writeNext(line);
    csvw.flush();
    csvw.close();
  }

  @Test
  public void flushQuietlyWillNotThrowException() throws IOException {
    String[] line = {"Foo", "bar's"};
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
    String[] line = {"Foo", "bar's"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.DEFAULT_QUOTE_CHARACTER, '\'');
    csvw.writeNext(line);
    csvw.close();
    assertEquals("\"Foo\",\"bar''s\"\n", sw.toString());
  }

  @Test
  public void testNoQuotingNoEscaping() throws IOException {
    String[] line = {"\"Foo\",\"Bar\""};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.NO_QUOTE_CHARACTER, CsvWriter.NO_ESCAPE_CHARACTER);
    csvw.writeNext(line);
    csvw.close();
    assertEquals("\"Foo\",\"Bar\"\n", sw.toString());
  }

  @Test
  public void testNestedQuotes() {
    String[] data = new String[]{"\"\"", "test"};
    String oracle = new String("\"\"\"\"\"\",\"test\"\n");

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

    assertTrue(oracle.equals(fileContents.toString()));
  }

  @Test
  public void testAlternateLineFeeds() throws IOException {
    String[] line = {"Foo", "Bar", "baz"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriter(sw, CsvWriter.DEFAULT_SEPARATOR, CsvWriter.DEFAULT_QUOTE_CHARACTER, "\r");
    csvw.writeNext(line);
    csvw.close();
    
    String result = sw.toString();
    assertTrue(result.endsWith("\r"));
  }

//  @Test
//  public void testResultSetWithHeaders() throws SQLException, IOException {
//    String[] header = {"Foo", "Bar", "baz"};
//    String[] value = {"v1", "v2", "v3"};
//
//    StringWriter sw = new StringWriter();
//    CsvWriter csvw = new CsvWriter(sw);
//    csvw.setResultService(new ResultSetHelperService());
//
//    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
//
//    csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
//    assertFalse(csvw.checkError());
//    String result = sw.toString();
//
//    assertNotNull(result);
//    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
//  }
//
//  @Test
//  public void testMultiLineResultSetWithHeaders() throws SQLException, IOException {
//    String[] header = {"Foo", "Bar", "baz"};
//    String[] value = {"v1", "v2", "v3"};
//
//    StringWriter sw = new StringWriter();
//    CsvWriter csvw = new CsvWriter(sw);
//    csvw.setResultService(new ResultSetHelperService());
//
//    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);
//
//    csvw.writeAll(rs, true); // don't need a result set since I am mocking the result.
//    assertFalse(csvw.checkError());
//    String result = sw.toString();
//
//    assertNotNull(result);
//    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
//  }

//  @Test
//  public void testResultSetWithoutHeaders() throws SQLException, IOException {
//    String[] header = {"Foo", "Bar", "baz"};
//    String[] value = {"v1", "v2", "v3"};
//
//    StringWriter sw = new StringWriter();
//    CsvWriter csvw = new CsvWriter(sw);
//    csvw.setResultService(new ResultSetHelperService());
//
//    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
//
//    csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.
//    assertFalse(csvw.checkError());
//    String result = sw.toString();
//
//    assertNotNull(result);
//    assertEquals("\"v1\",\"v2\",\"v3\"\n", result);
//  }
//
//  @Test
//  public void testMultiLineResultSetWithoutHeaders() throws SQLException, IOException {
//    String[] header = {"Foo", "Bar", "baz"};
//    String[] value = {"v1", "v2", "v3"};
//
//    StringWriter sw = new StringWriter();
//    CsvWriter csvw = new CsvWriter(sw);
//    csvw.setResultService(new ResultSetHelperService());
//
//    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 3);
//
//    csvw.writeAll(rs, false); // don't need a result set since I am mocking the result.
//
//    assertFalse(csvw.checkError());
//    String result = sw.toString();
//
//    assertNotNull(result);
//    assertEquals("\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n\"v1\",\"v2\",\"v3\"\n", result);
//  }
//
//  @Test
//  public void testResultSetTrim() throws SQLException, IOException {
//    String[] header = {"Foo", "Bar", "baz"};
//    String[] value = {"v1         ", "v2 ", "v3"};
//
//    StringWriter sw = new StringWriter();
//    CsvWriter csvw = new CsvWriter(sw);
//    csvw.setResultService(new ResultSetHelperService());
//
//    ResultSet rs = MockResultSetBuilder.buildResultSet(header, value, 1);
//
//    csvw.writeAll(rs, true, true); // don't need a result set since I am mocking the result.
//    assertFalse(csvw.checkError());
//    String result = sw.toString();
//
//    assertNotNull(result);
//    assertEquals("\"Foo\",\"Bar\",\"baz\"\n\"v1\",\"v2\",\"v3\"\n", result);
//  }
}

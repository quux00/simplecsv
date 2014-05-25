package net.quux00.simplecsv;

import net.quux00.simplecsv.SimpleCsvParser;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.CsvWriter;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UniCodeTest {
  
  CsvParser csvParser;
  
  private static final String COMPOUND_STRING = "??,??";
  private static final String COMPOUND_STRING_WITH_QUOTES = "\"??\",\"??\"";
  private static final String FIRST_STRING = "??";
  private static final String SECOND_STRING = "??";
  private static final List<String> UNICODE_LIST = Arrays.asList(FIRST_STRING, SECOND_STRING);
  private static final List<String> MIXED_LIST = Arrays.asList("eins, 1", "ichi", FIRST_STRING, SECOND_STRING);
  private static final List<String> ASCII_LIST = Arrays.asList("foo", "bar");
  private static final String ASCII_STRING_WITH_QUOTES = "\"foo\",\"bar\"";

  @Test
  public void canParseUnicode() throws IOException {
    csvParser = new SimpleCsvParser();
    String simpleString = COMPOUND_STRING;
    List<String> items = csvParser.parse(simpleString);
    assertEquals(2, items.size());
    assertEquals(FIRST_STRING, items.get(0));
    assertEquals(SECOND_STRING, items.get(1));
    assertEquals(UNICODE_LIST, items);
  }

  @Test
  public void readerTest() throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(FIRST_STRING));
    String testString = reader.readLine();
    assertEquals(FIRST_STRING, testString);
  }

  @Test
  public void writerTest(){
    StringWriter sw = new StringWriter();
    sw.write(FIRST_STRING);
    assertEquals(FIRST_STRING, sw.toString());
  }

  @Test
  public void runUniCodeThroughCSVReader() throws IOException {
    CsvReader reader = new CsvReader(new StringReader(COMPOUND_STRING));
    List<String> items = reader.readNext();
    reader.close();
    assertEquals(2, items.size());
    assertEquals(FIRST_STRING, items.get(0));
    assertEquals(SECOND_STRING, items.get(1));
    assertEquals(UNICODE_LIST, items);
  }

  @Test
  public void runUniCodeThroughCsvWriter() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(UNICODE_LIST);
    writer.close();
    assertEquals(COMPOUND_STRING_WITH_QUOTES.trim(), sw.toString().trim());
  }

  @Test
  public void runASCIIThroughCsvWriter() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(ASCII_LIST);
    writer.close();
    assertEquals(ASCII_STRING_WITH_QUOTES.trim(), sw.toString().trim());
  }

  @Test
  public void writeThenReadAscii() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(ASCII_LIST);
    writer.close();

    CsvReader reader = new CsvReader(new StringReader(sw.toString()));
    List<String> items = reader.readNext();
    reader.close();
    assertEquals(2, items.size());
    assertEquals(ASCII_LIST, items);
  }

  @Test
  public void writeThenReadTwiceAscii() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(ASCII_LIST);
    writer.writeNext(ASCII_LIST);
    writer.close();
    
    CsvReader reader = new CsvReader(new StringReader(sw.toString()));
    List<List<String>> lines = reader.readAll();
    reader.close();
    assertEquals(2, lines.size());

    List<String> items = lines.get(0);
    assertEquals(2, items.size());
    assertEquals(ASCII_LIST, items);


    items = lines.get(1);
    assertEquals(2, items.size());
    assertEquals(ASCII_LIST, items);
  }

  @Test
  public void writeThenReadTwiceUnicode() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(UNICODE_LIST);
    writer.writeNext(UNICODE_LIST);
    writer.close();

    CsvReader reader = new CsvReader(new StringReader(sw.toString()));

    List<List<String>> lines = reader.readAll();
    reader.close();
    assertEquals(2, lines.size());

    List<String> items = lines.get(0);
    assertEquals(2, items.size());
    assertEquals(UNICODE_LIST, items);

    items = lines.get(1);
    assertEquals(2, items.size());
    assertEquals(UNICODE_LIST, items);
  }

  @Test
  public void writeThenReadTwiceMixedUnicode() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter writer = new CsvWriter(sw);
    writer.writeNext(MIXED_LIST);
    writer.writeNext(MIXED_LIST);
    writer.close();

    CsvReader reader = new CsvReader(new StringReader(sw.toString()));
    List<List<String>> lines = reader.readAll();
    reader.close();
    assertEquals(2, lines.size());

    List<String> items = lines.get(0);
    assertEquals(4, items.size());
    assertEquals(MIXED_LIST, items);
    assertArrayEquals(MIXED_LIST.toArray(), items.toArray());

    items = lines.get(1);
    assertEquals(4, items.size());
    assertEquals(MIXED_LIST, items);
    assertArrayEquals(MIXED_LIST.toArray(), items.toArray());
  }
}

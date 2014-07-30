package net.quux00.simplecsv;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A very CSV reader that can take any underlying parser that implements the 
 * CsvParser interface.  Note that CsvReader does NOT implement the Reader interface,
 * but instead must have a Reader supplied when it is constructed.
 */
public class CsvReader implements Closeable, Iterable<List<String>> {

  private BufferedReader br;
  private int recordNumber = 1;

  CsvParser parser;
  int skipLines;

  /**
   * The default line to start reading.
   */
  public static final int DEFAULT_SKIP_LINES = 0;

  /**
   * Constructs CsvReader using a comma for the separator.
   * Defaults to using a SimpleCsvParser.
   *
   * @param reader the reader to an underlying CSV source.
   */
  public CsvReader(Reader reader) {
    this(reader, DEFAULT_SKIP_LINES, new SimpleCsvParser());
  }

  /**
   * Constructs CsvReader with supplied separator and quote char.
   * Defaults to using a SimpleCsvParser.
   *
   * @param reader    the reader to an underlying CSV source.
   * @param line      the line number to skip for start reading
   */
  public CsvReader(Reader reader, int line) {
    this(reader, line, new SimpleCsvParser());
  }
  
  /**
   * Constructs CsvReader with supplied separator and quote char.
   *
   * @param reader     the reader to an underlying CSV source.
   * @param csvParser  the parser to use
   */
  public CsvReader(Reader reader, CsvParser csvParser) {
    this(reader, DEFAULT_SKIP_LINES, csvParser);
  }

  /**
   * Constructs CsvReader with supplied separator and quote char.
   *
   * @param reader    the reader to an underlying CSV source.
   * @param line      the line number to skip for start reading
   * @param csvParser the parser to use to parse input
   */
  public CsvReader(Reader reader, int line, CsvParser csvParser) {
    this.br = (reader instanceof BufferedReader ?
        (BufferedReader) reader : new BufferedReader(reader));
    this.skipLines = line;
    this.parser = csvParser;
  }

  /**
   * Reads the entire file into a List with each element being a 
   * String[] of tokens.
   *
   * @return a List of String[], with each String[] representing 
   *         a line of the file.
   * @throws IOException if bad things happen during the read
   */
  public List<List<String>> readAll() throws IOException {

    List<List<String>> allElements = new ArrayList<List<String>>();
    while (true) {
      List<String> nextLineAsTokens = readNext();
      if (nextLineAsTokens == null) {
        break;
      }
      allElements.add(nextLineAsTokens);      
    }
    return allElements;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate entry.
   * @throws IOException if bad things happen during the read
   */
  public List<String> readNext() throws IOException {
    try {
      while (skipLines > 0) {
        if (parser.parseNext(br) == null) {
          // if we reacher EOF, then consider all lines skipped
          skipLines = 0;
        } else {
          recordNumber++;
          skipLines--;
        }
      }

      List<String> next = parser.parseNext(br);
      if (next != null) {
        recordNumber++;
      }
      return next;
      
    } catch (IllegalArgumentException re) {
      // we append the record number that caused the exception
      IllegalArgumentException nre = new IllegalArgumentException(re.getMessage() + ": " + recordNumber + ".");
      nre.setStackTrace(re.getStackTrace());
      throw nre;
    }
  }


  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  public void close() throws IOException {
    br.close();
  }

  public Iterator<List<String>> iterator() {
    try {
      return new CsvIterator(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

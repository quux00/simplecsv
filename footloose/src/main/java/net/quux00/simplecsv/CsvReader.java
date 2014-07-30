package net.quux00.simplecsv;

/**
 * Copyright 2005 Bytecode Pty Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A very simple CSV reader released under a commercial-friendly license.
 *
 * @author Glen Smith  // FOOTLOOSEVERSION
 */
public class CsvReader implements Closeable, Iterable<String[]> {

  private BufferedReader br;

  private final CsvParser parser;

  private int skipLines;
  private int recordNumber = 1;

  /**
   * The default line to start reading.
   */
  public static final int DEFAULT_SKIP_LINES = 0;

  /**
   * Constructs CsvReader using a comma for the separator.
   *
   * @param reader the reader to an underlying CSV source.
   */
  public CsvReader(Reader reader) {
    this(reader, DEFAULT_SKIP_LINES, new CsvParser());
  }

  /**
   * Constructs CsvReader with supplied separator and quote char.
   *
   * @param reader the reader to an underlying CSV source.
   * @param line the line number to skip for start reading
   */
  public CsvReader(Reader reader, int line) {
    this(reader, line, new CsvParser());
  }

  /**
   * Constructs CsvReader with supplied separator and quote char.
   *
   * @param reader the reader to an underlying CSV source.
   * @param csvParser the parser to use
   */
  public CsvReader(Reader reader, CsvParser csvParser) {
    this(reader, DEFAULT_SKIP_LINES, csvParser);
  }

  /**
   * Constructs CsvReader with supplied separator and quote char.
   *
   * @param reader the reader to an underlying CSV source.
   * @param line the line number to skip for start reading
   * @param csvParser the parser to use to parse input
   */
  public CsvReader(Reader reader, int line, CsvParser csvParser) {
    this.br = reader instanceof BufferedReader
        ? (BufferedReader) reader
            : new BufferedReader(reader);
        this.skipLines = line;
        this.parser = csvParser;
  }

  /**
   * Reads the entire file into a List with each element being a String[] of
   * tokens.
   *
   * @return a List of String[], with each String[] representing a line of the
   * file.
   * @throws IOException if bad things happen during the read
   */
  public List<String[]> readAll() throws IOException {
    List<String[]> allElements = new ArrayList<String[]>();
    String[] nextRecord;
    while ((nextRecord = readNext()) != null) {
      allElements.add(nextRecord);
    }
    return allElements;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a String[] with each comma-separated element as a separate entry.
   * @throws IOException if bad things happen during the read
   * @throws CsvRecordException (also extends IOException) with problematic
   * record number if certain errors can be better described.
   */
  public String[] readNext() throws IOException, CsvRecordException {
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
      if (next == null) {
        return null;
      }
      recordNumber++;
      return next.toArray(new String[next.size()]);
    } catch (CsvRecordException re) {
      // we append the record number that caused the exception
      CsvRecordException nre = new CsvRecordException(re.getMessage() + ": " + recordNumber + ".");
      nre.setStackTrace(re.getStackTrace());
      throw nre;
    }
  }

  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  @Override
  public void close() throws IOException {
    br.close();
  }

  @Override
  public Iterator<String[]> iterator() {
    try {
      return new CsvIterator(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
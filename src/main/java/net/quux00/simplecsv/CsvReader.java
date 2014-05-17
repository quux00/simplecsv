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
public class CsvReader implements Closeable, Iterable<String[]> {

  private BufferedReader br;
  private boolean hasNext = true;
  private boolean linesSkiped;

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
  public List<String[]> readAll() throws IOException {

    List<String[]> allElements = new ArrayList<String[]>();
    while (hasNext) {
      String[] nextLineAsTokens = readNext();
      if (nextLineAsTokens != null) {
        allElements.add(nextLineAsTokens);
      }
    }
    return allElements;
  }

  /**
   * Reads the next line from the buffer and converts to a string array.
   *
   * @return a string array with each comma-separated element as a separate entry.
   * @throws IOException if bad things happen during the read
   */
  public String[] readNext() throws IOException {
    String ln = getNextLine();
    return parser.parse(ln);
  }

  /**
   * Reads the next line from the file.
   *
   * @return the next line from the file without trailing newline
   * @throws IOException if bad things happen during the read
   */
  // TODO: not sure I want all this nonsense about hasNext ...
  private String getNextLine() throws IOException {
    if (!this.linesSkiped) {
      for (int i = 0; i < skipLines; i++) {
        br.readLine();
      }
      this.linesSkiped = true;
    }
    String nextLine = br.readLine();
    if (nextLine == null) {
      hasNext = false;
    }
    return hasNext ? nextLine : null;
  }

  /**
   * Closes the underlying reader.
   *
   * @throws IOException if the close fails
   */
  public void close() throws IOException {
    br.close();
  }

  public Iterator<String[]> iterator() {
    try {
      return new CsvIterator(this);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

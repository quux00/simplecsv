package net.quux00.simplecsv;

import java.io.Reader;

public class CsvReaderBuilder {
  final Reader reader;
  int skipLines = CsvReader.DEFAULT_SKIP_LINES;
  CsvParser parser = null;

  /**
   * Sets the reader to an underlying Csv source
   *
   * @param reader the reader to an underlying Csv source.
   */
  public CsvReaderBuilder(final Reader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("Reader may not be null");
    }
    this.reader = reader;
  }

  /**
   * Sets the line number to skip for start reading
   *
   * @param skipLines the line number to skip for start reading
   */
  public CsvReaderBuilder skipLines(final int skipLines) {
    this.skipLines = (skipLines <= 0 ? 0 : skipLines);
    return this;
  }


  /**
   * Sets the parser to use to parse the input
   *
   * @param csvParser the parser to use to parse the input
   */
  public CsvReaderBuilder csvParser(final CsvParser csvParser) {
    this.parser = csvParser;
    return this;
  }


  /**
   * Constructs CsvReader
   */
  public CsvReader build() {
    if (parser == null) {
      parser = new SimpleCsvParser();
    }
    return new CsvReader(reader, skipLines, parser);
  }
}

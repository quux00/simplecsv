package net.quux00.simplecsv;

import java.io.Writer;

public class CsvWriterBuilder {

  final Writer writer;
  char separator = CsvWriter.DEFAULT_SEPARATOR;
  char quotechar = CsvWriter.DEFAULT_QUOTE_CHARACTER;
  char escapechar = CsvWriter.DEFAULT_ESCAPE_CHARACTER;
  String lineEnd = CsvWriter.DEFAULT_LINE_END;
  
  public CsvWriterBuilder(Writer writer) {
    if (writer == null) {
      throw new IllegalArgumentException("Writer must not be null");
    }
    this.writer = writer;
  }
  
  public CsvWriterBuilder separator(final char separator) {
    this.separator = separator;
    return this;
  }
  
  public CsvWriterBuilder quoteChar(final char quotechar) {
    this.quotechar = quotechar;
    return this;
  }
  
  public CsvWriterBuilder escapeChar(final char escapeChar) {
    this.escapechar = escapeChar;
    return this;
  }
  
  public CsvWriterBuilder lineEnd(final String lineEnd) {
    this.lineEnd = lineEnd;
    return this;
  }
  
  public CsvWriter build() {
    return new CsvWriter(writer, separator, quotechar, escapechar, lineEnd);
  }
}

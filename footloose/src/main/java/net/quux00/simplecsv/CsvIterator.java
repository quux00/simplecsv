package net.quux00.simplecsv;

import java.io.IOException;
import java.util.Iterator;

public class CsvIterator implements Iterator<String[]> {
  private CsvReader reader;
  private String[] nextLine;

  public CsvIterator(CsvReader reader) throws IOException {
    this.reader = reader;
    nextLine = reader.readNext();
  }

  public boolean hasNext() {
    return nextLine != null;
  }

  public String[] next() {
    String[] temp = nextLine;
    try {
      nextLine = reader.readNext();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return temp;
  }

  public void remove() {
    throw new UnsupportedOperationException("This is a read only iterator.");
  }
}
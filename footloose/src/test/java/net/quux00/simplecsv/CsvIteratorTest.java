package net.quux00.simplecsv;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import net.quux00.simplecsv.CsvIterator;
import net.quux00.simplecsv.CsvReader;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: sconway
 * Date: 10/29/11
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CsvIteratorTest {

  private CsvIterator iterator;
  private CsvReader mockReader;
  private static final String[] STRINGS = {"test1", "test2"};

  @Before
  public void setUp() throws IOException {
    mockReader = mock(CsvReader.class);
    when(mockReader.readNext()).thenReturn(STRINGS);
    iterator = new CsvIterator(mockReader);
  }

  @Test(expected = RuntimeException.class)
  public void readerExceptionCausesRunTimeException() throws IOException {
    when(mockReader.readNext()).thenThrow(new IOException("reader threw test exception"));
    iterator.next();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void removethrowsUnsupportedOperationException() {
    iterator.remove();
  }

  @Test
  public void initialReadReturnsStrings() {
    assertArrayEquals(STRINGS, iterator.next());
  }

  @Test
  public void hasNextWorks() throws IOException {
    when(mockReader.readNext()).thenReturn(null);
    assertTrue(iterator.hasNext()); // initial read from constructor
    iterator.next();
    assertFalse(iterator.hasNext());
  }
}

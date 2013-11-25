package net.thornydev.simplecsv.bean;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;

import net.thornydev.simplecsv.CsvReader;

import org.junit.Test;

public class CsvToBeanTest {

  private static final String TEST_STRING = "name,orderNumber,num\n" +
      "kyle,abc123456,123\n" +
      "jimmy,def098765,456 ";

  private CsvReader createReader() {
    StringReader reader = new StringReader(TEST_STRING);
    return new CsvReader(reader);
  }

  @SuppressWarnings("rawtypes")
  private MappingStrategy createErrorMappingStrategy() {
    return new MappingStrategy() {

      public PropertyDescriptor findDescriptor(int col) throws IntrospectionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      public Object createBean() throws InstantiationException, IllegalAccessException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      public void captureHeader(CsvReader reader) throws IOException {
        throw new IOException("This is the test exception");
      }
    };
  }

  @Test(expected = RuntimeException.class)
  public void throwRuntimeExceptionWhenExceptionIsThrown() {
    CsvToBean<Object> bean = new CsvToBean<Object>();
    bean.parse(createErrorMappingStrategy(), createReader());
  }
}

package net.quux00.simplecsv.bean;

import static org.junit.Assert.assertEquals;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.bean.CsvToBean;
import net.quux00.simplecsv.bean.MappingStrategy;

import org.junit.Test;

public class CsvToBeanTest {

  private static final String TEST_STRING = "name,orderNumber,num\n" +
      "kyle,abc123456,123\n" +
      "jimmy,def098765,456 ";

  private CsvReader createMultiLineReader() {
    StringReader reader = new StringReader(TEST_STRING);    
    CsvParser p = new CsvParserBuilder().threadSafe(true).build();    
    return new CsvReader(reader, p);
  }

  private CsvReader createSimpleReader() {
    StringReader reader = new StringReader(TEST_STRING);    
    CsvParser p = new CsvParserBuilder().build();    
    return new CsvReader(reader, p);
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
    bean.parse(createErrorMappingStrategy(), createSimpleReader());
  }

  @Test
  public void testCsvToBeanWithSimpleParser() throws IOException {
    ColumnPositionMappingStrategy<MockBean> strat = new ColumnPositionMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    String[] columns = new String[] {"name", "orderNumber", "id"}; // the fields to bind do in your JavaBean
    strat.setColumnMapping(columns);

    CsvReader rdr = createSimpleReader();
    CsvToBean<MockBean> csv = new CsvToBean<MockBean>();
    List<MockBean> list = csv.parse(strat, rdr);

    assertEquals(3, list.size());
    MockBean bean = list.get(1);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals("123", bean.getId());
  }

  @Test
  public void testCsvToBeanWithMultiLineParser() throws IOException {
    ColumnPositionMappingStrategy<MockBean> strat = new ColumnPositionMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    String[] columns = new String[] {"name", "orderNumber", "id"}; // the fields to bind do in your JavaBean
    strat.setColumnMapping(columns);

    CsvReader rdr = createMultiLineReader();
    CsvToBean<MockBean> csv = new CsvToBean<MockBean>();
    List<MockBean> list = csv.parse(strat, rdr);

    assertEquals(3, list.size());
    MockBean bean = list.get(1);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals("123", bean.getId());
  }
}

package net.quux00.simplecsv.bean;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;
import net.quux00.simplecsv.bean.CsvToBean;
import net.quux00.simplecsv.bean.HeaderColumnNameMappingStrategy;

import org.junit.Test;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class HeaderColumnNameMappingStrategyTest {
  private static final String TEST_STRING = "name,orderNumber,num\n" +
      "kyle,abc123456,123\n" +
      "jimmy,def098765,456";

  private static final String TEST_QUOTED_STRING = "\"name\",\"orderNumber\",\"num\"\n" +
      "\"kyle\",\"abc123456\",\"123\"\n" +
      "\"jimmy\",\"def098765\",\"456\"";


  private List<MockBean> createTestParseResult(String parseString) {
    HeaderColumnNameMappingStrategy<MockBean> strat = new HeaderColumnNameMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    CsvToBean<MockBean> csv = new CsvToBean<MockBean>();
    return csv.parse(strat, new StringReader(parseString));
  }

  private List<MockBean> createTestParseResultWithMultiLineReader(String parseString) {
    HeaderColumnNameMappingStrategy<MockBean> strat = new HeaderColumnNameMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    CsvToBean<MockBean> csv = new CsvToBean<MockBean>();
    CsvParser p = new CsvParserBuilder().multiLine(true).build();
    CsvReader r = new CsvReader(new StringReader(parseString), p);
    return csv.parse(strat, r);
  }

  
  @Test
  public void testParse() {
    List<MockBean> list = createTestParseResult(TEST_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  @Test
  public void testParseWithMultiLineReader() {
    List<MockBean> list = createTestParseResultWithMultiLineReader(TEST_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  
  @Test
  public void testQuotedString() {
    List<MockBean> list = createTestParseResult(TEST_QUOTED_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  @Test
  public void testQuotedStringWithMultiLineReader() {
    List<MockBean> list = createTestParseResultWithMultiLineReader(TEST_QUOTED_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  
  @Test
  public void testParseWithSpacesInHeader() {
    List<MockBean> list = createTestParseResult(TEST_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  @Test
  public void testParseWithSpacesInHeaderWithMultiLineReader() {
    List<MockBean> list = createTestParseResultWithMultiLineReader(TEST_STRING);
    assertNotNull(list);
    assertTrue(list.size() == 2);
    MockBean bean = list.get(0);
    assertEquals("kyle", bean.getName());
    assertEquals("abc123456", bean.getOrderNumber());
    assertEquals(123, bean.getNum());
  }

  @Test
  public void verifyColumnNames() throws IOException, IntrospectionException {
    HeaderColumnNameMappingStrategy<MockBean> strat = new HeaderColumnNameMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    assertNull(strat.getColumnName(0));
    assertNull(strat.findDescriptor(0));

    StringReader reader = new StringReader(TEST_STRING);

    CsvReader csvReader = new CsvReader(reader);
    strat.captureHeader(csvReader);

    assertEquals("name", strat.getColumnName(0));
    assertEquals(strat.findDescriptor(0), strat.findDescriptor("name"));
    assertTrue(strat.matches("name", strat.findDescriptor("name")));
  }

  @Test
  public void verifyColumnNamesUsingMultiLineParser() throws IOException, IntrospectionException {
    HeaderColumnNameMappingStrategy<MockBean> strat = new HeaderColumnNameMappingStrategy<MockBean>();
    strat.setType(MockBean.class);
    assertNull(strat.getColumnName(0));
    assertNull(strat.findDescriptor(0));

    StringReader reader = new StringReader(TEST_STRING);

    CsvParser p = new CsvParserBuilder().multiLine(true).build();
    CsvReader csvReader = new CsvReader(reader, p);
    strat.captureHeader(csvReader);

    assertEquals("name", strat.getColumnName(0));
    assertEquals(strat.findDescriptor(0), strat.findDescriptor("name"));
    assertTrue(strat.matches("name", strat.findDescriptor("name")));
  }

  @Test
  public void verifyColumnNamesSettingTypeInConstructor() throws IOException, IntrospectionException {
    HeaderColumnNameMappingStrategy<MockBean> strat = 
        new HeaderColumnNameMappingStrategy<MockBean>(MockBean.class);
    assertNull(strat.getColumnName(0));
    assertNull(strat.findDescriptor(0));

    StringReader reader = new StringReader(TEST_STRING);

    CsvReader csvReader = new CsvReader(reader);
    strat.captureHeader(csvReader);

    assertEquals("name", strat.getColumnName(0));
    assertEquals(strat.findDescriptor(0), strat.findDescriptor("name"));
    assertTrue(strat.matches("name", strat.findDescriptor("name")));
  }
}

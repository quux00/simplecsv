package net.quux00.simplecsv.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import net.quux00.simplecsv.CsvReader;

import org.junit.Test;

public class OpenCsvIssue2153020 {

  final static String ADDRESS_FILE = "src/test/resources/2153020.csv";

  @Test
  public void testIssue() throws IOException {
    CsvReader reader = new CsvReader(new FileReader(ADDRESS_FILE));
    List<String> nextLine = reader.readNext();
    
    // line 1
    assertNotNull(nextLine);
    assertEquals(19, nextLine.size());
    assertEquals("Presentation Catalog", nextLine.get(0));
    assertEquals("Presentation Table", nextLine.get(1));
    assertEquals("Presentation Column", nextLine.get(2));
    assertEquals("Business Model", nextLine.get(3));
    assertEquals("Derived logical table", nextLine.get(4));
    assertEquals("Derived logical column", nextLine.get(5));
    assertEquals("Expression", nextLine.get(6));
    assertEquals("Logical Table", nextLine.get(7));
    assertEquals("Logical Column", nextLine.get(8));
    assertEquals("Logical Table Source", nextLine.get(9));
    assertEquals("Expression", nextLine.get(10));
    assertEquals("Initialization Block", nextLine.get(11));
    assertEquals("Variable", nextLine.get(12));
    assertEquals("Database", nextLine.get(13));
    assertEquals("Physical Catalog", nextLine.get(14));
    assertEquals("Physical Schema", nextLine.get(15));
    assertEquals("Physical Table", nextLine.get(16));
    assertEquals("Alias", nextLine.get(17));
    assertEquals("Physical Column", nextLine.get(18));

    // line 2
    nextLine = reader.readNext();
    assertNotNull(nextLine);
    assertEquals(19, nextLine.size());
    assertEquals("Accounts Payable Quickstart", nextLine.get(0));
    assertEquals("Accounts Payable Measures", nextLine.get(1));
    assertEquals("Transaction Number", nextLine.get(2));
    assertEquals("NF DWH Quickstart", nextLine.get(3));
    assertEquals("", nextLine.get(4));
    assertEquals("", nextLine.get(5));
    assertEquals("", nextLine.get(6));
    assertEquals("CAP Accounts Payable Overview", nextLine.get(7));
    assertEquals("Transaction Number", nextLine.get(8));
    assertEquals("CAP_ACCNTS_PAYABLE_OVERVIEW", nextLine.get(9));
    assertEquals("QSDB.\"\".QS_DWH.CAP_ACCNTS_PAYABLE_OVERVIEW.TRANSACTION_NUMBER", nextLine.get(10));
    assertEquals("", nextLine.get(11));
    assertEquals("", nextLine.get(12));
    assertEquals("QSDB", nextLine.get(13));
    assertEquals("", nextLine.get(14));
    assertEquals("QS_DWH", nextLine.get(15));
    assertEquals("CAP_ACCNTS_PAYABLE_OVERVIEW", nextLine.get(16));
    assertEquals("", nextLine.get(17));
    assertEquals("TRANSACTION_NUMBER", nextLine.get(18));
    
    reader.close();
  }
}

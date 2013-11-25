package net.thornydev.simplecsv.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;

import net.thornydev.simplecsv.CsvReader;

public class OpenCsvIssue2153020 {

  final static String ADDRESS_FILE = "src/test/resources/2153020.csv";

  @Test
  public void testIssue() throws IOException {
    CsvReader reader = new CsvReader(new FileReader(ADDRESS_FILE));
    String[] nextLine = reader.readNext();
    
    // line 1
    assertNotNull(nextLine);
    assertEquals(19, nextLine.length);
    assertEquals("Presentation Catalog", nextLine[0]);
    assertEquals("Presentation Table", nextLine[1]);
    assertEquals("Presentation Column", nextLine[2]);
    assertEquals("Business Model", nextLine[3]);
    assertEquals("Derived logical table", nextLine[4]);
    assertEquals("Derived logical column", nextLine[5]);
    assertEquals("Expression", nextLine[6]);
    assertEquals("Logical Table", nextLine[7]);
    assertEquals("Logical Column", nextLine[8]);
    assertEquals("Logical Table Source", nextLine[9]);
    assertEquals("Expression", nextLine[10]);
    assertEquals("Initialization Block", nextLine[11]);
    assertEquals("Variable", nextLine[12]);
    assertEquals("Database", nextLine[13]);
    assertEquals("Physical Catalog", nextLine[14]);
    assertEquals("Physical Schema", nextLine[15]);
    assertEquals("Physical Table", nextLine[16]);
    assertEquals("Alias", nextLine[17]);
    assertEquals("Physical Column", nextLine[18]);

    // line 2
    nextLine = reader.readNext();
    assertNotNull(nextLine);
    assertEquals(19, nextLine.length);
    assertEquals("Accounts Payable Quickstart", nextLine[0]);
    assertEquals("Accounts Payable Measures", nextLine[1]);
    assertEquals("Transaction Number", nextLine[2]);
    assertEquals("NF DWH Quickstart", nextLine[3]);
    assertEquals("", nextLine[4]);
    assertEquals("", nextLine[5]);
    assertEquals("", nextLine[6]);
    assertEquals("CAP Accounts Payable Overview", nextLine[7]);
    assertEquals("Transaction Number", nextLine[8]);
    assertEquals("CAP_ACCNTS_PAYABLE_OVERVIEW", nextLine[9]);
    assertEquals("QSDB.\"\".QS_DWH.CAP_ACCNTS_PAYABLE_OVERVIEW.TRANSACTION_NUMBER", nextLine[10]);
    assertEquals("", nextLine[11]);
    assertEquals("", nextLine[12]);
    assertEquals("QSDB", nextLine[13]);
    assertEquals("", nextLine[14]);
    assertEquals("QS_DWH", nextLine[15]);
    assertEquals("CAP_ACCNTS_PAYABLE_OVERVIEW", nextLine[16]);
    assertEquals("", nextLine[17]);
    assertEquals("TRANSACTION_NUMBER", nextLine[18]);
    
    reader.close();
  }
}

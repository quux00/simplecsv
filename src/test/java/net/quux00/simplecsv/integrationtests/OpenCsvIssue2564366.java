package net.quux00.simplecsv.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import net.quux00.simplecsv.CsvReader;

import org.junit.Test;

public class OpenCsvIssue2564366 {

  final static String ADDRESS_FILE = "src/test/resources/2564366.csv";

  @Test
  public void testIssue() throws IOException {
    CsvReader reader = new CsvReader(new FileReader(ADDRESS_FILE));
    List<String> nextLine = reader.readNext();
    
    assertNotNull(nextLine);
    assertEquals(14, nextLine.size());
    assertEquals("CompanyName", nextLine.get(0));
    assertEquals("CompanyNumber", nextLine.get(1));
    assertEquals("ClientName", nextLine.get(2));
    assertEquals("ClientFirstName", nextLine.get(3));
    assertEquals("ClientLastName", nextLine.get(4));
    assertEquals("ClientId", nextLine.get(5));
    assertEquals("ClientGroupId", nextLine.get(6));
    assertEquals("Logon", nextLine.get(7));
    assertEquals("LogonPW", nextLine.get(8));
    assertEquals("PublishKey", nextLine.get(9));
    assertEquals("HiddenKey", nextLine.get(10));
    assertEquals("PublishEncryptMode", nextLine.get(11));
    assertEquals("LanFolderId", nextLine.get(12));
    assertEquals("StaffId", nextLine.get(13));
    
    nextLine = reader.readNext();
    assertNotNull(nextLine);
    assertEquals(14, nextLine.size());
    assertEquals("MLBInc", nextLine.get(0));
    assertEquals("4", nextLine.get(1));
    assertEquals("Art Walk", nextLine.get(2));
    assertEquals("", nextLine.get(3));
    assertEquals("Art Walk", nextLine.get(4));
    assertEquals("", nextLine.get(5));
    assertEquals("'", nextLine.get(6));
    assertEquals("artwalk", nextLine.get(7));
    assertEquals("artwalk", nextLine.get(8));
    assertEquals("art1publishkey", nextLine.get(9));
    assertEquals("art1workkey", nextLine.get(10));
    assertEquals("1", nextLine.get(11));
    assertEquals("012345678", nextLine.get(12));
    assertEquals("", nextLine.get(13));

    reader.close();
  }
}

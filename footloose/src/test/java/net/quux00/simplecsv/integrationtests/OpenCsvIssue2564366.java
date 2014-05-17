package net.quux00.simplecsv.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.io.IOException;

import net.quux00.simplecsv.CsvReader;

import org.junit.Test;

public class OpenCsvIssue2564366 {

  final static String ADDRESS_FILE = "src/test/resources/2564366.csv";

  @Test
  public void testIssue() throws IOException {
    CsvReader reader = new CsvReader(new FileReader(ADDRESS_FILE));
    String[] nextLine = reader.readNext();
    
    assertNotNull(nextLine);
    assertEquals(14, nextLine.length);
    assertEquals("CompanyName", nextLine[0]);
    assertEquals("CompanyNumber", nextLine[1]);
    assertEquals("ClientName", nextLine[2]);
    assertEquals("ClientFirstName", nextLine[3]);
    assertEquals("ClientLastName", nextLine[4]);
    assertEquals("ClientId", nextLine[5]);
    assertEquals("ClientGroupId", nextLine[6]);
    assertEquals("Logon", nextLine[7]);
    assertEquals("LogonPW", nextLine[8]);
    assertEquals("PublishKey", nextLine[9]);
    assertEquals("HiddenKey", nextLine[10]);
    assertEquals("PublishEncryptMode", nextLine[11]);
    assertEquals("LanFolderId", nextLine[12]);
    assertEquals("StaffId", nextLine[13]);
    
    nextLine = reader.readNext();
    assertNotNull(nextLine);
    assertEquals(14, nextLine.length);
    assertEquals("MLBInc", nextLine[0]);
    assertEquals("4", nextLine[1]);
    assertEquals("Art Walk", nextLine[2]);
    assertEquals("", nextLine[3]);
    assertEquals("Art Walk", nextLine[4]);
    assertEquals("", nextLine[5]);
    assertEquals("'", nextLine[6]);
    assertEquals("artwalk", nextLine[7]);
    assertEquals("artwalk", nextLine[8]);
    assertEquals("art1publishkey", nextLine[9]);
    assertEquals("art1workkey", nextLine[10]);
    assertEquals("1", nextLine[11]);
    assertEquals("012345678", nextLine[12]);
    assertEquals("", nextLine[13]);

    reader.close();
  }

}

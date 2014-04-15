
package net.quux00.simplecsv;

import java.io.IOException;

public class CsvRecordException extends IOException {

    public CsvRecordException(String msg) {
        super(msg);
    }

}

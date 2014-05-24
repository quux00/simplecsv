package net.quux00.simplecsv.bench;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.quux00.simplecsv.CsvParser;
import net.quux00.simplecsv.CsvParserBuilder;
import net.quux00.simplecsv.CsvReader;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.logic.BlackHole;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
public class Benchmark {
  static final String longEntry1 = "aaaaaaaaaaaaaaaaaaaaaaaaaa bbbbbbbbbbbbbbbbbbbbbbbbbb cccccccccccccccccccccccccccccccccccccc ddddddddddddddddddddddddddddd efg 123456789012345678901234567890";
  static final String longEntry2 = "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.";
  static final String longLine = String.format("%s,, \"%s\"", longEntry1, longEntry2);
  
  static final String smallLine = "test,\"this,test,is,good\", \"\"\"test\"\"\" ,\"\\\"quote\\\"\"  ";
  
  /* ---[ simple/standard parser ]--- */
  
  @State(Scope.Thread)
  public static class StateSimpleParserStrictQuotesTrimWhitespace {
    CsvParser p = new CsvParserBuilder().
        strictQuotes(true).
        trimWhitespace(true).
        build();
  }

  @State(Scope.Thread)
  public static class StateSimpleDefaultParser {
    CsvParser p = new CsvParserBuilder().build();
  }

  @State(Scope.Thread)
  public static class StateSimpleParserRetainQuotesTrimWhitespace {
    CsvParser p = new CsvParserBuilder().
        trimWhitespace(true).
        retainOuterQuotes(true).
        build();
  }

  
  /* ---[ multi-line parsers ]--- */
  
  @State(Scope.Thread)
  public static class StateMultiLineParserStrictQuotesTrimWhitespace {
    CsvParser p = new CsvParserBuilder().
        multiLine(true).
        strictQuotes(true).
        trimWhitespace(true).
        build();
  }

  @State(Scope.Thread)
  public static class StateMultiLineDefaultParser {
    CsvParser p = new CsvParserBuilder().multiLine(true).build();
  }

  @State(Scope.Thread)
  public static class StateMultiLineParserRetainQuotesTrimWhitespace {
    CsvParser p = new CsvParserBuilder().
        multiLine(true).
        trimWhitespace(true).
        retainOuterQuotes(true).
        build();
  }



  /* ---[ Benchmark 1: small single line ]--- */
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String[] benchStrictQuotesTrimWhitespaceSimple(StateSimpleParserStrictQuotesTrimWhitespace state) throws IOException {
    return benchSmallLine0(state.p);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.NANOSECONDS)  
  public String[] benchStrictQuotesTrimWhitespaceMultiLine(StateMultiLineParserStrictQuotesTrimWhitespace state) throws IOException {
    return benchSmallLine0(state.p);
  }
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public String[] benchRetainQuotesTrimWhitespaceSimple(StateSimpleParserRetainQuotesTrimWhitespace state) throws IOException {
    return benchSmallLine0(state.p);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.NANOSECONDS)  
  public String[] benchRetainQuotesTrimWhitespaceMultiLine(StateMultiLineParserRetainQuotesTrimWhitespace state) throws IOException {
    return benchSmallLine0(state.p);
  }

  private String[] benchSmallLine0(CsvParser p) throws IOException {
    return p.parse(smallLine);
  }

  
  /* ---[ Benchmark 2: single long line string ]--- */
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)  
  public String[] benchLongInMemoryStringSimple(StateSimpleDefaultParser state) {
    return benchLongInMemoryString0(state.p);
  }
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)  
  public String[] benchLongInMemoryStringMultiLine(StateMultiLineDefaultParser state) {
    return benchLongInMemoryString0(state.p);
  }

  public String[] benchLongInMemoryString0(CsvParser p) {
    return p.parse(longLine);
  }
  

  /* ---[ Benchmark 3: small multi line csv file, parseNext ]--- */
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void benchSmallCsvFromFileSimple(StateSimpleDefaultParser state, BlackHole bh) throws IOException {
    benchSmallCsvFromFile0(state.p, bh);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void benchSmallCsvFromFileMultiLine(StateMultiLineDefaultParser state, BlackHole bh) throws IOException {
    benchSmallCsvFromFile0(state.p, bh);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void benchSmallCsvFromFileSimpleStrictQuotesTrim(StateSimpleParserStrictQuotesTrimWhitespace state, BlackHole bh) throws IOException {
    benchSmallCsvFromFile0(state.p, bh);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void benchSmallCsvFromFileMultiLineStrictQuotesTrim(StateMultiLineParserStrictQuotesTrimWhitespace state, BlackHole bh) throws IOException {
    benchSmallCsvFromFile0(state.p, bh);
  }

  private void benchSmallCsvFromFile0(CsvParser p, BlackHole bh) throws IOException {
    // from http://www.wunderground.com/history/airport/KNUQ/2007/1/1/CustomHistory.html?dayend=31&monthend=12&yearend=2007&req_city=NA&req_state=NA&req_statename=NA&format=1
    FileReader fr = new FileReader("testdata/small.csv");
    BufferedReader br = new BufferedReader(fr);
    
    while (true) {
      List<String> toks = p.parseNext(br);
      if (toks == null) {
        break;
      }
      
      bh.consume(toks);
    }
    
    fr.close();
    br.close();
  }
  
  
  /* ---[ Benchmark 4: large multi line csv file, parseNext ]--- */
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileSimple(StateSimpleDefaultParser state, BlackHole bh) throws IOException {
    benchLargeCsvFromFile0(state.p, bh);
  }
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileMultiLine(StateMultiLineDefaultParser state, BlackHole bh) throws IOException {
    benchLargeCsvFromFile0(state.p, bh);
  }

  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileSimpleStrictQuotesTrim(StateSimpleParserStrictQuotesTrimWhitespace state, BlackHole bh) throws IOException {
    benchLargeCsvFromFile0(state.p, bh);
  }
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileMultiLineStrictQuotesTrim(StateMultiLineParserStrictQuotesTrimWhitespace state, BlackHole bh) throws IOException {
    benchLargeCsvFromFile0(state.p, bh);
  }
  
  
  private void benchLargeCsvFromFile0(CsvParser p, BlackHole bh) throws IOException {
    // from http://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time
    FileReader fr = new FileReader("testdata/large.csv");
    BufferedReader br = new BufferedReader(fr);
    
    while (true) {
      List<String> toks = p.parseNext(br);
      if (toks == null) {
        break;
      }
      bh.consume(toks);
    }
    
    fr.close();
    br.close();
  }
  
  
  
  /* ---[ Benchmark 5: large multi line csv file, CsvReader ]--- */  
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileCsvReaderSimple(StateSimpleDefaultParser state, BlackHole bh) throws IOException {
    benchLargeCsvFromFileWithCsvReader0(state.p, bh);
  }
  
  @GenerateMicroBenchmark
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  @Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
  public void benchLargeCsvFromFileCsvReaderXMultiLine(StateMultiLineDefaultParser state, BlackHole bh) throws IOException {
    benchLargeCsvFromFileWithCsvReader0(state.p, bh);
  }
  
  private void benchLargeCsvFromFileWithCsvReader0(CsvParser p, BlackHole bh) throws IOException {
    // from http://www.transtats.bts.gov/DL_SelectFields.asp?Table_ID=236&DB_Short_Name=On-Time
    FileReader fr = new FileReader("testdata/large.csv");
    BufferedReader br = new BufferedReader(fr);
    
    CsvReader csvr = new CsvReader(br, p);
    while (true) {
      String[] toks = csvr.readNext();
      if (toks == null) {
        break;
      }
      bh.consume(toks);
    }

    csvr.close();
  }
}

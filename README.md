# simplecsv

A reboot of the OpenCSV parser for Java.

## Origins and Philosophy

In early 2013, I forked the OpenCSV project [from Sourceforge](http://opencsv.sourceforge.net), since it hadn't been touched in over two years.  Earlier, I had needed to add some functionality, which I posted back to the Sourceforge project's forum.  But that patch along with many others there were left untouched.  There were also a couple of key bugs that had been reported that looked serious enough to me that I didn't want to use the OpenCSV library until they were fixed.

After trying unsuccessfully to fix some of the key bugs in OpenCSV, I concluded that the core of the library -- the CSVParser -- was too complicated a patchwork to salvage.  I decided to rewrite it.  That effort led to forking the project entirely, with the primary intent of simplifying the parser code, but keeping it fast and generally in the spirit of the OpenCSV library.

The philosophy of simplecsv is largely based upon the OpenCSV library behavior but corrects flaws, inconsistencies and behaviors in that library that seem wrong to me.  In particular, I follow the approach of typical programming language compilers (e.g., javac) in how escaped characters are treated.  And yes, sadly, this is likely #15:  http://xkcd.com/927/

I toyed with keeping the name "OpenCSV" or even calling the library "ReOpenCSV", but in the end I believe the behavior is just different enough that that would be misleading.  My goal has been to simplify, so I call this "simplecsv".


* [Similarities and Differences from OpenCSV](#differences)
* [Options to the CsvParser](#options)
* [Examples](#examples)
 * [CsvParser](#csvparser)
 * [CsvReader](#csvreader)
 * [CsvWriter](#csvwriter)
 * [Dump SQL tables to CSV](#tables_to_csv)
 * [Bind CSV to a Java bean](#csv_to_beans)


<a name="differences"></a>
## Similarities and Differences from OpenCSV

All the supporting classes from OpenCSV, such as the CsvWriter, CsvReader, CsvIterator, BeanToCsv, CsvToBean and ResultSetHelper have been copied over more or less intact.

Almost all of the differences are in the parser.

I'll refer you to the [DIFFERENCES.md](https://github.com/quux00/simplecsv/blob/master/DIFFERENCES.md) document for details, but I will point out two big differences:

1. simplecsv does not adhere to [RFC 4180](http://tools.ietf.org/html/rfc4180), which says that two quotes in a row should be viewed as an an escape character and a quote.  OpenCSV did follow this, so that is a big difference.

2. simplecsv does not handle single CSV records spread across multiple lines. One record is assumed to be on each line.  OpenCSV had a "multi-line mode".


<a name="options"></a>
## Options to the CsvParser

With many Java CSV libraries, you have to use a Reader to get anything done.  In simplecsv (and OpenCSV), the CsvParser is a first-class citizen and can be used on its own or with a Reader.

As with OpenCSV, the separator or delimiter, the escape char and the quote char are configurable.

simplecsv also preserves many of the nice configurable options that OpenCSV provided, changes a few to be more consistent or sensible and adds a number of new ones.  Here is a quick breakdown of those options:


<br>
**Default behavior**

By default, fields do not have whitespace trimmed, unbalanced quotes will cause an exception to be thrown, the outer quotes, if present and not escaped, will be removed, and the entire string between the separators will be returned, including whitespace, escape characters and things outside of quotes if quotes are present.

The default separator is comma; the default escape char is backslash; the default quote char is double quote.

In the examples below, the `<<` and `>>` characters are not part of the string - they just indicate its start and end, so whitespace can be "seen" in the input.  Also these are examples are shown as if in a text file - not as they would appear in a Java string.  The outputs are shown with braces to indicate that the output is a `String[]` (if you call `CsvParser#parseLine()`) or a `List<String>` (if you call `CsvParser#parse()`).

    _Input_                                  _Output_
    >>"one", " two " , "3 3",\"four\"<<  =>  [one,  two  , 3 3,\"four\"]


<br><br>
**TrimWhitespace=true**

This changes the default behavior to trim all outer whitespace. Java's `Character.isWhitespace()` method is used to define "whitespace", so it includes CR and LF characters.  Outer quotes are still removed if not escaped.

    CsvParser p = new CsvParserBuilder().
      trimWhitespace(true).
      build();

    _Input_                                  _Output_
    >>"one", " two " , "3 3",\"four\"<<  =>  [one,two,3 3,\"four\"]


<br><br>
**AllowUnbalancedQuotes=true**

This changes the default behavior to accept unbalanced quotes and pass them on to the output, rather than throw an Exception.

If AllowUnbalancedQuotes=false (the default), you will get:

    _Input_          _Output_
    >>one,"""<<  =>  java.lang.IllegalArgumentException: Un-terminated quoted field at end of CSV line

If AllowUnbalancedQuotes=true, you will get:

    CsvParser p = new CsvParserBuilder().
      allowUnbalancedQuotes(true).
      build();

    _Input_          _Output_
    >>one,"""<<  =>  [one,"]


<br>
**RetainEscapeChars=false**

By default, escape chars are retained, like so:

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      build();

    _Input_             _Output_
    >>one,'\'\''<<  =>  [one,\'\']  (The escapes are in the string.)

But with

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      retainEscapeChars(false).
      build();

    _Input_             _Output_
    >>one,'\'\''<<  =>  [one,'']

Here it kept the inner quotes.  The outer quotes are removed as normal and the escape chars are removed.


<br><br>
**RetainOuterQuotes=true**

If you want to retain outer quotes that are present in the input, but *not* add them where they were not present, use this setting.

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      retainOuterQuotes(true).    
      build();


    _Input_                       _Output_
    >>one,'\'\'', 'three' <<  =>  [one,'\'\'', 'three' ]


<br><br>
**AlwaysQuoteOutput=true**

If you want the output to always be quoted, regardless of whether the each input field was originally quoted, use this setting:

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      alwaysQuoteOutput(true).
      build();


    _Input_                       _Output_
    >>one,'\'\'', 'three' <<  =>  ['one','\'\'', 'three' ]


<br>
**StrictQuotes=true**

This setting informs the parser to only keep characters that lie between unescaped quote chars. This sounds promising and can be useful at times, but it is the trickiest setting and it can have unexpected consequences, so make sure this is really what you want.  Look at the CsvParserTest unit test for many examples.  Here's a straightforward example:

    CsvParser p = new CsvParserBuilder().strictQuotes(true).build();


    _Input_                       _Output_
    >>this, "is","a test" xyz<<  =>  [,is,a test]

Since the first field (`this`) was not in quotes, it is gone.  The extra `xyz` at the end of the third field was not between quotes, so it is also left behind.  This setting has the by-product of trimming whitespace around the outsides of the quotes, but if that is all you want use the `trimWhiteSpace=true` option instead.
   
Here's a more complicated example:

    _Input_                               _Output_
    >>"abc"d"efg",1,"2", w"x"y""z <<  =>  [abcefg,,2,x]
 
Like I said, think carefully if this is the setting you really want.  I retained it from the original Open CSVParser in case others have found it beneficial.


Finally, you can combine any of the above options together.  The CsvParserTest unit tests shows a number of variations.  Here are some examples to give you an idea of how they combine:

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      separator('|').
      trimWhitespace(true).
      retainOuterQuotes(true).
      build();

    _Input_                      _Output_
    >> 'a' |' 'hello' '|c<<  =>  ['a',' 'hello' ',c]

    -------------------------------------------------

    CsvParser p = new CsvParserBuilder().
      retainOuterQuotes(true).
      allowUnbalancedQuotes(true).
      build();

    _Input_                  _Output_
    >>1, \"abc\"def\"<<  =>  [1, \"abc\"def\"]


    -------------------------------------------------


    CsvParser p = new CsvParserBuilder().
      strictQuotes(true).
      retainEscapeChars(false).
      alwaysQuoteOutput(true).
      build();
      
    _Input_                           _Output_
    >>abc',!@#",\""   xyz,<<  =>  [,","",]  field1: empty
                                            field2: ",""
                                            field3: empty


In the last example, note that even with `alwaysQuoteOutput(true)` set, empty results are not quoted, as they could be interpreted as "null".  Only non-empty fields get quoted in the output.



<a name="examples"></a>
## Examples

<a name="csvparser"></a>
### CsvParser

    // Construct a default parser
    CsvParser p1 = new CsvParser();

    // Construct a default with non-default options
    // using the CsvParserBuilder
    CsvParser p2 = new CsvParserBuilder().
      separator('|').
      retainOuterQuotes(true).
      allowUnbalancedQuotes(true).
      build();

    String line = getNextLine();
    // get parsed tokens as a String array
    String[] aryToks = p2.parseLine();

    line = getNextLine();
    // get parsed tokens as a List<String>
    List<String> lsToks = p2.parse();

<br>
<a name="csvreader"></a>
### CsvReader

    // create CsvReader with default CsvParser and skips no header lines
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvReader csvr = new CsvReader(fr);
    
    // create custom parser via CsvParserBuilder
    // and custom reader via CsvReaderBuilder, specifying the custom parser
    // to skip 1 header line and passing it the FileReader
    FileReader fr = new FileReader("src/test/resources/basic.csv");
    CsvParser p = new CsvParserBuilder().trimWhitespace(true).retainEscapeChars(false).build();
    CsvReader csvr = new CsvReaderBuilder(fr).skipLines(1).csvParser(p).build();
    
    // now read until all records are exhausted
    String[] toks;
    while ((toks = csvr.readNext()) != null) {
        // toks[] is an array of values from the line
        System.out.println(toks[0] + toks[1] + "etc...");
    }

<br>
<a name="csvwriter"></a>
### CsvWriter

    // create a default CsvWriter, which will write a comma separated file as output
    // For example, by default the output will be double quoted
    FileWriter fw = new FileWriter("yourfile.csv")
    CsvWriter csvw = new CsvWriter(fw);
    
    // With the CsvWriterBuilder you can specify a non-default:
    // separator, quoteChar, escapeChar and lineEnd
    // For example you can specify that you don't want any quotes in the output
    // and want a tab-separated output:

    String[] line = {"Foo", "Bar's", "Baz"};
    StringWriter sw = new StringWriter();
    CsvWriter csvw = new CsvWriterBuilder(sw).
        quoteChar(CsvWriter.NO_QUOTE_CHARACTER).
        separator('\t').
        build();
    csvw.writeNext(line);
    csvw.close();
    String result = sw.toString();

    assertEquals("Foo\tBar's\tBaz\n", result);


<br>
<a name="tables_to_csv"></a>
### Dump SQL tables to CSV

    // Exactly the same as with OpenCSV
    java.sql.ResultSet myResultSet = ....
    writer.writeAll(myResultSet, includeHeaders);


<br>
<a name="csv_to_beans"></a>
### Bind CSV to a Java bean

    // Exactly the same as with OpenCSV
    ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
    strat.setType(YourOrderBean.class);
    String[] columns = new String[] {"name", "orderNumber", "id"}; // the fields to bind do in your JavaBean
    strat.setColumnMapping(columns);
    
    CsvToBean csv = new CsvToBean();
    List list = csv.parse(strat, yourReader);

# simplecsv

A reboot of the OpenCSV parser for Java.

## Origins and Philosophy

In early 2013, I forked the OpenCSV project [from Sourceforge](http://opencsv.sourceforge.net), since it hadn't been touched in over two years.  Earlier, I had needed to add some functionality, which I posted back to the Sourceforge project's forum.  But that patch along with many others there were left untouched.  There were also a couple of key bugs that had been reported that looked serious enough to me that I didn't want to use the OpenCSV library until they were fixed.

After trying unsuccessfully to fix some of the key bugs in OpenCSV, I concluded that the core of the library -- the CSVParser -- was too complicated a patchwork to salvage.  I decided to rewrite it.  That effort led to forking the project entirely, with the primary intent of simplifying the parser code, but keeping it fast and generally in the spirit of the OpenCSV library.

Thanks to [footloosejava](https://github.com/footloosejava), version 2 of simplecsv now has support for reading CSV records from file that have new lines and supports properly parsing RFC4180 quoted quotes in a quoted field (see below for details if that is confusing).

I toyed with keeping the name "OpenCSV" or even calling the library "ReOpenCSV", but in the end I believe the behavior is just different enough that that would be misleading.  My goal has been to simplify, so I call this "simplecsv".

----

### TOC
* [Release Status](#status)
* [Similarities to OpenCSV](#opencsv)
* [Pluggable Parser Model](#pluggable)
 * [MultiLine Parser and RFC4180 compliance](#multiline)
* [Options to the CsvParser](#options)
 * [Details on CsvParser Options](#optionDetails)
* [Examples](#examples)
 * [CsvParser](#csvparser)
 * [CsvReader](#csvreader)
 * [CsvWriter](#csvwriter)
 * [Dump SQL tables to CSV](#tables_to_csv)
 * [Bind CSV to a Java bean](#csv_to_beans)



----

<a name="status"></a>
## Release Status

A 2.0 tag was applied in July 2014 and is now available in maven central: [http://search.maven.org/#search|ga|1|simplecsv](http://search.maven.org/#search|ga|1|simplecsv).  

<br>
<a name="opencsv"></a>
## Similarities to OpenCSV

All the supporting classes from OpenCSV, such as the CsvWriter, CsvReader, CsvIterator, BeanToCsv, CsvToBean and ResultSetHelper were copied over more or less intact.

Almost all of the differences are in the parsers.  As described below, simplecsv has a pluggable parser model and two parsers are current available: a "simple" one and a "multiline" one.

----

<a name="pluggable"></a>
## Pluggable Parser Model

With simplecsv-2.0, `CsvParser` is now an interface with two methods:

    List<String> parse(String s);
    List<String> parseNext(Reader reader) throws IOException;

Thus, simplecsv has a pluggable parser model.  The original CsvParser (from simplecsv-1.x) is now called `SimpleCsvParser` and a new parser `MultiLineCsvParser` has been added (by contributor [footlosejava](https://github.com/footloosejava)).

<br>
<a name="multiline"></a>
### MultiLine Parser and RFC4180 compliance

The `MultiLineCsvParser` parser adds three features not found in the `SimpleCsvParser`:

1. when reading from a file that has a newline in a quoted field, it will parse it as a single csv "record", whereas the `SimpleCsvParser` will treat the newline as the end of the record
2. it can follow [RFC4180](http://tools.ietf.org/html/rfc4180) in allowing quotes to escape quotes in a quoted field. This is described in more detail below if you aren't familiar with this (somewhat peculiar) RFC "standard".
3. it is threadsafe

Based on a series of benchmarks I ran, the `SimpleCsvParser` is typically 20 to 30% faster than the `MultiLineCsvParser`, so `SimpleCsvParser` is used as the default parser.  The `CsvParserBuilder` will return you a `MultiLineCsvParser` only if you ask for any one of the following options:

* `multiLine` (no surprise!)
* `supportRfc4180QuotedQuotes` - allow quotes inside a quoted field if they are doubled (a quoted quote), ala RFC 4180.  See the [Options to the CsvParser](#options) section for more details on this.
* `threadSafe`


<br>
<a name="options"></a>
## Options to the CsvParser

With many Java CSV libraries, you have to use a Reader to get anything done.  In simplecsv (as with OpenCSV), the CsvParser is a first-class citizen and can be used on its own. A CsvReader is really just a convenience wrapper around the CsvParser.

As with OpenCSV, the separator or delimiter, the escape char and the quote char are configurable.

simplecsv also preserves many of the nice configurable options that OpenCSV provided, changes a few to be more consistent or sensible and adds a number of new ones.  Here is a full listing of the options and their defaults:


    |----------------------------+---------|
    | Option                     | Default |
    |----------------------------+---------|
    | separator                  | `,`     |
    | quoteChar                  | `"`     |
    | escapeChar                 | `\`     |
    | trimWhitespace             | false   |
    | allowUnbalancedQuotes      | false   |
    | retainOuterQuotes          | false   |
    | alwaysQuoteOutput          | false   |
    | strictQuotes               | false   |
    | retainEscapeChars          | true    |
    | multiline                  | false   |
    | supportRfc4180QuotedQuotes | false   |
    | threadSafe                 | false   |
    |----------------------------+---------|


<a name="optionDetails"></a>
### Details on CsvParser Options

<br>
**Default behavior**

By default, fields do not have whitespace trimmed, unbalanced quotes will cause an exception to be thrown, the outer quotes, if present and not escaped, will be removed, and the entire string between the separators will be returned, including whitespace, escape characters and things outside of quotes if quotes are present.

The default separator is comma; the default escape char is backslash; the default quote char is double quote.

If any newlines are present, even inside quoted strings, they will be interpreted as the end of the csv record.

In the examples below, the `<<` and `>>` characters are not part of the string - they just indicate its start and end, so whitespace can be "seen" in the input.  Also these are examples are shown as if in a text file - not as they would appear in a Java string.  The outputs are shown with braces to indicate that the output is a `List<String>`  The spaces between words in the output are significant.

    _Input_                                  _Output_
    >>"one","two","3 3",\"four\"<<       =>  [one,two,3 3,\"four\"]
    >>"one", " two " , "3 3",\"four\"<<  =>  [one,  two  , 3 3,\"four\"]


*Important Notes:* 

* the CsvParser is fastest when using the default settings. Changing some settings can lead to a 10 to 30% decrease in overall throughput based on a series of (unpublished) benchmarks I have done.  So use as many of the default settings as you can if you are concerned about overall parsing throughput.
* each of the options described below are described in isolation - if you combine options you may get different results and some option combinations are not allowed.  Disallowed combinations are detected at construction time and an error will be thrown.



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


The key meaning of "allow unbalanced quotes" is that no Exception is thrown if the quotes are balanced when the parser gets to the end of the line/tuple.  The first quote that is seen is still considered the start of a quoted field.

Here's an example (many thanks to [Patricia Goldweic](https://github.com/pgoldweic)):

    CsvParser p = new CsvParserBuilder().
      separator('|').
      allowUnbalancedQuotes(true).
      build();

input:

    blah|this is a long name for this" record|blah2

The first `|` seen is interpreted as a field separator, but the second (between "record" and "blah2") is **not** because it is inside a quoted section.  It turns out that the quoted section doesn't have a close quote, but that is allowed since "allow unbalanced quotes" was set to true.

Thus the expected output will be:

    tok0: blah
    tok1: this is a long name for this" record|blah2


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


<br>
**MultiLine=true**

With this setting you will get the `MultiLineCsvParser`. It can handle newlines that are embedded in quoted strings and treat them as part of the quoted string *when reading from a file*, rather than the end of a csv record.  The key phrase there is "when reading from a file".

For example, the `SimpleCsvParser` and the `MultiLineCsvParser` will both parse this string the same *when passed as a single string*:

    String text = "Small example,\"This is a single string with a\nnew line in one column.\"";
    List<String> toks = p.parse(text);

For both parsers, the output will be:

    _Output_
    [Small example,"This is a single string with a\nnew line in one column."]

But if the input file was this:

    Small example,"This is a single string with a
    new line in one column."

and you use the CsvReader (which reads in one line at a time), now you will see the difference.  When you use the `SimpleCsvParser` with the `CsvReader` you will get an `IllegalArgumentException` stating that you have `Un-terminated quoted field at end of CSV line`.  But if you use the `MultiLineCsvParser` with the CsvReader you will get the output you saw above:

    _Output_
    [Small example,"This is a single string with a\nnew line in one column."]



<br>
**supportRfc4180QuotedQuotes=true**

With RFC4180, quotes are not allowed inside a quoted field unless they are "doubled" (quoted with a quote).

For instance, if you set `supportRfc4180QuotedQuotes=true` the double quote inside a quoted field will be treated as a quote escaping a quote:

    _Input_                       _Output_
    >>"foo ""bar"" quux"<<  =>  [foo,"bar",baz]


If you leave `supportRfc4180QuotedQuotes=false`, then you'll get:

    _Input_                       _Output_
    >>"foo ""bar"" quux"<<  =>  [foo,""bar"",baz]



<br>
**threadSafe=true**

This toggle provides no additional functionality over `multiLine=true`, but it is put it to emphasize that the `MultiLineCsvParser` is thread safe.  It is not yet clear whether this is a useful feature, but it is noted in case using it with parallel Java 8 streams is somehow useful.



<br>
**Combinations**

Finally, you can combine most options together.  Each of the options described below are described in isolation - if you combine other options you may get different results and some option combinations are not allowed.  Disallowed combinations are detected at construction time and an error will be thrown.

The `SimpleCsvParserTest` and `MultiLineCsvParserTest` unit tests shows a number of variations of combining options.  Here are some examples to give you an idea of how they combine:

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

```java
// Construct a default parser
CsvParser p1 = new SimpleCsvParser();
CsvParser p2 = new CsvParserBuilder().build();

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


// Construct a multiline parser that trims whitespace
// and supports doubled quotes in a quoted field
CsvParser p2 = new CsvParserBuilder().
  multiLine(true).
  supportRfc4180QuotedQuotes(true).
  trimWhiteSpace(true).
  build();
```

<br>
<a name="csvreader"></a>
### CsvReader

```java
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


// Use a MultiLine parser with the CsvReader
FileReader fr = new FileReader("src/test/resources/quotednl.csv");
CsvParser p = new CsvParserBuilder().
    multiLine(true).
    supportRfc4180QuotedQuotes(true).
    build();
csvr = new CsvReaderBuilder(fr).csvParser(p).build();
```

<br>
<a name="csvwriter"></a>
### CsvWriter

```java
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
```

<br>
<a name="tables_to_csv"></a>
### Dump SQL tables to CSV

```java
// Exactly the same as with OpenCSV
java.sql.ResultSet myResultSet = ....
writer.writeAll(myResultSet, includeHeaders);
```

<br>
<a name="csv_to_beans"></a>
### Bind CSV to a Java bean

```java
// Exactly the same as with OpenCSV
ColumnPositionMappingStrategy strat = new ColumnPositionMappingStrategy();
strat.setType(YourOrderBean.class);
String[] columns = new String[] {"name", "orderNumber", "id"}; // the fields to bind do in your JavaBean
strat.setColumnMapping(columns);

CsvToBean csv = new CsvToBean();
List list = csv.parse(strat, yourReader);
```


----

### Simplecsv-1.x Documentation

If you are still using simplecsv-1.x, the user guide can be found in the `doc` folder.


----

### LICENSE

Apache License, Version 2.0.  See the LICENSE.md file for copyright notices and license details.

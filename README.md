# simplecsv

A reboot of the OpenCSV parser for Java.

# Origins and Philosophy

In early 2013, I forked the OpenCSV project [on Sourceforge](http://opencsv.sourceforge.net), since it hadn't been touched in over two years.  Earlier, I had needed to add some functionality, which I contributed back to the Sourceforge project, but that patch along with many others there were left untouched.  There were also a couple of key bugs that had been reported that looked serious enough to me that I didn't want to use the OpenCSV library until they were fixed.

After trying unsuccessfully to fix some of the key bugs in OpenCSV, I concluded that the core of the library -- the CSVParser -- was too complicated a patchwork to salvage.  I decided to rewrite it.  That effort led to forking the project entirely, with the primary intent of simplifying the parser code, but keeping it fast and generally in the spirit of the OpenCSV library.

The philosophy of simplecsv is largely based upon the OpenCSV library behavior but corrects flaws, inconsistencies and behaviors in that library that seem wrong to me.  In particular, I follow the approach of typical programming language compilers (e.g., javac) in how escaped characters are treated.  And yes, sadly, this is likely #15:  http://xkcd.com/927/


## Differences from OpenCSV

Almost all of the differences are in the parser.  The other classes were largely copied over from the OpenCSV library as is or with minor changes/bug fixes.

See the DIFFERENCES.md document for details.



## Options to the CsvParser

With many Java CSV libraries, you have to use a Reader to get anything done.  In simplecsv (and OpenCSV), the CsvParser is a first-class citizen and can be used on its own or with a Reader.

As with OpenCSV, the separator or delimiter, the escape char and the quote char are configurable.

simplecsv also preserves many of the nice configurable options that OpenCSV provided, changes a few to be more consistent or sensible and adds a number of new ones.  Here is a quick breakdown of those options:


**Default behavior**

By default, fields do not have whitespace trimmed, unbalanced quotes will cause an exception to be thrown, the outer quotes, if present and not escaped, will be removed, and the entire string between the separators will be returned, including whitespace, escape characters and things outside of quotes if quotes are present.

The default separator is comma; the default escape char is backslash; the default quote char is double quote.

In the example the "<<" and ">>" characters represent the start and end of the string.  Also these are examples are shown as if in a text file - not as they would appear in a Java string.

    _Input_                                  _Output_
    >>"one", " two " , "3 3",\"four\"<<  =>  >>one,  two  , 3 3,\"four\"<<


**TrimWhitespace=true**

Changes the default behavior to trim all outer whitespace, as defined by Java's `Character.isWhitespace()` method (which includes CR and LF characters).  Outer quotes are still removed if not escaped.

    CsvParser p = new CsvParserBuilder().
      trimWhitespace(true).
      build();

    _Input_                                  _Output_
    >>"one", " two " , "3 3",\"four\"<<  =>  >>one,two,3 3,\"four\"<<


* AllowUnbalancedQuotes=true:  changes the default behavior to accept unbalanced quotes as pass them on

If AllowUnbalancedQuotes=false (the default), you will get:

    _Input_          _Output_
    >>one,"""<<  =>  java.lang.IllegalArgumentException: Un-terminated quoted field at end of CSV line

If AllowUnbalancedQuotes=true, you will get:

    CsvParser p = new CsvParserBuilder().
      allowUnbalancedQuotes(true).
      build();

    _Input_          _Output_
    >>one,"""<<  =>  >>one,"<<


* RetainEscapeChars=false: xxx

By default, escape chars are retained, like so:

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      build();

    _Input_             _Output_
    >>one,'\'\''<<  =>  >>one,\'\'<<

But with

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      retainEscapeChars(false).
      build();

    _Input_             _Output_
    >>one,'\'\''<<  =>  >>one,''<<

Here it kept the inner quotes are kept.  The outer quotes are removed as normal and the escape chars are removed.


* RetainOuterQuotes=true:  xxx

If you want to retain outer quotes that are present, but not add them where they were not present, use this setting.

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      retainOuterQuotes(true).    
      build();

    _Input_                       _Output_
    >>one,'\'\'', 'three' <<  =>  >>one,'\'\'', 'three' <<


* AlwaysQuoteOutput=true:  xxx

If you want the output to always be quoted, regardless of whether it was originally quoted, use this setting:

    CsvParser p = new CsvParserBuilder().
      quoteChar('\'').
      alwaysQuoteOutput(true).
      build();

    _Input_                       _Output_
    >>one,'\'\'', 'three' <<  =>  >>'one','\'\'', 'three' <<


* StrictQuotes=true:  xxx

This settings means to only keep things that are inside quote chars. This sounds promising and can be useful at times, but it is the trickiest setting and it can have unexpected consequences, so make sure this is really what you want.  Look at the CsvParserTest unit test for many examples.  Here's a straightforward example:

    CsvParser p = new CsvParserBuilder().strictQuotes(true).build();

    _Input_                       _Output_
    >>this, "is","a test" xyz<<  =>  >>,is,a test<<

Since the first field ("this") was not in quotes, it is gone.  The extra "xyz" at the end of the third field was not in quotes, so it is gone as well.  This setting has the by product of trimming whitespace around the outsides of the quotes, but if that is all you want use the trimWhiteSpace=true option instead.
   
Here's a more complicated example:

    _Input_                               _Output_
    >>"abc"d"efg",1,"2", w"x"y""z <<  =>  >>abcefg,,2,x<<
 
Like I said, think carefully if this is the setting you really want.  I retained it from the original Open CSVParser.




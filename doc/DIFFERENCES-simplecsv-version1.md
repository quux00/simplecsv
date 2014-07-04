### Differences between simplecsv version 1 and OpenCSV-2.3 (and 2.4)

* Default settings in simplecsv do not trim any whitespace
 * In OpenCSV, the default parser of `"this", "is","a test"` returns `>>is<<` for the second token, having removed the leading whitespace.  In simplecsv, the second token is `>> is<<`.  (The `<<` chars are shown to demarcate the start and end of the string.)
 * With simplecsv:
     * use the `withTrimWhitespace(true)` setting
     * or use `withStrictQuotes(true)` setting
   

* simplecsv replaces the `ignoreLeadingWhitespace` option with `trimWhitespace`

* The CsvParser no longer throws `IOException` (which makes no sense for a parser that does no IO)
 * instead, malformed entries (around unbalanced quotes) now throw the unchecked IllegalArgumentException
 * so you can remove all those try/catch blocks catching IOException if you'd like
 * the CsvReader still throws IOExceptions for actual IO issues

* Changed the behavior of whether to retain escapes
 * the OpenCSV library actively removes escape characters, like so:
     * input: `"\"word\""`    output: `"word"`
     * *Note*: these strings are printed as if they are in a text file, so the `\` character is literally in the string
 * a Csv parser is not a regex library, it is simply parsing what comes between separators and giving you that value, so the simplecsv parser behavior is:
     * input: `"\"word\""`    output: `\"word\"` (same except for leading quotes stripped)
 * simplecsv gives you the option of not retaining escape chars if desired; OpenCSV gave no such option


### RFC 4180

#### Using double quote to escape double quote

* OpenCSV reduces multiples quotes in a row to a single quote; simplecsv does not.
  * OpenCSV  : input `"Stan ""The Man"""`    output: `Stan "The Man"`
  * simplecsv : input `"Stan ""The Man"""`    output: `Stan ""The Man""`

The notion of escaping a quote with a quote comes from [RFC 4180](https://tools.ietf.org/html/rfc4180).  I believe that a significant reason that the OpenCSV parser code is so complex is due to following this rule.  The problem is that OpenCSV has to detect, differentiate and deal with three **different kinds** of quotes:

1. opening and closing quotes, which are not considered part of the value and discarded
2. quotes that are RFC 4180 escapes
3. internal (non opening-closing) quotes that are not escapes


#### Additional dictums in RFC 4180

RFC 4180 has a number of other tenets, some of which I find dubious.  I list below how simplecsv approaches these:

Quoting from the [wikipedia entry](https://en.wikipedia.org/wiki/Comma-separated_values), tenets include:

* CSV docs should use DOS-style lines that end with (CR/LF) characters (optional for the last line)
 * `[simplecsv]`: **rejected**: both Unix style and DOS styles lines endings are accepted and treated as whitespace

* An optional header record is allowed (there is no sure way to detect whether it is present)
 * `[simplecsv]`: **accepted** by the CSVReader, irrelevant to the CSVParser.  As with OpenCSV, you specify how many header lines there are to skip

* Each record "should" contain the same number of comma-separated fields.
 * `[simplecsv]`: **rejected**: whatever you give it, it will handle

* A (double) quote character in a field must be represented by two (double) quote characters.
 * `[simplecsv]`: **rejected**: as stated above, this is bizarre.  Simplecsv behaves more like typical programming language compilers when it comes to escaping quote characters.

* Any field may be quoted (with double quotes).
 * `[simplecsv]`: **too limiting**: yes, any field can be quoted by double quotes or any other character that the user designates as the quote char.

* Fields containing a line-break, double-quote, and/or commas should be quoted. (If they are not, the file will likely be impossible to process correctly).
 * `[simplecsv]`: **partially rejected**: First, agree that commas that are part of values should be quoted. Second, one should *escape*, not quote, quotes and embedded line breaks.  True line breaks within a record are not allowed by simplecsv - records are assumed to be on one line.

* Spaces are considered part of a field and should not be ignored.
 * `[simplecsv]`: **accepted, but configurable**: the default behavior of simplecsv is to keep everything except the outer quotes. If you want to trim whitespace from the edges of the token, then set the trimWhiteSpace=true flag.


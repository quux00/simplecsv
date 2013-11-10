* Default settings do not trim any whitespace
** use the withTrimWhitespace() setting
** or use withStrictQuotes() setting

* Got rid of ignoreLeadingWhitespace and added trimWhiteSpace which trims from both sides
** some opencsv tests left trailing whitespace, but trimmed leading whitespace => changed in simplecsv

* No longer throws IOException (which makes no sense for a CSVParser that does no IO)
** malformed entries (around unbalanced quotes) now throw the unchecked IllegalArgumentException
** so you can remove all those try/catch blocks catching IOException if you'd like

* Changed some bizarre behavior to what I consider a more correct behavior
OLD BEHAVIOR:  (parseMultipleQuotes method)
    String[] toks = parser.parseLine("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
    assertEquals("\"\"", toks[0]);   // check the tricky situation
    assertEquals("test\"\n", toks[1]);   // make sure we didn't ruin the next field..
    assertEquals(2, toks.length);

NEW BEHAVIOR
    String[] toks = parser.parseLine("\"\"\"\"\"\",\"test\"\n"); // """""","test"  representing:  "", test
    assertEquals("\"\"\"\"", toks[0]);   // check the tricky situation
    assertEquals("test\n", toks[1]);   // make sure we didn't ruin the next field..
    assertEquals(2, toks.length);

* Removes inconsistent behavior, such as:
    String[] nextItem = csvParser.parseLine("\"this\", \"is\",\"a test\""); //"this", "is","a test"
    second token is "is" in opencvs (for no apparent reason it removes the whitespace before a quote, even if strictquotes and ignoreLeadingwhite space are false)
    second token is " is" in simplecvs unless you turn on trimWhiteSpace flag

* Changed the behavior of escapes
** the opencsv library actively removes explicitly escaped characters, like so:
*** input: "\\\"word\\\""    output: "\"word\""
** but this is not a regex library, it is simply parsing what comes between separators and giving you that value, so the simplecsv parser behavior is:
*** input: "\\\"word\\\""    output: "\\\"word\\\"" (same)

* opencsv reduces multiples quotes in a row to a single quote; simplecsv does not

* simplecsv makes no attempt to comply with [RFC 4180](https://tools.ietf.org/html/rfc4180), which I consider to be limiting and flawed.

According to its [wikipedia entry](https://en.wikipedia.org/wiki/Comma-separated_values), RFC 4180 has the following requirements:
* DOS-style lines that end with (CR/LF) characters (optional for the last line)
** [simplecsv]: **rejected**: whatever line ending characters come in are ignored for parsing
* An optional header record (there is no sure way to detect whether it is present, so care is required when importing).
** [simplecsv]: **accepted** (by the CSVReader, irrelevant to the CSVParser)
* Each record "should" contain the same number of comma-separated fields.
** [simplecsv]: **rejected**: whatever you give it, it will handle
* Any field may be quoted (with double quotes).
** [simplecsv]: **rejected**: any field may be quoted with any quote char it desires and can be specified to the parser
* Fields containing a line-break, double-quote, and/or commas should be quoted. (If they are not, the file will likely be impossible to process correctly).
** [simplecsv]: **mostly rejected**: line-breaks do not need to be "quoted".  The quote char (not just double quotes) can be present if escaped (with a backslash) rather than "quoted"
* A (double) quote character in a field must be represented by two (double) quote characters.
** [simplecsv]: **rejected**: this is bizarre and non-standard in my experience.  Simplecsv behaves more like typical programming language compilers when it comes to escaping characters.
* Spaces are considered part of a field and should not be ignored.
** [simplecsv]: **accepted, but configurable**: the default behavior of simplecsv is to keep everything except the outer quotes. If you want to trim whitespace from the edges of the token, then set the trimWhiteSpace=true flag.

RFC 4180 makes it easy for the CSV library writer, but (in my view) inpractical for a great deal of CSV that exists out there.

* The philosophy of simplecsv is largely based upon the OpenCSV library behavior but corrects flaws, inconsistencies and behaviors in that library that seem wrong to me.  In general I follow the approach of typical programming language compilers (e.g., javac) in how escaped characters are treated.
** <document that here or link to examples later in text>
** and yes, sadly, this is likely #15:  http://xkcd.com/927/

/////////

# TODO: this table is now wrong
_Token_           _Java string repr_      _Default Settings_  _Strict Quotes_   _Retain Outer Quotes_  _Ignore Quotes_  _
"abc"d"efg"       "\"abc\"d\"efg\""       abc"d"efg           Error             "abc"d"efg"            abcdefg
"abc\"d\"efg"     "\"abc\\\"d\\\"efg\""   abc"d"efg           abc"d"efg         "abc\"d\"efg"          abcdefg
"abc"def"         "\"abc\"def\""          Error               Error             Error                  abc"def
"abc\"def"        "\"abc\\\"def\""        abc\"def            abc\"def          "abc\"def"             abc\"def

* opencsv behavior
** ignoreQuotes mode => allow unbalanced quotes
** strict quotes => only retains values inside of quotes; escaped quotes are treated as values, not quotes
** outer quotes always stripped for all modes (until I added retainOuterQuotes)

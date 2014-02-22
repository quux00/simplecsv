package net.quux00.simplecsv;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart of the simplecsv library is the parser.
 * If you want to construct anything except the default parser, it is recommended
 * that you use the CsvParserBuilder.
 * 
 * The parser can be used standalone without the Reader. For example in a Hadoop
 * MapReduce scenario, no reader is needed, just a parser, so almost all of the
 * core logic of the library is in the CsvParser, not other classes.
 * 
 * Options / configurations:
 *   - change the separator/delimiter char
 *   - change the quote char, including specifying no quote char by setting it to NULL_CHARACTER
 *   - change the escape char, including specifying no escape by setting it to NULL_CHARACTER
 *   - turn on strictQuotes mode
 *   - turn on trimWhitespace mode
 *   - turn on allowUnbalancedQuotes mode
 *   - turn off retainEscapeChars mode
 *   - turn on alwaysQuoteOutput mode
 * 
 * @NotThreadSafe - only use one CsvParser per thread
 */
public class CsvParser {
  final char separator;
  final char quotechar;
  final char escapechar;
  final boolean strictQuotes;             // if true, characters outside the quotes are ignored
  final boolean trimWhiteSpace;           // if true, trim leading/trailing white space from tokens
  final boolean allowedUnbalancedQuotes;  // if true, allows unbalanced quotes in a token
  final boolean retainOuterQuotes;        // if true, outer quote chars are retained
  final boolean retainEscapeChars;        // if true, leaves escape chars in; if false removes them
  final boolean alwaysQuoteOutput;        // if true, put quote around around all outgoing tokens
  
  final State state = new State();
  
  public static final char DEFAULT_SEPARATOR = ',';
  public static final char DEFAULT_QUOTE_CHAR = '"';
  public static final char DEFAULT_ESCAPE_CHAR = '\\';
  public static final boolean DEFAULT_STRICT_QUOTES = false;
  public static final boolean DEFAULT_TRIM_WS = false;
  public static final boolean DEFAULT_RETAIN_OUTER_QUOTES = false;
  public static final boolean DEFAULT_ALLOW_UNBALANCED_QUOTES = false;
  public static final boolean DEFAULT_RETAIN_ESCAPE_CHARS = true;
  public static final boolean DEFAULT_ALWAYS_QUOTE_OUTPUT = false;
  
  static final int INITIAL_READ_SIZE = 128;
  
  // This is the "null" character - if a value is set to this then it is ignored.
  static final char NULL_CHARACTER = '\0';
  
  public CsvParser() {
    separator = DEFAULT_SEPARATOR;
    quotechar = DEFAULT_QUOTE_CHAR;
    escapechar = DEFAULT_ESCAPE_CHAR;
    strictQuotes = DEFAULT_STRICT_QUOTES;
    trimWhiteSpace = DEFAULT_TRIM_WS;
    allowedUnbalancedQuotes = DEFAULT_ALLOW_UNBALANCED_QUOTES;
    retainOuterQuotes = DEFAULT_RETAIN_OUTER_QUOTES;
    retainEscapeChars = DEFAULT_RETAIN_ESCAPE_CHARS;
    alwaysQuoteOutput = DEFAULT_ALWAYS_QUOTE_OUTPUT;
  }

  /**
   * Constructor with all options settable.  Unless you want the default behavior,
   * use the Builder to set the options you want.
   * @param separator  single char that separates values in the list
   * @param quotechar  single char that is used to quote values
   * @param escapechar  single char that is used to escape values
   * @param strictQuotes  setting to only accept values if they are between quotechars
   * @param trimWhiteSpace  trims leading and trailing whitespace of each token before it is returned
   * @param allowedUnbalancedQuotes  
   * @param retainOuterQuotes
   */
  public CsvParser(final char separator, final char quotechar, final char escapechar,
      final boolean strictQuotes, final boolean trimWhiteSpace, final boolean allowedUnbalancedQuotes,
      final boolean retainOuterQuotes, final boolean retainEscapeChars, final boolean alwaysQuoteOutput) 
  {
    this.separator = separator;
    this.quotechar = quotechar;
    this.escapechar = escapechar;
    this.strictQuotes = strictQuotes;
    this.trimWhiteSpace = trimWhiteSpace;
    this.allowedUnbalancedQuotes = allowedUnbalancedQuotes;
    this.retainOuterQuotes = retainOuterQuotes;
    this.retainEscapeChars = retainEscapeChars;
    this.alwaysQuoteOutput = alwaysQuoteOutput;
    
    checkInvariants();
  }
  
  private void checkInvariants() {
    if (anyCharactersAreTheSame(separator, quotechar, escapechar)) {
      throw new UnsupportedOperationException("The separator, quote, and escape characters must be different!");
    }
    if (separator == NULL_CHARACTER) {
      throw new UnsupportedOperationException("The separator character must be defined!");
    }
    if (quotechar == NULL_CHARACTER && alwaysQuoteOutput) {
      throw new UnsupportedOperationException("The quote character must be defined to set alwaysQuoteOutput=true!");      
    }
  }
  
  private boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
    return isSameCharacter(separator, quotechar) || isSameCharacter(separator, escape) || isSameCharacter(quotechar, escape);
  }

  private boolean isSameCharacter(char c1, char c2) {
    return c1 != NULL_CHARACTER && c1 == c2;
  }
  
  
  // keep track of mutable States for FSM of parsing
  static class State {
    boolean inQuotes = false;
    boolean inEscape = false;
    
    public void quoteFound() {
      if (!inEscape) {
        inQuotes = !inQuotes;
      }
    }
    
    public void escapeFound(boolean escFound) {
      if (escFound) {
        inEscape = !inEscape;
      } else {
        inEscape = false;
      }
    }

    public void reset() {
      inQuotes = inEscape = false;
    }
  }
  
  /**
   * Parses a single line of text (as defined by the presence of LF or CRLF chars)
   * according to the parser parameters you've set up and returns each parsed token
   * as an List of String.
   * 
   * @param ln Single line of text to parse
   * @return parsed tokens as List<String>
   */
  public List<String> parse(String ln) {
    if (ln == null) { 
      return null; 
    }
    
    StringBuilder sb = new StringBuilder(INITIAL_READ_SIZE);
    List<String> toks = new ArrayList<String>();
    state.reset();
    
    for (int i = 0; i < ln.length(); i++) {
      char c = ln.charAt(i);
      
      if (isQuoteChar(c)) {
        handleQuote(sb);
      
      } else if (isEscapeChar(c)) {
        handleEscape(sb);
      
      } else if (c == separator && !state.inQuotes) {
        toks.add( handleEndOfToken(sb) );
        
      } else {
        handleRegular(sb, c);
      }
    }
    
    // done parsing the line
    if (state.inQuotes && !allowedUnbalancedQuotes) {
      throw new IllegalArgumentException("Un-terminated quoted field at end of CSV line");
    }
    toks.add( handleEndOfToken(sb) );

    return toks;
  }
  
  /**
   * Parses a single line of text (as defined by the presence of LF or CRLF chars)
   * according to the parser parameters you've set up and returns each parsed token
   * as an array of String.  Delegates to the parse method for the actual work.
   * 
   * @param ln Single line of text
   * @return parsed tokens as String[]
   */
  public String[] parseLine(String ln) {
    List<String> toks = parse(ln);
    if (toks == null) {
      return null;
    } else {
      return toks.toArray(new String[toks.size()]);
    }
  }

  
  /* --------------------------------- */
  /* ---[ internal helper methods ]--- */
  /* --------------------------------- */
  
  boolean isEscapeChar(char c) {
    // if the escapechar is set to the NULL_CHAR then it shouldn't
    // match anything => nothing is the escapechar
    return c == escapechar && escapechar != NULL_CHARACTER;
  }
  
  boolean isQuoteChar(char c) {
    // if the quotechar is set to the NULL_CHAR then it shouldn't
    // match anything => nothing is the quotechar
    return c == quotechar && quotechar != NULL_CHARACTER;
  }
  
  String handleEndOfToken(StringBuilder sb) {
    // in strictQuotes mode you don't know when to add the last seen
    // quote until the token is done; if the buffer has any characters
    // then you know a first quote was seen, so add the closing quote
    if (strictQuotes && sb.length() > 0) {
      sb.append(quotechar);
    }
    String tok = trim(sb);
    state.escapeFound(false);
    sb.setLength(0);
    return tok;
  }

  void appendRegularChar(StringBuilder sb, char c) {
    if (state.inEscape && !retainEscapeChars) {
      switch (c) {
        case 'n': 
          sb.append('\n');
          break;
        case 't': 
          sb.append('\t');
          break;
        case 'r': 
          sb.append('\r');
          break;
        case 'b': 
          sb.append('\b');
          break;
        case 'f': 
          sb.append('\f');
          break;
        default:
          sb.append(c);
          break;
      }
    } else {
      sb.append(c);
    }
    state.escapeFound(false);    
  }
  
  void handleRegular(StringBuilder sb, char c) {
    if (strictQuotes) {
      if (state.inQuotes) {
        appendRegularChar(sb, c);
      }
    } else {
      appendRegularChar(sb, c);
    }
  }
  
  void handleEscape(StringBuilder sb) {
    state.escapeFound(true);
    if (retainEscapeChars) {
      if (strictQuotes) {
        if (state.inQuotes) {
          sb.append(escapechar);
        }
      } else {
        sb.append(escapechar);        
      }
    }
  }
  
  void handleQuote(StringBuilder sb) {
    // always retain outer quotes while parsing and then remove them at the end if appropriate
    if (strictQuotes) {
      if (state.inQuotes) {
        if (state.inEscape) {
          sb.append(quotechar);
        }
      } else {
        // if buffer has nothing in it, then no quote has yet been seen
        // so this is the first one, thus add it 
        // (and remove later if don't want to retain outer quotes)
        if (sb.length() == 0) {
          sb.append(quotechar);
        }
      }
      
    } else {
      sb.append(quotechar);
    }
    state.quoteFound();
    state.escapeFound(false);       
  }
  
  String trim(StringBuilder sb) {
    int left = 0;
    int right = sb.length() - 1;
    
    if (alwaysQuoteOutput) {
      if (trimWhiteSpace) {
        int[] indexes = idxTrimSpaces(sb, left, right);
        left = indexes[0];
        right = indexes[1];
      } 
      return ensureQuoted(sb, left, right);
      
    } else { 
      if (!retainOuterQuotes) {
        if (trimWhiteSpace) {
          int[] indexes = idxTrimSpaces(sb, left, right);
          left = indexes[0];
          right = indexes[1];

          indexes = idxTrimEdgeQuotes(sb, left, right);
          left = indexes[0];
          right = indexes[1];

          indexes = idxTrimSpaces(sb, left, right);
          left = indexes[0];
          right = indexes[1];      
        } else {
          pluckOuterQuotes(sb, left, right);
          left = 0;
          right = sb.length() - 1;
        }

      } else if (trimWhiteSpace) {
        int[] indexes = idxTrimSpaces(sb, left, right);
        left = indexes[0];
        right = indexes[1];      
      }
      return sb.substring(left, right+1);
    }
  }

  String ensureQuoted(StringBuilder sb, int left, int right) {
    if (left > right) {
      return "";  // do not quote empty string
    }
    
    int newLeft  = readLeftWhiteSpace(sb, left, right);
    int newRight = readRightWhiteSpace(sb, left, right);
    
    // if there are already edge quotes (ignoring spaces) then just return the 
    // string marked by the left and right indices
    if (sb.charAt(newLeft) == quotechar && sb.charAt(newRight) == quotechar) {
      return sb.substring(left, right + 1);
    
    } else {
      if (right == sb.length() - 1) {
        sb.append(quotechar);
      } else {
        sb.setCharAt(right + 1, quotechar);
      }
      
      if (left == 0) {
        return String.valueOf(quotechar) + sb.substring(left, right + 2);

      } else {
        sb.setCharAt(left - 1, quotechar);
        return sb.substring(left - 1, right + 2);
      }
    }
  }

  /**
   * Only adjusts the left and right index if both the first and last char
   * in the buffer are quotechars.
   * The left and right indexes returned should be used to create a string
   * without edge quotes with: sb.substring(left, right+1)
   * 
   * Note: if the string of empty quotes (not empty string), meaning "\"\""
   * is passed in, it will return left = 1, right = 0, which gives you empty
   * string when you do: sb.substring(left, right+1)
   * @param sb
   * @param left index to look for left quote at
   * @param right index to look for right quote at
   * @return int[2]:  int[0]=> adjusted left index, which will be left or left + 1
   *                  int[1]=> adjusted left index, which will be right or right - 1
   */
  int[] idxTrimEdgeQuotes(StringBuilder sb, int left, int right) {
    if (sb.length() < 2) {
      return new int[]{left, right};
    
    } else if (sb.charAt(left) == quotechar && sb.charAt(right) == quotechar) {
      return new int[]{left+1, right-1};      
    
    } else {
      return new int[]{left, right};
    }
  }
  
  /**
   * Searches the StringBuilder passed in for white space on the left and
   * right sides, starting at the left and right indices provided and returns
   * a tuple (as int[2]) of the revised left and right indices after adjusting
   * for spaces on the "edges" (starting from initial left and right indices).
   * Does NOT mutate any state.
   * 
   * @param sb StringBuilder to parse
   * @param left
   * @param right
   * @return array of int, actual a two-entry tuple. [0] is the left index
   * and [1] is the right index into the StringBuilder after "trimming" for
   * spaces.
   */
  int[] idxTrimSpaces(final StringBuilder sb, int left, int right) {
    if (sb.length() < 2) {
      return new int[]{left, right};
    }
    
    int newLeft  = readLeftWhiteSpace(sb, left, right);
    int newRight = readRightWhiteSpace(sb, left, right);

    if (newLeft > newRight) {
      newLeft = left;
      newRight = right;
    } 

    int[] ary = new int[2];
    ary[0] = newLeft;
    ary[1] = newRight;
    return ary;
  }
  
  
  void pluckOuterQuotes(StringBuilder sb, int left, int right) {
    if (sb.length() < 2) {
      return;
    }

    int newLeft  = readLeftWhiteSpace(sb, left, right);
    int newRight = readRightWhiteSpace(sb, left, right);
    
    if (sb.charAt(newLeft) == quotechar && sb.charAt(newRight) == quotechar) {
      sb.deleteCharAt(newRight);
      sb.deleteCharAt(newLeft);
    }
  }
  
  /**
   * Starting from the left side of the string reads to the first
   * non-white space char (or end of string)
   * For speed reasons, this code assumes your left and right boundary
   * conditions are correct and that the StringBuilder is of size >= 1,
   * so make sure to do checks before calling this method.
   * 
   * @param sb StringBuilder with at least one char (should not be null or size 0)
   * @param left left boundary index of the current StringBuilder
   * @param right right boundary index of the current StringBuilder
   * @return idx one beyond the last white space char
   */
  int readLeftWhiteSpace(StringBuilder sb, int left, int right) {
    for (int i = left; i <= right; i++) {
      if (!Character.isWhitespace(sb.charAt(i))) {
        return i;
      }
    }
    return left;
  }

  /**
   * Starting from the right side of the string reads to the first
   * non-white space char (or start of string)
   * For speed reasons, this code assumes your left and right boundary
   * conditions are correct and that the StringBuilder is of size >= 1,
   * so make sure to do checks before calling this method.
   * @param sb     StringBuilder with at least one char (should not be null or size 0)
   * @param left   left boundary index of the current StringBuilder 
   * @param right  right boundary index of the current StringBuilder 
   * @return idx one before the last white space char (reading from the right)
   */
  int readRightWhiteSpace(StringBuilder sb, int left, int right) {
    for (int i = right; i >= left; i--) {
      if (!Character.isWhitespace(sb.charAt(i))) {
        return i;
      }
    }
    return right;
  }
}

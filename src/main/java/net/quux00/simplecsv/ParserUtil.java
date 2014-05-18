package net.quux00.simplecsv;

public class ParserUtil {

  public static final char DEFAULT_SEPARATOR = ',';
  public static final char DEFAULT_QUOTE_CHAR = '"';
  public static final char DEFAULT_ESCAPE_CHAR = '\\';
  public static final boolean DEFAULT_STRICT_QUOTES = false;
  public static final boolean DEFAULT_TRIM_WS = false;
  public static final boolean DEFAULT_RETAIN_OUTER_QUOTES = false;
  public static final boolean DEFAULT_ALLOW_UNBALANCED_QUOTES = false;
  public static final boolean DEFAULT_RETAIN_ESCAPE_CHARS = true;
  public static final boolean DEFAULT_ALWAYS_QUOTE_OUTPUT = false;
  public static final boolean DEFAULT_ALLOW_DOUBLED_ESCAPED_QUOTES = false; // if true, allows quotes to exist within a quoted field as long as they are doubled.

  // This is the "null" character - if a value is set to this then it is ignored.
  public static final char NULL_CHARACTER = '\0';

  public static void pluckOuterQuotes(StringBuilder sb, int left, int right) {
    pluckOuterQuotes(sb, left, right, '"');
  }
  
  public static void pluckOuterQuotes(StringBuilder sb, int left, int right, char quotechar) {
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
  public static int readLeftWhiteSpace(StringBuilder sb, int left, int right) {
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
  public static int readRightWhiteSpace(StringBuilder sb, int left, int right) {
    for (int i = right; i >= left; i--) {
      if (!Character.isWhitespace(sb.charAt(i))) {
        return i;
      }
    }
    return right;
  }
  
  public static boolean anyCharactersAreTheSame(char separator, char quotechar, char escape) {
    return isSameCharacter(separator, quotechar) || 
        isSameCharacter(separator, escape) || 
        isSameCharacter(quotechar, escape);
  }

  public static boolean isSameCharacter(char c1, char c2) {
    return c1 != ParserUtil.NULL_CHARACTER && c1 == c2;
  }
}
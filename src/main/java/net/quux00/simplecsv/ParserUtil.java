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

  
  /**
   * Only adjusts the left and right index if both the first and last char in
   * the buffer are quotechars. The left and right indexes returned should be
   * used to create a string without edge quotes with: sb.substring(left,
   * right+1)
   *
   * Note: if the string of empty quotes (not empty string), meaning "\"\"" is
   * passed in, it will return left = 1, right = 0, which gives you empty
   * string when you do: sb.substring(left, right+1)
   *
   * @param sb
   * @param left index to look for left quote at
   * @param right index to look for right quote at
   * @return int[2]: int[0]=> adjusted left index, which will be left or left
   * + 1 int[1]=> adjusted left index, which will be right or right - 1
   */
  public static int[] idxTrimEdgeQuotes(StringBuilder sb, int left, int right, char quotechar) {
    if (sb.length() < 2) {
      return new int[]{left, right};

    } else if (sb.charAt(left) == quotechar && sb.charAt(right) == quotechar) {
      return new int[]{left + 1, right - 1};

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
   * @return array of int, actual a two-entry tuple. [0] is the left index and
   * [1] is the right index into the StringBuilder after "trimming" for
   * spaces.
   */
  public static int[] idxTrimSpaces(final StringBuilder sb, int left, int right) {
    if (sb.length() < 2) {
      return new int[]{left, right};
    }

    int newLeft = ParserUtil.readLeftWhiteSpace(sb, left, right);
    int newRight = ParserUtil.readRightWhiteSpace(sb, left, right);

    if (newLeft > newRight) {
      newLeft = left;
      newRight = right;
    }

    int[] ary = new int[2];
    ary[0] = newLeft;
    ary[1] = newRight;
    return ary;
  }


  public static String ensureQuoted(StringBuilder sb, int left, int right, char quotechar) {
    if (left > right) {
      return "";  // do not quote empty string
    }

    int newLeft = ParserUtil.readLeftWhiteSpace(sb, left, right);
    int newRight = ParserUtil.readRightWhiteSpace(sb, left, right);

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
   * Convenience method to use when quotechar is the standard double quote.
   * 
   * Calls pluckOuterQuotes(StringBuilder sb, int left, int right, char quotechar)
   * with the quotechar set to '"'. 
   * @param sb StringBuilder with the String being built
   * @param left
   * @param right
   */
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
/*
 *  Copyright 2001-2014 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.format

import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReferenceArray
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.ReadablePartial

/**
 * Factory that creates instances of DateTimeFormatter from patterns and styles.
 * <p>
 * Datetime formatting is performed by the {@link DateTimeFormatter} class.
 * Three classes provide factory methods to create formatters, and this is one.
 * The others are {@link ISODateTimeFormat} and {@link DateTimeFormatterBuilder}.
 * <p>
 * This class provides two types of factory:
 * <ul>
 * <li>{@link #forPattern(String) Pattern} provides a DateTimeFormatter based on
 * a pattern string that is mostly compatible with the JDK date patterns.
 * <li>{@link #forStyle(String) Style} provides a DateTimeFormatter based on a
 * two character style, representing short, medium, long and full.
 * </ul>
 * <p>
 * For example, to use a patterm:
 * <pre>
 * DateTime dt = new DateTime();
 * DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM, yyyy");
 * String str = fmt.print(dt);
 * </pre>
 *
 * The pattern syntax is mostly compatible with java.text.SimpleDateFormat -
 * time zone names cannot be parsed and a few more symbols are supported.
 * All ASCII letters are reserved as pattern letters, which are defined as follows:
 * <blockquote>
 * <pre>
 * Symbol  Meaning                      Presentation  Examples
 * ------  -------                      ------------  -------
 * G       era                          text          AD
 * C       century of era (&gt;=0)         number        20
 * Y       year of era (&gt;=0)            year          1996
 *
 * x       weekyear                     year          1996
 * w       week of weekyear             number        27
 * e       day of week                  number        2
 * E       day of week                  text          Tuesday; Tue
 *
 * y       year                         year          1996
 * D       day of year                  number        189
 * M       month of year                month         July; Jul; 07
 * d       day of month                 number        10
 *
 * a       halfday of day               text          PM
 * K       hour of halfday (0~11)       number        0
 * h       clockhour of halfday (1~12)  number        12
 *
 * H       hour of day (0~23)           number        0
 * k       clockhour of day (1~24)      number        24
 * m       minute of hour               number        30
 * s       second of minute             number        55
 * S       fraction of second           millis        978
 *
 * z       time zone                    text          Pacific Standard Time; PST
 * Z       time zone offset/id          zone          -0800; -08:00; America/Los_Angeles
 *
 * '       escape for text              delimiter
 * ''      single quote                 literal       '
 * </pre>
 * </blockquote>
 * The count of pattern letters determine the format.
 * <p>
 * <strong>Text</strong>: If the number of pattern letters is 4 or more,
 * the full form is used; otherwise a short or abbreviated form is used if
 * available.
 * <p>
 * <strong>Number</strong>: The minimum number of digits.
 * Shorter numbers are zero-padded to this amount.
 * When parsing, any number of digits are accepted.
 * <p>
 * <strong>Year</strong>: Numeric presentation for year and weekyear fields
 * are handled specially. For example, if the count of 'y' is 2, the year
 * will be displayed as the zero-based year of the century, which is two
 * digits.
 * <p>
 * <strong>Month</strong>: 3 or over, use text, otherwise use number.
 * <p>
 * <strong>Millis</strong>: The exact number of fractional digits.
 * If more millisecond digits are available then specified the number will be truncated,
 * if there are fewer than specified then the number will be zero-padded to the right.
 * When parsing, only the exact number of digits are accepted.
 * <p>
 * <strong>Zone</strong>: 'Z' outputs offset without a colon, 'ZZ' outputs
 * the offset with a colon, 'ZZZ' or more outputs the zone id.
 * <p>
 * <strong>Zone names</strong>: Time zone names ('z') cannot be parsed.
 * <p>
 * Any characters in the pattern that are not in the ranges of ['a'..'z']
 * and ['A'..'Z'] will be treated as quoted text. For instance, characters
 * like ':', '.', ' ', '#' and '?' will appear in the resulting time text
 * even they are not embraced within single quotes.
 * <p>
 * DateTimeFormat is thread-safe and immutable, and the formatters it returns
 * are as well.
 *
 * @author Brian S O'Neill
 * @author Maxim Zhao
 * @since 1.0
 * @see ISODateTimeFormat
 * @see DateTimeFormatterBuilder
 */
object DateTimeFormat {
  /** Style constant for FULL. */
  private[format] val FULL: Int = 0
  /** Style constant for LONG. */
  private[format] val LONG: Int = 1
  /** Style constant for MEDIUM. */
  private[format] val MEDIUM: Int = 2
  /** Style constant for SHORT. */
  private[format] val SHORT: Int = 3
  /** Style constant for NONE. */
  private[format] val NONE: Int = 4
  /** Type constant for DATE only. */
  private[format] val DATE: Int = 0
  /** Type constant for TIME only. */
  private[format] val TIME: Int = 1
  /** Type constant for DATETIME. */
  private[format] val DATETIME: Int = 2
  /** Maximum size of the pattern cache. */
  private val PATTERN_CACHE_SIZE: Int = 500
  /** Maps patterns to formatters, patterns don't vary by locale. Size capped at PATTERN_CACHE_SIZE */
  private val cPatternCache: ConcurrentHashMap[String, DateTimeFormatter] = new ConcurrentHashMap[String, DateTimeFormatter]
  /** Maps patterns to formatters, patterns don't vary by locale. */
  private val cStyleCache: AtomicReferenceArray[DateTimeFormatter] = new AtomicReferenceArray[DateTimeFormatter](25)

  /**
   * Factory to create a formatter from a pattern string.
   * The pattern string is described above in the class level javadoc.
   * It is very similar to SimpleDateFormat patterns.
   * <p>
   * The format may contain locale specific output, and this will change as
   * you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   * For example:
   * <pre>
   * DateTimeFormat.forPattern(pattern).withLocale(Locale.FRANCE).print(dt);
   * </pre>
   *
   * @param pattern  pattern specification
   * @return the formatter
   * @throws IllegalArgumentException if the pattern is invalid
   */
  def forPattern(pattern: String): DateTimeFormatter = {
    return createFormatterForPattern(pattern)
  }

  /**
   * Factory to create a format from a two character style pattern.
   * <p>
   * The first character is the date style, and the second character is the
   * time style. Specify a character of 'S' for short style, 'M' for medium,
   * 'L' for long, and 'F' for full.
   * A date or time may be ommitted by specifying a style character '-'.
   * <p>
   * The returned formatter will dynamically adjust to the locale that
   * the print/parse takes place in. Thus you just call
   * {@link DateTimeFormatter#withLocale(Locale)} and the Short/Medium/Long/Full
   * style for that locale will be output. For example:
   * <pre>
   * DateTimeFormat.forStyle(style).withLocale(Locale.FRANCE).print(dt);
   * </pre>
   *
   * @param style  two characters from the set {"S", "M", "L", "F", "-"}
   * @return the formatter
   * @throws IllegalArgumentException if the style is invalid
   */
  def forStyle(style: String): DateTimeFormatter = {
    return createFormatterForStyle(style)
  }

  /**
   * Returns the pattern used by a particular style and locale.
   * <p>
   * The first character is the date style, and the second character is the
   * time style. Specify a character of 'S' for short style, 'M' for medium,
   * 'L' for long, and 'F' for full.
   * A date or time may be ommitted by specifying a style character '-'.
   *
   * @param style  two characters from the set {"S", "M", "L", "F", "-"}
   * @param locale  locale to use, null means default
   * @return the formatter
   * @throws IllegalArgumentException if the style is invalid
   * @since 1.3
   */
  def patternForStyle(style: String, locale: Locale): String = {
    val formatter: DateTimeFormatter = createFormatterForStyle(style)
    if (locale == null) {
      locale = Locale.getDefault
    }
    return (formatter.getPrinter0.asInstanceOf[DateTimeFormat.StyleFormatter]).getPattern(locale)
  }

  /**
   * Creates a format that outputs a short date format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def shortDate: DateTimeFormatter = {
    return createFormatterForStyleIndex(SHORT, NONE)
  }

  /**
   * Creates a format that outputs a short time format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def shortTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(NONE, SHORT)
  }

  /**
   * Creates a format that outputs a short datetime format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def shortDateTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(SHORT, SHORT)
  }

  /**
   * Creates a format that outputs a medium date format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def mediumDate: DateTimeFormatter = {
    return createFormatterForStyleIndex(MEDIUM, NONE)
  }

  /**
   * Creates a format that outputs a medium time format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def mediumTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(NONE, MEDIUM)
  }

  /**
   * Creates a format that outputs a medium datetime format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def mediumDateTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(MEDIUM, MEDIUM)
  }

  /**
   * Creates a format that outputs a long date format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def longDate: DateTimeFormatter = {
    return createFormatterForStyleIndex(LONG, NONE)
  }

  /**
   * Creates a format that outputs a long time format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def longTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(NONE, LONG)
  }

  /**
   * Creates a format that outputs a long datetime format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def longDateTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(LONG, LONG)
  }

  /**
   * Creates a format that outputs a full date format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def fullDate: DateTimeFormatter = {
    return createFormatterForStyleIndex(FULL, NONE)
  }

  /**
   * Creates a format that outputs a full time format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def fullTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(NONE, FULL)
  }

  /**
   * Creates a format that outputs a full datetime format.
   * <p>
   * The format will change as you change the locale of the formatter.
   * Call {@link DateTimeFormatter#withLocale(Locale)} to switch the locale.
   *
   * @return the formatter
   */
  def fullDateTime: DateTimeFormatter = {
    return createFormatterForStyleIndex(FULL, FULL)
  }

  /**
   * Parses the given pattern and appends the rules to the given
   * DateTimeFormatterBuilder.
   *
   * @param pattern  pattern specification
   * @throws IllegalArgumentException if the pattern is invalid
   */
  private[format] def appendPatternTo(builder: DateTimeFormatterBuilder, pattern: String) {
    parsePatternTo(builder, pattern)
  }

  /**
   * Parses the given pattern and appends the rules to the given
   * DateTimeFormatterBuilder.
   *
   * @param pattern  pattern specification
   * @throws IllegalArgumentException if the pattern is invalid
   * @see #forPattern
   */
  private def parsePatternTo(builder: DateTimeFormatterBuilder, pattern: String) {
    val length: Int = pattern.length
    val indexRef: Array[Int] = new Array[Int](1)
    {
      var i: Int = 0
      while (i < length) {
        {
          indexRef(0) = i
          val token: String = parseToken(pattern, indexRef)
          i = indexRef(0)
          val tokenLen: Int = token.length
          if (tokenLen == 0) {
            break //todo: break is not supported
          }
          val c: Char = token.charAt(0)
          c match {
            case 'G' =>
              builder.appendEraText
              break //todo: break is not supported
            case 'C' =>
              builder.appendCenturyOfEra(tokenLen, tokenLen)
              break //todo: break is not supported
            case 'x' =>
            case 'y' =>
            case 'Y' =>
              if (tokenLen == 2) {
                var lenientParse: Boolean = true
                if (i + 1 < length) {
                  indexRef(0) += 1
                  if (isNumericToken(parseToken(pattern, indexRef))) {
                    lenientParse = false
                  }
                  indexRef(0) -= 1
                }
                c match {
                  case 'x' =>
                    builder.appendTwoDigitWeekyear(new DateTime().getWeekyear - 30, lenientParse)
                    break //todo: break is not supported
                  case 'y' =>
                  case 'Y' =>
                  case _ =>
                    builder.appendTwoDigitYear(new DateTime().getYear - 30, lenientParse)
                    break //todo: break is not supported
                }
              }
              else {
                var maxDigits: Int = 9
                if (i + 1 < length) {
                  indexRef(0) += 1
                  if (isNumericToken(parseToken(pattern, indexRef))) {
                    maxDigits = tokenLen
                  }
                  indexRef(0) -= 1
                }
                c match {
                  case 'x' =>
                    builder.appendWeekyear(tokenLen, maxDigits)
                    break //todo: break is not supported
                  case 'y' =>
                    builder.appendYear(tokenLen, maxDigits)
                    break //todo: break is not supported
                  case 'Y' =>
                    builder.appendYearOfEra(tokenLen, maxDigits)
                    break //todo: break is not supported
                }
              }
              break //todo: break is not supported
            case 'M' =>
              if (tokenLen >= 3) {
                if (tokenLen >= 4) {
                  builder.appendMonthOfYearText
                }
                else {
                  builder.appendMonthOfYearShortText
                }
              }
              else {
                builder.appendMonthOfYear(tokenLen)
              }
              break //todo: break is not supported
            case 'd' =>
              builder.appendDayOfMonth(tokenLen)
              break //todo: break is not supported
            case 'a' =>
              builder.appendHalfdayOfDayText
              break //todo: break is not supported
            case 'h' =>
              builder.appendClockhourOfHalfday(tokenLen)
              break //todo: break is not supported
            case 'H' =>
              builder.appendHourOfDay(tokenLen)
              break //todo: break is not supported
            case 'k' =>
              builder.appendClockhourOfDay(tokenLen)
              break //todo: break is not supported
            case 'K' =>
              builder.appendHourOfHalfday(tokenLen)
              break //todo: break is not supported
            case 'm' =>
              builder.appendMinuteOfHour(tokenLen)
              break //todo: break is not supported
            case 's' =>
              builder.appendSecondOfMinute(tokenLen)
              break //todo: break is not supported
            case 'S' =>
              builder.appendFractionOfSecond(tokenLen, tokenLen)
              break //todo: break is not supported
            case 'e' =>
              builder.appendDayOfWeek(tokenLen)
              break //todo: break is not supported
            case 'E' =>
              if (tokenLen >= 4) {
                builder.appendDayOfWeekText
              }
              else {
                builder.appendDayOfWeekShortText
              }
              break //todo: break is not supported
            case 'D' =>
              builder.appendDayOfYear(tokenLen)
              break //todo: break is not supported
            case 'w' =>
              builder.appendWeekOfWeekyear(tokenLen)
              break //todo: break is not supported
            case 'z' =>
              if (tokenLen >= 4) {
                builder.appendTimeZoneName
              }
              else {
                builder.appendTimeZoneShortName(null)
              }
              break //todo: break is not supported
            case 'Z' =>
              if (tokenLen == 1) {
                builder.appendTimeZoneOffset(null, "Z", false, 2, 2)
              }
              else if (tokenLen == 2) {
                builder.appendTimeZoneOffset(null, "Z", true, 2, 2)
              }
              else {
                builder.appendTimeZoneId
              }
              break //todo: break is not supported
            case '\'' =>
              val sub: String = token.substring(1)
              if (sub.length == 1) {
                builder.appendLiteral(sub.charAt(0))
              }
              else {
                builder.appendLiteral(new String(sub))
              }
              break //todo: break is not supported
            case _ =>
              throw new IllegalArgumentException("Illegal pattern component: " + token)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }

  /**
   * Parses an individual token.
   *
   * @param pattern  the pattern string
   * @param indexRef  a single element array, where the input is the start
   *                  location and the output is the location after parsing the token
   * @return the parsed token
   */
  private def parseToken(pattern: String, indexRef: Array[Int]): String = {
    val buf: StringBuilder = new StringBuilder
    var i: Int = indexRef(0)
    val length: Int = pattern.length
    var c: Char = pattern.charAt(i)
    if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
      buf.append(c)
      while (i + 1 < length) {
        val peek: Char = pattern.charAt(i + 1)
        if (peek == c) {
          buf.append(c)
          i += 1
        }
        else {
          break //todo: break is not supported
        }
      }
    }
    else {
      buf.append('\'')
      var inLiteral: Boolean = false
      while (i < length) {
        {
          c = pattern.charAt(i)
          if (c == '\'') {
            if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
              i += 1
              buf.append(c)
            }
            else {
              inLiteral = !inLiteral
            }
          }
          else if (!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
            i -= 1
            break //todo: break is not supported
          }
          else {
            buf.append(c)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    indexRef(0) = i
    return buf.toString
  }

  /**
   * Returns true if token should be parsed as a numeric field.
   *
   * @param token  the token to parse
   * @return true if numeric field
   */
  private def isNumericToken(token: String): Boolean = {
    val tokenLen: Int = token.length
    if (tokenLen > 0) {
      val c: Char = token.charAt(0)
      c match {
        case 'c' =>
        case 'C' =>
        case 'x' =>
        case 'y' =>
        case 'Y' =>
        case 'd' =>
        case 'h' =>
        case 'H' =>
        case 'm' =>
        case 's' =>
        case 'S' =>
        case 'e' =>
        case 'D' =>
        case 'F' =>
        case 'w' =>
        case 'W' =>
        case 'k' =>
        case 'K' =>
          return true
        case 'M' =>
          if (tokenLen <= 2) {
            return true
          }
      }
    }
    return false
  }

  /**
   * Select a format from a custom pattern.
   *
   * @param pattern  pattern specification
   * @throws IllegalArgumentException if the pattern is invalid
   * @see #appendPatternTo
   */
  private def createFormatterForPattern(pattern: String): DateTimeFormatter = {
    if (pattern == null || pattern.length == 0) {
      throw new IllegalArgumentException("Invalid pattern specification")
    }
    var formatter: DateTimeFormatter = cPatternCache.get(pattern)
    if (formatter == null) {
      val builder: DateTimeFormatterBuilder = new DateTimeFormatterBuilder
      parsePatternTo(builder, pattern)
      formatter = builder.toFormatter
      if (cPatternCache.size < PATTERN_CACHE_SIZE) {
        val oldFormatter: DateTimeFormatter = cPatternCache.putIfAbsent(pattern, formatter)
        if (oldFormatter != null) {
          formatter = oldFormatter
        }
      }
    }
    return formatter
  }

  /**
   * Select a format from a two character style pattern. The first character
   * is the date style, and the second character is the time style. Specify a
   * character of 'S' for short style, 'M' for medium, 'L' for long, and 'F'
   * for full. A date or time may be ommitted by specifying a style character '-'.
   *
   * @param style  two characters from the set {"S", "M", "L", "F", "-"}
   * @throws IllegalArgumentException if the style is invalid
   */
  private def createFormatterForStyle(style: String): DateTimeFormatter = {
    if (style == null || style.length != 2) {
      throw new IllegalArgumentException("Invalid style specification: " + style)
    }
    val dateStyle: Int = selectStyle(style.charAt(0))
    val timeStyle: Int = selectStyle(style.charAt(1))
    if (dateStyle == NONE && timeStyle == NONE) {
      throw new IllegalArgumentException("Style '--' is invalid")
    }
    return createFormatterForStyleIndex(dateStyle, timeStyle)
  }

  /**
   * Gets the formatter for the specified style.
   *
   * @param dateStyle  the date style
   * @param timeStyle  the time style
   * @return the formatter
   */
  private def createFormatterForStyleIndex(dateStyle: Int, timeStyle: Int): DateTimeFormatter = {
    val index: Int = ((dateStyle << 2) + dateStyle) + timeStyle
    if (index >= cStyleCache.length) {
      return createDateTimeFormatter(dateStyle, timeStyle)
    }
    var f: DateTimeFormatter = cStyleCache.get(index)
    if (f == null) {
      f = createDateTimeFormatter(dateStyle, timeStyle)
      if (cStyleCache.compareAndSet(index, null, f) == false) {
        f = cStyleCache.get(index)
      }
    }
    return f
  }

  /**
   * Creates a formatter for the specified style.
   *
   * @param dateStyle  the date style
   * @param timeStyle  the time style
   * @return the formatter
   */
  private def createDateTimeFormatter(dateStyle: Int, timeStyle: Int): DateTimeFormatter = {
    var `type`: Int = DATETIME
    if (dateStyle == NONE) {
      `type` = TIME
    }
    else if (timeStyle == NONE) {
      `type` = DATE
    }
    val llf: DateTimeFormat.StyleFormatter = new DateTimeFormat.StyleFormatter(dateStyle, timeStyle, `type`)
    return new DateTimeFormatter(llf, llf)
  }

  /**
   * Gets the JDK style code from the Joda code.
   *
   * @param ch  the Joda style code
   * @return the JDK style code
   */
  private def selectStyle(ch: Char): Int = {
    ch match {
      case 'S' =>
        return SHORT
      case 'M' =>
        return MEDIUM
      case 'L' =>
        return LONG
      case 'F' =>
        return FULL
      case '-' =>
        return NONE
      case _ =>
        throw new IllegalArgumentException("Invalid style character: " + ch)
    }
  }

  private[format] object StyleFormatter {
    private val cCache: ConcurrentHashMap[DateTimeFormat.StyleFormatterCacheKey, DateTimeFormatter] = new ConcurrentHashMap[DateTimeFormat.StyleFormatterCacheKey, DateTimeFormatter]
  }

  private[format] class StyleFormatter extends InternalPrinter with InternalParser {
    private final val iDateStyle: Int = 0
    private final val iTimeStyle: Int = 0
    private final val iType: Int = 0

    private[format] def this(dateStyle: Int, timeStyle: Int, `type`: Int) {
      this()
      `super`
      iDateStyle = dateStyle
      iTimeStyle = timeStyle
      iType = `type`
    }

    def estimatePrintedLength: Int = {
      return 40
    }

    @throws(classOf[IOException])
    def printTo(appenadble: Appendable, instant: Long, chrono: Chronology, displayOffset: Int, displayZone: DateTimeZone, locale: Locale) {
      val p: InternalPrinter = getFormatter(locale).getPrinter0
      p.printTo(appenadble, instant, chrono, displayOffset, displayZone, locale)
    }

    @throws(classOf[IOException])
    def printTo(appendable: Appendable, partial: ReadablePartial, locale: Locale) {
      val p: InternalPrinter = getFormatter(locale).getPrinter0
      p.printTo(appendable, partial, locale)
    }

    def estimateParsedLength: Int = {
      return 40
    }

    def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
      val p: InternalParser = getFormatter(bucket.getLocale).getParser0
      return p.parseInto(bucket, text, position)
    }

    private def getFormatter(locale: Locale): DateTimeFormatter = {
      locale = (if (locale == null) Locale.getDefault else locale)
      val key: DateTimeFormat.StyleFormatterCacheKey = new DateTimeFormat.StyleFormatterCacheKey(iType, iDateStyle, iTimeStyle, locale)
      var f: DateTimeFormatter = StyleFormatter.cCache.get(key)
      if (f == null) {
        f = DateTimeFormat.forPattern(getPattern(locale))
        val oldFormatter: DateTimeFormatter = StyleFormatter.cCache.putIfAbsent(key, f)
        if (oldFormatter != null) {
          f = oldFormatter
        }
      }
      return f
    }

    private[format] def getPattern(locale: Locale): String = {
      var f: DateFormat = null
      iType match {
        case DATE =>
          f = DateFormat.getDateInstance(iDateStyle, locale)
          break //todo: break is not supported
        case TIME =>
          f = DateFormat.getTimeInstance(iTimeStyle, locale)
          break //todo: break is not supported
        case DATETIME =>
          f = DateFormat.getDateTimeInstance(iDateStyle, iTimeStyle, locale)
          break //todo: break is not supported
      }
      if (f.isInstanceOf[SimpleDateFormat] == false) {
        throw new IllegalArgumentException("No datetime pattern for locale: " + locale)
      }
      return (f.asInstanceOf[SimpleDateFormat]).toPattern
    }
  }

  private[format] class StyleFormatterCacheKey {
    private final val combinedTypeAndStyle: Int = 0
    private final val locale: Locale = null

    def this(iType: Int, iDateStyle: Int, iTimeStyle: Int, locale: Locale) {
      this()
      this.locale = locale
      this.combinedTypeAndStyle = iType + (iDateStyle << 4) + (iTimeStyle << 8)
    }

    override def hashCode: Int = {
      val prime: Int = 31
      var result: Int = 1
      result = prime * result + combinedTypeAndStyle
      result = prime * result + (if ((locale == null)) 0 else locale.hashCode)
      return result
    }

    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj == null) {
        return false
      }
      if (!(obj.isInstanceOf[DateTimeFormat.StyleFormatterCacheKey])) {
        return false
      }
      val other: DateTimeFormat.StyleFormatterCacheKey = obj.asInstanceOf[DateTimeFormat.StyleFormatterCacheKey]
      if (combinedTypeAndStyle != other.combinedTypeAndStyle) {
        return false
      }
      if (locale == null) {
        if (other.locale != null) {
          return false
        }
      }
      else if (!(locale == other.locale)) {
        return false
      }
      return true
    }
  }

}

class DateTimeFormat {
  /**
   * Constructor.
   *
   * @since 1.1 (previously private)
   */
  protected def this() {
    this()
    `super`
  }
}
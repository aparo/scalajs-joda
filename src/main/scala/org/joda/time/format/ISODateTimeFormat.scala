/*
 *  Copyright 2001-2009 Stephen Colebourne
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

import java.util.Collection
import java.util.HashSet
import java.util.Set
import org.joda.time.DateTimeFieldType

/**
 * Factory that creates instances of DateTimeFormatter based on the ISO8601 standard.
 * <p>
 * Date-time formatting is performed by the {@link DateTimeFormatter} class.
 * Three classes provide factory methods to create formatters, and this is one.
 * The others are {@link DateTimeFormat} and {@link DateTimeFormatterBuilder}.
 * <p>
 * ISO8601 is the international standard for data interchange. It defines a
 * framework, rather than an absolute standard. As a result this provider has a
 * number of methods that represent common uses of the framework. The most common
 * formats are {@link #date() date}, {@link #time() time}, and {@link #dateTime() dateTime}.
 * <p>
 * For example, to format a date time in ISO format:
 * <pre>
 * DateTime dt = new DateTime();
 * DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
 * String str = fmt.print(dt);
 * </pre>
 * <p>
 * Note that these formatters mostly follow the ISO8601 standard for printing.
 * For parsing, the formatters are more lenient and allow formats that are not
 * in strict compliance with the standard.
 * <p>
 * It is important to understand that these formatters are not linked to
 * the <code>ISOChronology</code>. These formatters may be used with any
 * chronology, however there may be certain side effects with more unusual
 * chronologies. For example, the ISO formatters rely on dayOfWeek being
 * single digit, dayOfMonth being two digit and dayOfYear being three digit.
 * A chronology with a ten day week would thus cause issues. However, in
 * general, it is safe to use these formatters with other chronologies.
 * <p>
 * ISODateTimeFormat is thread-safe and immutable, and the formatters it
 * returns are as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see DateTimeFormat
 * @see DateTimeFormatterBuilder
 */
object ISODateTimeFormat {
  /**
   * Returns a formatter that outputs only those fields specified.
   * <p>
   * This method examines the fields provided and returns an ISO-style
   * formatter that best fits. This can be useful for outputting
   * less-common ISO styles, such as YearMonth (YYYY-MM) or MonthDay (--MM-DD).
   * <p>
   * The list provided may have overlapping fields, such as dayOfWeek and
   * dayOfMonth. In this case, the style is chosen based on the following
   * list, thus in the example, the calendar style is chosen as dayOfMonth
   * is higher in priority than dayOfWeek:
   * <ul>
   * <li>monthOfYear - calendar date style
   * <li>dayOfYear - ordinal date style
   * <li>weekOfWeekYear - week date style
   * <li>dayOfMonth - calendar date style
   * <li>dayOfWeek - week date style
   * <li>year
   * <li>weekyear
   * </ul>
   * The supported formats are:
   * <pre>
   * Extended      Basic       Fields
   * 2005-03-25    20050325    year/monthOfYear/dayOfMonth
   * 2005-03       2005-03     year/monthOfYear
   * 2005--25      2005--25    year/dayOfMonth *
   * 2005          2005        year
   * --03-25       --0325      monthOfYear/dayOfMonth
   * --03          --03        monthOfYear
   * ---03         ---03       dayOfMonth
   * 2005-084      2005084     year/dayOfYear
   * -084          -084        dayOfYear
   * 2005-W12-5    2005W125    weekyear/weekOfWeekyear/dayOfWeek
   * 2005-W-5      2005W-5     weekyear/dayOfWeek *
   * 2005-W12      2005W12     weekyear/weekOfWeekyear
   * -W12-5        -W125       weekOfWeekyear/dayOfWeek
   * -W12          -W12        weekOfWeekyear
   * -W-5          -W-5        dayOfWeek
   * 10:20:30.040  102030.040  hour/minute/second/milli
   * 10:20:30      102030      hour/minute/second
   * 10:20         1020        hour/minute
   * 10            10          hour
   * -20:30.040    -2030.040   minute/second/milli
   * -20:30        -2030       minute/second
   * -20           -20         minute
   * --30.040      --30.040    second/milli
   * --30          --30        second
   * ---.040       ---.040     milli *
   * 10-30.040     10-30.040   hour/second/milli *
   * 10:20-.040    1020-.040   hour/minute/milli *
   * 10-30         10-30       hour/second *
   * 10--.040      10--.040    hour/milli *
   * -20-.040      -20-.040    minute/milli *
   * plus datetime formats like {date}T{time}
   * </pre>
   * * indiates that this is not an official ISO format and can be excluded
   * by passing in <code>strictISO</code> as <code>true</code>.
   * <p>
   * This method can side effect the input collection of fields.
   * If the input collection is modifiable, then each field that was added to
   * the formatter will be removed from the collection, including any duplicates.
   * If the input collection is unmodifiable then no side effect occurs.
   * <p>
   * This side effect processing is useful if you need to know whether all
   * the fields were converted into the formatter or not. To achieve this,
   * pass in a modifiable list, and check that it is empty on exit.
   *
   * @param fields  the fields to get a formatter for, not null,
   *                updated by the method call unless unmodifiable,
   *                removing those fields built in the formatter
   * @param extended  true to use the extended format (with separators)
   * @param strictISO  true to stick exactly to ISO8601, false to include additional formats
   * @return a suitable formatter
   * @throws IllegalArgumentException if there is no format for the fields
   * @since 1.1
   */
  def forFields(fields: Collection[DateTimeFieldType], extended: Boolean, strictISO: Boolean): DateTimeFormatter = {
    if (fields == null || fields.size == 0) {
      throw new IllegalArgumentException("The fields must not be null or empty")
    }
    val workingFields: Set[DateTimeFieldType] = new HashSet[DateTimeFieldType](fields)
    val inputSize: Int = workingFields.size
    var reducedPrec: Boolean = false
    val bld: DateTimeFormatterBuilder = new DateTimeFormatterBuilder
    if (workingFields.contains(DateTimeFieldType.monthOfYear)) {
      reducedPrec = dateByMonth(bld, workingFields, extended, strictISO)
    }
    else if (workingFields.contains(DateTimeFieldType.dayOfYear)) {
      reducedPrec = dateByOrdinal(bld, workingFields, extended, strictISO)
    }
    else if (workingFields.contains(DateTimeFieldType.weekOfWeekyear)) {
      reducedPrec = dateByWeek(bld, workingFields, extended, strictISO)
    }
    else if (workingFields.contains(DateTimeFieldType.dayOfMonth)) {
      reducedPrec = dateByMonth(bld, workingFields, extended, strictISO)
    }
    else if (workingFields.contains(DateTimeFieldType.dayOfWeek)) {
      reducedPrec = dateByWeek(bld, workingFields, extended, strictISO)
    }
    else if (workingFields.remove(DateTimeFieldType.year)) {
      bld.append(Constants.ye)
      reducedPrec = true
    }
    else if (workingFields.remove(DateTimeFieldType.weekyear)) {
      bld.append(Constants.we)
      reducedPrec = true
    }
    val datePresent: Boolean = (workingFields.size < inputSize)
    time(bld, workingFields, extended, strictISO, reducedPrec, datePresent)
    if (bld.canBuildFormatter == false) {
      throw new IllegalArgumentException("No valid format for fields: " + fields)
    }
    try {
      fields.retainAll(workingFields)
    }
    catch {
      case ex: UnsupportedOperationException => {
      }
    }
    return bld.toFormatter
  }

  /**
   * Creates a date using the calendar date format.
   * Specification reference: 5.2.1.
   *
   * @param bld  the builder
   * @param fields  the fields
   * @param extended  true to use extended format
   * @param strictISO  true to only allow ISO formats
   * @return true if reduced precision
   * @since 1.1
   */
  private def dateByMonth(bld: DateTimeFormatterBuilder, fields: Collection[DateTimeFieldType], extended: Boolean, strictISO: Boolean): Boolean = {
    var reducedPrec: Boolean = false
    if (fields.remove(DateTimeFieldType.year)) {
      bld.append(Constants.ye)
      if (fields.remove(DateTimeFieldType.monthOfYear)) {
        if (fields.remove(DateTimeFieldType.dayOfMonth)) {
          appendSeparator(bld, extended)
          bld.appendMonthOfYear(2)
          appendSeparator(bld, extended)
          bld.appendDayOfMonth(2)
        }
        else {
          bld.appendLiteral('-')
          bld.appendMonthOfYear(2)
          reducedPrec = true
        }
      }
      else {
        if (fields.remove(DateTimeFieldType.dayOfMonth)) {
          checkNotStrictISO(fields, strictISO)
          bld.appendLiteral('-')
          bld.appendLiteral('-')
          bld.appendDayOfMonth(2)
        }
        else {
          reducedPrec = true
        }
      }
    }
    else if (fields.remove(DateTimeFieldType.monthOfYear)) {
      bld.appendLiteral('-')
      bld.appendLiteral('-')
      bld.appendMonthOfYear(2)
      if (fields.remove(DateTimeFieldType.dayOfMonth)) {
        appendSeparator(bld, extended)
        bld.appendDayOfMonth(2)
      }
      else {
        reducedPrec = true
      }
    }
    else if (fields.remove(DateTimeFieldType.dayOfMonth)) {
      bld.appendLiteral('-')
      bld.appendLiteral('-')
      bld.appendLiteral('-')
      bld.appendDayOfMonth(2)
    }
    return reducedPrec
  }

  /**
   * Creates a date using the ordinal date format.
   * Specification reference: 5.2.2.
   *
   * @param bld  the builder
   * @param fields  the fields
   * @param extended  true to use extended format
   * @param strictISO  true to only allow ISO formats
   * @since 1.1
   */
  private def dateByOrdinal(bld: DateTimeFormatterBuilder, fields: Collection[DateTimeFieldType], extended: Boolean, strictISO: Boolean): Boolean = {
    var reducedPrec: Boolean = false
    if (fields.remove(DateTimeFieldType.year)) {
      bld.append(Constants.ye)
      if (fields.remove(DateTimeFieldType.dayOfYear)) {
        appendSeparator(bld, extended)
        bld.appendDayOfYear(3)
      }
      else {
        reducedPrec = true
      }
    }
    else if (fields.remove(DateTimeFieldType.dayOfYear)) {
      bld.appendLiteral('-')
      bld.appendDayOfYear(3)
    }
    return reducedPrec
  }

  /**
   * Creates a date using the calendar date format.
   * Specification reference: 5.2.3.
   *
   * @param bld  the builder
   * @param fields  the fields
   * @param extended  true to use extended format
   * @param strictISO  true to only allow ISO formats
   * @since 1.1
   */
  private def dateByWeek(bld: DateTimeFormatterBuilder, fields: Collection[DateTimeFieldType], extended: Boolean, strictISO: Boolean): Boolean = {
    var reducedPrec: Boolean = false
    if (fields.remove(DateTimeFieldType.weekyear)) {
      bld.append(Constants.we)
      if (fields.remove(DateTimeFieldType.weekOfWeekyear)) {
        appendSeparator(bld, extended)
        bld.appendLiteral('W')
        bld.appendWeekOfWeekyear(2)
        if (fields.remove(DateTimeFieldType.dayOfWeek)) {
          appendSeparator(bld, extended)
          bld.appendDayOfWeek(1)
        }
        else {
          reducedPrec = true
        }
      }
      else {
        if (fields.remove(DateTimeFieldType.dayOfWeek)) {
          checkNotStrictISO(fields, strictISO)
          appendSeparator(bld, extended)
          bld.appendLiteral('W')
          bld.appendLiteral('-')
          bld.appendDayOfWeek(1)
        }
        else {
          reducedPrec = true
        }
      }
    }
    else if (fields.remove(DateTimeFieldType.weekOfWeekyear)) {
      bld.appendLiteral('-')
      bld.appendLiteral('W')
      bld.appendWeekOfWeekyear(2)
      if (fields.remove(DateTimeFieldType.dayOfWeek)) {
        appendSeparator(bld, extended)
        bld.appendDayOfWeek(1)
      }
      else {
        reducedPrec = true
      }
    }
    else if (fields.remove(DateTimeFieldType.dayOfWeek)) {
      bld.appendLiteral('-')
      bld.appendLiteral('W')
      bld.appendLiteral('-')
      bld.appendDayOfWeek(1)
    }
    return reducedPrec
  }

  /**
   * Adds the time fields to the builder.
   * Specification reference: 5.3.1.
   *
   * @param bld  the builder
   * @param fields  the fields
   * @param extended  whether to use the extended format
   * @param strictISO  whether to be strict
   * @param reducedPrec  whether the date was reduced precision
   * @param datePresent  whether there was a date
   * @since 1.1
   */
  private def time(bld: DateTimeFormatterBuilder, fields: Collection[DateTimeFieldType], extended: Boolean, strictISO: Boolean, reducedPrec: Boolean, datePresent: Boolean) {
    val hour: Boolean = fields.remove(DateTimeFieldType.hourOfDay)
    val minute: Boolean = fields.remove(DateTimeFieldType.minuteOfHour)
    val second: Boolean = fields.remove(DateTimeFieldType.secondOfMinute)
    val milli: Boolean = fields.remove(DateTimeFieldType.millisOfSecond)
    if (!hour && !minute && !second && !milli) {
      return
    }
    if (hour || minute || second || milli) {
      if (strictISO && reducedPrec) {
        throw new IllegalArgumentException("No valid ISO8601 format for fields because Date was reduced precision: " + fields)
      }
      if (datePresent) {
        bld.appendLiteral('T')
      }
    }
    if (hour && minute && second || (hour && !second && !milli)) {
    }
    else {
      if (strictISO && datePresent) {
        throw new IllegalArgumentException("No valid ISO8601 format for fields because Time was truncated: " + fields)
      }
      if (!hour && (minute && second || (minute && !milli) || second)) {
      }
      else {
        if (strictISO) {
          throw new IllegalArgumentException("No valid ISO8601 format for fields: " + fields)
        }
      }
    }
    if (hour) {
      bld.appendHourOfDay(2)
    }
    else if (minute || second || milli) {
      bld.appendLiteral('-')
    }
    if (extended && hour && minute) {
      bld.appendLiteral(':')
    }
    if (minute) {
      bld.appendMinuteOfHour(2)
    }
    else if (second || milli) {
      bld.appendLiteral('-')
    }
    if (extended && minute && second) {
      bld.appendLiteral(':')
    }
    if (second) {
      bld.appendSecondOfMinute(2)
    }
    else if (milli) {
      bld.appendLiteral('-')
    }
    if (milli) {
      bld.appendLiteral('.')
      bld.appendMillisOfSecond(3)
    }
  }

  /**
   * Checks that the iso only flag is not set, throwing an exception if it is.
   *
   * @param fields  the fields
   * @param strictISO  true if only ISO formats allowed
   * @since 1.1
   */
  private def checkNotStrictISO(fields: Collection[DateTimeFieldType], strictISO: Boolean) {
    if (strictISO) {
      throw new IllegalArgumentException("No valid ISO8601 format for fields: " + fields)
    }
  }

  /**
   * Appends the separator if necessary.
   *
   * @param bld  the builder
   * @param extended  whether to append the separator
   * @param sep  the separator
   * @since 1.1
   */
  private def appendSeparator(bld: DateTimeFormatterBuilder, extended: Boolean) {
    if (extended) {
      bld.appendLiteral('-')
    }
  }

  /**
   * Returns a generic ISO date parser for parsing dates with a possible zone.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * date              = date-element ['T' offset]
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * offset            = 'Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])
   * </pre>
   */
  def dateParser: DateTimeFormatter = {
    return Constants.dp
  }

  /**
   * Returns a generic ISO date parser for parsing local dates.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * This parser is initialised with the local (UTC) time zone.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * </pre>
   * @since 1.3
   */
  def localDateParser: DateTimeFormatter = {
    return Constants.ldp
  }

  /**
   * Returns a generic ISO date parser for parsing dates.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * </pre>
   */
  def dateElementParser: DateTimeFormatter = {
    return Constants.dpe
  }

  /**
   * Returns a generic ISO time parser for parsing times with a possible zone.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * time           = ['T'] time-element [offset]
   * time-element   = HH [minute-element] | [fraction]
   * minute-element = ':' mm [second-element] | [fraction]
   * second-element = ':' ss [fraction]
   * fraction       = ('.' | ',') digit+
   * offset         = 'Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])
   * </pre>
   */
  def timeParser: DateTimeFormatter = {
    return Constants.tp
  }

  /**
   * Returns a generic ISO time parser for parsing local times.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * This parser is initialised with the local (UTC) time zone.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * time           = ['T'] time-element
   * time-element   = HH [minute-element] | [fraction]
   * minute-element = ':' mm [second-element] | [fraction]
   * second-element = ':' ss [fraction]
   * fraction       = ('.' | ',') digit+
   * </pre>
   * @since 1.3
   */
  def localTimeParser: DateTimeFormatter = {
    return Constants.ltp
  }

  /**
   * Returns a generic ISO time parser.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * time-element   = HH [minute-element] | [fraction]
   * minute-element = ':' mm [second-element] | [fraction]
   * second-element = ':' ss [fraction]
   * fraction       = ('.' | ',') digit+
   * </pre>
   */
  def timeElementParser: DateTimeFormatter = {
    return Constants.tpe
  }

  /**
   * Returns a generic ISO datetime parser which parses either a date or a time or both.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * datetime          = time | date-opt-time
   * time              = 'T' time-element [offset]
   * date-opt-time     = date-element ['T' [time-element] [offset]]
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * time-element      = HH [minute-element] | [fraction]
   * minute-element    = ':' mm [second-element] | [fraction]
   * second-element    = ':' ss [fraction]
   * fraction          = ('.' | ',') digit+
   * offset            = 'Z' | (('+' | '-') HH [':' mm [':' ss [('.' | ',') SSS]]])
   * </pre>
   */
  def dateTimeParser: DateTimeFormatter = {
    return Constants.dtp
  }

  /**
   * Returns a generic ISO datetime parser where the date is mandatory and the time is optional.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * This parser can parse zoned datetimes.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * date-opt-time     = date-element ['T' [time-element] [offset]]
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * time-element      = HH [minute-element] | [fraction]
   * minute-element    = ':' mm [second-element] | [fraction]
   * second-element    = ':' ss [fraction]
   * fraction          = ('.' | ',') digit+
   * </pre>
   * @since 1.3
   */
  def dateOptionalTimeParser: DateTimeFormatter = {
    return Constants.dotp
  }

  /**
   * Returns a generic ISO datetime parser where the date is mandatory and the time is optional.
   * <p>
   * The returned formatter can only be used for parsing, printing is unsupported.
   * <p>
   * This parser only parses local datetimes.
   * This parser is initialised with the local (UTC) time zone.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * It accepts formats described by the following syntax:
   * <pre>
   * datetime          = date-element ['T' time-element]
   * date-element      = std-date-element | ord-date-element | week-date-element
   * std-date-element  = yyyy ['-' MM ['-' dd]]
   * ord-date-element  = yyyy ['-' DDD]
   * week-date-element = xxxx '-W' ww ['-' e]
   * time-element      = HH [minute-element] | [fraction]
   * minute-element    = ':' mm [second-element] | [fraction]
   * second-element    = ':' ss [fraction]
   * fraction          = ('.' | ',') digit+
   * </pre>
   * @since 1.3
   */
  def localDateOptionalTimeParser: DateTimeFormatter = {
    return Constants.ldotp
  }

  /**
   * Returns a formatter for a full date as four digit year, two digit month
   * of year, and two digit day of month (yyyy-MM-dd).
   * <p>
   * The returned formatter prints and parses only this format.
   * See {@link #dateParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-MM-dd
   */
  def date: DateTimeFormatter = {
    return yearMonthDay
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, three digit fraction of second, and
   * time zone offset (HH:mm:ss.SSSZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   * See {@link #timeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for HH:mm:ss.SSSZZ
   */
  def time: DateTimeFormatter = {
    return Constants.t
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, and time zone offset (HH:mm:ssZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   * See {@link #timeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for HH:mm:ssZZ
   */
  def timeNoMillis: DateTimeFormatter = {
    return Constants.tx
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, three digit fraction of second, and
   * time zone offset prefixed by 'T' ('T'HH:mm:ss.SSSZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   * See {@link #timeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for 'T'HH:mm:ss.SSSZZ
   */
  def tTime: DateTimeFormatter = {
    return Constants.tt
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, and time zone offset prefixed
   * by 'T' ('T'HH:mm:ssZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   * See {@link #timeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for 'T'HH:mm:ssZZ
   */
  def tTimeNoMillis: DateTimeFormatter = {
    return Constants.ttx
  }

  /**
   * Returns a formatter that combines a full date and time, separated by a 'T'
   * (yyyy-MM-dd'T'HH:mm:ss.SSSZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm:ss.SSSZZ
   */
  def dateTime: DateTimeFormatter = {
    return Constants.dt
  }

  /**
   * Returns a formatter that combines a full date and time without millis,
   * separated by a 'T' (yyyy-MM-dd'T'HH:mm:ssZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm:ssZZ
   */
  def dateTimeNoMillis: DateTimeFormatter = {
    return Constants.dtx
  }

  /**
   * Returns a formatter for a full ordinal date, using a four
   * digit year and three digit dayOfYear (yyyy-DDD).
   * <p>
   * The returned formatter prints and parses only this format.
   * See {@link #dateParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-DDD
   * @since 1.1
   */
  def ordinalDate: DateTimeFormatter = {
    return Constants.od
  }

  /**
   * Returns a formatter for a full ordinal date and time, using a four
   * digit year and three digit dayOfYear (yyyy-DDD'T'HH:mm:ss.SSSZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-DDD'T'HH:mm:ss.SSSZZ
   * @since 1.1
   */
  def ordinalDateTime: DateTimeFormatter = {
    return Constants.odt
  }

  /**
   * Returns a formatter for a full ordinal date and time without millis,
   * using a four digit year and three digit dayOfYear (yyyy-DDD'T'HH:mm:ssZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for yyyy-DDD'T'HH:mm:ssZZ
   * @since 1.1
   */
  def ordinalDateTimeNoMillis: DateTimeFormatter = {
    return Constants.odtx
  }

  /**
   * Returns a formatter for a full date as four digit weekyear, two digit
   * week of weekyear, and one digit day of week (xxxx-'W'ww-e).
   * <p>
   * The returned formatter prints and parses only this format.
   * See {@link #dateParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for xxxx-'W'ww-e
   */
  def weekDate: DateTimeFormatter = {
    return Constants.wwd
  }

  /**
   * Returns a formatter that combines a full weekyear date and time,
   * separated by a 'T' (xxxx-'W'ww-e'T'HH:mm:ss.SSSZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for xxxx-'W'ww-e'T'HH:mm:ss.SSSZZ
   */
  def weekDateTime: DateTimeFormatter = {
    return Constants.wdt
  }

  /**
   * Returns a formatter that combines a full weekyear date and time without millis,
   * separated by a 'T' (xxxx-'W'ww-e'T'HH:mm:ssZZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HH:mm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   * See {@link #dateTimeParser()} for a more flexible parser that accepts different formats.
   *
   * @return a formatter for xxxx-'W'ww-e'T'HH:mm:ssZZ
   */
  def weekDateTimeNoMillis: DateTimeFormatter = {
    return Constants.wdtx
  }

  /**
   * Returns a basic formatter for a full date as four digit year, two digit
   * month of year, and two digit day of month (yyyyMMdd).
   * <p>
   * The returned formatter prints and parses only this format.
   *
   * @return a formatter for yyyyMMdd
   */
  def basicDate: DateTimeFormatter = {
    return Constants.bd
  }

  /**
   * Returns a basic formatter for a two digit hour of day, two digit minute
   * of hour, two digit second of minute, three digit millis, and time zone
   * offset (HHmmss.SSSZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   *
   * @return a formatter for HHmmss.SSSZ
   */
  def basicTime: DateTimeFormatter = {
    return Constants.bt
  }

  /**
   * Returns a basic formatter for a two digit hour of day, two digit minute
   * of hour, two digit second of minute, and time zone offset (HHmmssZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   *
   * @return a formatter for HHmmssZ
   */
  def basicTimeNoMillis: DateTimeFormatter = {
    return Constants.btx
  }

  /**
   * Returns a basic formatter for a two digit hour of day, two digit minute
   * of hour, two digit second of minute, three digit millis, and time zone
   * offset prefixed by 'T' ('T'HHmmss.SSSZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   *
   * @return a formatter for 'T'HHmmss.SSSZ
   */
  def basicTTime: DateTimeFormatter = {
    return Constants.btt
  }

  /**
   * Returns a basic formatter for a two digit hour of day, two digit minute
   * of hour, two digit second of minute, and time zone offset prefixed by 'T'
   * ('T'HHmmssZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   *
   * @return a formatter for 'T'HHmmssZ
   */
  def basicTTimeNoMillis: DateTimeFormatter = {
    return Constants.bttx
  }

  /**
   * Returns a basic formatter that combines a basic date and time, separated
   * by a 'T' (yyyyMMdd'T'HHmmss.SSSZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   *
   * @return a formatter for yyyyMMdd'T'HHmmss.SSSZ
   */
  def basicDateTime: DateTimeFormatter = {
    return Constants.bdt
  }

  /**
   * Returns a basic formatter that combines a basic date and time without millis,
   * separated by a 'T' (yyyyMMdd'T'HHmmssZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   *
   * @return a formatter for yyyyMMdd'T'HHmmssZ
   */
  def basicDateTimeNoMillis: DateTimeFormatter = {
    return Constants.bdtx
  }

  /**
   * Returns a formatter for a full ordinal date, using a four
   * digit year and three digit dayOfYear (yyyyDDD).
   * <p>
   * The returned formatter prints and parses only this format.
   *
   * @return a formatter for yyyyDDD
   * @since 1.1
   */
  def basicOrdinalDate: DateTimeFormatter = {
    return Constants.bod
  }

  /**
   * Returns a formatter for a full ordinal date and time, using a four
   * digit year and three digit dayOfYear (yyyyDDD'T'HHmmss.SSSZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   *
   * @return a formatter for yyyyDDD'T'HHmmss.SSSZ
   * @since 1.1
   */
  def basicOrdinalDateTime: DateTimeFormatter = {
    return Constants.bodt
  }

  /**
   * Returns a formatter for a full ordinal date and time without millis,
   * using a four digit year and three digit dayOfYear (yyyyDDD'T'HHmmssZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   *
   * @return a formatter for yyyyDDD'T'HHmmssZ
   * @since 1.1
   */
  def basicOrdinalDateTimeNoMillis: DateTimeFormatter = {
    return Constants.bodtx
  }

  /**
   * Returns a basic formatter for a full date as four digit weekyear, two
   * digit week of weekyear, and one digit day of week (xxxx'W'wwe).
   * <p>
   * The returned formatter prints and parses only this format.
   *
   * @return a formatter for xxxx'W'wwe
   */
  def basicWeekDate: DateTimeFormatter = {
    return Constants.bwd
  }

  /**
   * Returns a basic formatter that combines a basic weekyear date and time,
   * separated by a 'T' (xxxx'W'wwe'T'HHmmss.SSSZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which includes milliseconds.
   *
   * @return a formatter for xxxx'W'wwe'T'HHmmss.SSSZ
   */
  def basicWeekDateTime: DateTimeFormatter = {
    return Constants.bwdt
  }

  /**
   * Returns a basic formatter that combines a basic weekyear date and time
   * without millis, separated by a 'T' (xxxx'W'wwe'T'HHmmssZ).
   * <p>
   * The time zone offset is 'Z' for zero, and of the form '\u00b1HHmm' for non-zero.
   * The parser is strict by default, thus time string {@code 24:00} cannot be parsed.
   * <p>
   * The returned formatter prints and parses only this format, which excludes milliseconds.
   *
   * @return a formatter for xxxx'W'wwe'T'HHmmssZ
   */
  def basicWeekDateTimeNoMillis: DateTimeFormatter = {
    return Constants.bwdtx
  }

  /**
   * Returns a formatter for a four digit year. (yyyy)
   *
   * @return a formatter for yyyy
   */
  def year: DateTimeFormatter = {
    return Constants.ye
  }

  /**
   * Returns a formatter for a four digit year and two digit month of
   * year. (yyyy-MM)
   *
   * @return a formatter for yyyy-MM
   */
  def yearMonth: DateTimeFormatter = {
    return Constants.ym
  }

  /**
   * Returns a formatter for a four digit year, two digit month of year, and
   * two digit day of month. (yyyy-MM-dd)
   *
   * @return a formatter for yyyy-MM-dd
   */
  def yearMonthDay: DateTimeFormatter = {
    return Constants.ymd
  }

  /**
   * Returns a formatter for a four digit weekyear. (xxxx)
   *
   * @return a formatter for xxxx
   */
  def weekyear: DateTimeFormatter = {
    return Constants.we
  }

  /**
   * Returns a formatter for a four digit weekyear and two digit week of
   * weekyear. (xxxx-'W'ww)
   *
   * @return a formatter for xxxx-'W'ww
   */
  def weekyearWeek: DateTimeFormatter = {
    return Constants.ww
  }

  /**
   * Returns a formatter for a four digit weekyear, two digit week of
   * weekyear, and one digit day of week. (xxxx-'W'ww-e)
   *
   * @return a formatter for xxxx-'W'ww-e
   */
  def weekyearWeekDay: DateTimeFormatter = {
    return Constants.wwd
  }

  /**
   * Returns a formatter for a two digit hour of day. (HH)
   *
   * @return a formatter for HH
   */
  def hour: DateTimeFormatter = {
    return Constants.hde
  }

  /**
   * Returns a formatter for a two digit hour of day and two digit minute of
   * hour. (HH:mm)
   *
   * @return a formatter for HH:mm
   */
  def hourMinute: DateTimeFormatter = {
    return Constants.hm
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, and two digit second of minute. (HH:mm:ss)
   *
   * @return a formatter for HH:mm:ss
   */
  def hourMinuteSecond: DateTimeFormatter = {
    return Constants.hms
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, and three digit fraction of
   * second (HH:mm:ss.SSS). Parsing will parse up to 3 fractional second
   * digits.
   *
   * @return a formatter for HH:mm:ss.SSS
   */
  def hourMinuteSecondMillis: DateTimeFormatter = {
    return Constants.hmsl
  }

  /**
   * Returns a formatter for a two digit hour of day, two digit minute of
   * hour, two digit second of minute, and three digit fraction of
   * second (HH:mm:ss.SSS). Parsing will parse up to 9 fractional second
   * digits, throwing away all except the first three.
   *
   * @return a formatter for HH:mm:ss.SSS
   */
  def hourMinuteSecondFraction: DateTimeFormatter = {
    return Constants.hmsf
  }

  /**
   * Returns a formatter that combines a full date and two digit hour of
   * day. (yyyy-MM-dd'T'HH)
   *
   * @return a formatter for yyyy-MM-dd'T'HH
   */
  def dateHour: DateTimeFormatter = {
    return Constants.dh
  }

  /**
   * Returns a formatter that combines a full date, two digit hour of day,
   * and two digit minute of hour. (yyyy-MM-dd'T'HH:mm)
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm
   */
  def dateHourMinute: DateTimeFormatter = {
    return Constants.dhm
  }

  /**
   * Returns a formatter that combines a full date, two digit hour of day,
   * two digit minute of hour, and two digit second of
   * minute. (yyyy-MM-dd'T'HH:mm:ss)
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm:ss
   */
  def dateHourMinuteSecond: DateTimeFormatter = {
    return Constants.dhms
  }

  /**
   * Returns a formatter that combines a full date, two digit hour of day,
   * two digit minute of hour, two digit second of minute, and three digit
   * fraction of second (yyyy-MM-dd'T'HH:mm:ss.SSS). Parsing will parse up
   * to 3 fractional second digits.
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm:ss.SSS
   */
  def dateHourMinuteSecondMillis: DateTimeFormatter = {
    return Constants.dhmsl
  }

  /**
   * Returns a formatter that combines a full date, two digit hour of day,
   * two digit minute of hour, two digit second of minute, and three digit
   * fraction of second (yyyy-MM-dd'T'HH:mm:ss.SSS). Parsing will parse up
   * to 9 fractional second digits, throwing away all except the first three.
   *
   * @return a formatter for yyyy-MM-dd'T'HH:mm:ss.SSS
   */
  def dateHourMinuteSecondFraction: DateTimeFormatter = {
    return Constants.dhmsf
  }

  private[format] object Constants {
    private val ye: DateTimeFormatter = yearElement
    private val mye: DateTimeFormatter = monthElement
    private val dme: DateTimeFormatter = dayOfMonthElement
    private val we: DateTimeFormatter = weekyearElement
    private val wwe: DateTimeFormatter = weekElement
    private val dwe: DateTimeFormatter = dayOfWeekElement
    private val dye: DateTimeFormatter = dayOfYearElement
    private val hde: DateTimeFormatter = hourElement
    private val mhe: DateTimeFormatter = minuteElement
    private val sme: DateTimeFormatter = secondElement
    private val fse: DateTimeFormatter = fractionElement
    private val ze: DateTimeFormatter = offsetElement
    private val lte: DateTimeFormatter = literalTElement
    private val ym: DateTimeFormatter = yearMonth
    private val ymd: DateTimeFormatter = yearMonthDay
    private val ww: DateTimeFormatter = weekyearWeek
    private val wwd: DateTimeFormatter = weekyearWeekDay
    private val hm: DateTimeFormatter = hourMinute
    private val hms: DateTimeFormatter = hourMinuteSecond
    private val hmsl: DateTimeFormatter = hourMinuteSecondMillis
    private val hmsf: DateTimeFormatter = hourMinuteSecondFraction
    private val dh: DateTimeFormatter = dateHour
    private val dhm: DateTimeFormatter = dateHourMinute
    private val dhms: DateTimeFormatter = dateHourMinuteSecond
    private val dhmsl: DateTimeFormatter = dateHourMinuteSecondMillis
    private val dhmsf: DateTimeFormatter = dateHourMinuteSecondFraction
    private val t: DateTimeFormatter = time
    private val tx: DateTimeFormatter = timeNoMillis
    private val tt: DateTimeFormatter = tTime
    private val ttx: DateTimeFormatter = tTimeNoMillis
    private val dt: DateTimeFormatter = dateTime
    private val dtx: DateTimeFormatter = dateTimeNoMillis
    private val wdt: DateTimeFormatter = weekDateTime
    private val wdtx: DateTimeFormatter = weekDateTimeNoMillis
    private val od: DateTimeFormatter = ordinalDate
    private val odt: DateTimeFormatter = ordinalDateTime
    private val odtx: DateTimeFormatter = ordinalDateTimeNoMillis
    private val bd: DateTimeFormatter = basicDate
    private val bt: DateTimeFormatter = basicTime
    private val btx: DateTimeFormatter = basicTimeNoMillis
    private val btt: DateTimeFormatter = basicTTime
    private val bttx: DateTimeFormatter = basicTTimeNoMillis
    private val bdt: DateTimeFormatter = basicDateTime
    private val bdtx: DateTimeFormatter = basicDateTimeNoMillis
    private val bod: DateTimeFormatter = basicOrdinalDate
    private val bodt: DateTimeFormatter = basicOrdinalDateTime
    private val bodtx: DateTimeFormatter = basicOrdinalDateTimeNoMillis
    private val bwd: DateTimeFormatter = basicWeekDate
    private val bwdt: DateTimeFormatter = basicWeekDateTime
    private val bwdtx: DateTimeFormatter = basicWeekDateTimeNoMillis
    private val dpe: DateTimeFormatter = dateElementParser
    private val tpe: DateTimeFormatter = timeElementParser
    private val dp: DateTimeFormatter = dateParser
    private val ldp: DateTimeFormatter = localDateParser
    private val tp: DateTimeFormatter = timeParser
    private val ltp: DateTimeFormatter = localTimeParser
    private val dtp: DateTimeFormatter = dateTimeParser
    private val dotp: DateTimeFormatter = dateOptionalTimeParser
    private val ldotp: DateTimeFormatter = localDateOptionalTimeParser

    private def dateParser: DateTimeFormatter = {
      if (dp == null) {
        val tOffset: DateTimeParser = new DateTimeFormatterBuilder().appendLiteral('T').append(offsetElement).toParser
        return new DateTimeFormatterBuilder().append(dateElementParser).appendOptional(tOffset).toFormatter
      }
      return dp
    }

    private def localDateParser: DateTimeFormatter = {
      if (ldp == null) {
        return dateElementParser.withZoneUTC
      }
      return ldp
    }

    private def dateElementParser: DateTimeFormatter = {
      if (dpe == null) {
        return new DateTimeFormatterBuilder().append(null, Array[DateTimeParser](new DateTimeFormatterBuilder().append(yearElement).appendOptional(new DateTimeFormatterBuilder().append(monthElement).appendOptional(dayOfMonthElement.getParser).toParser).toParser, new DateTimeFormatterBuilder().append(weekyearElement).append(weekElement).appendOptional(dayOfWeekElement.getParser).toParser, new DateTimeFormatterBuilder().append(yearElement).append(dayOfYearElement).toParser)).toFormatter
      }
      return dpe
    }

    private def timeParser: DateTimeFormatter = {
      if (tp == null) {
        return new DateTimeFormatterBuilder().appendOptional(literalTElement.getParser).append(timeElementParser).appendOptional(offsetElement.getParser).toFormatter
      }
      return tp
    }

    private def localTimeParser: DateTimeFormatter = {
      if (ltp == null) {
        return new DateTimeFormatterBuilder().appendOptional(literalTElement.getParser).append(timeElementParser).toFormatter.withZoneUTC
      }
      return ltp
    }

    private def timeElementParser: DateTimeFormatter = {
      if (tpe == null) {
        val decimalPoint: DateTimeParser = new DateTimeFormatterBuilder().append(null, Array[DateTimeParser](new DateTimeFormatterBuilder().appendLiteral('.').toParser, new DateTimeFormatterBuilder().appendLiteral(',').toParser)).toParser
        return new DateTimeFormatterBuilder().append(hourElement).append(null, Array[DateTimeParser](new DateTimeFormatterBuilder().append(minuteElement).append(null, Array[DateTimeParser](new DateTimeFormatterBuilder().append(secondElement).appendOptional(new DateTimeFormatterBuilder().append(decimalPoint).appendFractionOfSecond(1, 9).toParser).toParser, new DateTimeFormatterBuilder().append(decimalPoint).appendFractionOfMinute(1, 9).toParser, null)).toParser, new DateTimeFormatterBuilder().append(decimalPoint).appendFractionOfHour(1, 9).toParser, null)).toFormatter
      }
      return tpe
    }

    private def dateTimeParser: DateTimeFormatter = {
      if (dtp == null) {
        val time: DateTimeParser = new DateTimeFormatterBuilder().appendLiteral('T').append(timeElementParser).appendOptional(offsetElement.getParser).toParser
        return new DateTimeFormatterBuilder().append(null, Array[DateTimeParser](time, dateOptionalTimeParser.getParser)).toFormatter
      }
      return dtp
    }

    private def dateOptionalTimeParser: DateTimeFormatter = {
      if (dotp == null) {
        val timeOrOffset: DateTimeParser = new DateTimeFormatterBuilder().appendLiteral('T').appendOptional(timeElementParser.getParser).appendOptional(offsetElement.getParser).toParser
        return new DateTimeFormatterBuilder().append(dateElementParser).appendOptional(timeOrOffset).toFormatter
      }
      return dotp
    }

    private def localDateOptionalTimeParser: DateTimeFormatter = {
      if (ldotp == null) {
        val time: DateTimeParser = new DateTimeFormatterBuilder().appendLiteral('T').append(timeElementParser).toParser
        return new DateTimeFormatterBuilder().append(dateElementParser).appendOptional(time).toFormatter.withZoneUTC
      }
      return ldotp
    }

    private def time: DateTimeFormatter = {
      if (t == null) {
        return new DateTimeFormatterBuilder().append(hourMinuteSecondFraction).append(offsetElement).toFormatter
      }
      return t
    }

    private def timeNoMillis: DateTimeFormatter = {
      if (tx == null) {
        return new DateTimeFormatterBuilder().append(hourMinuteSecond).append(offsetElement).toFormatter
      }
      return tx
    }

    private def tTime: DateTimeFormatter = {
      if (tt == null) {
        return new DateTimeFormatterBuilder().append(literalTElement).append(time).toFormatter
      }
      return tt
    }

    private def tTimeNoMillis: DateTimeFormatter = {
      if (ttx == null) {
        return new DateTimeFormatterBuilder().append(literalTElement).append(timeNoMillis).toFormatter
      }
      return ttx
    }

    private def dateTime: DateTimeFormatter = {
      if (dt == null) {
        return new DateTimeFormatterBuilder().append(date).append(tTime).toFormatter
      }
      return dt
    }

    private def dateTimeNoMillis: DateTimeFormatter = {
      if (dtx == null) {
        return new DateTimeFormatterBuilder().append(date).append(tTimeNoMillis).toFormatter
      }
      return dtx
    }

    private def ordinalDate: DateTimeFormatter = {
      if (od == null) {
        return new DateTimeFormatterBuilder().append(yearElement).append(dayOfYearElement).toFormatter
      }
      return od
    }

    private def ordinalDateTime: DateTimeFormatter = {
      if (odt == null) {
        return new DateTimeFormatterBuilder().append(ordinalDate).append(tTime).toFormatter
      }
      return odt
    }

    private def ordinalDateTimeNoMillis: DateTimeFormatter = {
      if (odtx == null) {
        return new DateTimeFormatterBuilder().append(ordinalDate).append(tTimeNoMillis).toFormatter
      }
      return odtx
    }

    private def weekDateTime: DateTimeFormatter = {
      if (wdt == null) {
        return new DateTimeFormatterBuilder().append(weekDate).append(tTime).toFormatter
      }
      return wdt
    }

    private def weekDateTimeNoMillis: DateTimeFormatter = {
      if (wdtx == null) {
        return new DateTimeFormatterBuilder().append(weekDate).append(tTimeNoMillis).toFormatter
      }
      return wdtx
    }

    private def basicDate: DateTimeFormatter = {
      if (bd == null) {
        return new DateTimeFormatterBuilder().appendYear(4, 4).appendFixedDecimal(DateTimeFieldType.monthOfYear, 2).appendFixedDecimal(DateTimeFieldType.dayOfMonth, 2).toFormatter
      }
      return bd
    }

    private def basicTime: DateTimeFormatter = {
      if (bt == null) {
        return new DateTimeFormatterBuilder().appendFixedDecimal(DateTimeFieldType.hourOfDay, 2).appendFixedDecimal(DateTimeFieldType.minuteOfHour, 2).appendFixedDecimal(DateTimeFieldType.secondOfMinute, 2).appendLiteral('.').appendFractionOfSecond(3, 9).appendTimeZoneOffset("Z", false, 2, 2).toFormatter
      }
      return bt
    }

    private def basicTimeNoMillis: DateTimeFormatter = {
      if (btx == null) {
        return new DateTimeFormatterBuilder().appendFixedDecimal(DateTimeFieldType.hourOfDay, 2).appendFixedDecimal(DateTimeFieldType.minuteOfHour, 2).appendFixedDecimal(DateTimeFieldType.secondOfMinute, 2).appendTimeZoneOffset("Z", false, 2, 2).toFormatter
      }
      return btx
    }

    private def basicTTime: DateTimeFormatter = {
      if (btt == null) {
        return new DateTimeFormatterBuilder().append(literalTElement).append(basicTime).toFormatter
      }
      return btt
    }

    private def basicTTimeNoMillis: DateTimeFormatter = {
      if (bttx == null) {
        return new DateTimeFormatterBuilder().append(literalTElement).append(basicTimeNoMillis).toFormatter
      }
      return bttx
    }

    private def basicDateTime: DateTimeFormatter = {
      if (bdt == null) {
        return new DateTimeFormatterBuilder().append(basicDate).append(basicTTime).toFormatter
      }
      return bdt
    }

    private def basicDateTimeNoMillis: DateTimeFormatter = {
      if (bdtx == null) {
        return new DateTimeFormatterBuilder().append(basicDate).append(basicTTimeNoMillis).toFormatter
      }
      return bdtx
    }

    private def basicOrdinalDate: DateTimeFormatter = {
      if (bod == null) {
        return new DateTimeFormatterBuilder().appendYear(4, 4).appendFixedDecimal(DateTimeFieldType.dayOfYear, 3).toFormatter
      }
      return bod
    }

    private def basicOrdinalDateTime: DateTimeFormatter = {
      if (bodt == null) {
        return new DateTimeFormatterBuilder().append(basicOrdinalDate).append(basicTTime).toFormatter
      }
      return bodt
    }

    private def basicOrdinalDateTimeNoMillis: DateTimeFormatter = {
      if (bodtx == null) {
        return new DateTimeFormatterBuilder().append(basicOrdinalDate).append(basicTTimeNoMillis).toFormatter
      }
      return bodtx
    }

    private def basicWeekDate: DateTimeFormatter = {
      if (bwd == null) {
        return new DateTimeFormatterBuilder().appendWeekyear(4, 4).appendLiteral('W').appendFixedDecimal(DateTimeFieldType.weekOfWeekyear, 2).appendFixedDecimal(DateTimeFieldType.dayOfWeek, 1).toFormatter
      }
      return bwd
    }

    private def basicWeekDateTime: DateTimeFormatter = {
      if (bwdt == null) {
        return new DateTimeFormatterBuilder().append(basicWeekDate).append(basicTTime).toFormatter
      }
      return bwdt
    }

    private def basicWeekDateTimeNoMillis: DateTimeFormatter = {
      if (bwdtx == null) {
        return new DateTimeFormatterBuilder().append(basicWeekDate).append(basicTTimeNoMillis).toFormatter
      }
      return bwdtx
    }

    private def yearMonth: DateTimeFormatter = {
      if (ym == null) {
        return new DateTimeFormatterBuilder().append(yearElement).append(monthElement).toFormatter
      }
      return ym
    }

    private def yearMonthDay: DateTimeFormatter = {
      if (ymd == null) {
        return new DateTimeFormatterBuilder().append(yearElement).append(monthElement).append(dayOfMonthElement).toFormatter
      }
      return ymd
    }

    private def weekyearWeek: DateTimeFormatter = {
      if (ww == null) {
        return new DateTimeFormatterBuilder().append(weekyearElement).append(weekElement).toFormatter
      }
      return ww
    }

    private def weekyearWeekDay: DateTimeFormatter = {
      if (wwd == null) {
        return new DateTimeFormatterBuilder().append(weekyearElement).append(weekElement).append(dayOfWeekElement).toFormatter
      }
      return wwd
    }

    private def hourMinute: DateTimeFormatter = {
      if (hm == null) {
        return new DateTimeFormatterBuilder().append(hourElement).append(minuteElement).toFormatter
      }
      return hm
    }

    private def hourMinuteSecond: DateTimeFormatter = {
      if (hms == null) {
        return new DateTimeFormatterBuilder().append(hourElement).append(minuteElement).append(secondElement).toFormatter
      }
      return hms
    }

    private def hourMinuteSecondMillis: DateTimeFormatter = {
      if (hmsl == null) {
        return new DateTimeFormatterBuilder().append(hourElement).append(minuteElement).append(secondElement).appendLiteral('.').appendFractionOfSecond(3, 3).toFormatter
      }
      return hmsl
    }

    private def hourMinuteSecondFraction: DateTimeFormatter = {
      if (hmsf == null) {
        return new DateTimeFormatterBuilder().append(hourElement).append(minuteElement).append(secondElement).append(fractionElement).toFormatter
      }
      return hmsf
    }

    private def dateHour: DateTimeFormatter = {
      if (dh == null) {
        return new DateTimeFormatterBuilder().append(date).append(literalTElement).append(hour).toFormatter
      }
      return dh
    }

    private def dateHourMinute: DateTimeFormatter = {
      if (dhm == null) {
        return new DateTimeFormatterBuilder().append(date).append(literalTElement).append(hourMinute).toFormatter
      }
      return dhm
    }

    private def dateHourMinuteSecond: DateTimeFormatter = {
      if (dhms == null) {
        return new DateTimeFormatterBuilder().append(date).append(literalTElement).append(hourMinuteSecond).toFormatter
      }
      return dhms
    }

    private def dateHourMinuteSecondMillis: DateTimeFormatter = {
      if (dhmsl == null) {
        return new DateTimeFormatterBuilder().append(date).append(literalTElement).append(hourMinuteSecondMillis).toFormatter
      }
      return dhmsl
    }

    private def dateHourMinuteSecondFraction: DateTimeFormatter = {
      if (dhmsf == null) {
        return new DateTimeFormatterBuilder().append(date).append(literalTElement).append(hourMinuteSecondFraction).toFormatter
      }
      return dhmsf
    }

    private def yearElement: DateTimeFormatter = {
      if (ye == null) {
        return new DateTimeFormatterBuilder().appendYear(4, 9).toFormatter
      }
      return ye
    }

    private def monthElement: DateTimeFormatter = {
      if (mye == null) {
        return new DateTimeFormatterBuilder().appendLiteral('-').appendMonthOfYear(2).toFormatter
      }
      return mye
    }

    private def dayOfMonthElement: DateTimeFormatter = {
      if (dme == null) {
        return new DateTimeFormatterBuilder().appendLiteral('-').appendDayOfMonth(2).toFormatter
      }
      return dme
    }

    private def weekyearElement: DateTimeFormatter = {
      if (we == null) {
        return new DateTimeFormatterBuilder().appendWeekyear(4, 9).toFormatter
      }
      return we
    }

    private def weekElement: DateTimeFormatter = {
      if (wwe == null) {
        return new DateTimeFormatterBuilder().appendLiteral("-W").appendWeekOfWeekyear(2).toFormatter
      }
      return wwe
    }

    private def dayOfWeekElement: DateTimeFormatter = {
      if (dwe == null) {
        return new DateTimeFormatterBuilder().appendLiteral('-').appendDayOfWeek(1).toFormatter
      }
      return dwe
    }

    private def dayOfYearElement: DateTimeFormatter = {
      if (dye == null) {
        return new DateTimeFormatterBuilder().appendLiteral('-').appendDayOfYear(3).toFormatter
      }
      return dye
    }

    private def literalTElement: DateTimeFormatter = {
      if (lte == null) {
        return new DateTimeFormatterBuilder().appendLiteral('T').toFormatter
      }
      return lte
    }

    private def hourElement: DateTimeFormatter = {
      if (hde == null) {
        return new DateTimeFormatterBuilder().appendHourOfDay(2).toFormatter
      }
      return hde
    }

    private def minuteElement: DateTimeFormatter = {
      if (mhe == null) {
        return new DateTimeFormatterBuilder().appendLiteral(':').appendMinuteOfHour(2).toFormatter
      }
      return mhe
    }

    private def secondElement: DateTimeFormatter = {
      if (sme == null) {
        return new DateTimeFormatterBuilder().appendLiteral(':').appendSecondOfMinute(2).toFormatter
      }
      return sme
    }

    private def fractionElement: DateTimeFormatter = {
      if (fse == null) {
        return new DateTimeFormatterBuilder().appendLiteral('.').appendFractionOfSecond(3, 9).toFormatter
      }
      return fse
    }

    private def offsetElement: DateTimeFormatter = {
      if (ze == null) {
        return new DateTimeFormatterBuilder().appendTimeZoneOffset("Z", true, 2, 4).toFormatter
      }
      return ze
    }
  }

}

class ISODateTimeFormat {
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
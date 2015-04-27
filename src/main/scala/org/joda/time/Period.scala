/*
 *  Copyright 2001-2010 Stephen Colebourne
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
package org.joda.time

import java.io.Serializable
import org.joda.convert.FromString
import org.joda.time.base.BasePeriod
import org.joda.time.chrono.ISOChronology
import org.joda.time.field.FieldUtils
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter

/**
 * An immutable time period specifying a set of duration field values.
 * <p>
 * A time period is divided into a number of fields, such as hours and seconds.
 * Which fields are supported is defined by the PeriodType class.
 * The default is the standard period type, which supports years, months, weeks, days,
 * hours, minutes, seconds and millis.
 * <p>
 * When this time period is added to an instant, the effect is of adding each field in turn.
 * As a result, this takes into account daylight savings time.
 * Adding a time period of 1 day to the day before daylight savings starts will only add
 * 23 hours rather than 24 to ensure that the time remains the same.
 * If this is not the behaviour you want, then see {@link Duration}.
 * <p>
 * The definition of a period also affects the equals method. A period of 1
 * day is not equal to a period of 24 hours, nor 1 hour equal to 60 minutes.
 * This is because periods represent an abstracted definition of a time period
 * (eg. a day may not actually be 24 hours, it might be 23 or 25 at daylight
 * savings boundary). To compare the actual duration of two periods, convert
 * both to durations using toDuration, an operation that emphasises that the
 * result may differ according to the date you choose.
 * <p>
 * Period is thread-safe and immutable, provided that the PeriodType is as well.
 * All standard PeriodType classes supplied are thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 * @see MutablePeriod
 */
@SerialVersionUID(741052353876488155L)
object Period {
  /**
   * A period of zero length and standard period type.
   * @since 1.4
   */
  val ZERO: Period = new Period

  /**
   * Parses a {@code Period} from the specified string.
   * <p>
   * This uses {@link ISOPeriodFormat#standard()}.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  @FromString def parse(str: String): Period = {
    return parse(str, ISOPeriodFormat.standard)
  }

  /**
   * Parses a {@code Period} from the specified string using a formatter.
   *
   * @param str  the string to parse, not null
   * @param formatter  the formatter to use, not null
   * @since 2.0
   */
  def parse(str: String, formatter: PeriodFormatter): Period = {
    return formatter.parsePeriod(str)
  }

  /**
   * Create a period with a specified number of years.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as months or days using the <code>withXxx()</code> methods.
   * For example, <code>Period.years(2).withMonths(6);</code>
   * <p>
   * If you want a year-based period that cannot have other fields added,
   * then you should consider using {@link Years}.
   *
   * @param years  the amount of years in this period
   * @return the period
   */
  def years(years: Int): Period = {
    return new Period(Array[Int](years, 0, 0, 0, 0, 0, 0, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of months.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as years or days using the <code>withXxx()</code> methods.
   * For example, <code>Period.months(2).withDays(6);</code>
   * <p>
   * If you want a month-based period that cannot have other fields added,
   * then you should consider using {@link Months}.
   *
   * @param months  the amount of months in this period
   * @return the period
   */
  def months(months: Int): Period = {
    return new Period(Array[Int](0, months, 0, 0, 0, 0, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of weeks.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as months or days using the <code>withXxx()</code> methods.
   * For example, <code>Period.weeks(2).withDays(6);</code>
   * <p>
   * If you want a week-based period that cannot have other fields added,
   * then you should consider using {@link Weeks}.
   *
   * @param weeks  the amount of weeks in this period
   * @return the period
   */
  def weeks(weeks: Int): Period = {
    return new Period(Array[Int](0, 0, weeks, 0, 0, 0, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of days.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as months or weeks using the <code>withXxx()</code> methods.
   * For example, <code>Period.days(2).withHours(6);</code>
   * <p>
   * If you want a day-based period that cannot have other fields added,
   * then you should consider using {@link Days}.
   *
   * @param days  the amount of days in this period
   * @return the period
   */
  def days(days: Int): Period = {
    return new Period(Array[Int](0, 0, 0, days, 0, 0, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of hours.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as months or days using the <code>withXxx()</code> methods.
   * For example, <code>Period.hours(2).withMinutes(30);</code>
   * <p>
   * If you want a hour-based period that cannot have other fields added,
   * then you should consider using {@link Hours}.
   *
   * @param hours  the amount of hours in this period
   * @return the period
   */
  def hours(hours: Int): Period = {
    return new Period(Array[Int](0, 0, 0, 0, hours, 0, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of minutes.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as days or hours using the <code>withXxx()</code> methods.
   * For example, <code>Period.minutes(2).withSeconds(30);</code>
   * <p>
   * If you want a minute-based period that cannot have other fields added,
   * then you should consider using {@link Minutes}.
   *
   * @param minutes  the amount of minutes in this period
   * @return the period
   */
  def minutes(minutes: Int): Period = {
    return new Period(Array[Int](0, 0, 0, 0, 0, minutes, 0, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of seconds.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as days or hours using the <code>withXxx()</code> methods.
   * For example, <code>Period.seconds(2).withMillis(30);</code>
   * <p>
   * If you want a second-based period that cannot have other fields added,
   * then you should consider using {@link Seconds}.
   *
   * @param seconds  the amount of seconds in this period
   * @return the period
   */
  def seconds(seconds: Int): Period = {
    return new Period(Array[Int](0, 0, 0, 0, 0, 0, seconds, 0), PeriodType.standard)
  }

  /**
   * Create a period with a specified number of millis.
   * <p>
   * The standard period type is used, thus you can add other fields such
   * as days or hours using the <code>withXxx()</code> methods.
   * For example, <code>Period.millis(20).withSeconds(30);</code>
   *
   * @param millis  the amount of millis in this period
   * @return the period
   */
  def millis(millis: Int): Period = {
    return new Period(Array[Int](0, 0, 0, 0, 0, 0, 0, millis), PeriodType.standard)
  }

  /**
   * Creates a period from two partially specified times, calculating
   * by field difference.
   * <p>
   * The two partials must contain the same fields, thus you can specify
   * two <code>LocalDate</code> objects, or two <code>LocalTime</code> objects,
   * but not one of each. Also, the partial may not contain overlapping
   * fields, such as dayOfWeek and dayOfMonth.
   * <p>
   * Calculation by field difference works by extracting the difference
   * one field at a time and not wrapping into other fields.
   * Thus 2005-06-09/2007-04-12 will yield P1Y-2M3D.
   * <p>
   * For example, you have an event that always runs from the 27th of
   * each month to the 2nd of the next month. If you calculate this
   * period using a standard constructor, then you will get between
   * P3D and P6D depending on the month. If you use this method, then
   * you will get P1M-25D. This field-difference based period can
   * be successfully applied to each month of the year to obtain the
   * correct end date for a given start date.
   *
   * @param start  the start of the period, must not be null
   * @param end  the end of the period, must not be null
   * @throws IllegalArgumentException if the partials are null or invalid
   * @since 1.1
   */
  def fieldDifference(start: ReadablePartial, end: ReadablePartial): Period = {
    if (start == null || end == null) {
      throw new IllegalArgumentException("ReadablePartial objects must not be null")
    }
    if (start.size != end.size) {
      throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
    }
    val types: Array[DurationFieldType] = new Array[DurationFieldType](start.size)
    val values: Array[Int] = new Array[Int](start.size)
    {
      var i: Int = 0
      val isize: Int = start.size
      while (i < isize) {
        {
          if (start.getFieldType(i) ne end.getFieldType(i)) {
            throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
          }
          types(i) = start.getFieldType(i).getDurationType
          if (i > 0 && types(i - 1) eq types(i)) {
            throw new IllegalArgumentException("ReadablePartial objects must not have overlapping fields")
          }
          values(i) = end.getValue(i) - start.getValue(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return new Period(values, PeriodType.forFields(types))
  }
}

@SerialVersionUID(741052353876488155L)
final class Period extends BasePeriod with ReadablePeriod with Serializable {
  /**
   * Creates a new empty period with the standard set of fields.
   * <p>
   * One way to initialise a period is as follows:
   * <pre>
   * Period = new Period().withYears(6).withMonths(3).withSeconds(23);
   * </pre>
   * Bear in mind that this creates four period instances in total, three of
   * which are immediately discarded.
   * The alterative is more efficient, but less readable:
   * <pre>
   * Period = new Period(6, 3, 0, 0, 0, 0, 23, 0);
   * </pre>
   * The following is also slightly less wasteful:
   * <pre>
   * Period = Period.years(6).withMonths(3).withSeconds(23);
   * </pre>
   */
  def this() {
    this()
    `super`(0L, null, null)
  }

  /**
   * Create a period from a set of field values using the standard set of fields.
   * Note that the parameters specify the time fields hours, minutes,
   * seconds and millis, not the date fields.
   *
   * @param hours  amount of hours in this period
   * @param minutes  amount of minutes in this period
   * @param seconds  amount of seconds in this period
   * @param millis  amount of milliseconds in this period
   */
  def this(hours: Int, minutes: Int, seconds: Int, millis: Int) {
    this()
    `super`(0, 0, 0, 0, hours, minutes, seconds, millis, PeriodType.standard)
  }

  /**
   * Create a period from a set of field values using the standard set of fields.
   *
   * @param years  amount of years in this period
   * @param months  amount of months in this period
   * @param weeks  amount of weeks in this period
   * @param days  amount of days in this period
   * @param hours  amount of hours in this period
   * @param minutes  amount of minutes in this period
   * @param seconds  amount of seconds in this period
   * @param millis  amount of milliseconds in this period
   */
  def this(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int) {
    this()
    `super`(years, months, weeks, days, hours, minutes, seconds, millis, PeriodType.standard)
  }

  /**
   * Create a period from a set of field values.
   * <p>
   * There is usually little need to use this constructor.
   * The period type is used primarily to define how to split an interval into a period.
   * As this constructor already is split, the period type does no real work.
   *
   * @param years  amount of years in this period, which must be zero if unsupported
   * @param months  amount of months in this period, which must be zero if unsupported
   * @param weeks  amount of weeks in this period, which must be zero if unsupported
   * @param days  amount of days in this period, which must be zero if unsupported
   * @param hours  amount of hours in this period, which must be zero if unsupported
   * @param minutes  amount of minutes in this period, which must be zero if unsupported
   * @param seconds  amount of seconds in this period, which must be zero if unsupported
   * @param millis  amount of milliseconds in this period, which must be zero if unsupported
   * @param type  which set of fields this period supports, null means AllType
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  def this(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, `type`: PeriodType) {
    this()
    `super`(years, months, weeks, days, hours, minutes, seconds, millis, `type`)
  }

  /**
   * Creates a period from the given millisecond duration using the standard
   * set of fields.
   * <p>
   * Only precise fields in the period type will be used.
   * For the standard period type this is the time fields only.
   * Thus the year, month, week and day fields will not be populated.
   * <p>
   * If the duration is small, less than one day, then this method will perform
   * as you might expect and split the fields evenly.
   * <p>
   * If the duration is larger than one day then all the remaining duration will
   * be stored in the largest available precise field, hours in this case.
   * <p>
   * For example, a duration equal to (365 + 60 + 5) days will be converted to
   * ((365 + 60 + 5) * 24) hours by this constructor.
   * <p>
   * For more control over the conversion process, you have two options:
   * <ul>
   * <li>convert the duration to an {@link Interval}, and from there obtain the period
   * <li>specify a period type that contains precise definitions of the day and larger
   * fields, such as UTC
   * </ul>
   *
   * @param duration  the duration, in milliseconds
   */
  def this(duration: Long) {
    this()
    `super`(duration)
  }

  /**
   * Creates a period from the given millisecond duration.
   * <p>
   * Only precise fields in the period type will be used.
   * Imprecise fields will not be populated.
   * <p>
   * If the duration is small then this method will perform
   * as you might expect and split the fields evenly.
   * <p>
   * If the duration is large then all the remaining duration will
   * be stored in the largest available precise field.
   * For details as to which fields are precise, review the period type javadoc.
   *
   * @param duration  the duration, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   */
  def this(duration: Long, `type`: PeriodType) {
    this()
    `super`(duration, `type`, null)
  }

  /**
   * Creates a period from the given millisecond duration using the standard
   * set of fields.
   * <p>
   * Only precise fields in the period type will be used.
   * Imprecise fields will not be populated.
   * <p>
   * If the duration is small then this method will perform
   * as you might expect and split the fields evenly.
   * <p>
   * If the duration is large then all the remaining duration will
   * be stored in the largest available precise field.
   * For details as to which fields are precise, review the period type javadoc.
   *
   * @param duration  the duration, in milliseconds
   * @param chronology  the chronology to use to split the duration, null means ISO default
   */
  def this(duration: Long, chronology: Chronology) {
    this()
    `super`(duration, null, chronology)
  }

  /**
   * Creates a period from the given millisecond duration.
   * <p>
   * Only precise fields in the period type will be used.
   * Imprecise fields will not be populated.
   * <p>
   * If the duration is small then this method will perform
   * as you might expect and split the fields evenly.
   * <p>
   * If the duration is large then all the remaining duration will
   * be stored in the largest available precise field.
   * For details as to which fields are precise, review the period type javadoc.
   *
   * @param duration  the duration, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   * @param chronology  the chronology to use to split the duration, null means ISO default
   */
  def this(duration: Long, `type`: PeriodType, chronology: Chronology) {
    this()
    `super`(duration, `type`, chronology)
  }

  /**
   * Creates a period from the given interval endpoints using the standard
   * set of fields.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   */
  def this(startInstant: Long, endInstant: Long) {
    this()
    `super`(startInstant, endInstant, null, null)
  }

  /**
   * Creates a period from the given interval endpoints.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   */
  def this(startInstant: Long, endInstant: Long, `type`: PeriodType) {
    this()
    `super`(startInstant, endInstant, `type`, null)
  }

  /**
   * Creates a period from the given interval endpoints using the standard
   * set of fields.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @param chrono  the chronology to use, null means ISO in default zone
   */
  def this(startInstant: Long, endInstant: Long, chrono: Chronology) {
    this()
    `super`(startInstant, endInstant, null, chrono)
  }

  /**
   * Creates a period from the given interval endpoints.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   * @param chrono  the chronology to use, null means ISO in default zone
   */
  def this(startInstant: Long, endInstant: Long, `type`: PeriodType, chrono: Chronology) {
    this()
    `super`(startInstant, endInstant, `type`, chrono)
  }

  /**
   * Creates a period between the given instants using the standard set of fields.
   * <p>
   * Most calculations performed by this method have obvious results.
   * The special case is where the calculation is from a "long" month to a "short" month.
   * Here, the result favours increasing the months field rather than the days.
   * For example, 2013-01-31 to 2013-02-28 is treated as one whole month.
   * By contrast, 2013-01-31 to 2013-03-30 is treated as one month and 30 days
   * (exposed as 4 weeks and 2 days).
   * The results are explained by considering that the start date plus the
   * calculated period result in the end date.
   *
   * @param startInstant  interval start, null means now
   * @param endInstant  interval end, null means now
   */
  def this(startInstant: ReadableInstant, endInstant: ReadableInstant) {
    this()
    `super`(startInstant, endInstant, null)
  }

  /**
   * Creates a period between the given instants.
   * <p>
   * Most calculations performed by this method have obvious results.
   * The special case is where the calculation is from a "long" month to a "short" month.
   * Here, the result favours increasing the months field rather than the days.
   * For example, 2013-01-31 to 2013-02-28 is treated as one whole month.
   * By contrast, 2013-01-31 to 2013-03-30 is treated as one month and 30 days.
   * The results are explained by considering that the start date plus the
   * calculated period result in the end date.
   *
   * @param startInstant  interval start, null means now
   * @param endInstant  interval end, null means now
   * @param type  which set of fields this period supports, null means standard
   */
  def this(startInstant: ReadableInstant, endInstant: ReadableInstant, `type`: PeriodType) {
    this()
    `super`(startInstant, endInstant, `type`)
  }

  /**
   * Creates a period from two partially specified times.
   * <p>
   * The two partials must contain the same fields, thus you can specify
   * two <code>LocalDate</code> objects, or two <code>LocalTime</code> objects,
   * but not one of each.
   * As these are Partial objects, time zones have no effect on the result.
   * <p>
   * The two partials must also both be contiguous - see
   * {@link DateTimeUtils#isContiguous(ReadablePartial)} for a definition.
   * Both <code>LocalDate</code> and <code>LocalTime</code> are contiguous.
   * <p>
   * Most calculations performed by this method have obvious results.
   * The special case is where the calculation is from a "long" month to a "short" month.
   * Here, the result favours increasing the months field rather than the days.
   * For example, 2013-01-31 to 2013-02-28 is treated as one whole month.
   * By contrast, 2013-01-31 to 2013-03-30 is treated as one month and 30 days
   * (exposed as 4 weeks and 2 days).
   * The results are explained by considering that the start date plus the
   * calculated period result in the end date.
   * <p>
   * An alternative way of constructing a Period from two Partials
   * is {@link #fieldDifference(ReadablePartial, ReadablePartial)}.
   * That method handles all kinds of partials.
   *
   * @param start  the start of the period, must not be null
   * @param end  the end of the period, must not be null
   * @throws IllegalArgumentException if the partials are null or invalid
   * @since 1.1
   */
  def this(start: ReadablePartial, end: ReadablePartial) {
    this()
    `super`(start, end, null)
  }

  /**
   * Creates a period from two partially specified times.
   * <p>
   * The two partials must contain the same fields, thus you can specify
   * two <code>LocalDate</code> objects, or two <code>LocalTime</code> objects,
   * but not one of each.
   * As these are Partial objects, time zones have no effect on the result.
   * <p>
   * The two partials must also both be contiguous - see
   * {@link DateTimeUtils#isContiguous(ReadablePartial)} for a definition.
   * Both <code>LocalDate</code> and <code>LocalTime</code> are contiguous.
   * <p>
   * Most calculations performed by this method have obvious results.
   * The special case is where the calculation is from a "long" month to a "short" month.
   * Here, the result favours increasing the months field rather than the days.
   * For example, 2013-01-31 to 2013-02-28 is treated as one whole month.
   * By contrast, 2013-01-31 to 2013-03-30 is treated as one month and 30 days.
   * The results are explained by considering that the start date plus the
   * calculated period result in the end date.
   * <p>
   * An alternative way of constructing a Period from two Partials
   * is {@link #fieldDifference(ReadablePartial, ReadablePartial)}.
   * That method handles all kinds of partials.
   *
   * @param start  the start of the period, must not be null
   * @param end  the end of the period, must not be null
   * @param type  which set of fields this period supports, null means standard
   * @throws IllegalArgumentException if the partials are null or invalid
   * @since 1.1
   */
  def this(start: ReadablePartial, end: ReadablePartial, `type`: PeriodType) {
    this()
    `super`(start, end, `type`)
  }

  /**
   * Creates a period from the given start point and the duration.
   *
   * @param startInstant  the interval start, null means now
   * @param duration  the duration of the interval, null means zero-length
   */
  def this(startInstant: ReadableInstant, duration: ReadableDuration) {
    this()
    `super`(startInstant, duration, null)
  }

  /**
   * Creates a period from the given start point and the duration.
   *
   * @param startInstant  the interval start, null means now
   * @param duration  the duration of the interval, null means zero-length
   * @param type  which set of fields this period supports, null means standard
   */
  def this(startInstant: ReadableInstant, duration: ReadableDuration, `type`: PeriodType) {
    this()
    `super`(startInstant, duration, `type`)
  }

  /**
   * Creates a period from the given duration and end point.
   *
   * @param duration  the duration of the interval, null means zero-length
   * @param endInstant  the interval end, null means now
   */
  def this(duration: ReadableDuration, endInstant: ReadableInstant) {
    this()
    `super`(duration, endInstant, null)
  }

  /**
   * Creates a period from the given duration and end point.
   *
   * @param duration  the duration of the interval, null means zero-length
   * @param endInstant  the interval end, null means now
   * @param type  which set of fields this period supports, null means standard
   */
  def this(duration: ReadableDuration, endInstant: ReadableInstant, `type`: PeriodType) {
    this()
    `super`(duration, endInstant, `type`)
  }

  /**
   * Creates a period by converting or copying from another object.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadablePeriod, ReadableInterval and String.
   * The String formats are described by {@link ISOPeriodFormat#standard()}.
   *
   * @param period  period to convert
   * @throws IllegalArgumentException if period is invalid
   * @throws UnsupportedOperationException if an unsupported field's value is non-zero
   */
  def this(period: AnyRef) {
    this()
    `super`(period, null, null)
  }

  /**
   * Creates a period by converting or copying from another object.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadablePeriod, ReadableInterval and String.
   * The String formats are described by {@link ISOPeriodFormat#standard()}.
   *
   * @param period  period to convert
   * @param type  which set of fields this period supports, null means use converter
   * @throws IllegalArgumentException if period is invalid
   * @throws UnsupportedOperationException if an unsupported field's value is non-zero
   */
  def this(period: AnyRef, `type`: PeriodType) {
    this()
    `super`(period, `type`, null)
  }

  /**
   * Creates a period by converting or copying from another object.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadablePeriod, ReadableInterval and String.
   * The String formats are described by {@link ISOPeriodFormat#standard()}.
   *
   * @param period  period to convert
   * @param chrono  the chronology to use, null means ISO in default zone
   * @throws IllegalArgumentException if period is invalid
   * @throws UnsupportedOperationException if an unsupported field's value is non-zero
   */
  def this(period: AnyRef, chrono: Chronology) {
    this()
    `super`(period, null, chrono)
  }

  /**
   * Creates a period by converting or copying from another object.
   * <p>
   * The recognised object types are defined in
   * {@link org.joda.time.convert.ConverterManager ConverterManager} and
   * include ReadablePeriod, ReadableInterval and String.
   * The String formats are described by {@link ISOPeriodFormat#standard()}.
   *
   * @param period  period to convert
   * @param type  which set of fields this period supports, null means use converter
   * @param chrono  the chronology to use, null means ISO in default zone
   * @throws IllegalArgumentException if period is invalid
   * @throws UnsupportedOperationException if an unsupported field's value is non-zero
   */
  def this(period: AnyRef, `type`: PeriodType, chrono: Chronology) {
    this()
    `super`(period, `type`, chrono)
  }

  /**
   * Constructor used when we trust ourselves.
   *
   * @param values  the values to use, not null, not cloned
   * @param type  which set of fields this period supports, not null
   */
  private def this(values: Array[Int], `type`: PeriodType) {
    this()
    `super`(values, `type`)
  }

  /**
   * Get this period as an immutable <code>Period</code> object
   * by returning <code>this</code>.
   *
   * @return <code>this</code>
   */
  override def toPeriod: Period = {
    return this
  }

  /**
   * Gets the years field part of the period.
   *
   * @return the number of years in the period, zero if unsupported
   */
  def getYears: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.YEAR_INDEX)
  }

  /**
   * Gets the months field part of the period.
   *
   * @return the number of months in the period, zero if unsupported
   */
  def getMonths: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.MONTH_INDEX)
  }

  /**
   * Gets the weeks field part of the period.
   *
   * @return the number of weeks in the period, zero if unsupported
   */
  def getWeeks: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.WEEK_INDEX)
  }

  /**
   * Gets the days field part of the period.
   *
   * @return the number of days in the period, zero if unsupported
   */
  def getDays: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.DAY_INDEX)
  }

  /**
   * Gets the hours field part of the period.
   *
   * @return the number of hours in the period, zero if unsupported
   */
  def getHours: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.HOUR_INDEX)
  }

  /**
   * Gets the minutes field part of the period.
   *
   * @return the number of minutes in the period, zero if unsupported
   */
  def getMinutes: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.MINUTE_INDEX)
  }

  /**
   * Gets the seconds field part of the period.
   *
   * @return the number of seconds in the period, zero if unsupported
   */
  def getSeconds: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.SECOND_INDEX)
  }

  /**
   * Gets the millis field part of the period.
   *
   * @return the number of millis in the period, zero if unsupported
   */
  def getMillis: Int = {
    return getPeriodType.getIndexedField(this, PeriodType.MILLI_INDEX)
  }

  /**
   * Creates a new Period instance with the same field values but
   * different PeriodType.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param type  the period type to use, null means standard
   * @return the new period instance
   * @throws IllegalArgumentException if the new period won't accept all of the current fields
   */
  def withPeriodType(`type`: PeriodType): Period = {
    `type` = DateTimeUtils.getPeriodType(`type`)
    if (`type` == getPeriodType) {
      return this
    }
    return new Period(this, `type`)
  }

  /**
   * Creates a new Period instance with the fields from the specified period
   * copied on top of those from this period.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param period  the period to copy from, null ignored
   * @return the new period instance
   * @throws IllegalArgumentException if a field type is unsupported
   */
  def withFields(period: ReadablePeriod): Period = {
    if (period == null) {
      return this
    }
    var newValues: Array[Int] = getValues
    newValues = super.mergePeriodInto(newValues, period)
    return new Period(newValues, getPeriodType)
  }

  /**
   * Creates a new Period instance with the specified field set to a new value.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param field  the field to set, not null
   * @param value  the value to set to
   * @return the new period instance
   * @throws IllegalArgumentException if the field type is null or unsupported
   */
  def withField(field: DurationFieldType, value: Int): Period = {
    if (field == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    val newValues: Array[Int] = getValues
    super.setFieldInto(newValues, field, value)
    return new Period(newValues, getPeriodType)
  }

  /**
   * Creates a new Period instance with the valueToAdd added to the specified field.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param field  the field to set, not null
   * @param value  the value to add
   * @return the new period instance
   * @throws IllegalArgumentException if the field type is null or unsupported
   */
  def withFieldAdded(field: DurationFieldType, value: Int): Period = {
    if (field == null) {
      throw new IllegalArgumentException("Field must not be null")
    }
    if (value == 0) {
      return this
    }
    val newValues: Array[Int] = getValues
    super.addFieldInto(newValues, field, value)
    return new Period(newValues, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of years.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param years  the amount of years to add, may be negative
   * @return the new period with the increased years
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withYears(years: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.YEAR_INDEX, values, years)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of months.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param months  the amount of months to add, may be negative
   * @return the new period with the increased months
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withMonths(months: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.MONTH_INDEX, values, months)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of weeks.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param weeks  the amount of weeks to add, may be negative
   * @return the new period with the increased weeks
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withWeeks(weeks: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.WEEK_INDEX, values, weeks)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of days.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param days  the amount of days to add, may be negative
   * @return the new period with the increased days
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withDays(days: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.DAY_INDEX, values, days)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of hours.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to add, may be negative
   * @return the new period with the increased hours
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withHours(hours: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.HOUR_INDEX, values, hours)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of minutes.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to add, may be negative
   * @return the new period with the increased minutes
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withMinutes(minutes: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.MINUTE_INDEX, values, minutes)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of seconds.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param seconds  the amount of seconds to add, may be negative
   * @return the new period with the increased seconds
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withSeconds(seconds: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.SECOND_INDEX, values, seconds)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of millis.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param millis  the amount of millis to add, may be negative
   * @return the new period with the increased millis
   * @throws UnsupportedOperationException if the field is not supported
   */
  def withMillis(millis: Int): Period = {
    val values: Array[Int] = getValues
    getPeriodType.setIndexedField(this, PeriodType.MILLI_INDEX, values, millis)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified period added.
   * <p>
   * Each field of the period is added separately. Thus a period of
   * 2 hours 30 minutes plus 3 hours 40 minutes will produce a result
   * of 5 hours 70 minutes - see {@link #normalizedStandard()}.
   * <p>
   * If the period being added contains a non-zero amount for a field that
   * is not supported in this period then an exception is thrown.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param period  the period to add, null adds zero and returns this
   * @return the new updated period
   * @throws UnsupportedOperationException if any field is not supported
   * @since 1.5
   */
  def plus(period: ReadablePeriod): Period = {
    if (period == null) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.YEAR_INDEX, values, period.get(DurationFieldType.YEARS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MONTH_INDEX, values, period.get(DurationFieldType.MONTHS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.WEEK_INDEX, values, period.get(DurationFieldType.WEEKS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.DAY_INDEX, values, period.get(DurationFieldType.DAYS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.HOUR_INDEX, values, period.get(DurationFieldType.HOURS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MINUTE_INDEX, values, period.get(DurationFieldType.MINUTES_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.SECOND_INDEX, values, period.get(DurationFieldType.SECONDS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MILLI_INDEX, values, period.get(DurationFieldType.MILLIS_TYPE))
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of years added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param years  the amount of years to add, may be negative
   * @return the new period with the increased years
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusYears(years: Int): Period = {
    if (years == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.YEAR_INDEX, values, years)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of months added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param months  the amount of months to add, may be negative
   * @return the new period plus the increased months
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusMonths(months: Int): Period = {
    if (months == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.MONTH_INDEX, values, months)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of weeks added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param weeks  the amount of weeks to add, may be negative
   * @return the new period plus the increased weeks
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusWeeks(weeks: Int): Period = {
    if (weeks == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.WEEK_INDEX, values, weeks)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of days added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param days  the amount of days to add, may be negative
   * @return the new period plus the increased days
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusDays(days: Int): Period = {
    if (days == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.DAY_INDEX, values, days)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of hours added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to add, may be negative
   * @return the new period plus the increased hours
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusHours(hours: Int): Period = {
    if (hours == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.HOUR_INDEX, values, hours)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of minutes added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to add, may be negative
   * @return the new period plus the increased minutes
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusMinutes(minutes: Int): Period = {
    if (minutes == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.MINUTE_INDEX, values, minutes)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of seconds added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param seconds  the amount of seconds to add, may be negative
   * @return the new period plus the increased seconds
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusSeconds(seconds: Int): Period = {
    if (seconds == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.SECOND_INDEX, values, seconds)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period plus the specified number of millis added.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param millis  the amount of millis to add, may be negative
   * @return the new period plus the increased millis
   * @throws UnsupportedOperationException if the field is not supported
   */
  def plusMillis(millis: Int): Period = {
    if (millis == 0) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.MILLI_INDEX, values, millis)
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified period subtracted.
   * <p>
   * Each field of the period is subtracted separately. Thus a period of
   * 3 hours 30 minutes minus 2 hours 40 minutes will produce a result
   * of 1 hour and -10 minutes - see {@link #normalizedStandard()}.
   * <p>
   * If the period being added contains a non-zero amount for a field that
   * is not supported in this period then an exception is thrown.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param period  the period to add, null adds zero and returns this
   * @return the new updated period
   * @throws UnsupportedOperationException if any field is not supported
   * @since 1.5
   */
  def minus(period: ReadablePeriod): Period = {
    if (period == null) {
      return this
    }
    val values: Array[Int] = getValues
    getPeriodType.addIndexedField(this, PeriodType.YEAR_INDEX, values, -period.get(DurationFieldType.YEARS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MONTH_INDEX, values, -period.get(DurationFieldType.MONTHS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.WEEK_INDEX, values, -period.get(DurationFieldType.WEEKS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.DAY_INDEX, values, -period.get(DurationFieldType.DAYS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.HOUR_INDEX, values, -period.get(DurationFieldType.HOURS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MINUTE_INDEX, values, -period.get(DurationFieldType.MINUTES_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.SECOND_INDEX, values, -period.get(DurationFieldType.SECONDS_TYPE))
    getPeriodType.addIndexedField(this, PeriodType.MILLI_INDEX, values, -period.get(DurationFieldType.MILLIS_TYPE))
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new period with the specified number of years taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param years  the amount of years to take away, may be negative
   * @return the new period with the increased years
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusYears(years: Int): Period = {
    return plusYears(-years)
  }

  /**
   * Returns a new period minus the specified number of months taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param months  the amount of months to take away, may be negative
   * @return the new period minus the increased months
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusMonths(months: Int): Period = {
    return plusMonths(-months)
  }

  /**
   * Returns a new period minus the specified number of weeks taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param weeks  the amount of weeks to take away, may be negative
   * @return the new period minus the increased weeks
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusWeeks(weeks: Int): Period = {
    return plusWeeks(-weeks)
  }

  /**
   * Returns a new period minus the specified number of days taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param days  the amount of days to take away, may be negative
   * @return the new period minus the increased days
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusDays(days: Int): Period = {
    return plusDays(-days)
  }

  /**
   * Returns a new period minus the specified number of hours taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param hours  the amount of hours to take away, may be negative
   * @return the new period minus the increased hours
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusHours(hours: Int): Period = {
    return plusHours(-hours)
  }

  /**
   * Returns a new period minus the specified number of minutes taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param minutes  the amount of minutes to take away, may be negative
   * @return the new period minus the increased minutes
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusMinutes(minutes: Int): Period = {
    return plusMinutes(-minutes)
  }

  /**
   * Returns a new period minus the specified number of seconds taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param seconds  the amount of seconds to take away, may be negative
   * @return the new period minus the increased seconds
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusSeconds(seconds: Int): Period = {
    return plusSeconds(-seconds)
  }

  /**
   * Returns a new period minus the specified number of millis taken away.
   * <p>
   * This period instance is immutable and unaffected by this method call.
   *
   * @param millis  the amount of millis to take away, may be negative
   * @return the new period minus the increased millis
   * @throws UnsupportedOperationException if the field is not supported
   */
  def minusMillis(millis: Int): Period = {
    return plusMillis(-millis)
  }

  /**
   * Returns a new instance with each element in this period multiplied
   * by the specified scalar.
   *
   * @param scalar  the scalar to multiply by, not null
   * @return a { @code Period} based on this period with the amounts multiplied by the scalar, never null
   * @throws ArithmeticException if the capacity of any field is exceeded
   * @since 2.1
   */
  def multipliedBy(scalar: Int): Period = {
    if (this eq Period.ZERO || scalar == 1) {
      return this
    }
    val values: Array[Int] = getValues
    {
      var i: Int = 0
      while (i < values.length) {
        {
          values(i) = FieldUtils.safeMultiply(values(i), scalar)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return new Period(values, getPeriodType)
  }

  /**
   * Returns a new instance with each amount in this period negated.
   *
   * @return a { @code Period} based on this period with the amounts negated, never null
   * @throws ArithmeticException if any field has the minimum value
   * @since 2.1
   */
  def negated: Period = {
    return multipliedBy(-1)
  }

  /**
   * Converts this period to a period in weeks assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a period representing the number of standard weeks in this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @throws ArithmeticException if the number of weeks is too large to be represented
   * @since 1.5
   */
  def toStandardWeeks: Weeks = {
    checkYearsAndMonths("Weeks")
    var millis: Long = getMillis
    millis += (getSeconds.toLong) * DateTimeConstants.MILLIS_PER_SECOND
    millis += (getMinutes.toLong) * DateTimeConstants.MILLIS_PER_MINUTE
    millis += (getHours.toLong) * DateTimeConstants.MILLIS_PER_HOUR
    millis += (getDays.toLong) * DateTimeConstants.MILLIS_PER_DAY
    val weeks: Long = (getWeeks.toLong) + millis / DateTimeConstants.MILLIS_PER_WEEK
    return Weeks.weeks(FieldUtils.safeToInt(weeks))
  }

  /**
   * Converts this period to a period in days assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a period representing the number of standard days in this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @throws ArithmeticException if the number of days is too large to be represented
   * @since 1.5
   */
  def toStandardDays: Days = {
    checkYearsAndMonths("Days")
    var millis: Long = getMillis
    millis += (getSeconds.toLong) * DateTimeConstants.MILLIS_PER_SECOND
    millis += (getMinutes.toLong) * DateTimeConstants.MILLIS_PER_MINUTE
    millis += (getHours.toLong) * DateTimeConstants.MILLIS_PER_HOUR
    var days: Long = millis / DateTimeConstants.MILLIS_PER_DAY
    days = FieldUtils.safeAdd(days, getDays)
    days = FieldUtils.safeAdd(days, (getWeeks.toLong) * (DateTimeConstants.DAYS_PER_WEEK.toLong))
    return Days.days(FieldUtils.safeToInt(days))
  }

  /**
   * Converts this period to a period in hours assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a period representing the number of standard hours in this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @throws ArithmeticException if the number of hours is too large to be represented
   * @since 1.5
   */
  def toStandardHours: Hours = {
    checkYearsAndMonths("Hours")
    var millis: Long = getMillis
    millis += (getSeconds.toLong) * DateTimeConstants.MILLIS_PER_SECOND
    millis += (getMinutes.toLong) * DateTimeConstants.MILLIS_PER_MINUTE
    var hours: Long = millis / DateTimeConstants.MILLIS_PER_HOUR
    hours = FieldUtils.safeAdd(hours, getHours)
    hours = FieldUtils.safeAdd(hours, (getDays.toLong) * (DateTimeConstants.HOURS_PER_DAY.toLong))
    hours = FieldUtils.safeAdd(hours, (getWeeks.toLong) * (DateTimeConstants.HOURS_PER_WEEK.toLong))
    return Hours.hours(FieldUtils.safeToInt(hours))
  }

  /**
   * Converts this period to a period in minutes assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a period representing the number of standard minutes in this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @throws ArithmeticException if the number of minutes is too large to be represented
   * @since 1.5
   */
  def toStandardMinutes: Minutes = {
    checkYearsAndMonths("Minutes")
    var millis: Long = getMillis
    millis += (getSeconds.toLong) * DateTimeConstants.MILLIS_PER_SECOND
    var minutes: Long = millis / DateTimeConstants.MILLIS_PER_MINUTE
    minutes = FieldUtils.safeAdd(minutes, getMinutes)
    minutes = FieldUtils.safeAdd(minutes, (getHours.toLong) * (DateTimeConstants.MINUTES_PER_HOUR.toLong))
    minutes = FieldUtils.safeAdd(minutes, (getDays.toLong) * (DateTimeConstants.MINUTES_PER_DAY.toLong))
    minutes = FieldUtils.safeAdd(minutes, (getWeeks.toLong) * (DateTimeConstants.MINUTES_PER_WEEK.toLong))
    return Minutes.minutes(FieldUtils.safeToInt(minutes))
  }

  /**
   * Converts this period to a period in seconds assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert between different types of period.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a period representing the number of standard seconds in this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @throws ArithmeticException if the number of seconds is too large to be represented
   * @since 1.5
   */
  def toStandardSeconds: Seconds = {
    checkYearsAndMonths("Seconds")
    var seconds: Long = getMillis / DateTimeConstants.MILLIS_PER_SECOND
    seconds = FieldUtils.safeAdd(seconds, getSeconds)
    seconds = FieldUtils.safeAdd(seconds, (getMinutes.toLong) * (DateTimeConstants.SECONDS_PER_MINUTE.toLong))
    seconds = FieldUtils.safeAdd(seconds, (getHours.toLong) * (DateTimeConstants.SECONDS_PER_HOUR.toLong))
    seconds = FieldUtils.safeAdd(seconds, (getDays.toLong) * (DateTimeConstants.SECONDS_PER_DAY.toLong))
    seconds = FieldUtils.safeAdd(seconds, (getWeeks.toLong) * (DateTimeConstants.SECONDS_PER_WEEK.toLong))
    return Seconds.seconds(FieldUtils.safeToInt(seconds))
  }

  /**
   * Converts this period to a duration assuming a
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to convert from a period to a duration.
   * However to achieve this it makes the assumption that all
   * weeks are 7 days, all days are 24 hours, all hours are 60 minutes and
   * all minutes are 60 seconds. This is not true when daylight savings time
   * is considered, and may also not be true for some unusual chronologies.
   * However, it is included as it is a useful operation for many
   * applications and business rules.
   * <p>
   * If the period contains years or months, an exception will be thrown.
   *
   * @return a duration equivalent to this period
   * @throws UnsupportedOperationException if the period contains years or months
   * @since 1.5
   */
  def toStandardDuration: Duration = {
    checkYearsAndMonths("Duration")
    var millis: Long = getMillis
    millis += ((getSeconds.toLong) * (DateTimeConstants.MILLIS_PER_SECOND.toLong))
    millis += ((getMinutes.toLong) * (DateTimeConstants.MILLIS_PER_MINUTE.toLong))
    millis += ((getHours.toLong) * (DateTimeConstants.MILLIS_PER_HOUR.toLong))
    millis += ((getDays.toLong) * (DateTimeConstants.MILLIS_PER_DAY.toLong))
    millis += ((getWeeks.toLong) * (DateTimeConstants.MILLIS_PER_WEEK.toLong))
    return new Duration(millis)
  }

  /**
   * Check that there are no years or months in the period.
   *
   * @param destintionType  the destination type, not null
   * @throws UnsupportedOperationException if the period contains years or months
   */
  private def checkYearsAndMonths(destintionType: String) {
    if (getMonths != 0) {
      throw new UnsupportedOperationException("Cannot convert to " + destintionType + " as this period contains months and months vary in length")
    }
    if (getYears != 0) {
      throw new UnsupportedOperationException("Cannot convert to " + destintionType + " as this period contains years and years vary in length")
    }
  }

  /**
   * Normalizes this period using standard rules, assuming a 12 month year,
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute.
   * <p>
   * This method allows you to normalize a period.
   * However to achieve this it makes the assumption that all years are
   * 12 months, all weeks are 7 days, all days are 24 hours,
   * all hours are 60 minutes and all minutes are 60 seconds. This is not
   * true when daylight savings time is considered, and may also not be true
   * for some chronologies. However, it is included as it is a useful operation
   * for many applications and business rules.
   * <p>
   * If the period contains years or months, then the months will be
   * normalized to be between 0 and 11. The days field and below will be
   * normalized as necessary, however this will not overflow into the months
   * field. Thus a period of 1 year 15 months will normalize to 2 years 3 months.
   * But a period of 1 month 40 days will remain as 1 month 40 days.
   * <p>
   * The result will always have a <code>PeriodType</code> of standard, thus
   * days will be grouped into weeks.
   *
   * @return a normalized period equivalent to this period
   * @throws ArithmeticException if any field is too large to be represented
   * @since 1.5
   */
  def normalizedStandard: Period = {
    return normalizedStandard(PeriodType.standard)
  }

  /**
   * Normalizes this period using standard rules, assuming a 12 month year,
   * 7 day week, 24 hour day, 60 minute hour and 60 second minute,
   * providing control over how the result is split into fields.
   * <p>
   * This method allows you to normalize a period.
   * However to achieve this it makes the assumption that all years are
   * 12 months, all weeks are 7 days, all days are 24 hours,
   * all hours are 60 minutes and all minutes are 60 seconds. This is not
   * true when daylight savings time is considered, and may also not be true
   * for some chronologies. However, it is included as it is a useful operation
   * for many applications and business rules.
   * <p>
   * If the period contains years or months, then the months will be
   * normalized to be between 0 and 11. The days field and below will be
   * normalized as necessary, however this will not overflow into the months
   * field. Thus a period of 1 year 15 months will normalize to 2 years 3 months.
   * But a period of 1 month 40 days will remain as 1 month 40 days.
   * <p>
   * The PeriodType parameter controls how the result is created. It allows
   * you to omit certain fields from the result if desired. For example,
   * you may not want the result to include weeks, in which case you pass
   * in <code>PeriodType.yearMonthDayTime()</code>.
   *
   * @param type  the period type of the new period, null means standard type
   * @return a normalized period equivalent to this period
   * @throws ArithmeticException if any field is too large to be represented
   * @throws UnsupportedOperationException if this period contains non-zero
   *                                       years or months but the specified period type does not support them
   * @since 1.5
   */
  def normalizedStandard(`type`: PeriodType): Period = {
    `type` = DateTimeUtils.getPeriodType(`type`)
    var millis: Long = getMillis
    millis += ((getSeconds.toLong) * (DateTimeConstants.MILLIS_PER_SECOND.toLong))
    millis += ((getMinutes.toLong) * (DateTimeConstants.MILLIS_PER_MINUTE.toLong))
    millis += ((getHours.toLong) * (DateTimeConstants.MILLIS_PER_HOUR.toLong))
    millis += ((getDays.toLong) * (DateTimeConstants.MILLIS_PER_DAY.toLong))
    millis += ((getWeeks.toLong) * (DateTimeConstants.MILLIS_PER_WEEK.toLong))
    var result: Period = new Period(millis, `type`, ISOChronology.getInstanceUTC)
    val years: Int = getYears
    val months: Int = getMonths
    if (years != 0 || months != 0) {
      var totalMonths: Long = years * 12L + months
      if (`type`.isSupported(DurationFieldType.YEARS_TYPE)) {
        val normalizedYears: Int = FieldUtils.safeToInt(totalMonths / 12)
        result = result.withYears(normalizedYears)
        totalMonths = totalMonths - (normalizedYears * 12)
      }
      if (`type`.isSupported(DurationFieldType.MONTHS_TYPE)) {
        val normalizedMonths: Int = FieldUtils.safeToInt(totalMonths)
        result = result.withMonths(normalizedMonths)
        totalMonths = totalMonths - normalizedMonths
      }
      if (totalMonths != 0) {
        throw new UnsupportedOperationException("Unable to normalize as PeriodType is missing either years or months but period has a month/year amount: " + toString)
      }
    }
    return result
  }
}
/*
 *  Copyright 2001-2011 Stephen Colebourne
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
import org.joda.time.field.FieldUtils
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter

/**
 * Standard mutable time period implementation.
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
 * MutablePeriod is mutable and not thread-safe, unless concurrent threads
 * are not invoking mutator methods.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 * @see Period
 */
@SerialVersionUID(3436451121567212165L)
object MutablePeriod {
  /**
   * Parses a {@code MutablePeriod} from the specified string.
   * <p>
   * This uses {@link ISOPeriodFormat#standard()}.
   *
   * @param str  the string to parse, not null
   * @since 2.0
   */
  @FromString def parse(str: String): MutablePeriod = {
    return parse(str, ISOPeriodFormat.standard)
  }

  /**
   * Parses a {@code MutablePeriod} from the specified string using a formatter.
   *
   * @param str  the string to parse, not null
   * @param formatter  the formatter to use, not null
   * @since 2.0
   */
  def parse(str: String, formatter: PeriodFormatter): MutablePeriod = {
    return formatter.parsePeriod(str).toMutablePeriod
  }
}

@SerialVersionUID(3436451121567212165L)
class MutablePeriod extends BasePeriod with ReadWritablePeriod with Cloneable with Serializable {
  /**
   * Creates a zero-length period using the standard period type.
   */
  def this() {
    this()
    `super`(0L, null, null)
  }

  /**
   * Creates a zero-length period using the specified period type.
   *
   * @param type  which set of fields this period supports
   */
  def this(`type`: PeriodType) {
    this()
    `super`(0L, `type`, null)
  }

  /**
   * Create a period from a set of field values using the standard set of fields.
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
   * fields, such as the UTC or precise types.
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
   * Creates a period from the given interval endpoints using the standard
   * set of fields.
   * <p>
   * The chronology of the start instant is used, unless that is null when the
   * chronology of the end instant is used instead.
   *
   * @param startInstant  interval start, null means now
   * @param endInstant  interval end, null means now
   */
  def this(startInstant: ReadableInstant, endInstant: ReadableInstant) {
    this()
    `super`(startInstant, endInstant, null)
  }

  /**
   * Creates a period from the given interval endpoints.
   * <p>
   * The chronology of the start instant is used, unless that is null when the
   * chronology of the end instant is used instead.
   *
   * @param startInstant  interval start, null means now
   * @param endInstant  interval end, null means now
   * @param type  which set of fields this period supports, null means AllType
   */
  def this(startInstant: ReadableInstant, endInstant: ReadableInstant, `type`: PeriodType) {
    this()
    `super`(startInstant, endInstant, `type`)
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
   * Clears the period, setting all values back to zero.
   */
  def clear {
    super.setValues(new Array[Int](size))
  }

  /**
   * Sets the value of one of the fields by index.
   *
   * @param index  the field index
   * @param value  the new value for the field
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  override def setValue(index: Int, value: Int) {
    super.setValue(index, value)
  }

  /**
   * Sets the value of one of the fields.
   * <p>
   * The field type specified must be one of those that is supported by the period.
   *
   * @param field  a DurationFieldType instance that is supported by this period, not null
   * @param value  the new value for the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def set(field: DurationFieldType, value: Int) {
    super.setField(field, value)
  }

  /**
   * Sets all the fields in one go from another ReadablePeriod.
   *
   * @param period  the period to set, null means zero length period
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  override def setPeriod(period: ReadablePeriod) {
    super.setPeriod(period)
  }

  /**
   * Sets all the fields in one go.
   *
   * @param years  amount of years in this period, which must be zero if unsupported
   * @param months  amount of months in this period, which must be zero if unsupported
   * @param weeks  amount of weeks in this period, which must be zero if unsupported
   * @param days  amount of days in this period, which must be zero if unsupported
   * @param hours  amount of hours in this period, which must be zero if unsupported
   * @param minutes  amount of minutes in this period, which must be zero if unsupported
   * @param seconds  amount of seconds in this period, which must be zero if unsupported
   * @param millis  amount of milliseconds in this period, which must be zero if unsupported
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  override def setPeriod(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int) {
    super.setPeriod(years, months, weeks, days, hours, minutes, seconds, millis)
  }

  /**
   * Sets all the fields in one go from an interval using the ISO chronology
   * and dividing the fields using the period type.
   *
   * @param interval  the interval to set, null means zero length
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(interval: ReadableInterval) {
    if (interval == null) {
      setPeriod(0L)
    }
    else {
      val chrono: Chronology = DateTimeUtils.getChronology(interval.getChronology)
      setPeriod(interval.getStartMillis, interval.getEndMillis, chrono)
    }
  }

  /**
   * Sets all the fields in one go from two instants representing an interval.
   * <p>
   * The chronology of the start instant is used, unless that is null when the
   * chronology of the end instant is used instead.
   *
   * @param start  the start instant, null means now
   * @param end  the end instant, null means now
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(start: ReadableInstant, end: ReadableInstant) {
    if (start eq end) {
      setPeriod(0L)
    }
    else {
      val startMillis: Long = DateTimeUtils.getInstantMillis(start)
      val endMillis: Long = DateTimeUtils.getInstantMillis(end)
      val chrono: Chronology = DateTimeUtils.getIntervalChronology(start, end)
      setPeriod(startMillis, endMillis, chrono)
    }
  }

  /**
   * Sets all the fields in one go from a millisecond interval using ISOChronology
   * and dividing the fields using the period type.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(startInstant: Long, endInstant: Long) {
    setPeriod(startInstant, endInstant, null)
  }

  /**
   * Sets all the fields in one go from a millisecond interval.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @param chrono  the chronology to use, null means ISO chronology
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(startInstant: Long, endInstant: Long, chrono: Chronology) {
    chrono = DateTimeUtils.getChronology(chrono)
    setValues(chrono.get(this, startInstant, endInstant))
  }

  /**
   * Sets all the fields in one go from a duration dividing the
   * fields using the period type.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration to set, null means zero length
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(duration: ReadableDuration) {
    setPeriod(duration, null)
  }

  /**
   * Sets all the fields in one go from a duration dividing the
   * fields using the period type.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration to set, null means zero length
   * @param chrono  the chronology to use, null means ISO default
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(duration: ReadableDuration, chrono: Chronology) {
    val durationMillis: Long = DateTimeUtils.getDurationMillis(duration)
    setPeriod(durationMillis, chrono)
  }

  /**
   * Sets all the fields in one go from a millisecond duration dividing the
   * fields using the period type.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration, in milliseconds
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(duration: Long) {
    setPeriod(duration, null)
  }

  /**
   * Sets all the fields in one go from a millisecond duration.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration, in milliseconds
   * @param chrono  the chronology to use, null means ISO chronology
   * @throws ArithmeticException if the set exceeds the capacity of the period
   */
  def setPeriod(duration: Long, chrono: Chronology) {
    chrono = DateTimeUtils.getChronology(chrono)
    setValues(chrono.get(this, duration))
  }

  /**
   * Adds to the value of one of the fields.
   * <p>
   * The field type specified must be one of those that is supported by the period.
   *
   * @param field  a DurationFieldType instance that is supported by this period, not null
   * @param value  the value to add to the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def add(field: DurationFieldType, value: Int) {
    super.addField(field, value)
  }

  /**
   * Adds a period to this one by adding each field in turn.
   *
   * @param period  the period to add, null means add nothing
   * @throws IllegalArgumentException if the period being added contains a field
   *                                  not supported by this period
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(period: ReadablePeriod) {
    super.addPeriod(period)
  }

  /**
   * Adds to each field of this period.
   *
   * @param years  amount of years to add to this period, which must be zero if unsupported
   * @param months  amount of months to add to this period, which must be zero if unsupported
   * @param weeks  amount of weeks to add to this period, which must be zero if unsupported
   * @param days  amount of days to add to this period, which must be zero if unsupported
   * @param hours  amount of hours to add to this period, which must be zero if unsupported
   * @param minutes  amount of minutes to add to this period, which must be zero if unsupported
   * @param seconds  amount of seconds to add to this period, which must be zero if unsupported
   * @param millis  amount of milliseconds to add to this period, which must be zero if unsupported
   * @throws IllegalArgumentException if the period being added contains a field
   *                                  not supported by this period
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int) {
    setPeriod(FieldUtils.safeAdd(getYears, years), FieldUtils.safeAdd(getMonths, months), FieldUtils.safeAdd(getWeeks, weeks), FieldUtils.safeAdd(getDays, days), FieldUtils.safeAdd(getHours, hours), FieldUtils.safeAdd(getMinutes, minutes), FieldUtils.safeAdd(getSeconds, seconds), FieldUtils.safeAdd(getMillis, millis))
  }

  /**
   * Adds an interval to this one by dividing the interval into
   * fields and calling {@link #add(ReadablePeriod)}.
   *
   * @param interval  the interval to add, null means add nothing
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(interval: ReadableInterval) {
    if (interval != null) {
      add(interval.toPeriod(getPeriodType))
    }
  }

  /**
   * Adds a duration to this one by dividing the duration into
   * fields and calling {@link #add(ReadablePeriod)}.
   *
   * @param duration  the duration to add, null means add nothing
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(duration: ReadableDuration) {
    if (duration != null) {
      add(new Period(duration.getMillis, getPeriodType))
    }
  }

  /**
   * Adds a millisecond duration to this one by dividing the duration into
   * fields and calling {@link #add(ReadablePeriod)}.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration, in milliseconds
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(duration: Long) {
    add(new Period(duration, getPeriodType))
  }

  /**
   * Adds a millisecond duration to this one by dividing the duration into
   * fields and calling {@link #add(ReadablePeriod)}.
   * <p>
   * When dividing the duration, only precise fields in the period type will be used.
   * For large durations, all the remaining duration will be stored in the largest
   * available precise field.
   *
   * @param duration  the duration, in milliseconds
   * @param chrono  the chronology to use, null means ISO default
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(duration: Long, chrono: Chronology) {
    add(new Period(duration, getPeriodType, chrono))
  }

  /**
   * Merges all the fields from the specified period into this one.
   * <p>
   * Fields that are not present in the specified period are left unaltered.
   *
   * @param period  the period to set, null ignored
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  override def mergePeriod(period: ReadablePeriod) {
    super.mergePeriod(period)
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
   * Sets the number of years of the period.
   *
   * @param years  the number of years
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setYears(years: Int) {
    super.setField(DurationFieldType.years, years)
  }

  /**
   * Adds the specified years to the number of years in the period.
   *
   * @param years  the number of years
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addYears(years: Int) {
    super.addField(DurationFieldType.years, years)
  }

  /**
   * Sets the number of months of the period.
   *
   * @param months  the number of months
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMonths(months: Int) {
    super.setField(DurationFieldType.months, months)
  }

  /**
   * Adds the specified months to the number of months in the period.
   *
   * @param months  the number of months
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMonths(months: Int) {
    super.addField(DurationFieldType.months, months)
  }

  /**
   * Sets the number of weeks of the period.
   *
   * @param weeks  the number of weeks
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setWeeks(weeks: Int) {
    super.setField(DurationFieldType.weeks, weeks)
  }

  /**
   * Adds the specified weeks to the number of weeks in the period.
   *
   * @param weeks  the number of weeks
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addWeeks(weeks: Int) {
    super.addField(DurationFieldType.weeks, weeks)
  }

  /**
   * Sets the number of days of the period.
   *
   * @param days  the number of days
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setDays(days: Int) {
    super.setField(DurationFieldType.days, days)
  }

  /**
   * Adds the specified days to the number of days in the period.
   *
   * @param days  the number of days
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addDays(days: Int) {
    super.addField(DurationFieldType.days, days)
  }

  /**
   * Sets the number of hours of the period.
   *
   * @param hours  the number of hours
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setHours(hours: Int) {
    super.setField(DurationFieldType.hours, hours)
  }

  /**
   * Adds the specified hours to the number of hours in the period.
   *
   * @param hours  the number of hours
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addHours(hours: Int) {
    super.addField(DurationFieldType.hours, hours)
  }

  /**
   * Sets the number of minutes of the period.
   *
   * @param minutes  the number of minutes
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMinutes(minutes: Int) {
    super.setField(DurationFieldType.minutes, minutes)
  }

  /**
   * Adds the specified minutes to the number of minutes in the period.
   *
   * @param minutes  the number of minutes
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMinutes(minutes: Int) {
    super.addField(DurationFieldType.minutes, minutes)
  }

  /**
   * Sets the number of seconds of the period.
   *
   * @param seconds  the number of seconds
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setSeconds(seconds: Int) {
    super.setField(DurationFieldType.seconds, seconds)
  }

  /**
   * Adds the specified seconds to the number of seconds in the period.
   *
   * @param seconds  the number of seconds
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addSeconds(seconds: Int) {
    super.addField(DurationFieldType.seconds, seconds)
  }

  /**
   * Sets the number of millis of the period.
   *
   * @param millis  the number of millis
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMillis(millis: Int) {
    super.setField(DurationFieldType.millis, millis)
  }

  /**
   * Adds the specified millis to the number of millis in the period.
   *
   * @param millis  the number of millis
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMillis(millis: Int) {
    super.addField(DurationFieldType.millis, millis)
  }

  /**
   * Clone this object without having to cast the returned object.
   *
   * @return a clone of the this object.
   */
  def copy: MutablePeriod = {
    return clone.asInstanceOf[MutablePeriod]
  }

  /**
   * Clone this object.
   *
   * @return a clone of this object.
   */
  override def clone: AnyRef = {
    try {
      return super.clone
    }
    catch {
      case ex: CloneNotSupportedException => {
        throw new InternalError("Clone error")
      }
    }
  }
}
/*
 *  Copyright 2001-2013 Stephen Colebourne
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
package org.joda.time.chrono

import java.io.Serializable
import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.IllegalFieldValueException
import org.joda.time.ReadablePartial
import org.joda.time.ReadablePeriod
import org.joda.time.field.FieldUtils
import org.joda.time.field.UnsupportedDateTimeField
import org.joda.time.field.UnsupportedDurationField

/**
 * BaseChronology provides a skeleton implementation for chronology
 * classes. Many utility methods are defined, but all fields are unsupported.
 * <p>
 * BaseChronology is thread-safe and immutable, and all subclasses must be
 * as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
abstract class BaseChronology() extends Chronology  {
  /**
   * Restricted constructor.
   */
//  protected def this() {
//    this()
//    `super`
//  }
//
  /**
   * Returns the DateTimeZone that this Chronology operates in, or null if
   * unspecified.
   *
   * @return DateTimeZone null if unspecified
   */
  def getZone: DateTimeZone

  /**
   * Returns an instance of this Chronology that operates in the UTC time
   * zone. Chronologies that do not operate in a time zone or are already
   * UTC must return themself.
   *
   * @return a version of this chronology that ignores time zones
   */
  def withUTC: Chronology

  /**
   * Returns an instance of this Chronology that operates in any time zone.
   *
   * @return a version of this chronology with a specific time zone
   * @param zone to use, or default if null
   * @see org.joda.time.chrono.ZonedChronology
   */
  def withZone(zone: DateTimeZone): Chronology

  /**
   * Returns a datetime millisecond instant, formed from the given year,
   * month, day, and millisecond values. The set of given values must refer
   * to a valid datetime, or else an IllegalArgumentException is thrown.
   * <p>
   * The default implementation calls upon separate DateTimeFields to
   * determine the result. Subclasses are encouraged to provide a more
   * efficient implementation.
   *
   * @param year year to use
   * @param monthOfYear month to use
   * @param dayOfMonth day of month to use
   * @param millisOfDay millisecond to use
   * @return millisecond instant from 1970-01-01T00:00:00Z
   */
  @throws(classOf[IllegalArgumentException])
  def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    var instant: Long = year.set(0, year)
    instant = monthOfYear.set(instant, monthOfYear)
    instant = dayOfMonth.set(instant, dayOfMonth)
    return millisOfDay.set(instant, millisOfDay)
  }

  /**
   * Returns a datetime millisecond instant, formed from the given year,
   * month, day, hour, minute, second, and millisecond values. The set of
   * given values must refer to a valid datetime, or else an
   * IllegalArgumentException is thrown.
   * <p>
   * The default implementation calls upon separate DateTimeFields to
   * determine the result. Subclasses are encouraged to provide a more
   * efficient implementation.
   *
   * @param year year to use
   * @param monthOfYear month to use
   * @param dayOfMonth day of month to use
   * @param hourOfDay hour to use
   * @param minuteOfHour minute to use
   * @param secondOfMinute second to use
   * @param millisOfSecond millisecond to use
   * @return millisecond instant from 1970-01-01T00:00:00Z
   */
  @throws(classOf[IllegalArgumentException])
  def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    var instant: Long = year.set(0, year)
    instant = monthOfYear.set(instant, monthOfYear)
    instant = dayOfMonth.set(instant, dayOfMonth)
    instant = hourOfDay.set(instant, hourOfDay)
    instant = minuteOfHour.set(instant, minuteOfHour)
    instant = secondOfMinute.set(instant, secondOfMinute)
    return millisOfSecond.set(instant, millisOfSecond)
  }

  /**
   * Returns a datetime millisecond instant, from from the given instant,
   * hour, minute, second, and millisecond values. The set of given values
   * must refer to a valid datetime, or else an IllegalArgumentException is
   * thrown.
   * <p>
   * The default implementation calls upon separate DateTimeFields to
   * determine the result. Subclasses are encouraged to provide a more
   * efficient implementation.
   *
   * @param instant instant to start from
   * @param hourOfDay hour to use
   * @param minuteOfHour minute to use
   * @param secondOfMinute second to use
   * @param millisOfSecond millisecond to use
   * @return millisecond instant from 1970-01-01T00:00:00Z
   */
  @throws(classOf[IllegalArgumentException])
  def getDateTimeMillis(instant: Long, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    instant = hourOfDay.set(instant, hourOfDay)
    instant = minuteOfHour.set(instant, minuteOfHour)
    instant = secondOfMinute.set(instant, secondOfMinute)
    return millisOfSecond.set(instant, millisOfSecond)
  }

  /**
   * Validates whether the fields stored in a partial instant are valid.
   * <p>
   * This implementation uses {@link DateTimeField#getMinimumValue(ReadablePartial, int[])}
   * and {@link DateTimeField#getMaximumValue(ReadablePartial, int[])}.
   *
   * @param partial  the partial instant to validate
   * @param values  the values to validate, not null unless the partial is empty
   * @throws IllegalArgumentException if the instant is invalid
   */
  def validate(partial: ReadablePartial, values: Array[Int]) {
    val size: Int = partial.size
    {
      var i: Int = 0
      while (i < size) {
        {
          val value: Int = values(i)
          val field: DateTimeField = partial.getField(i)
          if (value < field.getMinimumValue) {
            throw new IllegalFieldValueException(field.getType, Integer.valueOf(value), Integer.valueOf(field.getMinimumValue), null)
          }
          if (value > field.getMaximumValue) {
            throw new IllegalFieldValueException(field.getType, Integer.valueOf(value), null, Integer.valueOf(field.getMaximumValue))
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    {
      var i: Int = 0
      while (i < size) {
        {
          val value: Int = values(i)
          val field: DateTimeField = partial.getField(i)
          if (value < field.getMinimumValue(partial, values)) {
            throw new IllegalFieldValueException(field.getType, Integer.valueOf(value), Integer.valueOf(field.getMinimumValue(partial, values)), null)
          }
          if (value > field.getMaximumValue(partial, values)) {
            throw new IllegalFieldValueException(field.getType, Integer.valueOf(value), null, Integer.valueOf(field.getMaximumValue(partial, values)))
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }

  /**
   * Gets the values of a partial from an instant.
   *
   * @param partial  the partial instant to use
   * @param instant  the instant to query
   * @return the values of the partial extracted from the instant
   */
  def get(partial: ReadablePartial, instant: Long): Array[Int] = {
    val size: Int = partial.size
    val values: Array[Int] = new Array[Int](size)
    {
      var i: Int = 0
      while (i < size) {
        {
          values(i) = partial.getFieldType(i).getField(this).get(instant)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return values
  }

  /**
   * Sets the partial into the instant.
   *
   * @param partial  the partial instant to use
   * @param instant  the instant to update
   * @return the updated instant
   */
  def set(partial: ReadablePartial, instant: Long): Long = {
    {
      var i: Int = 0
      val isize: Int = partial.size
      while (i < isize) {
        {
          instant = partial.getFieldType(i).getField(this).set(instant, partial.getValue(i))
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return instant
  }

  /**
   * Gets the values of a period from an interval.
   *
   * @param period  the period instant to use
   * @param startInstant  the start instant of an interval to query
   * @param endInstant  the start instant of an interval to query
   * @return the values of the period extracted from the interval
   */
  def get(period: ReadablePeriod, startInstant: Long, endInstant: Long): Array[Int] = {
    val size: Int = period.size
    val values: Array[Int] = new Array[Int](size)
    if (startInstant != endInstant) {
      {
        var i: Int = 0
        while (i < size) {
          {
            val field: DurationField = period.getFieldType(i).getField(this)
            val value: Int = field.getDifference(endInstant, startInstant)
            if (value != 0) {
              startInstant = field.add(startInstant, value)
            }
            values(i) = value
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    return values
  }

  /**
   * Gets the values of a period from an interval.
   *
   * @param period  the period instant to use
   * @param duration  the duration to query
   * @return the values of the period extracted from the duration
   */
  def get(period: ReadablePeriod, duration: Long): Array[Int] = {
    val size: Int = period.size
    val values: Array[Int] = new Array[Int](size)
    if (duration != 0) {
      var current: Long = 0
      {
        var i: Int = 0
        while (i < size) {
          {
            val field: DurationField = period.getFieldType(i).getField(this)
            if (field.isPrecise) {
              val value: Int = field.getDifference(duration, current)
              current = field.add(current, value)
              values(i) = value
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    return values
  }

  /**
   * Adds the period to the instant, specifying the number of times to add.
   *
   * @param period  the period to add, null means add nothing
   * @param instant  the instant to add to
   * @param scalar  the number of times to add
   * @return the updated instant
   */
  def add(period: ReadablePeriod, instant: Long, scalar: Int): Long = {
    if (scalar != 0 && period != null) {
      {
        var i: Int = 0
        val isize: Int = period.size
        while (i < isize) {
          {
            val value: Long = period.getValue(i)
            if (value != 0) {
              instant = period.getFieldType(i).getField(this).add(instant, value * scalar)
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
    }
    return instant
  }

  /**
   * Adds the duration to the instant, specifying the number of times to add.
   *
   * @param instant  the instant to add to
   * @param duration  the duration to add
   * @param scalar  the number of times to add
   * @return the updated instant
   */
  def add(instant: Long, duration: Long, scalar: Int): Long = {
    if (duration == 0 || scalar == 0) {
      return instant
    }
    val add: Long = FieldUtils.safeMultiply(duration, scalar)
    return FieldUtils.safeAdd(instant, add)
  }

  /**
   * Get the millis duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def millis: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.millis)
  }

  /**
   * Get the millis of second field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def millisOfSecond: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.millisOfSecond, millis)
  }

  /**
   * Get the millis of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def millisOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.millisOfDay, millis)
  }

  /**
   * Get the seconds duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def seconds: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.seconds)
  }

  /**
   * Get the second of minute field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def secondOfMinute: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.secondOfMinute, seconds)
  }

  /**
   * Get the second of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def secondOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.secondOfDay, seconds)
  }

  /**
   * Get the minutes duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def minutes: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.minutes)
  }

  /**
   * Get the minute of hour field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def minuteOfHour: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.minuteOfHour, minutes)
  }

  /**
   * Get the minute of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def minuteOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.minuteOfDay, minutes)
  }

  /**
   * Get the hours duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def hours: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.hours)
  }

  /**
   * Get the hour of day (0-23) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def hourOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.hourOfDay, hours)
  }

  /**
   * Get the hour of day (offset to 1-24) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def clockhourOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.clockhourOfDay, hours)
  }

  /**
   * Get the halfdays duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def halfdays: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.halfdays)
  }

  /**
   * Get the hour of am/pm (0-11) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def hourOfHalfday: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.hourOfHalfday, hours)
  }

  /**
   * Get the hour of am/pm (offset to 1-12) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def clockhourOfHalfday: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.clockhourOfHalfday, hours)
  }

  /**
   * Get the AM(0) PM(1) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def halfdayOfDay: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.halfdayOfDay, halfdays)
  }

  /**
   * Get the days duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def days: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.days)
  }

  /**
   * Get the day of week field for this chronology.
   *
   * <p>DayOfWeek values are defined in
   * {@link org.joda.time.DateTimeConstants DateTimeConstants}.
   * They use the ISO definitions, where 1 is Monday and 7 is Sunday.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfWeek: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfWeek, days)
  }

  /**
   * Get the day of month field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfMonth: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfMonth, days)
  }

  /**
   * Get the day of year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfYear: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.dayOfYear, days)
  }

  /**
   * Get the weeks duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def weeks: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.weeks)
  }

  /**
   * Get the week of a week based year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekOfWeekyear: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekOfWeekyear, weeks)
  }

  /**
   * Get the weekyears duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def weekyears: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.weekyears)
  }

  /**
   * Get the year of a week based year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekyear: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekyear, weekyears)
  }

  /**
   * Get the year of a week based year in a century field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekyearOfCentury: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.weekyearOfCentury, weekyears)
  }

  /**
   * Get the months duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def months: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.months)
  }

  /**
   * Get the month of year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def monthOfYear: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.monthOfYear, months)
  }

  /**
   * Get the years duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def years: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.years)
  }

  /**
   * Get the year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def year: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.year, years)
  }

  /**
   * Get the year of era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def yearOfEra: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.yearOfEra, years)
  }

  /**
   * Get the year of century field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def yearOfCentury: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.yearOfCentury, years)
  }

  /**
   * Get the centuries duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def centuries: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.centuries)
  }

  /**
   * Get the century of era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def centuryOfEra: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.centuryOfEra, centuries)
  }

  /**
   * Get the eras duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def eras: DurationField = {
    return UnsupportedDurationField.getInstance(DurationFieldType.eras)
  }

  /**
   * Get the era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def era: DateTimeField = {
    return UnsupportedDateTimeField.getInstance(DateTimeFieldType.era, eras)
  }

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  override def toString: String
}
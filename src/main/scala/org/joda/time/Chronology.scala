/*
 *  Copyright 2001-2005 Stephen Colebourne
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

/**
 * Chronology provides access to the individual date time fields for a
 * chronological calendar system.
 * <p>
 * Various chronologies are supported by subclasses including ISO
 * and GregorianJulian. To construct a Chronology you should use the
 * factory methods on the chronology subclass in the chrono package.
 * <p>
 * For example, to obtain the current time in the coptic calendar system:
 * <pre>
 * DateTime dt = new DateTime(CopticChronology.getInstance());
 * </pre>
 * <p>
 * The provided chronology implementations are:
 * <ul>
 * <li>ISO - The <i>de facto<i> world calendar system, based on the ISO-8601 standard
 * <li>GJ - Historically accurate calendar with Julian followed by Gregorian
 * <li>Gregorian - The Gregorian calendar system used for all time (proleptic)
 * <li>Julian - The Julian calendar system used for all time (proleptic)
 * <li>Buddhist - The Buddhist calendar system which is an offset in years from GJ
 * <li>Coptic - The Coptic calendar system which defines 30 day months
 * <li>Ethiopic - The Ethiopic calendar system which defines 30 day months
 * <li>Islamic - The Islamic, or Hijri, lunar calendar system
 * </ul>
 * Hopefully future releases will contain more chronologies.
 * <p>
 * This class defines a number of fields with names from the ISO8601 standard.
 * It does not 'strongly' define these fields however, thus implementations
 * are free to interpret the field names as they wish.
 * For example, a week could be defined as 10 days and a month as 40 days in a
 * special WeirdChronology implementation. Clearly the GJ and ISO
 * implementations provided use the field names as you would expect.
 *
 * @see org.joda.time.chrono.ISOChronology
 * @see org.joda.time.chrono.GJChronology
 * @see org.joda.time.chrono.GregorianChronology
 * @see org.joda.time.chrono.JulianChronology
 * @see org.joda.time.chrono.CopticChronology
 * @see org.joda.time.chrono.BuddhistChronology
 * @see org.joda.time.chrono.EthiopicChronology
 * @see org.joda.time.chrono.IslamicChronology
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
abstract class Chronology {
  /**
   * Returns the DateTimeZone that this Chronology operates in, or null if
   * unspecified.
   *
   * @return the DateTimeZone, null if unspecified
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
   * @throws IllegalArgumentException if the values are invalid
   */
  def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long

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
   * @throws IllegalArgumentException if the values are invalid
   */
  def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long

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
   * @throws IllegalArgumentException if the values are invalid
   */
  def getDateTimeMillis(instant: Long, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long

  /**
   * Validates whether the values are valid for the fields of a partial instant.
   *
   * @param partial  the partial instant to validate
   * @param values  the values to validate, not null, match fields in partial
   * @throws IllegalArgumentException if the instant is invalid
   */
  def validate(partial: ReadablePartial, values: Array[Int])

  /**
   * Gets the values of a partial from an instant.
   *
   * @param partial  the partial instant to use
   * @param instant  the instant to query
   * @return the values of this partial extracted from the instant
   */
  def get(partial: ReadablePartial, instant: Long): Array[Int]

  /**
   * Sets the partial into the instant.
   *
   * @param partial  the partial instant to use
   * @param instant  the instant to update
   * @return the updated instant
   */
  def set(partial: ReadablePartial, instant: Long): Long

  /**
   * Gets the values of a period from an interval.
   *
   * @param period  the period instant to use
   * @param startInstant  the start instant of an interval to query
   * @param endInstant  the start instant of an interval to query
   * @return the values of the period extracted from the interval
   */
  def get(period: ReadablePeriod, startInstant: Long, endInstant: Long): Array[Int]

  /**
   * Gets the values of a period from an interval.
   *
   * @param period  the period instant to use
   * @param duration  the duration to query
   * @return the values of the period extracted from the duration
   */
  def get(period: ReadablePeriod, duration: Long): Array[Int]

  /**
   * Adds the period to the instant, specifying the number of times to add.
   *
   * @param period  the period to add, null means add nothing
   * @param instant  the instant to add to
   * @param scalar  the number of times to add
   * @return the updated instant
   */
  def add(period: ReadablePeriod, instant: Long, scalar: Int): Long

  /**
   * Adds the duration to the instant, specifying the number of times to add.
   *
   * @param instant  the instant to add to
   * @param duration  the duration to add
   * @param scalar  the number of times to add
   * @return the updated instant
   */
  def add(instant: Long, duration: Long, scalar: Int): Long

  /**
   * Get the millis duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def millis: DurationField

  /**
   * Get the millis of second field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def millisOfSecond: DateTimeField

  /**
   * Get the millis of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def millisOfDay: DateTimeField

  /**
   * Get the seconds duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def seconds: DurationField

  /**
   * Get the second of minute field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def secondOfMinute: DateTimeField

  /**
   * Get the second of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def secondOfDay: DateTimeField

  /**
   * Get the minutes duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def minutes: DurationField

  /**
   * Get the minute of hour field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def minuteOfHour: DateTimeField

  /**
   * Get the minute of day field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def minuteOfDay: DateTimeField

  /**
   * Get the hours duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def hours: DurationField

  /**
   * Get the hour of day (0-23) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def hourOfDay: DateTimeField

  /**
   * Get the hour of day (offset to 1-24) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def clockhourOfDay: DateTimeField

  /**
   * Get the halfdays duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def halfdays: DurationField

  /**
   * Get the hour of am/pm (0-11) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def hourOfHalfday: DateTimeField

  /**
   * Get the hour of am/pm (offset to 1-12) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def clockhourOfHalfday: DateTimeField

  /**
   * Get the AM(0) PM(1) field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def halfdayOfDay: DateTimeField

  /**
   * Get the days duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def days: DurationField

  /**
   * Get the day of week field for this chronology.
   *
   * <p>DayOfWeek values are defined in {@link DateTimeConstants}.
   * They use the ISO definitions, where 1 is Monday and 7 is Sunday.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfWeek: DateTimeField

  /**
   * Get the day of month field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfMonth: DateTimeField

  /**
   * Get the day of year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def dayOfYear: DateTimeField

  /**
   * Get the weeks duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def weeks: DurationField

  /**
   * Get the week of a week based year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekOfWeekyear: DateTimeField

  /**
   * Get the weekyears duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def weekyears: DurationField

  /**
   * Get the year of a week based year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekyear: DateTimeField

  /**
   * Get the year of a week based year in a century field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def weekyearOfCentury: DateTimeField

  /**
   * Get the months duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def months: DurationField

  /**
   * Get the month of year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def monthOfYear: DateTimeField

  /**
   * Get the years duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def years: DurationField

  /**
   * Get the year field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def year: DateTimeField

  /**
   * Get the year of era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def yearOfEra: DateTimeField

  /**
   * Get the year of century field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def yearOfCentury: DateTimeField

  /**
   * Get the centuries duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def centuries: DurationField

  /**
   * Get the century of era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def centuryOfEra: DateTimeField

  /**
   * Get the eras duration field for this chronology.
   *
   * @return DurationField or UnsupportedDurationField if unsupported
   */
  def eras: DurationField

  /**
   * Get the era field for this chronology.
   *
   * @return DateTimeField or UnsupportedDateTimeField if unsupported
   */
  def era: DateTimeField

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  override def toString: String
}
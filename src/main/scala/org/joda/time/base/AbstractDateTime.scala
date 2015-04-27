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
package org.joda.time.base

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import org.joda.convert.ToString
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.ReadableDateTime
import org.joda.time.format.DateTimeFormat

/**
 * AbstractDateTime provides the common behaviour for datetime classes.
 * <p>
 * This class should generally not be used directly by API users.
 * The {@link ReadableDateTime} interface should be used when different
 * kinds of date/time objects are to be referenced.
 * <p>
 * Whenever you want to implement <code>ReadableDateTime</code> you should
 * extend this class.
 * <p>
 * AbstractDateTime subclasses may be mutable and not thread-safe.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
abstract class AbstractDateTime extends AbstractInstant with ReadableDateTime {
  /**
   * Constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Get the value of one of the fields of a datetime.
   * <p>
   * This method uses the chronology of the datetime to obtain the value.
   * It is essentially a generic way of calling one of the get methods.
   *
   * @param type  a field type, usually obtained from DateTimeFieldType
   * @return the value of that field
   * @throws IllegalArgumentException if the field type is null
   */
  override def get(`type`: DateTimeFieldType): Int = {
    if (`type` == null) {
      throw new IllegalArgumentException("The DateTimeFieldType must not be null")
    }
    return `type`.getField(getChronology).get(getMillis)
  }

  /**
   * Get the era field value.
   *
   * @return the era
   */
  def getEra: Int = {
    return getChronology.era.get(getMillis)
  }

  /**
   * Get the year of era field value.
   *
   * @return the year of era
   */
  def getCenturyOfEra: Int = {
    return getChronology.centuryOfEra.get(getMillis)
  }

  /**
   * Get the year of era field value.
   *
   * @return the year of era
   */
  def getYearOfEra: Int = {
    return getChronology.yearOfEra.get(getMillis)
  }

  /**
   * Get the year of century field value.
   *
   * @return the year of century
   */
  def getYearOfCentury: Int = {
    return getChronology.yearOfCentury.get(getMillis)
  }

  /**
   * Get the year field value.
   *
   * @return the year
   */
  def getYear: Int = {
    return getChronology.year.get(getMillis)
  }

  /**
   * Get the weekyear field value.
   * <p>
   * The weekyear is the year that matches with the weekOfWeekyear field.
   * In the standard ISO8601 week algorithm, the first week of the year
   * is that in which at least 4 days are in the year. As a result of this
   * definition, day 1 of the first week may be in the previous year.
   * The weekyear allows you to query the effective year for that day.
   *
   * @return the year of a week based year
   */
  def getWeekyear: Int = {
    return getChronology.weekyear.get(getMillis)
  }

  /**
   * Get the month of year field value.
   *
   * @return the month of year
   */
  def getMonthOfYear: Int = {
    return getChronology.monthOfYear.get(getMillis)
  }

  /**
   * Get the week of weekyear field value.
   * <p>
   * This field is associated with the "weekyear" via {@link #getWeekyear()}.
   * In the standard ISO8601 week algorithm, the first week of the year
   * is that in which at least 4 days are in the year. As a result of this
   * definition, day 1 of the first week may be in the previous year.
   *
   * @return the week of a week based year
   */
  def getWeekOfWeekyear: Int = {
    return getChronology.weekOfWeekyear.get(getMillis)
  }

  /**
   * Get the day of year field value.
   *
   * @return the day of year
   */
  def getDayOfYear: Int = {
    return getChronology.dayOfYear.get(getMillis)
  }

  /**
   * Get the day of month field value.
   * <p>
   * The values for the day of month are defined in {@link org.joda.time.DateTimeConstants}.
   *
   * @return the day of month
   */
  def getDayOfMonth: Int = {
    return getChronology.dayOfMonth.get(getMillis)
  }

  /**
   * Get the day of week field value.
   * <p>
   * The values for the day of week are defined in {@link org.joda.time.DateTimeConstants}.
   *
   * @return the day of week
   */
  def getDayOfWeek: Int = {
    return getChronology.dayOfWeek.get(getMillis)
  }

  /**
   * Get the hour of day field value.
   *
   * @return the hour of day
   */
  def getHourOfDay: Int = {
    return getChronology.hourOfDay.get(getMillis)
  }

  /**
   * Get the minute of day field value.
   *
   * @return the minute of day
   */
  def getMinuteOfDay: Int = {
    return getChronology.minuteOfDay.get(getMillis)
  }

  /**
   * Get the minute of hour field value.
   *
   * @return the minute of hour
   */
  def getMinuteOfHour: Int = {
    return getChronology.minuteOfHour.get(getMillis)
  }

  /**
   * Get the second of day field value.
   *
   * @return the second of day
   */
  def getSecondOfDay: Int = {
    return getChronology.secondOfDay.get(getMillis)
  }

  /**
   * Get the second of minute field value.
   *
   * @return the second of minute
   */
  def getSecondOfMinute: Int = {
    return getChronology.secondOfMinute.get(getMillis)
  }

  /**
   * Get the millis of day field value.
   *
   * @return the millis of day
   */
  def getMillisOfDay: Int = {
    return getChronology.millisOfDay.get(getMillis)
  }

  /**
   * Get the millis of second field value.
   *
   * @return the millis of second
   */
  def getMillisOfSecond: Int = {
    return getChronology.millisOfSecond.get(getMillis)
  }

  /**
   * Get the date time as a <code>java.util.Calendar</code>, assigning
   * exactly the same millisecond instant.
   * The locale is passed in, enabling Calendar to select the correct
   * localized subclass.
   * <p>
   * The JDK and Joda-Time both have time zone implementations and these
   * differ in accuracy. Joda-Time's implementation is generally more up to
   * date and thus more accurate - for example JDK1.3 has no historical data.
   * The effect of this is that the field values of the <code>Calendar</code>
   * may differ from those of this object, even though the milliseond value
   * is the same. Most of the time this just means that the JDK field values
   * are wrong, as our time zone information is more up to date.
   *
   * @param locale  the locale to get the Calendar for, or default if null
   * @return a localized Calendar initialised with this datetime
   */
  def toCalendar(locale: Locale): Calendar = {
    if (locale == null) {
      locale = Locale.getDefault
    }
    val zone: DateTimeZone = getZone
    val cal: Calendar = Calendar.getInstance(zone.toTimeZone, locale)
    cal.setTime(toDate)
    return cal
  }

  /**
   * Get the date time as a <code>java.util.GregorianCalendar</code>,
   * assigning exactly the same millisecond instant.
   * <p>
   * The JDK and Joda-Time both have time zone implementations and these
   * differ in accuracy. Joda-Time's implementation is generally more up to
   * date and thus more accurate - for example JDK1.3 has no historical data.
   * The effect of this is that the field values of the <code>Calendar</code>
   * may differ from those of this object, even though the milliseond value
   * is the same. Most of the time this just means that the JDK field values
   * are wrong, as our time zone information is more up to date.
   *
   * @return a GregorianCalendar initialised with this datetime
   */
  def toGregorianCalendar: GregorianCalendar = {
    val zone: DateTimeZone = getZone
    val cal: GregorianCalendar = new GregorianCalendar(zone.toTimeZone)
    cal.setTime(toDate)
    return cal
  }

  /**
   * Output the date time in ISO8601 format (yyyy-MM-ddTHH:mm:ss.SSSZZ).
   * <p>
   * Note that this method does not output the chronology or time-zone.
   * This can be confusing, as the equals and hashCode methods use both
   * chronology and time-zone. If two objects are not {@code equal} but have the
   * same {@code toString} then either the chronology or time-zone differs.
   *
   * @return ISO8601 time formatted string, not null
   */
  /*@ToString*/ override def toString: String = {
    return super.toString
  }

  /**
   * Output the instant using the specified format pattern.
   *
   * @param pattern  the pattern specification, null means use <code>toString</code>
   * @return the formatted string, not null
   * @see  org.joda.time.format.DateTimeFormat
   */
  def toString(pattern: String): String = {
    if (pattern == null) {
      return toString
    }
    return DateTimeFormat.forPattern(pattern).print(this)
  }

  /**
   * Output the instant using the specified format pattern.
   *
   * @param pattern  the pattern specification, null means use <code>toString</code>
   * @param locale  Locale to use, null means default
   * @return the formatted string, not null
   * @see  org.joda.time.format.DateTimeFormat
   */
  @throws(classOf[IllegalArgumentException])
  def toString(pattern: String, locale: Locale): String = {
    if (pattern == null) {
      return toString
    }
    return DateTimeFormat.forPattern(pattern).withLocale(locale).print(this)
  }
}
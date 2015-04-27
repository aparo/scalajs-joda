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

import java.util.Locale

/**
 * Defines an instant in time that can be queried using datetime fields.
 * <p>
 * The implementation of this interface may be mutable or immutable.
 * This interface only gives access to retrieve data, never to change it.
 * <p>
 * Methods in your application should be defined using <code>ReadableDateTime</code>
 * as a parameter if the method only wants to read the datetime, and not perform
 * any advanced manipulations.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
trait ReadableDateTime extends ReadableInstant {
  /**
   * Get the day of week field value.
   * <p>
   * The values for the day of week are defined in {@link DateTimeConstants}.
   *
   * @return the day of week
   */
  def getDayOfWeek: Int

  /**
   * Get the day of month field value.
   *
   * @return the day of month
   */
  def getDayOfMonth: Int

  /**
   * Get the day of year field value.
   *
   * @return the day of year
   */
  def getDayOfYear: Int

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
  def getWeekOfWeekyear: Int

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
  def getWeekyear: Int

  /**
   * Get the month of year field value.
   *
   * @return the month of year
   */
  def getMonthOfYear: Int

  /**
   * Get the year field value.
   *
   * @return the year
   */
  def getYear: Int

  /**
   * Get the year of era field value.
   *
   * @return the year of era
   */
  def getYearOfEra: Int

  /**
   * Get the year of century field value.
   *
   * @return the year of century
   */
  def getYearOfCentury: Int

  /**
   * Get the year of era field value.
   *
   * @return the year of era
   */
  def getCenturyOfEra: Int

  /**
   * Get the era field value.
   *
   * @return the era
   */
  def getEra: Int

  /**
   * Get the millis of second field value.
   *
   * @return the millis of second
   */
  def getMillisOfSecond: Int

  /**
   * Get the millis of day field value.
   *
   * @return the millis of day
   */
  def getMillisOfDay: Int

  /**
   * Get the second of minute field value.
   *
   * @return the second of minute
   */
  def getSecondOfMinute: Int

  /**
   * Get the second of day field value.
   *
   * @return the second of day
   */
  def getSecondOfDay: Int

  /**
   * Get the minute of hour field value.
   *
   * @return the minute of hour
   */
  def getMinuteOfHour: Int

  /**
   * Get the minute of day field value.
   *
   * @return the minute of day
   */
  def getMinuteOfDay: Int

  /**
   * Get the hour of day field value.
   *
   * @return the hour of day
   */
  def getHourOfDay: Int

  /**
   * Get this object as a DateTime.
   * <p>
   * If the implementation of the interface is a DateTime, it is returned directly.
   *
   * @return a DateTime using the same millis
   */
  def toDateTime: DateTime

  /**
   * Get this object as a MutableDateTime, always returning a new instance.
   *
   * @return a MutableDateTime using the same millis
   */
  def toMutableDateTime: MutableDateTime

  /**
   * Output the instant using the specified format pattern.
   *
   * @param pattern  pattern specification
   * @throws IllegalArgumentException  if pattern is invalid
   * @see  org.joda.time.format.DateTimeFormat
   */
  @throws(classOf[IllegalArgumentException])
  def toString(pattern: String): String

  /**
   * Output the instant using the specified format pattern.
   *
   * @param pattern  pattern specification
   * @param locale  Locale to use, or null for default
   * @throws IllegalArgumentException  if pattern is invalid
   * @see  org.joda.time.format.DateTimeFormat
   */
  @throws(classOf[IllegalArgumentException])
  def toString(pattern: String, locale: Locale): String
}
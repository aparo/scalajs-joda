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
 * Defines an instant in time that can be queried and modified using datetime fields.
 * <p>
 * The implementation of this interface will be mutable.
 * It may provide more advanced methods than those in the interface.
 * <p>
 * Methods in your application should be defined using <code>ReadWritableDateTime</code>
 * as a parameter if the method wants to manipulate and change a date in simple ways.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
trait ReadWritableDateTime extends ReadableDateTime with ReadWritableInstant {
  /**
   * Set the year to the specified value.
   *
   * @param year  the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setYear(year: Int)

  /**
   * Add a number of years to the date.
   *
   * @param years  the years to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addYears(years: Int)

  /**
   * Set the weekyear to the specified value.
   *
   * @param weekyear  the weekyear
   * @throws IllegalArgumentException if the value is invalid
   */
  def setWeekyear(weekyear: Int)

  /**
   * Add a number of weekyears to the date.
   *
   * @param weekyears  the weekyears to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addWeekyears(weekyears: Int)

  /**
   * Set the month of the year to the specified value.
   *
   * @param monthOfYear  the month of the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMonthOfYear(monthOfYear: Int)

  /**
   * Add a number of months to the date.
   *
   * @param months  the months to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMonths(months: Int)

  /**
   * Set the week of weekyear to the specified value.
   *
   * @param weekOfWeekyear the week of the weekyear
   * @throws IllegalArgumentException if the value is invalid
   */
  def setWeekOfWeekyear(weekOfWeekyear: Int)

  /**
   * Add a number of weeks to the date.
   *
   * @param weeks  the weeks to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addWeeks(weeks: Int)

  /**
   * Set the day of year to the specified value.
   *
   * @param dayOfYear the day of the year
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfYear(dayOfYear: Int)

  /**
   * Set the day of the month to the specified value.
   *
   * @param dayOfMonth  the day of the month
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfMonth(dayOfMonth: Int)

  /**
   * Set the day of week to the specified value.
   *
   * @param dayOfWeek  the day of the week
   * @throws IllegalArgumentException if the value is invalid
   */
  def setDayOfWeek(dayOfWeek: Int)

  /**
   * Add a number of days to the date.
   *
   * @param days  the days to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addDays(days: Int)

  /**
   * Set the hour of the day to the specified value.
   *
   * @param hourOfDay  the hour of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setHourOfDay(hourOfDay: Int)

  /**
   * Add a number of hours to the date.
   *
   * @param hours  the hours to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addHours(hours: Int)

  /**
   * Set the minute of the day to the specified value.
   *
   * @param minuteOfDay  the minute of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMinuteOfDay(minuteOfDay: Int)

  /**
   * Set the minute of the hour to the specified value.
   *
   * @param minuteOfHour  the minute of hour
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMinuteOfHour(minuteOfHour: Int)

  /**
   * Add a number of minutes to the date.
   *
   * @param minutes  the minutes to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMinutes(minutes: Int)

  /**
   * Set the second of the day to the specified value.
   *
   * @param secondOfDay  the second of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setSecondOfDay(secondOfDay: Int)

  /**
   * Set the second of the minute to the specified value.
   *
   * @param secondOfMinute  the second of minute
   * @throws IllegalArgumentException if the value is invalid
   */
  def setSecondOfMinute(secondOfMinute: Int)

  /**
   * Add a number of seconds to the date.
   *
   * @param seconds  the seconds to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addSeconds(seconds: Int)

  /**
   * Set the millis of the day to the specified value.
   *
   * @param millisOfDay  the millis of day
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMillisOfDay(millisOfDay: Int)

  /**
   * Set the millis of the second to the specified value.
   *
   * @param millisOfSecond  the millis of second
   * @throws IllegalArgumentException if the value is invalid
   */
  def setMillisOfSecond(millisOfSecond: Int)

  /**
   * Add a number of milliseconds to the date. The implementation of this
   * method differs from the {@link #add(long)} method in that a
   * DateTimeField performs the addition.
   *
   * @param millis  the milliseconds to add
   * @throws IllegalArgumentException if the value is invalid
   */
  def addMillis(millis: Int)

  /**
   * Set the date from fields.
   * The time part of this object will be unaffected.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @throws IllegalArgumentException if any value is invalid
   */
  def setDate(year: Int, monthOfYear: Int, dayOfMonth: Int)

  /**
   * Set the time from fields.
   * The date part of this object will be unaffected.
   *
   * @param hour  the hour
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @throws IllegalArgumentException if any value is invalid
   */
  def setTime(hour: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int)

  /**
   * Set the date and time from fields.
   *
   * @param year  the year
   * @param monthOfYear  the month of the year
   * @param dayOfMonth  the day of the month
   * @param hourOfDay  the hour of the day
   * @param minuteOfHour  the minute of the hour
   * @param secondOfMinute  the second of the minute
   * @param millisOfSecond  the millisecond of the second
   * @throws IllegalArgumentException if any value is invalid
   */
  def setDateTime(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int)
}
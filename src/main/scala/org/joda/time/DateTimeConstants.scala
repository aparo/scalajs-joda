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
 * DateTimeConstants is a non-instantiable class of constants used in
 * the date time system. These are the ISO8601 constants, but should be
 * used by all chronologies.
 * <p>
 * DateTimeConstants is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object DateTimeConstants {
  /** Constant (1) representing January, the first month (ISO) */
  val JANUARY: Int = 1
  /** Constant (2) representing February, the second month (ISO) */
  val FEBRUARY: Int = 2
  /** Constant (3) representing March, the third month (ISO) */
  val MARCH: Int = 3
  /** Constant (4) representing April, the fourth month (ISO) */
  val APRIL: Int = 4
  /** Constant (5) representing May, the fifth month (ISO) */
  val MAY: Int = 5
  /** Constant (6) representing June, the sixth month (ISO) */
  val JUNE: Int = 6
  /** Constant (7) representing July, the seventh month (ISO) */
  val JULY: Int = 7
  /** Constant (8) representing August, the eighth month (ISO) */
  val AUGUST: Int = 8
  /** Constant (9) representing September, the nineth month (ISO) */
  val SEPTEMBER: Int = 9
  /** Constant (10) representing October, the tenth month (ISO) */
  val OCTOBER: Int = 10
  /** Constant (11) representing November, the eleventh month (ISO) */
  val NOVEMBER: Int = 11
  /** Constant (12) representing December, the twelfth month (ISO) */
  val DECEMBER: Int = 12
  /** Constant (1) representing Monday, the first day of the week (ISO) */
  val MONDAY: Int = 1
  /** Constant (2) representing Tuesday, the second day of the week (ISO) */
  val TUESDAY: Int = 2
  /** Constant (3) representing Wednesday, the third day of the week (ISO) */
  val WEDNESDAY: Int = 3
  /** Constant (4) representing Thursday, the fourth day of the week (ISO) */
  val THURSDAY: Int = 4
  /** Constant (5) representing Friday, the fifth day of the week (ISO) */
  val FRIDAY: Int = 5
  /** Constant (6) representing Saturday, the sixth day of the week (ISO) */
  val SATURDAY: Int = 6
  /** Constant (7) representing Sunday, the seventh day of the week (ISO) */
  val SUNDAY: Int = 7
  /** Constant (0) representing AM, the morning (from Calendar) */
  val AM: Int = 0
  /** Constant (1) representing PM, the afternoon (from Calendar) */
  val PM: Int = 1
  /** Constant (0) representing BC, years before zero (from Calendar) */
  val BC: Int = 0
  /** Alternative constant (0) representing BCE, Before Common Era (secular) */
  val BCE: Int = 0
  /**
   * Constant (1) representing AD, years after zero (from Calendar).
   * <p>
   * All new chronologies with differrent Era values should try to assign
   * eras as follows. The era that was in force at 1970-01-01 (ISO) is assigned
   * the value 1. Earlier eras are assigned sequentially smaller numbers.
   * Later eras are assigned sequentially greater numbers.
   */
  val AD: Int = 1
  /**
   * Alternative constant (1) representing CE, Common Era (secular).
   * <p>
   * All new chronologies with differrent Era values should try to assign
   * eras as follows. The era that was in force at 1970-01-01 (ISO) is assigned
   * the value 1. Earlier eras are assigned sequentially smaller numbers.
   * Later eras are assigned sequentially greater numbers.
   */
  val CE: Int = 1
  /** Milliseconds in one second (1000) (ISO) */
  val MILLIS_PER_SECOND: Int = 1000
  /** Seconds in one minute (60) (ISO) */
  val SECONDS_PER_MINUTE: Int = 60
  /** Milliseconds in one minute (ISO) */
  val MILLIS_PER_MINUTE: Int = MILLIS_PER_SECOND * SECONDS_PER_MINUTE
  /** Minutes in one hour (ISO) */
  val MINUTES_PER_HOUR: Int = 60
  /** Seconds in one hour (ISO) */
  val SECONDS_PER_HOUR: Int = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
  /** Milliseconds in one hour (ISO) */
  val MILLIS_PER_HOUR: Int = MILLIS_PER_MINUTE * MINUTES_PER_HOUR
  /** Hours in a typical day (24) (ISO). Due to time zone offset changes, the
    * number of hours per day can vary. */
  val HOURS_PER_DAY: Int = 24
  /** Minutes in a typical day (ISO). Due to time zone offset changes, the number
    * of minutes per day can vary. */
  val MINUTES_PER_DAY: Int = MINUTES_PER_HOUR * HOURS_PER_DAY
  /** Seconds in a typical day (ISO). Due to time zone offset changes, the number
    * of seconds per day can vary. */
  val SECONDS_PER_DAY: Int = SECONDS_PER_HOUR * HOURS_PER_DAY
  /** Milliseconds in a typical day (ISO). Due to time zone offset changes, the
    * number of milliseconds per day can vary. */
  val MILLIS_PER_DAY: Int = MILLIS_PER_HOUR * HOURS_PER_DAY
  /** Days in one week (7) (ISO) */
  val DAYS_PER_WEEK: Int = 7
  /** Hours in a typical week. Due to time zone offset changes, the number of
    * hours per week can vary. */
  val HOURS_PER_WEEK: Int = HOURS_PER_DAY * DAYS_PER_WEEK
  /** Minutes in a typical week (ISO). Due to time zone offset changes, the number
    * of minutes per week can vary. */
  val MINUTES_PER_WEEK: Int = MINUTES_PER_DAY * DAYS_PER_WEEK
  /** Seconds in a typical week (ISO). Due to time zone offset changes, the number
    * of seconds per week can vary. */
  val SECONDS_PER_WEEK: Int = SECONDS_PER_DAY * DAYS_PER_WEEK
  /** Milliseconds in a typical week (ISO). Due to time zone offset changes, the
    * number of milliseconds per week can vary. */
  val MILLIS_PER_WEEK: Int = MILLIS_PER_DAY * DAYS_PER_WEEK
}

class DateTimeConstants {
  /**
   * Restrictive constructor
   */
  protected def this() {
    this()
  }
}
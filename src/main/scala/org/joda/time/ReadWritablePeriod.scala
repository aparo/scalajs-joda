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
 * Defines a duration of time that can be queried and modified using datetime fields.
 * <p>
 * The implementation of this interface will be mutable.
 * It may provide more advanced methods than those in the interface.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
trait ReadWritablePeriod extends ReadablePeriod {
  /**
   * Clears the period, setting all values back to zero.
   */
  def clear

  /**
   * Sets the value of one of the fields by index.
   *
   * @param index  the field index
   * @param value  the new value for the field
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def setValue(index: Int, value: Int)

  /**
   * Sets the value of one of the fields.
   * <p>
   * The field type specified must be one of those that is supported by the period.
   *
   * @param field  a DurationFieldType instance that is supported by this period
   * @param value  the new value for the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def set(field: DurationFieldType, value: Int)

  /**
   * Sets all the fields in one go from another ReadablePeriod.
   *
   * @param period  the period to set, null means zero length period
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  def setPeriod(period: ReadablePeriod)

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
  def setPeriod(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int)

  /**
   * Sets all the fields in one go from an interval dividing the
   * fields using the period type.
   *
   * @param interval  the interval to set, null means zero length
   */
  def setPeriod(interval: ReadableInterval)

  /**
   * Adds to the value of one of the fields.
   * <p>
   * The field type specified must be one of those that is supported by the period.
   *
   * @param field  a DurationFieldType instance that is supported by this period
   * @param value  the value to add to the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def add(field: DurationFieldType, value: Int)

  /**
   * Adds a period to this one by adding each field in turn.
   *
   * @param period  the period to add, null means add nothing
   * @throws IllegalArgumentException if the period being added contains a field
   *                                  not supported by this period
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(period: ReadablePeriod)

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
  def add(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int)

  /**
   * Adds an interval to this one by dividing the interval into
   * fields and then adding each field in turn.
   *
   * @param interval  the interval to add, null means add nothing
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def add(interval: ReadableInterval)

  /**
   * Sets the number of years of the period.
   *
   * @param years  the number of years
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setYears(years: Int)

  /**
   * Adds the specified years to the number of years in the period.
   *
   * @param years  the number of years
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addYears(years: Int)

  /**
   * Sets the number of months of the period.
   *
   * @param months  the number of months
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMonths(months: Int)

  /**
   * Adds the specified months to the number of months in the period.
   *
   * @param months  the number of months
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMonths(months: Int)

  /**
   * Sets the number of weeks of the period.
   *
   * @param weeks  the number of weeks
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setWeeks(weeks: Int)

  /**
   * Adds the specified weeks to the number of weeks in the period.
   *
   * @param weeks  the number of weeks
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addWeeks(weeks: Int)

  /**
   * Sets the number of days of the period.
   *
   * @param days  the number of days
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setDays(days: Int)

  /**
   * Adds the specified days to the number of days in the period.
   *
   * @param days  the number of days
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addDays(days: Int)

  /**
   * Sets the number of hours of the period.
   *
   * @param hours  the number of hours
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setHours(hours: Int)

  /**
   * Adds the specified hours to the number of hours in the period.
   *
   * @param hours  the number of hours
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addHours(hours: Int)

  /**
   * Sets the number of minutes of the period.
   *
   * @param minutes  the number of minutes
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMinutes(minutes: Int)

  /**
   * Adds the specified minutes to the number of minutes in the period.
   *
   * @param minutes  the number of minutes
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMinutes(minutes: Int)

  /**
   * Sets the number of seconds of the period.
   *
   * @param seconds  the number of seconds
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setSeconds(seconds: Int)

  /**
   * Adds the specified seconds to the number of seconds in the period.
   *
   * @param seconds  the number of seconds
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addSeconds(seconds: Int)

  /**
   * Sets the number of millis of the period.
   *
   * @param millis  the number of millis
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   */
  def setMillis(millis: Int)

  /**
   * Adds the specified millis to the number of millis in the period.
   *
   * @param millis  the number of millis
   * @throws IllegalArgumentException if field is not supported and the value is non-zero
   * @throws ArithmeticException if the addition exceeds the capacity of the period
   */
  def addMillis(millis: Int)
}
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
package org.joda.time

/**
 * Defines the calculation engine for duration fields.
 * The interface defines a set of methods that manipulate a millisecond duration
 * with regards to a single field, such as months or seconds.
 * <p>
 * This design is extensible so, if you wish, you can extract a different field from
 * the millisecond duration. A number of standard implementations are provided to assist.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
abstract class DurationField extends Comparable[DurationField] {
  /**
   * Get the type of the field.
   *
   * @return field type
   */
  def getType: DurationFieldType

  /**
   * Get the name of the field.
   * <p>
   * By convention, names are plural.
   *
   * @return field name
   */
  def getName: String

  /**
   * Returns true if this field is supported.
   *
   * @return true if this field is supported
   */
  def isSupported: Boolean

  /**
   * Is this field precise. A precise field can calculate its value from
   * milliseconds without needing a reference date. Put another way, a
   * precise field's unit size is not variable.
   *
   * @return true if precise
   * @see #getUnitMillis()
   */
  def isPrecise: Boolean

  /**
   * Returns the amount of milliseconds per unit value of this field. For
   * example, if this field represents "seconds", then this returns the
   * milliseconds in one second.
   * <p>
   * For imprecise fields, the unit size is variable, and so this method
   * returns a suitable average value.
   *
   * @return the unit size of this field, in milliseconds
   * @see #isPrecise()
   */
  def getUnitMillis: Long

  /**
   * Get the value of this field from the milliseconds, which is approximate
   * if this field is imprecise.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @return the value of the field, in the units of the field, which may be
   *         negative
   * @throws ArithmeticException if the value is too large for an int
   */
  def getValue(duration: Long): Int

  /**
   * Get the value of this field from the milliseconds, which is approximate
   * if this field is imprecise.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValueAsLong(duration: Long): Long

  /**
   * Get the value of this field from the milliseconds relative to an
   * instant. For precise fields this method produces the same result as for
   * the single argument get method.
   * <p>
   * If the millisecond duration is positive, then the instant is treated as a
   * "start instant". If negative, the instant is treated as an "end instant".
   *
   * @param duration  the milliseconds to query, which may be negative
   * @param instant  the start instant to calculate relative to
   * @return the value of the field, in the units of the field, which may be
   *         negative
   * @throws ArithmeticException if the value is too large for an int
   */
  def getValue(duration: Long, instant: Long): Int

  /**
   * Get the value of this field from the milliseconds relative to an
   * instant. For precise fields this method produces the same result as for
   * the single argument get method.
   * <p>
   * If the millisecond duration is positive, then the instant is treated as a
   * "start instant". If negative, the instant is treated as an "end instant".
   *
   * @param duration  the milliseconds to query, which may be negative
   * @param instant  the start instant to calculate relative to
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValueAsLong(duration: Long, instant: Long): Long

  /**
   * Get the millisecond duration of this field from its value, which is
   * approximate if this field is imprecise.
   *
   * @param value  the value of the field, which may be negative
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Int): Long

  /**
   * Get the millisecond duration of this field from its value, which is
   * approximate if this field is imprecise.
   *
   * @param value  the value of the field, which may be negative
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Long): Long

  /**
   * Get the millisecond duration of this field from its value relative to an
   * instant. For precise fields this method produces the same result as for
   * the single argument getMillis method.
   * <p>
   * If the value is positive, then the instant is treated as a "start
   * instant". If negative, the instant is treated as an "end instant".
   *
   * @param value  the value of the field, which may be negative
   * @param instant  the instant to calculate relative to
   * @return the millisecond duration that the field represents, which may be
   *         negative
   */
  def getMillis(value: Int, instant: Long): Long

  /**
   * Get the millisecond duration of this field from its value relative to an
   * instant. For precise fields this method produces the same result as for
   * the single argument getMillis method.
   * <p>
   * If the value is positive, then the instant is treated as a "start
   * instant". If negative, the instant is treated as an "end instant".
   *
   * @param value  the value of the field, which may be negative
   * @param instant  the instant to calculate relative to
   * @return the millisecond duration that the field represents, which may be
   *         negative
   */
  def getMillis(value: Long, instant: Long): Long

  /**
   * Adds a duration value (which may be negative) to the instant.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to add to
   * @param value  the value to add, in the units of the field
   * @return the updated milliseconds
   */
  def add(instant: Long, value: Int): Long

  /**
   * Adds a duration value (which may be negative) to the instant.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to add to
   * @param value  the value to add, in the units of the field
   * @return the updated milliseconds
   */
  def add(instant: Long, value: Long): Long

  /**
   * Subtracts a duration value (which may be negative) from the instant.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to subtract from
   * @param value  the value to subtract, in the units of the field
   * @return the updated milliseconds
   * @since 1.1
   */
  def subtract(instant: Long, value: Int): Long = {
    if (value == Integer.MIN_VALUE) {
      return subtract(instant, value.toLong)
    }
    return add(instant, -value)
  }

  /**
   * Subtracts a duration value (which may be negative) from the instant.
   *
   * @param instant  the milliseconds from 1970-01-01T00:00:00Z to subtract from
   * @param value  the value to subtract, in the units of the field
   * @return the updated milliseconds
   * @since 1.1
   */
  def subtract(instant: Long, value: Long): Long = {
    if (value == Long.MIN_VALUE) {
      throw new ArithmeticException("Long.MIN_VALUE cannot be negated")
    }
    return add(instant, -value)
  }

  /**
   * Computes the difference between two instants, as measured in the units
   * of this field. Any fractional units are dropped from the result. Calling
   * getDifference reverses the effect of calling add. In the following code:
   *
   * <pre>
   * long instant = ...
   * int v = ...
   * int age = getDifference(add(instant, v), instant);
   * </pre>
   *
   * The value 'age' is the same as the value 'v'.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int

  /**
   * Computes the difference between two instants, as measured in the units
   * of this field. Any fractional units are dropped from the result. Calling
   * getDifference reverses the effect of calling add. In the following code:
   *
   * <pre>
   * long instant = ...
   * long v = ...
   * long age = getDifferenceAsLong(add(instant, v), instant);
   * </pre>
   *
   * The value 'age' is the same as the value 'v'.
   *
   * @param minuendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                       subtract from
   * @param subtrahendInstant the milliseconds from 1970-01-01T00:00:00Z to
   *                          subtract off the minuend
   * @return the difference in the units of this field
   */
  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String
}
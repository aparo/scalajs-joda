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
package org.joda.time.base

import java.io.Serializable
import org.joda.time.Chronology
import org.joda.time.DateTimeUtils
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.MutablePeriod
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.ReadableInstant
import org.joda.time.ReadablePartial
import org.joda.time.ReadablePeriod
import org.joda.time.chrono.ISOChronology
import org.joda.time.field.FieldUtils

/**
 * BaseSingleFieldPeriod is an abstract implementation of ReadablePeriod that
 * manages a single duration field, such as days or minutes.
 * <p>
 * This class should generally not be used directly by API users.
 * The {@link ReadablePeriod} interface should be used when different
 * kinds of period objects are to be referenced.
 * <p>
 * BaseSingleFieldPeriod subclasses may be mutable and not thread-safe.
 *
 * @author Stephen Colebourne
 * @since 1.4
 */
@SerialVersionUID(9386874258972L)
object BaseSingleFieldPeriod {
  /** The start of 1972. */
  private val START_1972: Long = 2L * 365L * 86400L * 1000L

  /**
   * Calculates the number of whole units between the two specified datetimes.
   *
   * @param start  the start instant, validated to not be null
   * @param end  the end instant, validated to not be null
   * @param field  the field type to use, must not be null
   * @return the period
   * @throws IllegalArgumentException if the instants are null or invalid
   */
  protected def between(start: ReadableInstant, end: ReadableInstant, field: DurationFieldType): Int = {
    if (start == null || end == null) {
      throw new IllegalArgumentException("ReadableInstant objects must not be null")
    }
    val chrono: Chronology = DateTimeUtils.getInstantChronology(start)
    val amount: Int = field.getField(chrono).getDifference(end.getMillis, start.getMillis)
    return amount
  }

  /**
   * Calculates the number of whole units between the two specified partial datetimes.
   * <p>
   * The two partials must contain the same fields, for example you can specify
   * two <code>LocalDate</code> objects.
   *
   * @param start  the start partial date, validated to not be null
   * @param end  the end partial date, validated to not be null
   * @param zeroInstance  the zero instance constant, must not be null
   * @return the period
   * @throws IllegalArgumentException if the partials are null or invalid
   */
  protected def between(start: ReadablePartial, end: ReadablePartial, zeroInstance: ReadablePeriod): Int = {
    if (start == null || end == null) {
      throw new IllegalArgumentException("ReadablePartial objects must not be null")
    }
    if (start.size != end.size) {
      throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
    }
    {
      var i: Int = 0
      val isize: Int = start.size
      while (i < isize) {
        {
          if (start.getFieldType(i) ne end.getFieldType(i)) {
            throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    if (DateTimeUtils.isContiguous(start) == false) {
      throw new IllegalArgumentException("ReadablePartial objects must be contiguous")
    }
    val chrono: Chronology = DateTimeUtils.getChronology(start.getChronology).withUTC
    val values: Array[Int] = chrono.get(zeroInstance, chrono.set(start, START_1972), chrono.set(end, START_1972))
    return values(0)
  }

  /**
   * Creates a new instance representing the number of complete standard length units
   * in the specified period.
   * <p>
   * This factory method converts all fields from the period to hours using standardised
   * durations for each field. Only those fields which have a precise duration in
   * the ISO UTC chronology can be converted.
   * <ul>
   * <li>One week consists of 7 days.
   * <li>One day consists of 24 hours.
   * <li>One hour consists of 60 minutes.
   * <li>One minute consists of 60 seconds.
   * <li>One second consists of 1000 milliseconds.
   * </ul>
   * Months and Years are imprecise and periods containing these values cannot be converted.
   *
   * @param period  the period to get the number of hours from, must not be null
   * @param millisPerUnit  the number of milliseconds in one standard unit of this period
   * @throws IllegalArgumentException if the period contains imprecise duration values
   */
  protected def standardPeriodIn(period: ReadablePeriod, millisPerUnit: Long): Int = {
    if (period == null) {
      return 0
    }
    val iso: Chronology = ISOChronology.getInstanceUTC
    var duration: Long = 0L
    {
      var i: Int = 0
      while (i < period.size) {
        {
          val value: Int = period.getValue(i)
          if (value != 0) {
            val field: DurationField = period.getFieldType(i).getField(iso)
            if (field.isPrecise == false) {
              throw new IllegalArgumentException("Cannot convert period to duration as " + field.getName + " is not precise in the period " + period)
            }
            duration = FieldUtils.safeAdd(duration, FieldUtils.safeMultiply(field.getUnitMillis, value))
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return FieldUtils.safeToInt(duration / millisPerUnit)
  }
}

@SerialVersionUID(9386874258972L)
abstract class BaseSingleFieldPeriod extends ReadablePeriod with Comparable[BaseSingleFieldPeriod] with Serializable {
  /** The period in the units of this period. */
  @volatile
  private var iPeriod: Int = 0

  /**
   * Creates a new instance representing the specified period.
   *
   * @param period  the period to represent
   */
  protected def this(period: Int) {
    this()
    `super`
    iPeriod = period
  }

  /**
   * Gets the amount of this period.
   *
   * @return the period value
   */
  protected def getValue: Int = {
    return iPeriod
  }

  /**
   * Sets the amount of this period.
   * To make a subclass immutable you must declare it final, or block this method.
   *
   * @param value  the period value
   */
  protected def setValue(value: Int) {
    iPeriod = value
  }

  /**
   * Gets the single duration field type.
   *
   * @return the duration field type, not null
   */
  def getFieldType: DurationFieldType

  /**
   * Gets the period type which matches the duration field type.
   *
   * @return the period type, not null
   */
  def getPeriodType: PeriodType

  /**
   * Gets the number of fields that this period supports, which is one.
   *
   * @return the number of fields supported, which is one
   */
  def size: Int = {
    return 1
  }

  /**
   * Gets the field type at the specified index.
   * <p>
   * The only index supported by this period is zero which returns the
   * field type of this class.
   *
   * @param index  the index to retrieve, which must be zero
   * @return the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getFieldType(index: Int): DurationFieldType = {
    if (index != 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index))
    }
    return getFieldType
  }

  /**
   * Gets the value at the specified index.
   * <p>
   * The only index supported by this period is zero.
   *
   * @param index  the index to retrieve, which must be zero
   * @return the value of the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getValue(index: Int): Int = {
    if (index != 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index))
    }
    return getValue
  }

  /**
   * Gets the value of a duration field represented by this period.
   * <p>
   * If the field type specified does not match the type used by this class
   * then zero is returned.
   *
   * @param type  the field type to query, null returns zero
   * @return the value of that field, zero if field not supported
   */
  def get(`type`: DurationFieldType): Int = {
    if (`type` eq getFieldType) {
      return getValue
    }
    return 0
  }

  /**
   * Checks whether the duration field specified is supported by this period.
   *
   * @param type  the type to check, may be null which returns false
   * @return true if the field is supported
   */
  def isSupported(`type`: DurationFieldType): Boolean = {
    return (`type` eq getFieldType)
  }

  /**
   * Get this period as an immutable <code>Period</code> object.
   * The period will use <code>PeriodType.standard()</code>.
   *
   * @return a <code>Period</code> representing the same number of days
   */
  def toPeriod: Period = {
    return Period.ZERO.withFields(this)
  }

  /**
   * Get this object as a <code>MutablePeriod</code>.
   * <p>
   * This will always return a new <code>MutablePeriod</code> with the same fields.
   * The period will use <code>PeriodType.standard()</code>.
   *
   * @return a MutablePeriod using the same field set and values
   */
  def toMutablePeriod: MutablePeriod = {
    val period: MutablePeriod = new MutablePeriod
    period.add(this)
    return period
  }

  /**
   * Compares this object with the specified object for equality based on the
   * value of each field. All ReadablePeriod instances are accepted, but only
   * those with a matching <code>PeriodType</code> can return true.
   *
   * @param period  a readable period to check against
   * @return true if all the field values are equal, false if
   *         not or the period is null or of an incorrect type
   */
  override def equals(period: AnyRef): Boolean = {
    if (this eq period) {
      return true
    }
    if (period.isInstanceOf[ReadablePeriod] == false) {
      return false
    }
    val other: ReadablePeriod = period.asInstanceOf[ReadablePeriod]
    return (other.getPeriodType eq getPeriodType && other.getValue(0) == getValue)
  }

  /**
   * Gets a hash code for the period as defined by ReadablePeriod.
   *
   * @return a hash code
   */
  override def hashCode: Int = {
    var total: Int = 17
    total = 27 * total + getValue
    total = 27 * total + getFieldType.hashCode
    return total
  }

  /**
   * Compares this period to another object of the same class.
   *
   * @param other  the other period, must not be null
   * @return zero if equal, positive if greater, negative if less
   * @throws NullPointerException if the other period is null
   * @throws ClassCastException if the other period is of a different type
   */
  def compareTo(other: BaseSingleFieldPeriod): Int = {
    if (other.getClass ne getClass) {
      throw new ClassCastException(getClass + " cannot be compared to " + other.getClass)
    }
    val otherValue: Int = other.getValue
    val thisValue: Int = getValue
    if (thisValue > otherValue) {
      return 1
    }
    if (thisValue < otherValue) {
      return -1
    }
    return 0
  }
}
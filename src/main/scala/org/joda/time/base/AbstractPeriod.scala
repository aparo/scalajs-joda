/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import org.joda.convert.ToString
import org.joda.time.DurationFieldType
import org.joda.time.MutablePeriod
import org.joda.time.Period
import org.joda.time.ReadablePeriod
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter

/**
 * AbstractPeriod provides the common behaviour for period classes.
 * <p>
 * This class should generally not be used directly by API users. The
 * {@link ReadablePeriod} interface should be used when different
 * kinds of periods are to be referenced.
 * <p>
 * AbstractPeriod subclasses may be mutable and not thread-safe.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
abstract class AbstractPeriod extends ReadablePeriod {
  /**
   * Constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the number of fields that this period supports.
   *
   * @return the number of fields supported
   * @since 2.0 (previously on BasePeriod)
   */
  def size: Int = {
    return getPeriodType.size
  }

  /**
   * Gets the field type at the specified index.
   *
   * @param index  the index to retrieve
   * @return the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   * @since 2.0 (previously on BasePeriod)
   */
  def getFieldType(index: Int): DurationFieldType = {
    return getPeriodType.getFieldType(index)
  }

  /**
   * Gets an array of the field types that this period supports.
   * <p>
   * The fields are returned largest to smallest, for example Hours, Minutes, Seconds.
   *
   * @return the fields supported in an array that may be altered, largest to smallest
   */
  def getFieldTypes: Array[DurationFieldType] = {
    val result: Array[DurationFieldType] = new Array[DurationFieldType](size)
    {
      var i: Int = 0
      while (i < result.length) {
        {
          result(i) = getFieldType(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return result
  }

  /**
   * Gets an array of the value of each of the fields that this period supports.
   * <p>
   * The fields are returned largest to smallest, for example Hours, Minutes, Seconds.
   * Each value corresponds to the same array index as <code>getFields()</code>
   *
   * @return the current values of each field in an array that may be altered, largest to smallest
   */
  def getValues: Array[Int] = {
    val result: Array[Int] = new Array[Int](size)
    {
      var i: Int = 0
      while (i < result.length) {
        {
          result(i) = getValue(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return result
  }

  /**
   * Gets the value of one of the fields.
   * <p>
   * If the field type specified is not supported by the period then zero
   * is returned.
   *
   * @param type  the field type to query, null returns zero
   * @return the value of that field, zero if field not supported
   */
  def get(`type`: DurationFieldType): Int = {
    val index: Int = indexOf(`type`)
    if (index == -1) {
      return 0
    }
    return getValue(index)
  }

  /**
   * Checks whether the field specified is supported by this period.
   *
   * @param type  the type to check, may be null which returns false
   * @return true if the field is supported
   */
  def isSupported(`type`: DurationFieldType): Boolean = {
    return getPeriodType.isSupported(`type`)
  }

  /**
   * Gets the index of the field in this period.
   *
   * @param type  the type to check, may be null which returns -1
   * @return the index of -1 if not supported
   */
  def indexOf(`type`: DurationFieldType): Int = {
    return getPeriodType.indexOf(`type`)
  }

  /**
   * Get this period as an immutable <code>Period</code> object.
   *
   * @return a Period using the same field set and values
   */
  def toPeriod: Period = {
    return new Period(this)
  }

  /**
   * Get this object as a <code>MutablePeriod</code>.
   * <p>
   * This will always return a new <code>MutablePeriod</code> with the same fields.
   *
   * @return a MutablePeriod using the same field set and values
   */
  def toMutablePeriod: MutablePeriod = {
    return new MutablePeriod(this)
  }

  /**
   * Compares this object with the specified object for equality based
   * on the value of each field. All ReadablePeriod instances are accepted.
   * <p>
   * Note that a period of 1 day is not equal to a period of 24 hours,
   * nor is 1 hour equal to 60 minutes. Only periods with the same amount
   * in each field are equal.
   * <p>
   * This is because periods represent an abstracted definition of a time
   * period (eg. a day may not actually be 24 hours, it might be 23 or 25
   * at daylight savings boundary).
   * <p>
   * To compare the actual duration of two periods, convert both to
   * {@link org.joda.time.Duration Duration}s, an operation that emphasises
   * that the result may differ according to the date you choose.
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
    if (size != other.size) {
      return false
    }
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (getValue(i) != other.getValue(i) || getFieldType(i) ne other.getFieldType(i)) {
            return false
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return true
  }

  /**
   * Gets a hash code for the period as defined by ReadablePeriod.
   *
   * @return a hash code
   */
  override def hashCode: Int = {
    var total: Int = 17
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          total = 27 * total + getValue(i)
          total = 27 * total + getFieldType(i).hashCode
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return total
  }

  /**
   * Gets the value as a String in the ISO8601 duration format.
   * <p>
   * For example, "PT6H3M7S" represents 6 hours, 3 minutes, 7 seconds.
   * <p>
   * For more control over the output, see
   * {@link org.joda.time.format.PeriodFormatterBuilder PeriodFormatterBuilder}.
   *
   * @return the value as an ISO8601 string
   */
  /*@ToString*/ override def toString: String = {
    return ISOPeriodFormat.standard.print(this)
  }

  /**
   * Uses the specified formatter to convert this period to a String.
   *
   * @param formatter  the formatter to use, null means use <code>toString()</code>.
   * @return the formatted string
   * @since 1.5
   */
  def toString(formatter: PeriodFormatter): String = {
    if (formatter == null) {
      return toString
    }
    return formatter.print(this)
  }
}
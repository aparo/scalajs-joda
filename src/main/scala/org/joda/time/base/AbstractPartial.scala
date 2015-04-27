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
package org.joda.time.base

import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeUtils
import org.joda.time.DurationFieldType
import org.joda.time.ReadableInstant
import org.joda.time.ReadablePartial
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter

/**
 * AbstractPartial provides a standard base implementation of most methods
 * in the ReadablePartial interface.
 * <p>
 * Calculations on are performed using a {@link Chronology}.
 * This chronology is set to be in the UTC time zone for all calculations.
 * <p>
 * The methods on this class use {@link ReadablePartial#size()},
 * {@link AbstractPartial#getField(int, Chronology)} and
 * {@link ReadablePartial#getValue(int)} to calculate their results.
 * Subclasses may have a better implementation.
 * <p>
 * AbstractPartial allows subclasses may be mutable and not thread-safe.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
abstract class AbstractPartial extends ReadablePartial with Comparable[ReadablePartial] {
  /**
   * Constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the field for a specific index in the chronology specified.
   * <p>
   * This method must not use any instance variables.
   *
   * @param index  the index to retrieve
   * @param chrono  the chronology to use
   * @return the field
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  protected def getField(index: Int, chrono: Chronology): DateTimeField

  /**
   * Gets the field type at the specifed index.
   *
   * @param index  the index
   * @return the field type
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getFieldType(index: Int): DateTimeFieldType = {
    return getField(index, getChronology).getType
  }

  /**
   * Gets an array of the field types that this partial supports.
   * <p>
   * The fields are returned largest to smallest, for example Hour, Minute, Second.
   *
   * @return the fields supported in an array that may be altered, largest to smallest
   */
  def getFieldTypes: Array[DateTimeFieldType] = {
    val result: Array[DateTimeFieldType] = new Array[DateTimeFieldType](size)
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
   * Gets the field at the specifed index.
   *
   * @param index  the index
   * @return the field
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getField(index: Int): DateTimeField = {
    return getField(index, getChronology)
  }

  /**
   * Gets an array of the fields that this partial supports.
   * <p>
   * The fields are returned largest to smallest, for example Hour, Minute, Second.
   *
   * @return the fields supported in an array that may be altered, largest to smallest
   */
  def getFields: Array[DateTimeField] = {
    val result: Array[DateTimeField] = new Array[DateTimeField](size)
    {
      var i: Int = 0
      while (i < result.length) {
        {
          result(i) = getField(i)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return result
  }

  /**
   * Gets an array of the value of each of the fields that this partial supports.
   * <p>
   * The fields are returned largest to smallest, for example Hour, Minute, Second.
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
   * Get the value of one of the fields of a datetime.
   * <p>
   * The field specified must be one of those that is supported by the partial.
   *
   * @param type  a DateTimeFieldType instance that is supported by this partial
   * @return the value of that field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  def get(`type`: DateTimeFieldType): Int = {
    return getValue(indexOfSupported(`type`))
  }

  /**
   * Checks whether the field specified is supported by this partial.
   *
   * @param type  the type to check, may be null which returns false
   * @return true if the field is supported
   */
  def isSupported(`type`: DateTimeFieldType): Boolean = {
    return (indexOf(`type`) != -1)
  }

  /**
   * Gets the index of the specified field, or -1 if the field is unsupported.
   *
   * @param type  the type to check, may be null which returns -1
   * @return the index of the field, -1 if unsupported
   */
  def indexOf(`type`: DateTimeFieldType): Int = {
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (getFieldType(i) eq `type`) {
            return i
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return -1
  }

  /**
   * Gets the index of the specified field, throwing an exception if the
   * field is unsupported.
   *
   * @param type  the type to check, not null
   * @return the index of the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  protected def indexOfSupported(`type`: DateTimeFieldType): Int = {
    val index: Int = indexOf(`type`)
    if (index == -1) {
      throw new IllegalArgumentException("Field '" + `type` + "' is not supported")
    }
    return index
  }

  /**
   * Gets the index of the first fields to have the specified duration,
   * or -1 if the field is unsupported.
   *
   * @param type  the type to check, may be null which returns -1
   * @return the index of the field, -1 if unsupported
   */
  protected def indexOf(`type`: DurationFieldType): Int = {
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (getFieldType(i).getDurationType eq `type`) {
            return i
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return -1
  }

  /**
   * Gets the index of the first fields to have the specified duration,
   * throwing an exception if the field is unsupported.
   *
   * @param type  the type to check, not null
   * @return the index of the field
   * @throws IllegalArgumentException if the field is null or not supported
   */
  protected def indexOfSupported(`type`: DurationFieldType): Int = {
    val index: Int = indexOf(`type`)
    if (index == -1) {
      throw new IllegalArgumentException("Field '" + `type` + "' is not supported")
    }
    return index
  }

  /**
   * Resolves this partial against another complete instant to create a new
   * full instant. The combination is performed using the chronology of the
   * specified instant.
   * <p>
   * For example, if this partial represents a time, then the result of this
   * method will be the datetime from the specified base instant plus the
   * time from this partial.
   *
   * @param baseInstant  the instant that provides the missing fields, null means now
   * @return the combined datetime
   */
  def toDateTime(baseInstant: ReadableInstant): DateTime = {
    val chrono: Chronology = DateTimeUtils.getInstantChronology(baseInstant)
    val instantMillis: Long = DateTimeUtils.getInstantMillis(baseInstant)
    val resolved: Long = chrono.set(this, instantMillis)
    return new DateTime(resolved, chrono)
  }

  /**
   * Compares this ReadablePartial with another returning true if the chronology,
   * field types and values are equal.
   *
   * @param partial  an object to check against
   * @return true if fields and values are equal
   */
  override def equals(partial: AnyRef): Boolean = {
    if (this eq partial) {
      return true
    }
    if (partial.isInstanceOf[ReadablePartial] == false) {
      return false
    }
    val other: ReadablePartial = partial.asInstanceOf[ReadablePartial]
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
    return FieldUtils.equals(getChronology, other.getChronology)
  }

  /**
   * Gets a hash code for the ReadablePartial that is compatible with the
   * equals method.
   *
   * @return a suitable hash code
   */
  override def hashCode: Int = {
    var total: Int = 157
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          total = 23 * total + getValue(i)
          total = 23 * total + getFieldType(i).hashCode
        }
        ({
          i += 1; i - 1
        })
      }
    }
    total += getChronology.hashCode
    return total
  }

  /**
   * Compares this partial with another returning an integer
   * indicating the order.
   * <p>
   * The fields are compared in order, from largest to smallest.
   * The first field that is non-equal is used to determine the result.
   * <p>
   * The specified object must be a partial instance whose field types
   * match those of this partial.
   * <p>
   * NOTE: Prior to v2.0, the {@code Comparable} interface was only implemented
   * in this class and not in the {@code ReadablePartial} interface.
   *
   * @param other  an object to check against
   * @return negative if this is less, zero if equal, positive if greater
   * @throws ClassCastException if the partial is the wrong class
   *                            or if it has field types that don't match
   * @throws NullPointerException if the partial is null
   * @since 1.1
   */
  def compareTo(other: ReadablePartial): Int = {
    if (this eq other) {
      return 0
    }
    if (size != other.size) {
      throw new ClassCastException("ReadablePartial objects must have matching field types")
    }
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (getFieldType(i) ne other.getFieldType(i)) {
            throw new ClassCastException("ReadablePartial objects must have matching field types")
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (getValue(i) > other.getValue(i)) {
            return 1
          }
          if (getValue(i) < other.getValue(i)) {
            return -1
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return 0
  }

  /**
   * Is this partial later than the specified partial.
   * <p>
   * The fields are compared in order, from largest to smallest.
   * The first field that is non-equal is used to determine the result.
   * <p>
   * You may not pass null into this method. This is because you need
   * a time zone to accurately determine the current date.
   *
   * @param partial  a partial to check against, must not be null
   * @return true if this date is after the date passed in
   * @throws IllegalArgumentException if the specified partial is null
   * @throws ClassCastException if the partial has field types that don't match
   * @since 1.1
   */
  def isAfter(partial: ReadablePartial): Boolean = {
    if (partial == null) {
      throw new IllegalArgumentException("Partial cannot be null")
    }
    return compareTo(partial) > 0
  }

  /**
   * Is this partial earlier than the specified partial.
   * <p>
   * The fields are compared in order, from largest to smallest.
   * The first field that is non-equal is used to determine the result.
   * <p>
   * You may not pass null into this method. This is because you need
   * a time zone to accurately determine the current date.
   *
   * @param partial  a partial to check against, must not be null
   * @return true if this date is before the date passed in
   * @throws IllegalArgumentException if the specified partial is null
   * @throws ClassCastException if the partial has field types that don't match
   * @since 1.1
   */
  def isBefore(partial: ReadablePartial): Boolean = {
    if (partial == null) {
      throw new IllegalArgumentException("Partial cannot be null")
    }
    return compareTo(partial) < 0
  }

  /**
   * Is this partial the same as the specified partial.
   * <p>
   * The fields are compared in order, from largest to smallest.
   * If all fields are equal, the result is true.
   * <p>
   * You may not pass null into this method. This is because you need
   * a time zone to accurately determine the current date.
   *
   * @param partial  a partial to check against, must not be null
   * @return true if this date is the same as the date passed in
   * @throws IllegalArgumentException if the specified partial is null
   * @throws ClassCastException if the partial has field types that don't match
   * @since 1.1
   */
  def isEqual(partial: ReadablePartial): Boolean = {
    if (partial == null) {
      throw new IllegalArgumentException("Partial cannot be null")
    }
    return compareTo(partial) == 0
  }

  /**
   * Uses the specified formatter to convert this partial to a String.
   *
   * @param formatter  the formatter to use, null means use <code>toString()</code>.
   * @return the formatted string
   * @since 1.1
   */
  def toString(formatter: DateTimeFormatter): String = {
    if (formatter == null) {
      return toString
    }
    return formatter.print(this)
  }
}
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
package org.joda.time.field

import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.ReadablePartial

/**
 * Wraps another field such that zero values are replaced with one more than
 * it's maximum. This is particularly useful for implementing an clockhourOfDay
 * field, where the midnight value of 0 is replaced with 24.
 * <p>
 * ZeroIsMaxDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(961749798233026866L)
final class ZeroIsMaxDateTimeField extends DecoratedDateTimeField {
  /**
   * Constructor.
   *
   * @param field  the base field
   * @param type  the field type this field will actually use
   * @throws IllegalArgumentException if wrapped field's minimum value is not zero
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType) {
    this()
    `super`(field, `type`)
    if (field.getMinimumValue != 0) {
      throw new IllegalArgumentException("Wrapped field's minumum value must be zero")
    }
  }

  override def get(instant: Long): Int = {
    var value: Int = getWrappedField.get(instant)
    if (value == 0) {
      value = getMaximumValue
    }
    return value
  }

  override def add(instant: Long, value: Int): Long = {
    return getWrappedField.add(instant, value)
  }

  override def add(instant: Long, value: Long): Long = {
    return getWrappedField.add(instant, value)
  }

  override def addWrapField(instant: Long, value: Int): Long = {
    return getWrappedField.addWrapField(instant, value)
  }

  override def addWrapField(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    return getWrappedField.addWrapField(instant, fieldIndex, values, valueToAdd)
  }

  override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getWrappedField.getDifference(minuendInstant, subtrahendInstant)
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  override def set(instant: Long, value: Int): Long = {
    val max: Int = getMaximumValue
    FieldUtils.verifyValueBounds(this, value, 1, max)
    if (value == max) {
      value = 0
    }
    return getWrappedField.set(instant, value)
  }

  override def isLeap(instant: Long): Boolean = {
    return getWrappedField.isLeap(instant)
  }

  override def getLeapAmount(instant: Long): Int = {
    return getWrappedField.getLeapAmount(instant)
  }

  override def getLeapDurationField: DurationField = {
    return getWrappedField.getLeapDurationField
  }

  /**
   * Always returns 1.
   *
   * @return the minimum value of 1
   */
  override def getMinimumValue: Int = {
    return 1
  }

  /**
   * Always returns 1.
   *
   * @return the minimum value of 1
   */
  override def getMinimumValue(instant: Long): Int = {
    return 1
  }

  /**
   * Always returns 1.
   *
   * @return the minimum value of 1
   */
  override def getMinimumValue(instant: ReadablePartial): Int = {
    return 1
  }

  /**
   * Always returns 1.
   *
   * @return the minimum value of 1
   */
  override def getMinimumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return 1
  }

  /**
   * Get the maximum value for the field, which is one more than the wrapped
   * field's maximum value.
   *
   * @return the maximum value
   */
  override def getMaximumValue: Int = {
    return getWrappedField.getMaximumValue + 1
  }

  /**
   * Get the maximum value for the field, which is one more than the wrapped
   * field's maximum value.
   *
   * @return the maximum value
   */
  override def getMaximumValue(instant: Long): Int = {
    return getWrappedField.getMaximumValue(instant) + 1
  }

  /**
   * Get the maximum value for the field, which is one more than the wrapped
   * field's maximum value.
   *
   * @return the maximum value
   */
  override def getMaximumValue(instant: ReadablePartial): Int = {
    return getWrappedField.getMaximumValue(instant) + 1
  }

  /**
   * Get the maximum value for the field, which is one more than the wrapped
   * field's maximum value.
   *
   * @return the maximum value
   */
  override def getMaximumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return getWrappedField.getMaximumValue(instant, values) + 1
  }

  override def roundFloor(instant: Long): Long = {
    return getWrappedField.roundFloor(instant)
  }

  override def roundCeiling(instant: Long): Long = {
    return getWrappedField.roundCeiling(instant)
  }

  override def roundHalfFloor(instant: Long): Long = {
    return getWrappedField.roundHalfFloor(instant)
  }

  override def roundHalfCeiling(instant: Long): Long = {
    return getWrappedField.roundHalfCeiling(instant)
  }

  override def roundHalfEven(instant: Long): Long = {
    return getWrappedField.roundHalfEven(instant)
  }

  override def remainder(instant: Long): Long = {
    return getWrappedField.remainder(instant)
  }
}
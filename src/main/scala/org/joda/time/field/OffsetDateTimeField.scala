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

/**
 * Generic offset adjusting datetime field.
 * <p>
 * OffsetDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(3145790132623583142L)
class OffsetDateTimeField extends DecoratedDateTimeField {
  private final val iOffset: Int = 0
  private final val iMin: Int = 0
  private final val iMax: Int = 0

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param offset  offset to add to field values
   * @throws IllegalArgumentException if offset is zero
   */
  def this(field: DateTimeField, offset: Int) {
    this()
    `this`(field, (if (field == null) null else field.getType), offset, Integer.MIN_VALUE, Integer.MAX_VALUE)
  }

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param type  the field type this field actually uses
   * @param offset  offset to add to field values
   * @throws IllegalArgumentException if offset is zero
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType, offset: Int) {
    this()
    `this`(field, `type`, offset, Integer.MIN_VALUE, Integer.MAX_VALUE)
  }

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param type  the field type this field actually uses
   * @param offset  offset to add to field values
   * @param minValue  minimum allowed value
   * @param maxValue  maximum allowed value
   * @throws IllegalArgumentException if offset is zero
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType, offset: Int, minValue: Int, maxValue: Int) {
    this()
    `super`(field, `type`)
    if (offset == 0) {
      throw new IllegalArgumentException("The offset cannot be zero")
    }
    iOffset = offset
    if (minValue < (field.getMinimumValue + offset)) {
      iMin = field.getMinimumValue + offset
    }
    else {
      iMin = minValue
    }
    if (maxValue > (field.getMaximumValue + offset)) {
      iMax = field.getMaximumValue + offset
    }
    else {
      iMax = maxValue
    }
  }

  /**
   * Get the amount of offset units from the specified time instant.
   *
   * @param instant  the time instant in millis to query.
   * @return the amount of units extracted from the input.
   */
  override def get(instant: Long): Int = {
    return super.get(instant) + iOffset
  }

  /**
   * Add the specified amount of offset units to the specified time
   * instant. The amount added may be negative.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of units to add (can be negative).
   * @return the updated time instant.
   */
  override def add(instant: Long, amount: Int): Long = {
    instant = super.add(instant, amount)
    FieldUtils.verifyValueBounds(this, get(instant), iMin, iMax)
    return instant
  }

  /**
   * Add the specified amount of offset units to the specified time
   * instant. The amount added may be negative.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of units to add (can be negative).
   * @return the updated time instant.
   */
  override def add(instant: Long, amount: Long): Long = {
    instant = super.add(instant, amount)
    FieldUtils.verifyValueBounds(this, get(instant), iMin, iMax)
    return instant
  }

  /**
   * Add to the offset component of the specified time instant,
   * wrapping around within that component if necessary.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of units to add (can be negative).
   * @return the updated time instant.
   */
  override def addWrapField(instant: Long, amount: Int): Long = {
    return set(instant, FieldUtils.getWrappedValue(get(instant), amount, iMin, iMax))
  }

  /**
   * Set the specified amount of offset units to the specified time instant.
   *
   * @param instant  the time instant in millis to update.
   * @param value  value of units to set.
   * @return the updated time instant.
   * @throws IllegalArgumentException if value is too large or too small.
   */
  override def set(instant: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, iMin, iMax)
    return super.set(instant, value - iOffset)
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
   * Get the minimum value for the field.
   *
   * @return the minimum value
   */
  override def getMinimumValue: Int = {
    return iMin
  }

  /**
   * Get the maximum value for the field.
   *
   * @return the maximum value
   */
  override def getMaximumValue: Int = {
    return iMax
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

  /**
   * Returns the offset added to the field values.
   *
   * @return the offset
   */
  def getOffset: Int = {
    return iOffset
  }
}
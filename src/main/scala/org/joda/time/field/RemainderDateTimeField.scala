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
 * Counterpart remainder datetime field to {@link DividedDateTimeField}. The
 * field's unit duration is unchanged, but the range duration is scaled
 * accordingly.
 * <p>
 * RemainderDateTimeField is thread-safe and immutable.
 *
 * @see DividedDateTimeField
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(5708241235177666790L)
class RemainderDateTimeField extends DecoratedDateTimeField {
  private[field] final val iDivisor: Int = 0
  private[field] final val iDurationField: DurationField = null
  private[field] final val iRangeField: DurationField = null

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param type  the field type this field actually uses
   * @param divisor  divisor, such as 100 years in a century
   * @throws IllegalArgumentException if divisor is less than two
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType, divisor: Int) {
    this()
    `super`(field, `type`)
    if (divisor < 2) {
      throw new IllegalArgumentException("The divisor must be at least 2")
    }
    val rangeField: DurationField = field.getDurationField
    if (rangeField == null) {
      iRangeField = null
    }
    else {
      iRangeField = new ScaledDurationField(rangeField, `type`.getRangeDurationType, divisor)
    }
    iDurationField = field.getDurationField
    iDivisor = divisor
  }

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param rangeField  the range field
   * @param type  the field type this field actually uses
   * @param divisor  divisor, such as 100 years in a century
   * @throws IllegalArgumentException if divisor is less than two
   */
  def this(field: DateTimeField, rangeField: DurationField, `type`: DateTimeFieldType, divisor: Int) {
    this()
    `super`(field, `type`)
    if (divisor < 2) {
      throw new IllegalArgumentException("The divisor must be at least 2")
    }
    iRangeField = rangeField
    iDurationField = field.getDurationField
    iDivisor = divisor
  }

  /**
   * Construct a RemainderDateTimeField that compliments the given
   * DividedDateTimeField.
   *
   * @param dividedField  complimentary divided field, like "century()".
   */
  def this(dividedField: DividedDateTimeField) {
    this()
    `this`(dividedField, dividedField.getType)
  }

  /**
   * Construct a RemainderDateTimeField that compliments the given
   * DividedDateTimeField.
   *
   * @param dividedField  complimentary divided field, like "century()".
   * @param type  the field type this field actually uses
   */
  def this(dividedField: DividedDateTimeField, `type`: DateTimeFieldType) {
    this()
    `this`(dividedField, dividedField.getWrappedField.getDurationField, `type`)
  }

  /**
   * Construct a RemainderDateTimeField that compliments the given
   * DividedDateTimeField.
   * This constructor allows the duration field to be set.
   *
   * @param dividedField  complimentary divided field, like "century()".
   * @param durationField  the duration field
   * @param type  the field type this field actually uses
   */
  def this(dividedField: DividedDateTimeField, durationField: DurationField, `type`: DateTimeFieldType) {
    this()
    `super`(dividedField.getWrappedField, `type`)
    iDivisor = dividedField.iDivisor
    iDurationField = durationField
    iRangeField = dividedField.iDurationField
  }

  /**
   * Get the remainder from the specified time instant.
   *
   * @param instant  the time instant in millis to query.
   * @return the remainder extracted from the input.
   */
  override def get(instant: Long): Int = {
    val value: Int = getWrappedField.get(instant)
    if (value >= 0) {
      return value % iDivisor
    }
    else {
      return (iDivisor - 1) + ((value + 1) % iDivisor)
    }
  }

  /**
   * Add the specified amount to the specified time instant, wrapping around
   * within the remainder range if necessary. The amount added may be
   * negative.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount to add (can be negative).
   * @return the updated time instant.
   */
  override def addWrapField(instant: Long, amount: Int): Long = {
    return set(instant, FieldUtils.getWrappedValue(get(instant), amount, 0, iDivisor - 1))
  }

  /**
   * Set the specified amount of remainder units to the specified time instant.
   *
   * @param instant  the time instant in millis to update.
   * @param value  value of remainder units to set.
   * @return the updated time instant.
   * @throws IllegalArgumentException if value is too large or too small.
   */
  override def set(instant: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, 0, iDivisor - 1)
    val divided: Int = getDivided(getWrappedField.get(instant))
    return getWrappedField.set(instant, divided * iDivisor + value)
  }

  override def getDurationField: DurationField = {
    return iDurationField
  }

  /**
   * Returns a scaled version of the wrapped field's unit duration field.
   */
  override def getRangeDurationField: DurationField = {
    return iRangeField
  }

  /**
   * Get the minimum value for the field, which is always zero.
   *
   * @return the minimum value of zero.
   */
  override def getMinimumValue: Int = {
    return 0
  }

  /**
   * Get the maximum value for the field, which is always one less than the
   * divisor.
   *
   * @return the maximum value
   */
  override def getMaximumValue: Int = {
    return iDivisor - 1
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
   * Returns the divisor applied, in the field's units.
   *
   * @return the divisor
   */
  def getDivisor: Int = {
    return iDivisor
  }

  private def getDivided(value: Int): Int = {
    if (value >= 0) {
      return value / iDivisor
    }
    else {
      return ((value + 1) / iDivisor) - 1
    }
  }
}
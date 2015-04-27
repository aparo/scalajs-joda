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
 * Divides a DateTimeField such that the retrieved values are reduced by a
 * fixed divisor. The field's unit duration is scaled accordingly, but the
 * range duration is unchanged.
 * <p>
 * DividedDateTimeField is thread-safe and immutable.
 *
 * @see RemainderDateTimeField
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(8318475124230605365L)
class DividedDateTimeField extends DecoratedDateTimeField {
  private[field] final val iDivisor: Int = 0
  private[field] final val iDurationField: DurationField = null
  private[field] final val iRangeDurationField: DurationField = null
  private final val iMin: Int = 0
  private final val iMax: Int = 0

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param type  the field type this field will actually use
   * @param divisor  divisor, such as 100 years in a century
   * @throws IllegalArgumentException if divisor is less than two
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType, divisor: Int) {
    this()
    `this`(field, field.getRangeDurationField, `type`, divisor)
  }

  /**
   * Constructor.
   *
   * @param field  the field to wrap, like "year()".
   * @param rangeField  the range field, null to derive
   * @param type  the field type this field will actually use
   * @param divisor  divisor, such as 100 years in a century
   * @throws IllegalArgumentException if divisor is less than two
   */
  def this(field: DateTimeField, rangeField: DurationField, `type`: DateTimeFieldType, divisor: Int) {
    this()
    `super`(field, `type`)
    if (divisor < 2) {
      throw new IllegalArgumentException("The divisor must be at least 2")
    }
    val unitField: DurationField = field.getDurationField
    if (unitField == null) {
      iDurationField = null
    }
    else {
      iDurationField = new ScaledDurationField(unitField, `type`.getDurationType, divisor)
    }
    iRangeDurationField = rangeField
    iDivisor = divisor
    val i: Int = field.getMinimumValue
    val min: Int = if ((i >= 0)) i / divisor else ((i + 1) / divisor - 1)
    val j: Int = field.getMaximumValue
    val max: Int = if ((j >= 0)) j / divisor else ((j + 1) / divisor - 1)
    iMin = min
    iMax = max
  }

  /**
   * Construct a DividedDateTimeField that compliments the given
   * RemainderDateTimeField.
   *
   * @param remainderField  complimentary remainder field, like "yearOfCentury()".
   * @param type  the field type this field will actually use
   */
  def this(remainderField: RemainderDateTimeField, `type`: DateTimeFieldType) {
    this()
    `this`(remainderField, null, `type`)
  }

  /**
   * Construct a DividedDateTimeField that compliments the given
   * RemainderDateTimeField.
   *
   * @param remainderField  complimentary remainder field, like "yearOfCentury()".
   * @param rangeField  the range field, null to derive
   * @param type  the field type this field will actually use
   */
  def this(remainderField: RemainderDateTimeField, rangeField: DurationField, `type`: DateTimeFieldType) {
    this()
    `super`(remainderField.getWrappedField, `type`)
    val divisor: Int = iDivisor = remainderField.iDivisor
    iDurationField = remainderField.iRangeField
    iRangeDurationField = rangeField
    val field: DateTimeField = getWrappedField
    val i: Int = field.getMinimumValue
    val min: Int = if ((i >= 0)) i / divisor else ((i + 1) / divisor - 1)
    val j: Int = field.getMaximumValue
    val max: Int = if ((j >= 0)) j / divisor else ((j + 1) / divisor - 1)
    iMin = min
    iMax = max
  }

  override def getRangeDurationField: DurationField = {
    if (iRangeDurationField != null) {
      return iRangeDurationField
    }
    return super.getRangeDurationField
  }

  /**
   * Get the amount of scaled units from the specified time instant.
   *
   * @param instant  the time instant in millis to query.
   * @return the amount of scaled units extracted from the input.
   */
  override def get(instant: Long): Int = {
    val value: Int = getWrappedField.get(instant)
    if (value >= 0) {
      return value / iDivisor
    }
    else {
      return ((value + 1) / iDivisor) - 1
    }
  }

  /**
   * Add the specified amount of scaled units to the specified time
   * instant. The amount added may be negative.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of scaled units to add (can be negative).
   * @return the updated time instant.
   */
  override def add(instant: Long, amount: Int): Long = {
    return getWrappedField.add(instant, amount * iDivisor)
  }

  /**
   * Add the specified amount of scaled units to the specified time
   * instant. The amount added may be negative.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of scaled units to add (can be negative).
   * @return the updated time instant.
   */
  override def add(instant: Long, amount: Long): Long = {
    return getWrappedField.add(instant, amount * iDivisor)
  }

  /**
   * Add to the scaled component of the specified time instant,
   * wrapping around within that component if necessary.
   *
   * @param instant  the time instant in millis to update.
   * @param amount  the amount of scaled units to add (can be negative).
   * @return the updated time instant.
   */
  override def addWrapField(instant: Long, amount: Int): Long = {
    return set(instant, FieldUtils.getWrappedValue(get(instant), amount, iMin, iMax))
  }

  override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getWrappedField.getDifference(minuendInstant, subtrahendInstant) / iDivisor
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant) / iDivisor
  }

  /**
   * Set the specified amount of scaled units to the specified time instant.
   *
   * @param instant  the time instant in millis to update.
   * @param value  value of scaled units to set.
   * @return the updated time instant.
   * @throws IllegalArgumentException if value is too large or too small.
   */
  override def set(instant: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, iMin, iMax)
    val remainder: Int = getRemainder(getWrappedField.get(instant))
    return getWrappedField.set(instant, value * iDivisor + remainder)
  }

  /**
   * Returns a scaled version of the wrapped field's unit duration field.
   */
  override def getDurationField: DurationField = {
    return iDurationField
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
    val field: DateTimeField = getWrappedField
    return field.roundFloor(field.set(instant, get(instant) * iDivisor))
  }

  override def remainder(instant: Long): Long = {
    return set(instant, get(getWrappedField.remainder(instant)))
  }

  /**
   * Returns the divisor applied, in the field's units.
   *
   * @return the divisor
   */
  def getDivisor: Int = {
    return iDivisor
  }

  private def getRemainder(value: Int): Int = {
    if (value >= 0) {
      return value % iDivisor
    }
    else {
      return (iDivisor - 1) + ((value + 1) % iDivisor)
    }
  }
}
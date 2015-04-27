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
package org.joda.time.field

import org.joda.time.DurationField
import org.joda.time.DurationFieldType

/**
 * Scales a DurationField such that it's unit millis becomes larger in
 * magnitude.
 * <p>
 * ScaledDurationField is thread-safe and immutable.
 *
 * @see PreciseDurationField
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-3205227092378684157L)
class ScaledDurationField extends DecoratedDurationField {
  private final val iScalar: Int = 0

  /**
   * Constructor
   *
   * @param field  the field to wrap, like "year()".
   * @param type  the type this field will actually use
   * @param scalar  scalar, such as 100 years in a century
   * @throws IllegalArgumentException if scalar is zero or one.
   */
  def this(field: DurationField, `type`: DurationFieldType, scalar: Int) {
    this()
    `super`(field, `type`)
    if (scalar == 0 || scalar == 1) {
      throw new IllegalArgumentException("The scalar must not be 0 or 1")
    }
    iScalar = scalar
  }

  override def getValue(duration: Long): Int = {
    return getWrappedField.getValue(duration) / iScalar
  }

  override def getValueAsLong(duration: Long): Long = {
    return getWrappedField.getValueAsLong(duration) / iScalar
  }

  override def getValue(duration: Long, instant: Long): Int = {
    return getWrappedField.getValue(duration, instant) / iScalar
  }

  override def getValueAsLong(duration: Long, instant: Long): Long = {
    return getWrappedField.getValueAsLong(duration, instant) / iScalar
  }

  override def getMillis(value: Int): Long = {
    val scaled: Long = (value.toLong) * (iScalar.toLong)
    return getWrappedField.getMillis(scaled)
  }

  override def getMillis(value: Long): Long = {
    val scaled: Long = FieldUtils.safeMultiply(value, iScalar)
    return getWrappedField.getMillis(scaled)
  }

  override def getMillis(value: Int, instant: Long): Long = {
    val scaled: Long = (value.toLong) * (iScalar.toLong)
    return getWrappedField.getMillis(scaled, instant)
  }

  override def getMillis(value: Long, instant: Long): Long = {
    val scaled: Long = FieldUtils.safeMultiply(value, iScalar)
    return getWrappedField.getMillis(scaled, instant)
  }

  override def add(instant: Long, value: Int): Long = {
    val scaled: Long = (value.toLong) * (iScalar.toLong)
    return getWrappedField.add(instant, scaled)
  }

  override def add(instant: Long, value: Long): Long = {
    val scaled: Long = FieldUtils.safeMultiply(value, iScalar)
    return getWrappedField.add(instant, scaled)
  }

  override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getWrappedField.getDifference(minuendInstant, subtrahendInstant) / iScalar
  }

  override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant) / iScalar
  }

  override def getUnitMillis: Long = {
    return getWrappedField.getUnitMillis * iScalar
  }

  /**
   * Returns the scalar applied, in the field's units.
   *
   * @return the scalar
   */
  def getScalar: Int = {
    return iScalar
  }

  /**
   * Compares this duration field to another.
   * Two fields are equal if of the same type and duration.
   *
   * @param obj  the object to compare to
   * @return if equal
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    else if (obj.isInstanceOf[ScaledDurationField]) {
      val other: ScaledDurationField = obj.asInstanceOf[ScaledDurationField]
      return ((getWrappedField == other.getWrappedField)) && (getType eq other.getType) && (iScalar == other.iScalar)
    }
    return false
  }

  /**
   * Gets a hash code for this instance.
   *
   * @return a suitable hashcode
   */
  override def hashCode: Int = {
    val scalar: Long = iScalar
    var hash: Int = (scalar ^ (scalar >>> 32)).toInt
    hash += getType.hashCode
    hash += getWrappedField.hashCode
    return hash
  }
}
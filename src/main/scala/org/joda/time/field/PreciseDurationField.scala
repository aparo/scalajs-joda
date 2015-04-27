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

import org.joda.time.DurationFieldType

/**
 * Duration field class representing a field with a fixed unit length.
 * <p>
 * PreciseDurationField is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-8346152187724495365L)
class PreciseDurationField extends BaseDurationField {
  /** The size of the unit */
  private final val iUnitMillis: Long = 0L

  /**
   * Constructor.
   *
   * @param type  the field type
   * @param unitMillis  the unit milliseconds
   */
  def this(`type`: DurationFieldType, unitMillis: Long) {
    this()
    `super`(`type`)
    iUnitMillis = unitMillis
  }

  /**
   * This field is precise.
   *
   * @return true always
   */
  final def isPrecise: Boolean = {
    return true
  }

  /**
   * Returns the amount of milliseconds per unit value of this field.
   *
   * @return the unit size of this field, in milliseconds
   */
  final def getUnitMillis: Long = {
    return iUnitMillis
  }

  /**
   * Get the value of this field from the milliseconds.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @param instant  ignored
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValueAsLong(duration: Long, instant: Long): Long = {
    return duration / iUnitMillis
  }

  /**
   * Get the millisecond duration of this field from its value.
   *
   * @param value  the value of the field, which may be negative
   * @param instant  ignored
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Int, instant: Long): Long = {
    return value * iUnitMillis
  }

  /**
   * Get the millisecond duration of this field from its value.
   *
   * @param value  the value of the field, which may be negative
   * @param instant  ignored
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Long, instant: Long): Long = {
    return FieldUtils.safeMultiply(value, iUnitMillis)
  }

  def add(instant: Long, value: Int): Long = {
    val addition: Long = value * iUnitMillis
    return FieldUtils.safeAdd(instant, addition)
  }

  def add(instant: Long, value: Long): Long = {
    val addition: Long = FieldUtils.safeMultiply(value, iUnitMillis)
    return FieldUtils.safeAdd(instant, addition)
  }

  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    val difference: Long = FieldUtils.safeSubtract(minuendInstant, subtrahendInstant)
    return difference / iUnitMillis
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
    else if (obj.isInstanceOf[PreciseDurationField]) {
      val other: PreciseDurationField = obj.asInstanceOf[PreciseDurationField]
      return (getType eq other.getType) && (iUnitMillis == other.iUnitMillis)
    }
    return false
  }

  /**
   * Gets a hash code for this instance.
   *
   * @return a suitable hashcode
   */
  override def hashCode: Int = {
    val millis: Long = iUnitMillis
    var hash: Int = (millis ^ (millis >>> 32)).toInt
    hash += getType.hashCode
    return hash
  }
}
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
package org.joda.time.field

import java.io.Serializable
import org.joda.time.DurationField
import org.joda.time.DurationFieldType

/**
 * BaseDurationField provides the common behaviour for DurationField
 * implementations.
 * <p>
 * This class should generally not be used directly by API users. The
 * DurationField class should be used when different kinds of DurationField
 * objects are to be referenced.
 * <p>
 * BaseDurationField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @see DecoratedDurationField
 * @since 1.0
 */
@SerialVersionUID(-2554245107589433218L)
abstract class BaseDurationField extends DurationField with Serializable {
  /** A desriptive name for the field. */
  private final val iType: DurationFieldType = null

  protected def this(`type`: DurationFieldType) {
    this()
    `super`
    if (`type` == null) {
      throw new IllegalArgumentException("The type must not be null")
    }
    iType = `type`
  }

  final def getType: DurationFieldType = {
    return iType
  }

  final def getName: String = {
    return iType.getName
  }

  /**
   * @return true always
   */
  final def isSupported: Boolean = {
    return true
  }

  /**
   * Get the value of this field from the milliseconds, which is approximate
   * if this field is imprecise.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValue(duration: Long): Int = {
    return FieldUtils.safeToInt(getValueAsLong(duration))
  }

  /**
   * Get the value of this field from the milliseconds, which is approximate
   * if this field is imprecise.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValueAsLong(duration: Long): Long = {
    return duration / getUnitMillis
  }

  /**
   * Get the value of this field from the milliseconds relative to an
   * instant.
   *
   * <p>If the milliseconds is positive, then the instant is treated as a
   * "start instant". If negative, the instant is treated as an "end
   * instant".
   *
   * <p>The default implementation returns
   * <code>Utils.safeToInt(getAsLong(millisDuration, instant))</code>.
   *
   * @param duration  the milliseconds to query, which may be negative
   * @param instant  the start instant to calculate relative to
   * @return the value of the field, in the units of the field, which may be
   *         negative
   */
  def getValue(duration: Long, instant: Long): Int = {
    return FieldUtils.safeToInt(getValueAsLong(duration, instant))
  }

  /**
   * Get the millisecond duration of this field from its value, which is
   * approximate if this field is imprecise.
   *
   * @param value  the value of the field, which may be negative
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Int): Long = {
    return value * getUnitMillis
  }

  /**
   * Get the millisecond duration of this field from its value, which is
   * approximate if this field is imprecise.
   *
   * @param value  the value of the field, which may be negative
   * @return the milliseconds that the field represents, which may be
   *         negative
   */
  def getMillis(value: Long): Long = {
    return FieldUtils.safeMultiply(value, getUnitMillis)
  }

  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return FieldUtils.safeToInt(getDifferenceAsLong(minuendInstant, subtrahendInstant))
  }

  def compareTo(otherField: DurationField): Int = {
    val otherMillis: Long = otherField.getUnitMillis
    val thisMillis: Long = getUnitMillis
    if (thisMillis == otherMillis) {
      return 0
    }
    if (thisMillis < otherMillis) {
      return -1
    }
    else {
      return 1
    }
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = {
    return "DurationField[" + getName + ']'
  }
}
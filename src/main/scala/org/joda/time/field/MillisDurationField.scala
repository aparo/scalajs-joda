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
 * Duration field class representing a field with a fixed unit length of one
 * millisecond.
 * <p>
 * MillisDurationField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(2656707858124633367L)
object MillisDurationField {
  /** Singleton instance. */
  val INSTANCE: DurationField = new MillisDurationField
}

@SerialVersionUID(2656707858124633367L)
final class MillisDurationField extends DurationField with Serializable {
  /**
   * Restricted constructor.
   */
  private def this() {
    this()
    `super`
  }

  def getType: DurationFieldType = {
    return DurationFieldType.millis
  }

  def getName: String = {
    return "millis"
  }

  /**
   * Returns true as this field is supported.
   *
   * @return true always
   */
  def isSupported: Boolean = {
    return true
  }

  /**
   * Returns true as this field is precise.
   *
   * @return true always
   */
  final def isPrecise: Boolean = {
    return true
  }

  /**
   * Returns the amount of milliseconds per unit value of this field.
   *
   * @return one always
   */
  final def getUnitMillis: Long = {
    return 1
  }

  def getValue(duration: Long): Int = {
    return FieldUtils.safeToInt(duration)
  }

  def getValueAsLong(duration: Long): Long = {
    return duration
  }

  def getValue(duration: Long, instant: Long): Int = {
    return FieldUtils.safeToInt(duration)
  }

  def getValueAsLong(duration: Long, instant: Long): Long = {
    return duration
  }

  def getMillis(value: Int): Long = {
    return value
  }

  def getMillis(value: Long): Long = {
    return value
  }

  def getMillis(value: Int, instant: Long): Long = {
    return value
  }

  def getMillis(value: Long, instant: Long): Long = {
    return value
  }

  def add(instant: Long, value: Int): Long = {
    return FieldUtils.safeAdd(instant, value)
  }

  def add(instant: Long, value: Long): Long = {
    return FieldUtils.safeAdd(instant, value)
  }

  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return FieldUtils.safeToInt(FieldUtils.safeSubtract(minuendInstant, subtrahendInstant))
  }

  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return FieldUtils.safeSubtract(minuendInstant, subtrahendInstant)
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

  override def equals(obj: AnyRef): Boolean = {
    if (obj.isInstanceOf[MillisDurationField]) {
      return getUnitMillis == (obj.asInstanceOf[MillisDurationField]).getUnitMillis
    }
    return false
  }

  override def hashCode: Int = {
    return getUnitMillis.toInt
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = {
    return "DurationField[millis]"
  }

  /**
   * Deserialize to the singleton.
   */
  private def readResolve: AnyRef = {
    return MillisDurationField.INSTANCE
  }
}
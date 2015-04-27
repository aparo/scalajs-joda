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
import java.util.HashMap
import org.joda.time.DurationField
import org.joda.time.DurationFieldType

/**
 * A placeholder implementation to use when a duration field is not supported.
 * <p>
 * UnsupportedDurationField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-6390301302770925357L)
object UnsupportedDurationField {
  /** The cache of unsupported duration field instances */
  private var cCache: HashMap[DurationFieldType, UnsupportedDurationField] = null

  /**
   * Gets an instance of UnsupportedDurationField for a specific named field.
   * The returned instance is cached.
   *
   * @param type  the type to obtain
   * @return the instance
   */
  def getInstance(`type`: DurationFieldType): UnsupportedDurationField = {
    var field: UnsupportedDurationField = null
    if (cCache == null) {
      cCache = new HashMap[DurationFieldType, UnsupportedDurationField](7)
      field = null
    }
    else {
      field = cCache.get(`type`)
    }
    if (field == null) {
      field = new UnsupportedDurationField(`type`)
      cCache.put(`type`, field)
    }
    return field
  }
}

@SerialVersionUID(-6390301302770925357L)
final class UnsupportedDurationField extends DurationField with Serializable {
  /** The name of the field */
  private final val iType: DurationFieldType = null

  /**
   * Constructor.
   *
   * @param type  the type to use
   */
  private def this(`type`: DurationFieldType) {
    this()
    iType = `type`
  }

  final def getType: DurationFieldType = {
    return iType
  }

  def getName: String = {
    return iType.getName
  }

  /**
   * This field is not supported.
   *
   * @return false always
   */
  def isSupported: Boolean = {
    return false
  }

  /**
   * This field is precise.
   *
   * @return true always
   */
  def isPrecise: Boolean = {
    return true
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getValue(duration: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getValueAsLong(duration: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getValue(duration: Long, instant: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getValueAsLong(duration: Long, instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMillis(value: Int): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMillis(value: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMillis(value: Int, instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMillis(value: Long, instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def add(instant: Long, value: Int): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def add(instant: Long, value: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    throw unsupported
  }

  /**
   * Always returns zero.
   *
   * @return zero always
   */
  def getUnitMillis: Long = {
    return 0
  }

  /**
   * Always returns zero, indicating that sort order is not relevent.
   *
   * @return zero always
   */
  def compareTo(durationField: DurationField): Int = {
    return 0
  }

  /**
   * Compares this duration field to another.
   *
   * @param obj  the object to compare to
   * @return true if equal
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    else if (obj.isInstanceOf[UnsupportedDurationField]) {
      val other: UnsupportedDurationField = obj.asInstanceOf[UnsupportedDurationField]
      if (other.getName == null) {
        return (getName == null)
      }
      return ((other.getName == getName))
    }
    return false
  }

  /**
   * Gets a suitable hashcode.
   *
   * @return the hashcode
   */
  override def hashCode: Int = {
    return getName.hashCode
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = {
    return "UnsupportedDurationField[" + getName + ']'
  }

  /**
   * Ensure proper singleton serialization
   */
  private def readResolve: AnyRef = {
    return UnsupportedDurationField.getInstance(iType)
  }

  private def unsupported: UnsupportedOperationException = {
    return new UnsupportedOperationException(iType + " field is unsupported")
  }
}
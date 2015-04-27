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
import java.util.Locale
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.ReadablePartial

/**
 * A placeholder implementation to use when a datetime field is not supported.
 * <p>
 * UnsupportedDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-1934618396111902255L)
object UnsupportedDateTimeField {
  /** The cache of unsupported datetime field instances */
  private var cCache: HashMap[DateTimeFieldType, UnsupportedDateTimeField] = null

  /**
   * Gets an instance of UnsupportedDateTimeField for a specific named field.
   * Names should be of standard format, such as 'monthOfYear' or 'hourOfDay'.
   * The returned instance is cached.
   *
   * @param type  the type to obtain
   * @return the instance
   * @throws IllegalArgumentException if durationField is null
   */
  def getInstance(`type`: DateTimeFieldType, durationField: DurationField): UnsupportedDateTimeField = {
    var field: UnsupportedDateTimeField = null
    if (cCache == null) {
      cCache = new HashMap[DateTimeFieldType, UnsupportedDateTimeField](7)
      field = null
    }
    else {
      field = cCache.get(`type`)
      if (field != null && field.getDurationField ne durationField) {
        field = null
      }
    }
    if (field == null) {
      field = new UnsupportedDateTimeField(`type`, durationField)
      cCache.put(`type`, field)
    }
    return field
  }
}

@SerialVersionUID(-1934618396111902255L)
final class UnsupportedDateTimeField extends DateTimeField with Serializable {
  /** The field type */
  private final val iType: DateTimeFieldType = null
  /** The duration of the datetime field */
  private final val iDurationField: DurationField = null

  /**
   * Constructor.
   *
   * @param type  the field type
   * @param durationField  the duration to use
   */
  private def this(`type`: DateTimeFieldType, durationField: DurationField) {
    this()
    if (`type` == null || durationField == null) {
      throw new IllegalArgumentException
    }
    iType = `type`
    iDurationField = durationField
  }

  def getType: DateTimeFieldType = {
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
   * This field is not lenient.
   *
   * @return false always
   */
  def isLenient: Boolean = {
    return false
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def get(instant: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsText(instant: Long, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsText(instant: Long): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsText(partial: ReadablePartial, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsText(fieldValue: Int, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsShortText(instant: Long, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsShortText(instant: Long): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsShortText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsShortText(partial: ReadablePartial, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getAsShortText(fieldValue: Int, locale: Locale): String = {
    throw unsupported
  }

  /**
   * Delegates to the duration field.
   *
   * @throws UnsupportedOperationException if the duration is unsupported
   */
  def add(instant: Long, value: Int): Long = {
    return getDurationField.add(instant, value)
  }

  /**
   * Delegates to the duration field.
   *
   * @throws UnsupportedOperationException if the duration is unsupported
   */
  def add(instant: Long, value: Long): Long = {
    return getDurationField.add(instant, value)
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def add(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def addWrapPartial(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def addWrapField(instant: Long, value: Int): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def addWrapField(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    throw unsupported
  }

  /**
   * Delegates to the duration field.
   *
   * @throws UnsupportedOperationException if the duration is unsupported
   */
  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return getDurationField.getDifference(minuendInstant, subtrahendInstant)
  }

  /**
   * Delegates to the duration field.
   *
   * @throws UnsupportedOperationException if the duration is unsupported
   */
  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return getDurationField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def set(instant: Long, value: Int): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def set(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], newValue: Int): Array[Int] = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def set(instant: Long, text: String, locale: Locale): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def set(instant: Long, text: String): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def set(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], text: String, locale: Locale): Array[Int] = {
    throw unsupported
  }

  /**
   * Even though this DateTimeField is unsupported, the duration field might
   * be supported.
   *
   * @return a possibly supported DurationField
   */
  def getDurationField: DurationField = {
    return iDurationField
  }

  /**
   * Always returns null.
   *
   * @return null always
   */
  def getRangeDurationField: DurationField = {
    return null
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def isLeap(instant: Long): Boolean = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getLeapAmount(instant: Long): Int = {
    throw unsupported
  }

  /**
   * Always returns null.
   *
   * @return null always
   */
  def getLeapDurationField: DurationField = {
    return null
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMinimumValue: Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMinimumValue(instant: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMinimumValue(instant: ReadablePartial): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMinimumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumValue: Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumValue(instant: Long): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumValue(instant: ReadablePartial): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumTextLength(locale: Locale): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def getMaximumShortTextLength(locale: Locale): Int = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def roundFloor(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def roundCeiling(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def roundHalfFloor(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def roundHalfCeiling(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def roundHalfEven(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Always throws UnsupportedOperationException
   *
   * @throws UnsupportedOperationException
   */
  def remainder(instant: Long): Long = {
    throw unsupported
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = {
    return "UnsupportedDateTimeField"
  }

  /**
   * Ensure proper singleton serialization
   */
  private def readResolve: AnyRef = {
    return UnsupportedDateTimeField.getInstance(iType, iDurationField)
  }

  private def unsupported: UnsupportedOperationException = {
    return new UnsupportedOperationException(iType + " field is unsupported")
  }
}
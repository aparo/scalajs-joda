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

import java.io.Serializable
import java.util.Locale
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DurationField
import org.joda.time.ReadablePartial

/**
 * <code>DelegatedDateTimeField</code> delegates each method call to the
 * date time field it wraps.
 * <p>
 * DelegatedDateTimeField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see DecoratedDateTimeField
 */
@SerialVersionUID(-4730164440214502503L)
class DelegatedDateTimeField extends DateTimeField with Serializable {
  /** The DateTimeField being wrapped. */
  private final val iField: DateTimeField = null
  /** The range duration. */
  private final val iRangeDurationField: DurationField = null
  /** The override field type. */
  private final val iType: DateTimeFieldType = null

  /**
   * Constructor.
   *
   * @param field  the field being decorated
   */
  def this(field: DateTimeField) {
    this()
    `this`(field, null)
  }

  /**
   * Constructor.
   *
   * @param field  the field being decorated
   * @param type  the field type override
   */
  def this(field: DateTimeField, `type`: DateTimeFieldType) {
    this()
    `this`(field, null, `type`)
  }

  /**
   * Constructor.
   *
   * @param field  the field being decorated
   * @param rangeField  the range field, null to derive
   * @param type  the field type override
   */
  def this(field: DateTimeField, rangeField: DurationField, `type`: DateTimeFieldType) {
    this()
    `super`
    if (field == null) {
      throw new IllegalArgumentException("The field must not be null")
    }
    iField = field
    iRangeDurationField = rangeField
    iType = (if (`type` == null) field.getType else `type`)
  }

  /**
   * Gets the wrapped date time field.
   *
   * @return the wrapped DateTimeField
   */
  final def getWrappedField: DateTimeField = {
    return iField
  }

  def getType: DateTimeFieldType = {
    return iType
  }

  def getName: String = {
    return iType.getName
  }

  def isSupported: Boolean = {
    return iField.isSupported
  }

  def isLenient: Boolean = {
    return iField.isLenient
  }

  def get(instant: Long): Int = {
    return iField.get(instant)
  }

  def getAsText(instant: Long, locale: Locale): String = {
    return iField.getAsText(instant, locale)
  }

  def getAsText(instant: Long): String = {
    return iField.getAsText(instant)
  }

  def getAsText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    return iField.getAsText(partial, fieldValue, locale)
  }

  def getAsText(partial: ReadablePartial, locale: Locale): String = {
    return iField.getAsText(partial, locale)
  }

  def getAsText(fieldValue: Int, locale: Locale): String = {
    return iField.getAsText(fieldValue, locale)
  }

  def getAsShortText(instant: Long, locale: Locale): String = {
    return iField.getAsShortText(instant, locale)
  }

  def getAsShortText(instant: Long): String = {
    return iField.getAsShortText(instant)
  }

  def getAsShortText(partial: ReadablePartial, fieldValue: Int, locale: Locale): String = {
    return iField.getAsShortText(partial, fieldValue, locale)
  }

  def getAsShortText(partial: ReadablePartial, locale: Locale): String = {
    return iField.getAsShortText(partial, locale)
  }

  def getAsShortText(fieldValue: Int, locale: Locale): String = {
    return iField.getAsShortText(fieldValue, locale)
  }

  def add(instant: Long, value: Int): Long = {
    return iField.add(instant, value)
  }

  def add(instant: Long, value: Long): Long = {
    return iField.add(instant, value)
  }

  def add(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    return iField.add(instant, fieldIndex, values, valueToAdd)
  }

  def addWrapPartial(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    return iField.addWrapPartial(instant, fieldIndex, values, valueToAdd)
  }

  def addWrapField(instant: Long, value: Int): Long = {
    return iField.addWrapField(instant, value)
  }

  def addWrapField(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
    return iField.addWrapField(instant, fieldIndex, values, valueToAdd)
  }

  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return iField.getDifference(minuendInstant, subtrahendInstant)
  }

  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return iField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  def set(instant: Long, value: Int): Long = {
    return iField.set(instant, value)
  }

  def set(instant: Long, text: String, locale: Locale): Long = {
    return iField.set(instant, text, locale)
  }

  def set(instant: Long, text: String): Long = {
    return iField.set(instant, text)
  }

  def set(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], newValue: Int): Array[Int] = {
    return iField.set(instant, fieldIndex, values, newValue)
  }

  def set(instant: ReadablePartial, fieldIndex: Int, values: Array[Int], text: String, locale: Locale): Array[Int] = {
    return iField.set(instant, fieldIndex, values, text, locale)
  }

  def getDurationField: DurationField = {
    return iField.getDurationField
  }

  def getRangeDurationField: DurationField = {
    if (iRangeDurationField != null) {
      return iRangeDurationField
    }
    return iField.getRangeDurationField
  }

  def isLeap(instant: Long): Boolean = {
    return iField.isLeap(instant)
  }

  def getLeapAmount(instant: Long): Int = {
    return iField.getLeapAmount(instant)
  }

  def getLeapDurationField: DurationField = {
    return iField.getLeapDurationField
  }

  def getMinimumValue: Int = {
    return iField.getMinimumValue
  }

  def getMinimumValue(instant: Long): Int = {
    return iField.getMinimumValue(instant)
  }

  def getMinimumValue(instant: ReadablePartial): Int = {
    return iField.getMinimumValue(instant)
  }

  def getMinimumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return iField.getMinimumValue(instant, values)
  }

  def getMaximumValue: Int = {
    return iField.getMaximumValue
  }

  def getMaximumValue(instant: Long): Int = {
    return iField.getMaximumValue(instant)
  }

  def getMaximumValue(instant: ReadablePartial): Int = {
    return iField.getMaximumValue(instant)
  }

  def getMaximumValue(instant: ReadablePartial, values: Array[Int]): Int = {
    return iField.getMaximumValue(instant, values)
  }

  def getMaximumTextLength(locale: Locale): Int = {
    return iField.getMaximumTextLength(locale)
  }

  def getMaximumShortTextLength(locale: Locale): Int = {
    return iField.getMaximumShortTextLength(locale)
  }

  def roundFloor(instant: Long): Long = {
    return iField.roundFloor(instant)
  }

  def roundCeiling(instant: Long): Long = {
    return iField.roundCeiling(instant)
  }

  def roundHalfFloor(instant: Long): Long = {
    return iField.roundHalfFloor(instant)
  }

  def roundHalfCeiling(instant: Long): Long = {
    return iField.roundHalfCeiling(instant)
  }

  def roundHalfEven(instant: Long): Long = {
    return iField.roundHalfEven(instant)
  }

  def remainder(instant: Long): Long = {
    return iField.remainder(instant)
  }

  override def toString: String = {
    return ("DateTimeField[" + getName + ']')
  }
}
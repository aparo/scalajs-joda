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
 * <code>DelegatedDurationField</code> delegates each method call to the
 * duration field it wraps.
 * <p>
 * DelegatedDurationField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @see DecoratedDurationField
 * @since 1.0
 */
@SerialVersionUID(-5576443481242007829L)
class DelegatedDurationField extends DurationField with Serializable {
  /** The DurationField being wrapped */
  private final val iField: DurationField = null
  /** The field type */
  private final val iType: DurationFieldType = null

  /**
   * Constructor.
   *
   * @param field  the base field
   */
  protected def this(field: DurationField) {
    this()
    `this`(field, null)
  }

  /**
   * Constructor.
   *
   * @param field  the base field
   * @param type  the field type to use
   */
  protected def this(field: DurationField, `type`: DurationFieldType) {
    this()
    `super`
    if (field == null) {
      throw new IllegalArgumentException("The field must not be null")
    }
    iField = field
    iType = (if (`type` == null) field.getType else `type`)
  }

  /**
   * Gets the wrapped duration field.
   *
   * @return the wrapped DurationField
   */
  final def getWrappedField: DurationField = {
    return iField
  }

  def getType: DurationFieldType = {
    return iType
  }

  def getName: String = {
    return iType.getName
  }

  /**
   * Returns true if this field is supported.
   */
  def isSupported: Boolean = {
    return iField.isSupported
  }

  def isPrecise: Boolean = {
    return iField.isPrecise
  }

  def getValue(duration: Long): Int = {
    return iField.getValue(duration)
  }

  def getValueAsLong(duration: Long): Long = {
    return iField.getValueAsLong(duration)
  }

  def getValue(duration: Long, instant: Long): Int = {
    return iField.getValue(duration, instant)
  }

  def getValueAsLong(duration: Long, instant: Long): Long = {
    return iField.getValueAsLong(duration, instant)
  }

  def getMillis(value: Int): Long = {
    return iField.getMillis(value)
  }

  def getMillis(value: Long): Long = {
    return iField.getMillis(value)
  }

  def getMillis(value: Int, instant: Long): Long = {
    return iField.getMillis(value, instant)
  }

  def getMillis(value: Long, instant: Long): Long = {
    return iField.getMillis(value, instant)
  }

  def add(instant: Long, value: Int): Long = {
    return iField.add(instant, value)
  }

  def add(instant: Long, value: Long): Long = {
    return iField.add(instant, value)
  }

  def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
    return iField.getDifference(minuendInstant, subtrahendInstant)
  }

  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return iField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  def getUnitMillis: Long = {
    return iField.getUnitMillis
  }

  def compareTo(durationField: DurationField): Int = {
    return iField.compareTo(durationField)
  }

  override def equals(obj: AnyRef): Boolean = {
    if (obj.isInstanceOf[DelegatedDurationField]) {
      return iField == (obj.asInstanceOf[DelegatedDurationField]).iField
    }
    return false
  }

  override def hashCode: Int = {
    return iField.hashCode ^ iType.hashCode
  }

  override def toString: String = {
    return if ((iType == null)) iField.toString else ("DurationField[" + iType + ']')
  }
}
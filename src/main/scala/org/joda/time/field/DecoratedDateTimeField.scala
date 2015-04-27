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
 * <code>DecoratedDateTimeField</code> extends {@link BaseDateTimeField},
 * implementing only the minimum required set of methods. These implemented
 * methods delegate to a wrapped field.
 * <p>
 * This design allows new DateTimeField types to be defined that piggyback on
 * top of another, inheriting all the safe method implementations from
 * BaseDateTimeField. Should any method require pure delegation to the
 * wrapped field, simply override and use the provided getWrappedField method.
 * <p>
 * DecoratedDateTimeField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see DelegatedDateTimeField
 */
@SerialVersionUID(203115783733757597L)
abstract class DecoratedDateTimeField extends BaseDateTimeField {
  /** The DateTimeField being wrapped */
  private final val iField: DateTimeField = null

  /**
   * Constructor.
   *
   * @param field  the field being decorated
   * @param type  allow type to be overridden
   */
  protected def this(field: DateTimeField, `type`: DateTimeFieldType) {
    this()
    `super`(`type`)
    if (field == null) {
      throw new IllegalArgumentException("The field must not be null")
    }
    if (!field.isSupported) {
      throw new IllegalArgumentException("The field must be supported")
    }
    iField = field
  }

  /**
   * Gets the wrapped date time field.
   *
   * @return the wrapped DateTimeField
   */
  final def getWrappedField: DateTimeField = {
    return iField
  }

  def isLenient: Boolean = {
    return iField.isLenient
  }

  def get(instant: Long): Int = {
    return iField.get(instant)
  }

  def set(instant: Long, value: Int): Long = {
    return iField.set(instant, value)
  }

  def getDurationField: DurationField = {
    return iField.getDurationField
  }

  def getRangeDurationField: DurationField = {
    return iField.getRangeDurationField
  }

  def getMinimumValue: Int = {
    return iField.getMinimumValue
  }

  def getMaximumValue: Int = {
    return iField.getMaximumValue
  }

  def roundFloor(instant: Long): Long = {
    return iField.roundFloor(instant)
  }
}
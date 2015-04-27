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
 * <code>DecoratedDurationField</code> extends {@link BaseDurationField},
 * implementing only the minimum required set of methods. These implemented
 * methods delegate to a wrapped field.
 * <p>
 * This design allows new DurationField types to be defined that piggyback on
 * top of another, inheriting all the safe method implementations from
 * BaseDurationField. Should any method require pure delegation to the
 * wrapped field, simply override and use the provided getWrappedField method.
 * <p>
 * DecoratedDurationField is thread-safe and immutable, and its subclasses must
 * be as well.
 *
 * @author Brian S O'Neill
 * @see DelegatedDurationField
 * @since 1.0
 */
@SerialVersionUID(8019982251647420015L)
class DecoratedDurationField extends BaseDurationField {
  /** The DurationField being wrapped */
  private final val iField: DurationField = null

  /**
   * Constructor.
   *
   * @param field  the base field
   * @param type  the type to actually use
   */
  def this(field: DurationField, `type`: DurationFieldType) {
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
   * Gets the wrapped duration field.
   *
   * @return the wrapped DurationField
   */
  final def getWrappedField: DurationField = {
    return iField
  }

  def isPrecise: Boolean = {
    return iField.isPrecise
  }

  def getValueAsLong(duration: Long, instant: Long): Long = {
    return iField.getValueAsLong(duration, instant)
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

  def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
    return iField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
  }

  def getUnitMillis: Long = {
    return iField.getUnitMillis
  }
}
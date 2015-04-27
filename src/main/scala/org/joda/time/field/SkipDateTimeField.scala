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

import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.IllegalFieldValueException

/**
 * Wraps another field such that a certain value is skipped.
 * <p>
 * This is most useful for years where you want to skip zero, so the
 * sequence runs ...,2,1,-1,-2,...
 * <p>
 * SkipDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-8869148464118507846L)
final class SkipDateTimeField extends DelegatedDateTimeField {
  /** The chronology to wrap. */
  private final val iChronology: Chronology = null
  /** The value to skip. */
  private final val iSkip: Int = 0
  /** The calculated minimum value. */
  @transient
  private var iMinValue: Int = 0

  /**
   * Constructor that skips zero.
   *
   * @param chronology  the chronoogy to use
   * @param field  the field to skip zero on
   */
  def this(chronology: Chronology, field: DateTimeField) {
    this()
    `this`(chronology, field, 0)
  }

  /**
   * Constructor.
   *
   * @param chronology  the chronoogy to use
   * @param field  the field to skip zero on
   * @param skip  the value to skip
   */
  def this(chronology: Chronology, field: DateTimeField, skip: Int) {
    this()
    `super`(field)
    iChronology = chronology
    val min: Int = super.getMinimumValue
    if (min < skip) {
      iMinValue = min - 1
    }
    else if (min == skip) {
      iMinValue = skip + 1
    }
    else {
      iMinValue = min
    }
    iSkip = skip
  }

  override def get(millis: Long): Int = {
    var value: Int = super.get(millis)
    if (value <= iSkip) {
      value -= 1
    }
    return value
  }

  override def set(millis: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, iMinValue, getMaximumValue)
    if (value <= iSkip) {
      if (value == iSkip) {
        throw new IllegalFieldValueException(DateTimeFieldType.year, Integer.valueOf(value), null, null)
      }
      value += 1
    }
    return super.set(millis, value)
  }

  override def getMinimumValue: Int = {
    return iMinValue
  }

  private def readResolve: AnyRef = {
    return getType.getField(iChronology)
  }
}
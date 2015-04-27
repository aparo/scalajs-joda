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

/**
 * Wraps another field such that a certain value is added back into
 * the sequence of numbers.
 * <p>
 * This reverses the effect of SkipDateTimeField. This isn't very
 * elegant.
 * <p>
 * SkipUndoDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-5875876968979L)
final class SkipUndoDateTimeField extends DelegatedDateTimeField {
  /** The chronology to wrap. */
  private final val iChronology: Chronology = null
  /** The value to skip. */
  private final val iSkip: Int = 0
  /** The calculated minimum value. */
  @transient
  private var iMinValue: Int = 0

  /**
   * Constructor that reinserts zero.
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
      iMinValue = min + 1
    }
    else if (min == skip + 1) {
      iMinValue = skip
    }
    else {
      iMinValue = min
    }
    iSkip = skip
  }

  override def get(millis: Long): Int = {
    var value: Int = super.get(millis)
    if (value < iSkip) {
      value += 1
    }
    return value
  }

  override def set(millis: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, iMinValue, getMaximumValue)
    if (value <= iSkip) {
      value -= 1
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
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

import org.joda.time.DateTimeField

/**
 * Converts a lenient DateTimeField into a strict one. By being strict, the set
 * throws an IllegalArgumentException if the value is out of bounds.
 * <p>
 * StrictDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @see org.joda.time.chrono.StrictChronology
 * @see LenientDateTimeField
 * @since 1.0
 */
@SerialVersionUID(3154803964207950910L)
object StrictDateTimeField {
  /**
   * Returns a strict version of the given field. If it is already strict,
   * then it is returned as-is. Otherwise, a new StrictDateTimeField is
   * returned.
   */
  def getInstance(field: DateTimeField): DateTimeField = {
    if (field == null) {
      return null
    }
    if (field.isInstanceOf[LenientDateTimeField]) {
      field = (field.asInstanceOf[LenientDateTimeField]).getWrappedField
    }
    if (!field.isLenient) {
      return field
    }
    return new StrictDateTimeField(field)
  }
}

@SerialVersionUID(3154803964207950910L)
class StrictDateTimeField extends DelegatedDateTimeField {
  protected def this(field: DateTimeField) {
    this()
    `super`(field)
  }

  final override def isLenient: Boolean = {
    return false
  }

  /**
   * Does a bounds check before setting the value.
   *
   * @throws IllegalArgumentException if the value is invalid
   */
  override def set(instant: Long, value: Int): Long = {
    FieldUtils.verifyValueBounds(this, value, getMinimumValue(instant), getMaximumValue(instant))
    return super.set(instant, value)
  }
}
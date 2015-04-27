/*
 *  Copyright 2001-2007 Stephen Colebourne
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
 * Converts a strict DateTimeField into a lenient one. By being lenient, the
 * set method accepts out of bounds values, performing an addition instead.
 * <p>
 * LenientDateTimeField is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @see org.joda.time.chrono.LenientChronology
 * @see StrictDateTimeField
 * @since 1.0
 */
@SerialVersionUID(8714085824173290599L)
object LenientDateTimeField {
  /**
   * Returns a lenient version of the given field. If it is already lenient,
   * then it is returned as-is. Otherwise, a new LenientDateTimeField is
   * returned.
   */
  def getInstance(field: DateTimeField, base: Chronology): DateTimeField = {
    if (field == null) {
      return null
    }
    if (field.isInstanceOf[StrictDateTimeField]) {
      field = (field.asInstanceOf[StrictDateTimeField]).getWrappedField
    }
    if (field.isLenient) {
      return field
    }
    return new LenientDateTimeField(field, base)
  }
}

@SerialVersionUID(8714085824173290599L)
class LenientDateTimeField extends DelegatedDateTimeField {
  private final val iBase: Chronology = null

  protected def this(field: DateTimeField, base: Chronology) {
    this()
    `super`(field)
    iBase = base
  }

  final override def isLenient: Boolean = {
    return true
  }

  /**
   * Set values which may be out of bounds by adding the difference between
   * the new value and the current value.
   */
  override def set(instant: Long, value: Int): Long = {
    var localInstant: Long = iBase.getZone.convertUTCToLocal(instant)
    val difference: Long = FieldUtils.safeSubtract(value, get(instant))
    localInstant = getType.getField(iBase.withUTC).add(localInstant, difference)
    return iBase.getZone.convertLocalToUTC(localInstant, false, instant)
  }
}
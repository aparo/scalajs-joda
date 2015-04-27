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
package org.joda.time.chrono

import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.field.LenientDateTimeField

/**
 * Wraps another Chronology, ensuring all the fields are lenient.
 * <p>
 * LenientChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see LenientDateTimeField
 * @see StrictChronology
 */
@SerialVersionUID(-3148237568046877177L)
object LenientChronology {
  /**
   * Create a LenientChronology for any chronology.
   *
   * @param base the chronology to wrap
   * @throws IllegalArgumentException if chronology is null
   */
  def getInstance(base: Chronology): LenientChronology = {
    if (base == null) {
      throw new IllegalArgumentException("Must supply a chronology")
    }
    return new LenientChronology(base)
  }
}

@SerialVersionUID(-3148237568046877177L)
final class LenientChronology extends AssembledChronology {
  @transient
  private var iWithUTC: Chronology = null

  /**
   * Create a LenientChronology for any chronology.
   *
   * @param base the chronology to wrap
   */
  private def this(base: Chronology) {
    this()
    `super`(base, null)
  }

  def withUTC: Chronology = {
    if (iWithUTC == null) {
      if (getZone eq DateTimeZone.UTC) {
        iWithUTC = this
      }
      else {
        iWithUTC = LenientChronology.getInstance(getBase.withUTC)
      }
    }
    return iWithUTC
  }

  def withZone(zone: DateTimeZone): Chronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    if (zone eq DateTimeZone.UTC) {
      return withUTC
    }
    if (zone eq getZone) {
      return this
    }
    return LenientChronology.getInstance(getBase.withZone(zone))
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    fields.year = convertField(fields.year)
    fields.yearOfEra = convertField(fields.yearOfEra)
    fields.yearOfCentury = convertField(fields.yearOfCentury)
    fields.centuryOfEra = convertField(fields.centuryOfEra)
    fields.era = convertField(fields.era)
    fields.dayOfWeek = convertField(fields.dayOfWeek)
    fields.dayOfMonth = convertField(fields.dayOfMonth)
    fields.dayOfYear = convertField(fields.dayOfYear)
    fields.monthOfYear = convertField(fields.monthOfYear)
    fields.weekOfWeekyear = convertField(fields.weekOfWeekyear)
    fields.weekyear = convertField(fields.weekyear)
    fields.weekyearOfCentury = convertField(fields.weekyearOfCentury)
    fields.millisOfSecond = convertField(fields.millisOfSecond)
    fields.millisOfDay = convertField(fields.millisOfDay)
    fields.secondOfMinute = convertField(fields.secondOfMinute)
    fields.secondOfDay = convertField(fields.secondOfDay)
    fields.minuteOfHour = convertField(fields.minuteOfHour)
    fields.minuteOfDay = convertField(fields.minuteOfDay)
    fields.hourOfDay = convertField(fields.hourOfDay)
    fields.hourOfHalfday = convertField(fields.hourOfHalfday)
    fields.clockhourOfDay = convertField(fields.clockhourOfDay)
    fields.clockhourOfHalfday = convertField(fields.clockhourOfHalfday)
    fields.halfdayOfDay = convertField(fields.halfdayOfDay)
  }

  private final def convertField(field: DateTimeField): DateTimeField = {
    return LenientDateTimeField.getInstance(field, getBase)
  }

  /**
   * A lenient chronology is only equal to a lenient chronology with the
   * same base chronology.
   *
   * @param obj  the object to compare to
   * @return true if equal
   * @since 1.4
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[LenientChronology] == false) {
      return false
    }
    val chrono: LenientChronology = obj.asInstanceOf[LenientChronology]
    return getBase == chrono.getBase
  }

  /**
   * A suitable hashcode for the chronology.
   *
   * @return the hashcode
   * @since 1.4
   */
  override def hashCode: Int = {
    return 236548278 + getBase.hashCode * 7
  }

  /**
   * A debugging string for the chronology.
   *
   * @return the debugging string
   */
  def toString: String = {
    return "LenientChronology[" + getBase.toString + ']'
  }
}
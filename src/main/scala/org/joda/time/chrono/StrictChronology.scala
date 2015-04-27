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
import org.joda.time.field.StrictDateTimeField

/**
 * Wraps another Chronology, ensuring all the fields are strict.
 * <p>
 * StrictChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 * @see StrictDateTimeField
 * @see LenientChronology
 */
@SerialVersionUID(6633006628097111960L)
object StrictChronology {
  /**
   * Create a StrictChronology for any chronology.
   *
   * @param base the chronology to wrap
   * @throws IllegalArgumentException if chronology is null
   */
  def getInstance(base: Chronology): StrictChronology = {
    if (base == null) {
      throw new IllegalArgumentException("Must supply a chronology")
    }
    return new StrictChronology(base)
  }

  private def convertField(field: DateTimeField): DateTimeField = {
    return StrictDateTimeField.getInstance(field)
  }
}

@SerialVersionUID(6633006628097111960L)
final class StrictChronology extends AssembledChronology {
  @transient
  private var iWithUTC: Chronology = null

  /**
   * Create a StrictChronology for any chronology.
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
        iWithUTC = StrictChronology.getInstance(getBase.withUTC)
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
    return StrictChronology.getInstance(getBase.withZone(zone))
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    fields.year = StrictChronology.convertField(fields.year)
    fields.yearOfEra = StrictChronology.convertField(fields.yearOfEra)
    fields.yearOfCentury = StrictChronology.convertField(fields.yearOfCentury)
    fields.centuryOfEra = StrictChronology.convertField(fields.centuryOfEra)
    fields.era = StrictChronology.convertField(fields.era)
    fields.dayOfWeek = StrictChronology.convertField(fields.dayOfWeek)
    fields.dayOfMonth = StrictChronology.convertField(fields.dayOfMonth)
    fields.dayOfYear = StrictChronology.convertField(fields.dayOfYear)
    fields.monthOfYear = StrictChronology.convertField(fields.monthOfYear)
    fields.weekOfWeekyear = StrictChronology.convertField(fields.weekOfWeekyear)
    fields.weekyear = StrictChronology.convertField(fields.weekyear)
    fields.weekyearOfCentury = StrictChronology.convertField(fields.weekyearOfCentury)
    fields.millisOfSecond = StrictChronology.convertField(fields.millisOfSecond)
    fields.millisOfDay = StrictChronology.convertField(fields.millisOfDay)
    fields.secondOfMinute = StrictChronology.convertField(fields.secondOfMinute)
    fields.secondOfDay = StrictChronology.convertField(fields.secondOfDay)
    fields.minuteOfHour = StrictChronology.convertField(fields.minuteOfHour)
    fields.minuteOfDay = StrictChronology.convertField(fields.minuteOfDay)
    fields.hourOfDay = StrictChronology.convertField(fields.hourOfDay)
    fields.hourOfHalfday = StrictChronology.convertField(fields.hourOfHalfday)
    fields.clockhourOfDay = StrictChronology.convertField(fields.clockhourOfDay)
    fields.clockhourOfHalfday = StrictChronology.convertField(fields.clockhourOfHalfday)
    fields.halfdayOfDay = StrictChronology.convertField(fields.halfdayOfDay)
  }

  /**
   * A strict chronology is only equal to a strict chronology with the
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
    if (obj.isInstanceOf[StrictChronology] == false) {
      return false
    }
    val chrono: StrictChronology = obj.asInstanceOf[StrictChronology]
    return getBase == chrono.getBase
  }

  /**
   * A suitable hashcode for the chronology.
   *
   * @return the hashcode
   * @since 1.4
   */
  override def hashCode: Int = {
    return 352831696 + getBase.hashCode * 7
  }

  /**
   * A debugging string for the chronology.
   *
   * @return the debugging string
   */
  def toString: String = {
    return "StrictChronology[" + getBase.toString + ']'
  }
}
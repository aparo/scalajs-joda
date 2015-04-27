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

import java.io.IOException
import java.io.ObjectInputStream
import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.DurationField

/**
 * Abstract Chronology that enables chronologies to be assembled from
 * a container of fields.
 * <p>
 * AssembledChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-6728465968995518215L)
object AssembledChronology {

  /**
   * A container of fields used for assembling a chronology.
   */
  object Fields {
    private def isSupported(field: DurationField): Boolean = {
      return if (field == null) false else field.isSupported
    }

    private def isSupported(field: DateTimeField): Boolean = {
      return if (field == null) false else field.isSupported
    }
  }

  final class Fields {
    var millis: DurationField = null
    var seconds: DurationField = null
    var minutes: DurationField = null
    var hours: DurationField = null
    var halfdays: DurationField = null
    var days: DurationField = null
    var weeks: DurationField = null
    var weekyears: DurationField = null
    var months: DurationField = null
    var years: DurationField = null
    var centuries: DurationField = null
    var eras: DurationField = null
    var millisOfSecond: DateTimeField = null
    var millisOfDay: DateTimeField = null
    var secondOfMinute: DateTimeField = null
    var secondOfDay: DateTimeField = null
    var minuteOfHour: DateTimeField = null
    var minuteOfDay: DateTimeField = null
    var hourOfDay: DateTimeField = null
    var clockhourOfDay: DateTimeField = null
    var hourOfHalfday: DateTimeField = null
    var clockhourOfHalfday: DateTimeField = null
    var halfdayOfDay: DateTimeField = null
    var dayOfWeek: DateTimeField = null
    var dayOfMonth: DateTimeField = null
    var dayOfYear: DateTimeField = null
    var weekOfWeekyear: DateTimeField = null
    var weekyear: DateTimeField = null
    var weekyearOfCentury: DateTimeField = null
    var monthOfYear: DateTimeField = null
    var year: DateTimeField = null
    var yearOfEra: DateTimeField = null
    var yearOfCentury: DateTimeField = null
    var centuryOfEra: DateTimeField = null
    var era: DateTimeField = null

    private[chrono] def this() {
      this()
    }

    /**
     * Copy the supported fields from a chronology into this container.
     */
    def copyFieldsFrom(chrono: Chronology) {
      {
        var f: DurationField = null
        if (Fields.isSupported(f = chrono.millis)) {
          millis = f
        }
        if (Fields.isSupported(f = chrono.seconds)) {
          seconds = f
        }
        if (Fields.isSupported(f = chrono.minutes)) {
          minutes = f
        }
        if (Fields.isSupported(f = chrono.hours)) {
          hours = f
        }
        if (Fields.isSupported(f = chrono.halfdays)) {
          halfdays = f
        }
        if (Fields.isSupported(f = chrono.days)) {
          days = f
        }
        if (Fields.isSupported(f = chrono.weeks)) {
          weeks = f
        }
        if (Fields.isSupported(f = chrono.weekyears)) {
          weekyears = f
        }
        if (Fields.isSupported(f = chrono.months)) {
          months = f
        }
        if (Fields.isSupported(f = chrono.years)) {
          years = f
        }
        if (Fields.isSupported(f = chrono.centuries)) {
          centuries = f
        }
        if (Fields.isSupported(f = chrono.eras)) {
          eras = f
        }
      }
      {
        var f: DateTimeField = null
        if (Fields.isSupported(f = chrono.millisOfSecond)) {
          millisOfSecond = f
        }
        if (Fields.isSupported(f = chrono.millisOfDay)) {
          millisOfDay = f
        }
        if (Fields.isSupported(f = chrono.secondOfMinute)) {
          secondOfMinute = f
        }
        if (Fields.isSupported(f = chrono.secondOfDay)) {
          secondOfDay = f
        }
        if (Fields.isSupported(f = chrono.minuteOfHour)) {
          minuteOfHour = f
        }
        if (Fields.isSupported(f = chrono.minuteOfDay)) {
          minuteOfDay = f
        }
        if (Fields.isSupported(f = chrono.hourOfDay)) {
          hourOfDay = f
        }
        if (Fields.isSupported(f = chrono.clockhourOfDay)) {
          clockhourOfDay = f
        }
        if (Fields.isSupported(f = chrono.hourOfHalfday)) {
          hourOfHalfday = f
        }
        if (Fields.isSupported(f = chrono.clockhourOfHalfday)) {
          clockhourOfHalfday = f
        }
        if (Fields.isSupported(f = chrono.halfdayOfDay)) {
          halfdayOfDay = f
        }
        if (Fields.isSupported(f = chrono.dayOfWeek)) {
          dayOfWeek = f
        }
        if (Fields.isSupported(f = chrono.dayOfMonth)) {
          dayOfMonth = f
        }
        if (Fields.isSupported(f = chrono.dayOfYear)) {
          dayOfYear = f
        }
        if (Fields.isSupported(f = chrono.weekOfWeekyear)) {
          weekOfWeekyear = f
        }
        if (Fields.isSupported(f = chrono.weekyear)) {
          weekyear = f
        }
        if (Fields.isSupported(f = chrono.weekyearOfCentury)) {
          weekyearOfCentury = f
        }
        if (Fields.isSupported(f = chrono.monthOfYear)) {
          monthOfYear = f
        }
        if (Fields.isSupported(f = chrono.year)) {
          year = f
        }
        if (Fields.isSupported(f = chrono.yearOfEra)) {
          yearOfEra = f
        }
        if (Fields.isSupported(f = chrono.yearOfCentury)) {
          yearOfCentury = f
        }
        if (Fields.isSupported(f = chrono.centuryOfEra)) {
          centuryOfEra = f
        }
        if (Fields.isSupported(f = chrono.era)) {
          era = f
        }
      }
    }
  }

}

@SerialVersionUID(-6728465968995518215L)
abstract class AssembledChronology extends BaseChronology {
  private final val iBase: Chronology = null
  private final val iParam: AnyRef = null
  @transient
  private var iMillis: DurationField = null
  @transient
  private var iSeconds: DurationField = null
  @transient
  private var iMinutes: DurationField = null
  @transient
  private var iHours: DurationField = null
  @transient
  private var iHalfdays: DurationField = null
  @transient
  private var iDays: DurationField = null
  @transient
  private var iWeeks: DurationField = null
  @transient
  private var iWeekyears: DurationField = null
  @transient
  private var iMonths: DurationField = null
  @transient
  private var iYears: DurationField = null
  @transient
  private var iCenturies: DurationField = null
  @transient
  private var iEras: DurationField = null
  @transient
  private var iMillisOfSecond: DateTimeField = null
  @transient
  private var iMillisOfDay: DateTimeField = null
  @transient
  private var iSecondOfMinute: DateTimeField = null
  @transient
  private var iSecondOfDay: DateTimeField = null
  @transient
  private var iMinuteOfHour: DateTimeField = null
  @transient
  private var iMinuteOfDay: DateTimeField = null
  @transient
  private var iHourOfDay: DateTimeField = null
  @transient
  private var iClockhourOfDay: DateTimeField = null
  @transient
  private var iHourOfHalfday: DateTimeField = null
  @transient
  private var iClockhourOfHalfday: DateTimeField = null
  @transient
  private var iHalfdayOfDay: DateTimeField = null
  @transient
  private var iDayOfWeek: DateTimeField = null
  @transient
  private var iDayOfMonth: DateTimeField = null
  @transient
  private var iDayOfYear: DateTimeField = null
  @transient
  private var iWeekOfWeekyear: DateTimeField = null
  @transient
  private var iWeekyear: DateTimeField = null
  @transient
  private var iWeekyearOfCentury: DateTimeField = null
  @transient
  private var iMonthOfYear: DateTimeField = null
  @transient
  private var iYear: DateTimeField = null
  @transient
  private var iYearOfEra: DateTimeField = null
  @transient
  private var iYearOfCentury: DateTimeField = null
  @transient
  private var iCenturyOfEra: DateTimeField = null
  @transient
  private var iEra: DateTimeField = null
  @transient
  private var iBaseFlags: Int = 0

  /**
   * Constructor calls the assemble method, enabling subclasses to define its
   * supported fields. If a base chronology is supplied, the field set
   * initially contains references to each base chronology field.
   * <p>
   * Other methods in this class will delegate to the base chronology, if it
   * can be determined that the base chronology will produce the same results
   * as AbstractChronology.
   *
   * @param base optional base chronology to copy initial fields from
   * @param param optional param object avalable for assemble method
   */
  protected def this(base: Chronology, param: AnyRef) {
    this()
    iBase = base
    iParam = param
    setFields
  }

  def getZone: DateTimeZone = {
    var base: Chronology = null
    if ((({
      base = iBase; base
    })) != null) {
      return base.getZone
    }
    return null
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    var base: Chronology = null
    if ((({
      base = iBase; base
    })) != null && (iBaseFlags & 6) == 6) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
    }
    return super.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    var base: Chronology = null
    if ((({
      base = iBase; base
    })) != null && (iBaseFlags & 5) == 5) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    }
    return super.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(instant: Long, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    var base: Chronology = null
    if ((({
      base = iBase; base
    })) != null && (iBaseFlags & 1) == 1) {
      return base.getDateTimeMillis(instant, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    }
    return super.getDateTimeMillis(instant, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
  }

  final override def millis: DurationField = {
    return iMillis
  }

  final override def millisOfSecond: DateTimeField = {
    return iMillisOfSecond
  }

  final override def millisOfDay: DateTimeField = {
    return iMillisOfDay
  }

  final override def seconds: DurationField = {
    return iSeconds
  }

  final override def secondOfMinute: DateTimeField = {
    return iSecondOfMinute
  }

  final override def secondOfDay: DateTimeField = {
    return iSecondOfDay
  }

  final override def minutes: DurationField = {
    return iMinutes
  }

  final override def minuteOfHour: DateTimeField = {
    return iMinuteOfHour
  }

  final override def minuteOfDay: DateTimeField = {
    return iMinuteOfDay
  }

  final override def hours: DurationField = {
    return iHours
  }

  final override def hourOfDay: DateTimeField = {
    return iHourOfDay
  }

  final override def clockhourOfDay: DateTimeField = {
    return iClockhourOfDay
  }

  final override def halfdays: DurationField = {
    return iHalfdays
  }

  final override def hourOfHalfday: DateTimeField = {
    return iHourOfHalfday
  }

  final override def clockhourOfHalfday: DateTimeField = {
    return iClockhourOfHalfday
  }

  final override def halfdayOfDay: DateTimeField = {
    return iHalfdayOfDay
  }

  final override def days: DurationField = {
    return iDays
  }

  final override def dayOfWeek: DateTimeField = {
    return iDayOfWeek
  }

  final override def dayOfMonth: DateTimeField = {
    return iDayOfMonth
  }

  final override def dayOfYear: DateTimeField = {
    return iDayOfYear
  }

  final override def weeks: DurationField = {
    return iWeeks
  }

  final override def weekOfWeekyear: DateTimeField = {
    return iWeekOfWeekyear
  }

  final override def weekyears: DurationField = {
    return iWeekyears
  }

  final override def weekyear: DateTimeField = {
    return iWeekyear
  }

  final override def weekyearOfCentury: DateTimeField = {
    return iWeekyearOfCentury
  }

  final override def months: DurationField = {
    return iMonths
  }

  final override def monthOfYear: DateTimeField = {
    return iMonthOfYear
  }

  final override def years: DurationField = {
    return iYears
  }

  final override def year: DateTimeField = {
    return iYear
  }

  final override def yearOfEra: DateTimeField = {
    return iYearOfEra
  }

  final override def yearOfCentury: DateTimeField = {
    return iYearOfCentury
  }

  final override def centuries: DurationField = {
    return iCenturies
  }

  final override def centuryOfEra: DateTimeField = {
    return iCenturyOfEra
  }

  final override def eras: DurationField = {
    return iEras
  }

  final override def era: DateTimeField = {
    return iEra
  }

  /**
   * Invoked by the constructor and after deserialization to allow subclasses
   * to define all of its supported fields. All unset fields default to
   * unsupported instances.
   *
   * @param fields container of fields
   */
  protected def assemble(fields: AssembledChronology.Fields)

  /**
   * Returns the same base chronology as passed into the constructor.
   */
  protected final def getBase: Chronology = {
    return iBase
  }

  /**
   * Returns the same param object as passed into the constructor.
   */
  protected final def getParam: AnyRef = {
    return iParam
  }

  private def setFields {
    val fields: AssembledChronology.Fields = new AssembledChronology.Fields
    if (iBase != null) {
      fields.copyFieldsFrom(iBase)
    }
    assemble(fields) {
      val f: DurationField = null
      iMillis = if ((({
        f = fields.millis; f
      })) != null) f
      else super.millis
      iSeconds = if ((({
        f = fields.seconds; f
      })) != null) f
      else super.seconds
      iMinutes = if ((({
        f = fields.minutes; f
      })) != null) f
      else super.minutes
      iHours = if ((({
        f = fields.hours; f
      })) != null) f
      else super.hours
      iHalfdays = if ((({
        f = fields.halfdays; f
      })) != null) f
      else super.halfdays
      iDays = if ((({
        f = fields.days; f
      })) != null) f
      else super.days
      iWeeks = if ((({
        f = fields.weeks; f
      })) != null) f
      else super.weeks
      iWeekyears = if ((({
        f = fields.weekyears; f
      })) != null) f
      else super.weekyears
      iMonths = if ((({
        f = fields.months; f
      })) != null) f
      else super.months
      iYears = if ((({
        f = fields.years; f
      })) != null) f
      else super.years
      iCenturies = if ((({
        f = fields.centuries; f
      })) != null) f
      else super.centuries
      iEras = if ((({
        f = fields.eras; f
      })) != null) f
      else super.eras
    } {
      val f: DateTimeField = null
      iMillisOfSecond = if ((({
        f = fields.millisOfSecond; f
      })) != null) f
      else super.millisOfSecond
      iMillisOfDay = if ((({
        f = fields.millisOfDay; f
      })) != null) f
      else super.millisOfDay
      iSecondOfMinute = if ((({
        f = fields.secondOfMinute; f
      })) != null) f
      else super.secondOfMinute
      iSecondOfDay = if ((({
        f = fields.secondOfDay; f
      })) != null) f
      else super.secondOfDay
      iMinuteOfHour = if ((({
        f = fields.minuteOfHour; f
      })) != null) f
      else super.minuteOfHour
      iMinuteOfDay = if ((({
        f = fields.minuteOfDay; f
      })) != null) f
      else super.minuteOfDay
      iHourOfDay = if ((({
        f = fields.hourOfDay; f
      })) != null) f
      else super.hourOfDay
      iClockhourOfDay = if ((({
        f = fields.clockhourOfDay; f
      })) != null) f
      else super.clockhourOfDay
      iHourOfHalfday = if ((({
        f = fields.hourOfHalfday; f
      })) != null) f
      else super.hourOfHalfday
      iClockhourOfHalfday = if ((({
        f = fields.clockhourOfHalfday; f
      })) != null) f
      else super.clockhourOfHalfday
      iHalfdayOfDay = if ((({
        f = fields.halfdayOfDay; f
      })) != null) f
      else super.halfdayOfDay
      iDayOfWeek = if ((({
        f = fields.dayOfWeek; f
      })) != null) f
      else super.dayOfWeek
      iDayOfMonth = if ((({
        f = fields.dayOfMonth; f
      })) != null) f
      else super.dayOfMonth
      iDayOfYear = if ((({
        f = fields.dayOfYear; f
      })) != null) f
      else super.dayOfYear
      iWeekOfWeekyear = if ((({
        f = fields.weekOfWeekyear; f
      })) != null) f
      else super.weekOfWeekyear
      iWeekyear = if ((({
        f = fields.weekyear; f
      })) != null) f
      else super.weekyear
      iWeekyearOfCentury = if ((({
        f = fields.weekyearOfCentury; f
      })) != null) f
      else super.weekyearOfCentury
      iMonthOfYear = if ((({
        f = fields.monthOfYear; f
      })) != null) f
      else super.monthOfYear
      iYear = if ((({
        f = fields.year; f
      })) != null) f
      else super.year
      iYearOfEra = if ((({
        f = fields.yearOfEra; f
      })) != null) f
      else super.yearOfEra
      iYearOfCentury = if ((({
        f = fields.yearOfCentury; f
      })) != null) f
      else super.yearOfCentury
      iCenturyOfEra = if ((({
        f = fields.centuryOfEra; f
      })) != null) f
      else super.centuryOfEra
      iEra = if ((({
        f = fields.era; f
      })) != null) f
      else super.era
    }
    var flags: Int = 0
    if (iBase == null) {
      flags = 0
    }
    else {
      flags = (if ((iHourOfDay eq iBase.hourOfDay && iMinuteOfHour eq iBase.minuteOfHour && iSecondOfMinute eq iBase.secondOfMinute && iMillisOfSecond eq iBase.millisOfSecond)) 1 else 0) | (if ((iMillisOfDay eq iBase.millisOfDay)) 2 else 0) | (if ((iYear eq iBase.year && iMonthOfYear eq iBase.monthOfYear && iDayOfMonth eq iBase.dayOfMonth)) 4 else 0)
    }
    iBaseFlags = flags
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream) {
    in.defaultReadObject
    setFields
  }
}
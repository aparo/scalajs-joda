/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTimeField
import org.joda.time.DateTimeUtils
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.IllegalFieldValueException
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.ReadableInstant
import org.joda.time.ReadablePartial
import org.joda.time.field.BaseDateTimeField
import org.joda.time.field.DecoratedDurationField
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * Implements the Gregorian/Julian calendar system which is the calendar system
 * used in most of the world. Wherever possible, it is recommended to use the
 * {@link ISOChronology} instead.
 * <p>
 * The Gregorian calendar replaced the Julian calendar, and the point in time
 * when this chronology switches can be controlled using the second parameter
 * of the getInstance method. By default this cutover is set to the date the
 * Gregorian calendar was first instituted, October 15, 1582.
 * <p>
 * Before this date, this chronology uses the proleptic Julian calendar
 * (proleptic means extending indefinitely). The Julian calendar has leap years
 * every four years, whereas the Gregorian has special rules for 100 and 400
 * years. A meaningful result will thus be obtained for all input values.
 * However before 8 CE, Julian leap years were irregular, and before 45 BCE
 * there was no Julian calendar.
 * <p>
 * This chronology differs from
 * {@link java.util.GregorianCalendar GregorianCalendar} in that years
 * in BCE are returned correctly. Thus year 1 BCE is returned as -1 instead of 1.
 * The yearOfEra field produces results compatible with GregorianCalendar.
 * <p>
 * The Julian calendar does not have a year zero, and so year -1 is followed by
 * year 1. If the Gregorian cutover date is specified at or before year -1
 * (Julian), year zero is defined. In other words, the proleptic Gregorian
 * chronology used by this class has a year zero.
 * <p>
 * To create a pure proleptic Julian chronology, use {@link JulianChronology},
 * and to create a pure proleptic Gregorian chronology, use
 * {@link GregorianChronology}.
 * <p>
 * GJChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-2545574827706931671L)
object GJChronology {
  /**
   * Convert a datetime from one chronology to another.
   */
  private def convertByYear(instant: Long, from: Chronology, to: Chronology): Long = {
    return to.getDateTimeMillis(from.year.get(instant), from.monthOfYear.get(instant), from.dayOfMonth.get(instant), from.millisOfDay.get(instant))
  }

  /**
   * Convert a datetime from one chronology to another.
   */
  private def convertByWeekyear(instant: Long, from: Chronology, to: Chronology): Long = {
    var newInstant: Long = 0L
    newInstant = to.weekyear.set(0, from.weekyear.get(instant))
    newInstant = to.weekOfWeekyear.set(newInstant, from.weekOfWeekyear.get(instant))
    newInstant = to.dayOfWeek.set(newInstant, from.dayOfWeek.get(instant))
    newInstant = to.millisOfDay.set(newInstant, from.millisOfDay.get(instant))
    return newInstant
  }

  /**
   * The default GregorianJulian cutover point.
   */
  private[chrono] val DEFAULT_CUTOVER: Instant = new Instant(-12219292800000L)
  /** Cache of zone to chronology list */
  private val cCache: ConcurrentHashMap[GJCacheKey, GJChronology] = new ConcurrentHashMap[GJCacheKey, GJChronology]

  /**
   * Factory method returns instances of the default GJ cutover
   * chronology. This uses a cutover date of October 15, 1582 (Gregorian)
   * 00:00:00 UTC. For this value, October 4, 1582 (Julian) is followed by
   * October 15, 1582 (Gregorian).
   *
   * <p>The first day of the week is designated to be
   * {@link org.joda.time.DateTimeConstants#MONDAY Monday},
   * and the minimum days in the first week of the year is 4.
   *
   * <p>The time zone of the returned instance is UTC.
   */
  def getInstanceUTC: GJChronology = {
    return getInstance(DateTimeZone.UTC, DEFAULT_CUTOVER, 4)
  }

  /**
   * Factory method returns instances of the default GJ cutover
   * chronology. This uses a cutover date of October 15, 1582 (Gregorian)
   * 00:00:00 UTC. For this value, October 4, 1582 (Julian) is followed by
   * October 15, 1582 (Gregorian).
   *
   * <p>The first day of the week is designated to be
   * {@link org.joda.time.DateTimeConstants#MONDAY Monday},
   * and the minimum days in the first week of the year is 4.
   *
   * <p>The returned chronology is in the default time zone.
   */
  def getInstance: GJChronology = {
    return getInstance(DateTimeZone.getDefault, DEFAULT_CUTOVER, 4)
  }

  /**
   * Factory method returns instances of the GJ cutover chronology. This uses
   * a cutover date of October 15, 1582 (Gregorian) 00:00:00 UTC. For this
   * value, October 4, 1582 (Julian) is followed by October 15, 1582
   * (Gregorian).
   *
   * <p>The first day of the week is designated to be
   * {@link org.joda.time.DateTimeConstants#MONDAY Monday},
   * and the minimum days in the first week of the year is 4.
   *
   * @param zone  the time zone to use, null is default
   */
  def getInstance(zone: DateTimeZone): GJChronology = {
    return getInstance(zone, DEFAULT_CUTOVER, 4)
  }

  /**
   * Factory method returns instances of the GJ cutover chronology. Any
   * cutover date may be specified.
   *
   * <p>The first day of the week is designated to be
   * {@link org.joda.time.DateTimeConstants#MONDAY Monday},
   * and the minimum days in the first week of the year is 4.
   *
   * @param zone  the time zone to use, null is default
   * @param gregorianCutover  the cutover to use, null means default
   */
  def getInstance(zone: DateTimeZone, gregorianCutover: ReadableInstant): GJChronology = {
    return getInstance(zone, gregorianCutover, 4)
  }

  /**
   * Factory method returns instances of the GJ cutover chronology. Any
   * cutover date may be specified.
   *
   * @param zone  the time zone to use, null is default
   * @param gregorianCutover  the cutover to use, null means default
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   */
  def getInstance(zone: DateTimeZone, gregorianCutover: ReadableInstant, minDaysInFirstWeek: Int): GJChronology = {
    zone = DateTimeUtils.getZone(zone)
    var cutoverInstant: Instant = null
    if (gregorianCutover == null) {
      cutoverInstant = DEFAULT_CUTOVER
    }
    else {
      cutoverInstant = gregorianCutover.toInstant
      val cutoverDate: LocalDate = new LocalDate(cutoverInstant.getMillis, GregorianChronology.getInstance(zone))
      if (cutoverDate.getYear <= 0) {
        throw new IllegalArgumentException("Cutover too early. Must be on or after 0001-01-01.")
      }
    }
    val cacheKey: GJCacheKey = new GJCacheKey(zone, cutoverInstant, minDaysInFirstWeek)
    var chrono: GJChronology = cCache.get(cacheKey)
    if (chrono == null) {
      if (zone eq DateTimeZone.UTC) {
        chrono = new GJChronology(JulianChronology.getInstance(zone, minDaysInFirstWeek), GregorianChronology.getInstance(zone, minDaysInFirstWeek), cutoverInstant)
      }
      else {
        chrono = getInstance(DateTimeZone.UTC, cutoverInstant, minDaysInFirstWeek)
        chrono = new GJChronology(ZonedChronology.getInstance(chrono, zone), chrono.iJulianChronology, chrono.iGregorianChronology, chrono.iCutoverInstant)
      }
      val oldChrono: GJChronology = cCache.putIfAbsent(cacheKey, chrono)
      if (oldChrono != null) {
        chrono = oldChrono
      }
    }
    return chrono
  }

  /**
   * Factory method returns instances of the GJ cutover chronology. Any
   * cutover date may be specified.
   *
   * @param zone  the time zone to use, null is default
   * @param gregorianCutover  the cutover to use
   * @param minDaysInFirstWeek  minimum number of days in first week of the year; default is 4
   */
  def getInstance(zone: DateTimeZone, gregorianCutover: Long, minDaysInFirstWeek: Int): GJChronology = {
    var cutoverInstant: Instant = null
    if (gregorianCutover == DEFAULT_CUTOVER.getMillis) {
      cutoverInstant = null
    }
    else {
      cutoverInstant = new Instant(gregorianCutover)
    }
    return getInstance(zone, cutoverInstant, minDaysInFirstWeek)
  }

  /**
   * Links the duration back to a ImpreciseCutoverField.
   */
  @SerialVersionUID(4097975388007713084L)
  private class LinkedDurationField extends DecoratedDurationField {
    private final val iField: GJChronology#ImpreciseCutoverField = null

    private[chrono] def this(durationField: DurationField, dateTimeField: GJChronology#ImpreciseCutoverField) {
      this()
      `super`(durationField, durationField.getType)
      iField = dateTimeField
    }

    override def add(instant: Long, value: Int): Long = {
      return iField.add(instant, value)
    }

    override def add(instant: Long, value: Long): Long = {
      return iField.add(instant, value)
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      return iField.getDifference(minuendInstant, subtrahendInstant)
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      return iField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
    }
  }

}

@SerialVersionUID(-2545574827706931671L)
final class GJChronology extends AssembledChronology {
  private var iJulianChronology: JulianChronology = null
  private var iGregorianChronology: GregorianChronology = null
  private var iCutoverInstant: Instant = null
  private var iCutoverMillis: Long = 0L
  private var iGapDuration: Long = 0L

  /**
   * @param julian chronology used before the cutover instant
   * @param gregorian chronology used at and after the cutover instant
   * @param cutoverInstant instant when the gregorian chronology began
   */
  private def this(julian: JulianChronology, gregorian: GregorianChronology, cutoverInstant: Instant) {
    this()
    `super`(null, Array[AnyRef](julian, gregorian, cutoverInstant))
  }

  /**
   * Called when applying a time zone.
   */
  private def this(base: Chronology, julian: JulianChronology, gregorian: GregorianChronology, cutoverInstant: Instant) {
    this()
    `super`(base, Array[AnyRef](julian, gregorian, cutoverInstant))
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    return GJChronology.getInstance(getZone, iCutoverInstant, getMinimumDaysInFirstWeek)
  }

  override def getZone: DateTimeZone = {
    var base: Chronology = null
    if ((({
      base = getBase; base
    })) != null) {
      return base.getZone
    }
    return DateTimeZone.UTC
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return withZone(DateTimeZone.UTC)
  }

  /**
   * Gets the Chronology in a specific time zone.
   *
   * @param zone  the zone to get the chronology in, null is default
   * @return the chronology
   */
  def withZone(zone: DateTimeZone): Chronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    if (zone eq getZone) {
      return this
    }
    return GJChronology.getInstance(zone, iCutoverInstant, getMinimumDaysInFirstWeek)
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    var base: Chronology = null
    if ((({
      base = getBase; base
    })) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
    }
    var instant: Long = iGregorianChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
    if (instant < iCutoverMillis) {
      instant = iJulianChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
      if (instant >= iCutoverMillis) {
        throw new IllegalArgumentException("Specified date does not exist")
      }
    }
    return instant
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    var base: Chronology = null
    if ((({
      base = getBase; base
    })) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    }
    var instant: Long = 0L
    try {
      instant = iGregorianChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    }
    catch {
      case ex: IllegalFieldValueException => {
        if (monthOfYear != 2 || dayOfMonth != 29) {
          throw ex
        }
        instant = iGregorianChronology.getDateTimeMillis(year, monthOfYear, 28, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
        if (instant >= iCutoverMillis) {
          throw ex
        }
      }
    }
    if (instant < iCutoverMillis) {
      instant = iJulianChronology.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
      if (instant >= iCutoverMillis) {
        throw new IllegalArgumentException("Specified date does not exist")
      }
    }
    return instant
  }

  /**
   * Gets the cutover instant between Gregorian and Julian chronologies.
   * @return the cutover instant
   */
  def getGregorianCutover: Instant = {
    return iCutoverInstant
  }

  /**
   * Gets the minimum days needed for a week to be the first week in a year.
   *
   * @return the minimum days
   */
  def getMinimumDaysInFirstWeek: Int = {
    return iGregorianChronology.getMinimumDaysInFirstWeek
  }

  /**
   * Checks if this chronology instance equals another.
   *
   * @param obj  the object to compare to
   * @return true if equal
   * @since 1.6
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[GJChronology]) {
      val chrono: GJChronology = obj.asInstanceOf[GJChronology]
      return iCutoverMillis == chrono.iCutoverMillis && getMinimumDaysInFirstWeek == chrono.getMinimumDaysInFirstWeek && (getZone == chrono.getZone)
    }
    return false
  }

  /**
   * A suitable hash code for the chronology.
   *
   * @return the hash code
   * @since 1.6
   */
  override def hashCode: Int = {
    return "GJ".hashCode * 11 + getZone.hashCode + getMinimumDaysInFirstWeek + iCutoverInstant.hashCode
  }

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  def toString: String = {
    val sb: StringBuffer = new StringBuffer(60)
    sb.append("GJChronology")
    sb.append('[')
    sb.append(getZone.getID)
    if (iCutoverMillis != GJChronology.DEFAULT_CUTOVER.getMillis) {
      sb.append(",cutover=")
      var printer: DateTimeFormatter = null
      if (withUTC.dayOfYear.remainder(iCutoverMillis) == 0) {
        printer = ISODateTimeFormat.date
      }
      else {
        printer = ISODateTimeFormat.dateTime
      }
      printer.withChronology(withUTC).printTo(sb, iCutoverMillis)
    }
    if (getMinimumDaysInFirstWeek != 4) {
      sb.append(",mdfw=")
      sb.append(getMinimumDaysInFirstWeek)
    }
    sb.append(']')
    return sb.toString
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    val params: Array[AnyRef] = getParam.asInstanceOf[Array[AnyRef]]
    val julian: JulianChronology = params(0).asInstanceOf[JulianChronology]
    val gregorian: GregorianChronology = params(1).asInstanceOf[GregorianChronology]
    val cutoverInstant: Instant = params(2).asInstanceOf[Instant]
    iCutoverMillis = cutoverInstant.getMillis
    iJulianChronology = julian
    iGregorianChronology = gregorian
    iCutoverInstant = cutoverInstant
    if (getBase != null) {
      return
    }
    if (julian.getMinimumDaysInFirstWeek != gregorian.getMinimumDaysInFirstWeek) {
      throw new IllegalArgumentException
    }
    iGapDuration = iCutoverMillis - julianToGregorianByYear(iCutoverMillis)
    fields.copyFieldsFrom(gregorian)
    if (gregorian.millisOfDay.get(iCutoverMillis) == 0) {
      fields.millisOfSecond = new GJChronology#CutoverField(julian.millisOfSecond, fields.millisOfSecond, iCutoverMillis)
      fields.millisOfDay = new GJChronology#CutoverField(julian.millisOfDay, fields.millisOfDay, iCutoverMillis)
      fields.secondOfMinute = new GJChronology#CutoverField(julian.secondOfMinute, fields.secondOfMinute, iCutoverMillis)
      fields.secondOfDay = new GJChronology#CutoverField(julian.secondOfDay, fields.secondOfDay, iCutoverMillis)
      fields.minuteOfHour = new GJChronology#CutoverField(julian.minuteOfHour, fields.minuteOfHour, iCutoverMillis)
      fields.minuteOfDay = new GJChronology#CutoverField(julian.minuteOfDay, fields.minuteOfDay, iCutoverMillis)
      fields.hourOfDay = new GJChronology#CutoverField(julian.hourOfDay, fields.hourOfDay, iCutoverMillis)
      fields.hourOfHalfday = new GJChronology#CutoverField(julian.hourOfHalfday, fields.hourOfHalfday, iCutoverMillis)
      fields.clockhourOfDay = new GJChronology#CutoverField(julian.clockhourOfDay, fields.clockhourOfDay, iCutoverMillis)
      fields.clockhourOfHalfday = new GJChronology#CutoverField(julian.clockhourOfHalfday, fields.clockhourOfHalfday, iCutoverMillis)
      fields.halfdayOfDay = new GJChronology#CutoverField(julian.halfdayOfDay, fields.halfdayOfDay, iCutoverMillis)
    }
    {
      fields.era = new GJChronology#CutoverField(julian.era, fields.era, iCutoverMillis)
    }
    {
      fields.year = new GJChronology#ImpreciseCutoverField(julian.year, fields.year, iCutoverMillis)
      fields.years = fields.year.getDurationField
      fields.yearOfEra = new GJChronology#ImpreciseCutoverField(julian.yearOfEra, fields.yearOfEra, fields.years, iCutoverMillis)
      fields.centuryOfEra = new GJChronology#ImpreciseCutoverField(julian.centuryOfEra, fields.centuryOfEra, iCutoverMillis)
      fields.centuries = fields.centuryOfEra.getDurationField
      fields.yearOfCentury = new GJChronology#ImpreciseCutoverField(julian.yearOfCentury, fields.yearOfCentury, fields.years, fields.centuries, iCutoverMillis)
      fields.monthOfYear = new GJChronology#ImpreciseCutoverField(julian.monthOfYear, fields.monthOfYear, null, fields.years, iCutoverMillis)
      fields.months = fields.monthOfYear.getDurationField
      fields.weekyear = new GJChronology#ImpreciseCutoverField(julian.weekyear, fields.weekyear, null, iCutoverMillis, true)
      fields.weekyears = fields.weekyear.getDurationField
      fields.weekyearOfCentury = new GJChronology#ImpreciseCutoverField(julian.weekyearOfCentury, fields.weekyearOfCentury, fields.weekyears, fields.centuries, iCutoverMillis)
    }
    {
      val cutover: Long = gregorian.year.roundCeiling(iCutoverMillis)
      fields.dayOfYear = new GJChronology#CutoverField(julian.dayOfYear, fields.dayOfYear, fields.years, cutover, false)
    }
    {
      val cutover: Long = gregorian.weekyear.roundCeiling(iCutoverMillis)
      fields.weekOfWeekyear = new GJChronology#CutoverField(julian.weekOfWeekyear, fields.weekOfWeekyear, fields.weekyears, cutover, true)
    }
    {
      val cf: GJChronology#CutoverField = new GJChronology#CutoverField(julian.dayOfMonth, fields.dayOfMonth, iCutoverMillis)
      cf.iRangeDurationField = fields.months
      fields.dayOfMonth = cf
    }
  }

  private[chrono] def julianToGregorianByYear(instant: Long): Long = {
    return GJChronology.convertByYear(instant, iJulianChronology, iGregorianChronology)
  }

  private[chrono] def gregorianToJulianByYear(instant: Long): Long = {
    return GJChronology.convertByYear(instant, iGregorianChronology, iJulianChronology)
  }

  private[chrono] def julianToGregorianByWeekyear(instant: Long): Long = {
    return GJChronology.convertByWeekyear(instant, iJulianChronology, iGregorianChronology)
  }

  private[chrono] def gregorianToJulianByWeekyear(instant: Long): Long = {
    return GJChronology.convertByWeekyear(instant, iGregorianChronology, iJulianChronology)
  }

  /**
   * This basic cutover field adjusts calls to 'get' and 'set' methods, and
   * assumes that calls to add and addWrapField are unaffected by the cutover.
   */
  @SerialVersionUID(3528501219481026402L)
  private class CutoverField extends BaseDateTimeField {
    private[chrono] final val iJulianField: DateTimeField = null
    private[chrono] final val iGregorianField: DateTimeField = null
    private[chrono] final val iCutover: Long = 0L
    private[chrono] final val iConvertByWeekyear: Boolean = false
    protected var iDurationField: DurationField = null
    protected var iRangeDurationField: DurationField = null

    /**
     * @param julianField field from the chronology used before the cutover instant
     * @param gregorianField field from the chronology used at and after the cutover
     * @param cutoverMillis  the millis of the cutover
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, cutoverMillis: Long) {
      this()
      `this`(julianField, gregorianField, cutoverMillis, false)
    }

    /**
     * @param julianField field from the chronology used before the cutover instant
     * @param gregorianField field from the chronology used at and after the cutover
     * @param cutoverMillis  the millis of the cutover
     * @param convertByWeekyear
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, cutoverMillis: Long, convertByWeekyear: Boolean) {
      this()
      `this`(julianField, gregorianField, null, cutoverMillis, convertByWeekyear)
    }

    /**
     * @param julianField field from the chronology used before the cutover instant
     * @param gregorianField field from the chronology used at and after the cutover
     * @param rangeField  the range field
     * @param cutoverMillis  the millis of the cutover
     * @param convertByWeekyear
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, rangeField: DurationField, cutoverMillis: Long, convertByWeekyear: Boolean) {
      this()
      `super`(gregorianField.getType)
      iJulianField = julianField
      iGregorianField = gregorianField
      iCutover = cutoverMillis
      iConvertByWeekyear = convertByWeekyear
      iDurationField = gregorianField.getDurationField
      if (rangeField == null) {
        rangeField = gregorianField.getRangeDurationField
        if (rangeField == null) {
          rangeField = julianField.getRangeDurationField
        }
      }
      iRangeDurationField = rangeField
    }

    def isLenient: Boolean = {
      return false
    }

    def get(instant: Long): Int = {
      if (instant >= iCutover) {
        return iGregorianField.get(instant)
      }
      else {
        return iJulianField.get(instant)
      }
    }

    override def getAsText(instant: Long, locale: Locale): String = {
      if (instant >= iCutover) {
        return iGregorianField.getAsText(instant, locale)
      }
      else {
        return iJulianField.getAsText(instant, locale)
      }
    }

    override def getAsText(fieldValue: Int, locale: Locale): String = {
      return iGregorianField.getAsText(fieldValue, locale)
    }

    override def getAsShortText(instant: Long, locale: Locale): String = {
      if (instant >= iCutover) {
        return iGregorianField.getAsShortText(instant, locale)
      }
      else {
        return iJulianField.getAsShortText(instant, locale)
      }
    }

    override def getAsShortText(fieldValue: Int, locale: Locale): String = {
      return iGregorianField.getAsShortText(fieldValue, locale)
    }

    override def add(instant: Long, value: Int): Long = {
      return iGregorianField.add(instant, value)
    }

    override def add(instant: Long, value: Long): Long = {
      return iGregorianField.add(instant, value)
    }

    override def add(partial: ReadablePartial, fieldIndex: Int, values: Array[Int], valueToAdd: Int): Array[Int] = {
      if (valueToAdd == 0) {
        return values
      }
      if (DateTimeUtils.isContiguous(partial)) {
        var instant: Long = 0L
        {
          var i: Int = 0
          val isize: Int = partial.size
          while (i < isize) {
            {
              instant = partial.getFieldType(i).getField(GJChronology.this).set(instant, values(i))
            }
            ({
              i += 1; i - 1
            })
          }
        }
        instant = add(instant, valueToAdd)
        return GJChronology.this.get(partial, instant)
      }
      else {
        return super.add(partial, fieldIndex, values, valueToAdd)
      }
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      return iGregorianField.getDifference(minuendInstant, subtrahendInstant)
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      return iGregorianField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
    }

    def set(instant: Long, value: Int): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.set(instant, value)
        if (instant < iCutover) {
          if (instant + iGapDuration < iCutover) {
            instant = gregorianToJulian(instant)
          }
          if (get(instant) != value) {
            throw new IllegalFieldValueException(iGregorianField.getType, Integer.valueOf(value), null, null)
          }
        }
      }
      else {
        instant = iJulianField.set(instant, value)
        if (instant >= iCutover) {
          if (instant - iGapDuration >= iCutover) {
            instant = julianToGregorian(instant)
          }
          if (get(instant) != value) {
            throw new IllegalFieldValueException(iJulianField.getType, Integer.valueOf(value), null, null)
          }
        }
      }
      return instant
    }

    override def set(instant: Long, text: String, locale: Locale): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.set(instant, text, locale)
        if (instant < iCutover) {
          if (instant + iGapDuration < iCutover) {
            instant = gregorianToJulian(instant)
          }
        }
      }
      else {
        instant = iJulianField.set(instant, text, locale)
        if (instant >= iCutover) {
          if (instant - iGapDuration >= iCutover) {
            instant = julianToGregorian(instant)
          }
        }
      }
      return instant
    }

    def getDurationField: DurationField = {
      return iDurationField
    }

    def getRangeDurationField: DurationField = {
      return iRangeDurationField
    }

    override def isLeap(instant: Long): Boolean = {
      if (instant >= iCutover) {
        return iGregorianField.isLeap(instant)
      }
      else {
        return iJulianField.isLeap(instant)
      }
    }

    override def getLeapAmount(instant: Long): Int = {
      if (instant >= iCutover) {
        return iGregorianField.getLeapAmount(instant)
      }
      else {
        return iJulianField.getLeapAmount(instant)
      }
    }

    override def getLeapDurationField: DurationField = {
      return iGregorianField.getLeapDurationField
    }

    def getMinimumValue: Int = {
      return iJulianField.getMinimumValue
    }

    override def getMinimumValue(partial: ReadablePartial): Int = {
      return iJulianField.getMinimumValue(partial)
    }

    override def getMinimumValue(partial: ReadablePartial, values: Array[Int]): Int = {
      return iJulianField.getMinimumValue(partial, values)
    }

    override def getMinimumValue(instant: Long): Int = {
      if (instant < iCutover) {
        return iJulianField.getMinimumValue(instant)
      }
      var min: Int = iGregorianField.getMinimumValue(instant)
      instant = iGregorianField.set(instant, min)
      if (instant < iCutover) {
        min = iGregorianField.get(iCutover)
      }
      return min
    }

    def getMaximumValue: Int = {
      return iGregorianField.getMaximumValue
    }

    override def getMaximumValue(instant: Long): Int = {
      if (instant >= iCutover) {
        return iGregorianField.getMaximumValue(instant)
      }
      var max: Int = iJulianField.getMaximumValue(instant)
      instant = iJulianField.set(instant, max)
      if (instant >= iCutover) {
        max = iJulianField.get(iJulianField.add(iCutover, -1))
      }
      return max
    }

    override def getMaximumValue(partial: ReadablePartial): Int = {
      val instant: Long = GJChronology.getInstanceUTC.set(partial, 0L)
      return getMaximumValue(instant)
    }

    override def getMaximumValue(partial: ReadablePartial, values: Array[Int]): Int = {
      val chrono: Chronology = GJChronology.getInstanceUTC
      var instant: Long = 0L
      {
        var i: Int = 0
        val isize: Int = partial.size
        while (i < isize) {
          {
            val field: DateTimeField = partial.getFieldType(i).getField(chrono)
            if (values(i) <= field.getMaximumValue(instant)) {
              instant = field.set(instant, values(i))
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      return getMaximumValue(instant)
    }

    def roundFloor(instant: Long): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.roundFloor(instant)
        if (instant < iCutover) {
          if (instant + iGapDuration < iCutover) {
            instant = gregorianToJulian(instant)
          }
        }
      }
      else {
        instant = iJulianField.roundFloor(instant)
      }
      return instant
    }

    override def roundCeiling(instant: Long): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.roundCeiling(instant)
      }
      else {
        instant = iJulianField.roundCeiling(instant)
        if (instant >= iCutover) {
          if (instant - iGapDuration >= iCutover) {
            instant = julianToGregorian(instant)
          }
        }
      }
      return instant
    }

    override def getMaximumTextLength(locale: Locale): Int = {
      return Math.max(iJulianField.getMaximumTextLength(locale), iGregorianField.getMaximumTextLength(locale))
    }

    override def getMaximumShortTextLength(locale: Locale): Int = {
      return Math.max(iJulianField.getMaximumShortTextLength(locale), iGregorianField.getMaximumShortTextLength(locale))
    }

    protected def julianToGregorian(instant: Long): Long = {
      if (iConvertByWeekyear) {
        return julianToGregorianByWeekyear(instant)
      }
      else {
        return julianToGregorianByYear(instant)
      }
    }

    protected def gregorianToJulian(instant: Long): Long = {
      if (iConvertByWeekyear) {
        return gregorianToJulianByWeekyear(instant)
      }
      else {
        return gregorianToJulianByYear(instant)
      }
    }
  }

  /**
   * Cutover field for variable length fields. These fields internally call
   * set whenever add is called. As a result, the same correction applied to
   * set must be applied to add and addWrapField. Knowing when to use this
   * field requires specific knowledge of how the GJ fields are implemented.
   */
  @SerialVersionUID(3410248757173576441L)
  private final class ImpreciseCutoverField extends CutoverField {
    /**
     * Creates a duration field that links back to this.
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, cutoverMillis: Long) {
      this()
      `this`(julianField, gregorianField, null, cutoverMillis, false)
    }

    /**
     * Uses a shared duration field rather than creating a new one.
     *
     * @param durationField shared duration field
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, durationField: DurationField, cutoverMillis: Long) {
      this()
      `this`(julianField, gregorianField, durationField, cutoverMillis, false)
    }

    /**
     * Uses shared duration fields rather than creating a new one.
     *
     * @param durationField shared duration field
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, durationField: DurationField, rangeDurationField: DurationField, cutoverMillis: Long) {
      this()
      `this`(julianField, gregorianField, durationField, cutoverMillis, false)
      iRangeDurationField = rangeDurationField
    }

    /**
     * Uses a shared duration field rather than creating a new one.
     *
     * @param durationField shared duration field
     */
    private[chrono] def this(julianField: DateTimeField, gregorianField: DateTimeField, durationField: DurationField, cutoverMillis: Long, convertByWeekyear: Boolean) {
      this()
      `super`(julianField, gregorianField, cutoverMillis, convertByWeekyear)
      if (durationField == null) {
        durationField = new GJChronology.LinkedDurationField(iDurationField, this)
      }
      iDurationField = durationField
    }

    override def add(instant: Long, value: Int): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.add(instant, value)
        if (instant < iCutover) {
          if (instant + iGapDuration < iCutover) {
            if (iConvertByWeekyear) {
              val wyear: Int = iGregorianChronology.weekyear.get(instant)
              if (wyear <= 0) {
                instant = iGregorianChronology.weekyear.add(instant, -1)
              }
            }
            else {
              val year: Int = iGregorianChronology.year.get(instant)
              if (year <= 0) {
                instant = iGregorianChronology.year.add(instant, -1)
              }
            }
            instant = gregorianToJulian(instant)
          }
        }
      }
      else {
        instant = iJulianField.add(instant, value)
        if (instant >= iCutover) {
          if (instant - iGapDuration >= iCutover) {
            instant = julianToGregorian(instant)
          }
        }
      }
      return instant
    }

    override def add(instant: Long, value: Long): Long = {
      if (instant >= iCutover) {
        instant = iGregorianField.add(instant, value)
        if (instant < iCutover) {
          if (instant + iGapDuration < iCutover) {
            if (iConvertByWeekyear) {
              val wyear: Int = iGregorianChronology.weekyear.get(instant)
              if (wyear <= 0) {
                instant = iGregorianChronology.weekyear.add(instant, -1)
              }
            }
            else {
              val year: Int = iGregorianChronology.year.get(instant)
              if (year <= 0) {
                instant = iGregorianChronology.year.add(instant, -1)
              }
            }
            instant = gregorianToJulian(instant)
          }
        }
      }
      else {
        instant = iJulianField.add(instant, value)
        if (instant >= iCutover) {
          if (instant - iGapDuration >= iCutover) {
            instant = julianToGregorian(instant)
          }
        }
      }
      return instant
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      if (minuendInstant >= iCutover) {
        if (subtrahendInstant >= iCutover) {
          return iGregorianField.getDifference(minuendInstant, subtrahendInstant)
        }
        minuendInstant = gregorianToJulian(minuendInstant)
        return iJulianField.getDifference(minuendInstant, subtrahendInstant)
      }
      else {
        if (subtrahendInstant < iCutover) {
          return iJulianField.getDifference(minuendInstant, subtrahendInstant)
        }
        minuendInstant = julianToGregorian(minuendInstant)
        return iGregorianField.getDifference(minuendInstant, subtrahendInstant)
      }
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      if (minuendInstant >= iCutover) {
        if (subtrahendInstant >= iCutover) {
          return iGregorianField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
        }
        minuendInstant = gregorianToJulian(minuendInstant)
        return iJulianField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
      }
      else {
        if (subtrahendInstant < iCutover) {
          return iJulianField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
        }
        minuendInstant = julianToGregorian(minuendInstant)
        return iGregorianField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
      }
    }

    override def getMinimumValue(instant: Long): Int = {
      if (instant >= iCutover) {
        return iGregorianField.getMinimumValue(instant)
      }
      else {
        return iJulianField.getMinimumValue(instant)
      }
    }

    override def getMaximumValue(instant: Long): Int = {
      if (instant >= iCutover) {
        return iGregorianField.getMaximumValue(instant)
      }
      else {
        return iJulianField.getMaximumValue(instant)
      }
    }
  }

}
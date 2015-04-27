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
package org.joda.time.chrono

import java.util.HashMap
import java.util.Locale
import org.joda.time.Chronology
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.IllegalFieldValueException
import org.joda.time.IllegalInstantException
import org.joda.time.ReadablePartial
import org.joda.time.field.BaseDateTimeField
import org.joda.time.field.BaseDurationField

/**
 * Wraps another Chronology to add support for time zones.
 * <p>
 * ZonedChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-1079258847191166848L)
object ZonedChronology {
  /**
   * Create a ZonedChronology for any chronology, overriding any time zone it
   * may already have.
   *
   * @param base base chronology to wrap
   * @param zone the time zone
   * @throws IllegalArgumentException if chronology or time zone is null
   */
  def getInstance(base: Chronology, zone: DateTimeZone): ZonedChronology = {
    if (base == null) {
      throw new IllegalArgumentException("Must supply a chronology")
    }
    base = base.withUTC
    if (base == null) {
      throw new IllegalArgumentException("UTC chronology must not be null")
    }
    if (zone == null) {
      throw new IllegalArgumentException("DateTimeZone must not be null")
    }
    return new ZonedChronology(base, zone)
  }

  private[chrono] def useTimeArithmetic(field: DurationField): Boolean = {
    return field != null && field.getUnitMillis < DateTimeConstants.MILLIS_PER_HOUR * 12
  }

  @SerialVersionUID(-485345310999208286L)
  private[chrono] class ZonedDurationField extends BaseDurationField {
    private[chrono] final val iField: DurationField = null
    private[chrono] final val iTimeField: Boolean = false
    private[chrono] final val iZone: DateTimeZone = null

    private[chrono] def this(field: DurationField, zone: DateTimeZone) {
      this()
      `super`(field.getType)
      if (!field.isSupported) {
        throw new IllegalArgumentException
      }
      iField = field
      iTimeField = useTimeArithmetic(field)
      iZone = zone
    }

    def isPrecise: Boolean = {
      return if (iTimeField) iField.isPrecise else iField.isPrecise && this.iZone.isFixed
    }

    def getUnitMillis: Long = {
      return iField.getUnitMillis
    }

    override def getValue(duration: Long, instant: Long): Int = {
      return iField.getValue(duration, addOffset(instant))
    }

    def getValueAsLong(duration: Long, instant: Long): Long = {
      return iField.getValueAsLong(duration, addOffset(instant))
    }

    def getMillis(value: Int, instant: Long): Long = {
      return iField.getMillis(value, addOffset(instant))
    }

    def getMillis(value: Long, instant: Long): Long = {
      return iField.getMillis(value, addOffset(instant))
    }

    def add(instant: Long, value: Int): Long = {
      val offset: Int = getOffsetToAdd(instant)
      instant = iField.add(instant + offset, value)
      return instant - (if (iTimeField) offset else getOffsetFromLocalToSubtract(instant))
    }

    def add(instant: Long, value: Long): Long = {
      val offset: Int = getOffsetToAdd(instant)
      instant = iField.add(instant + offset, value)
      return instant - (if (iTimeField) offset else getOffsetFromLocalToSubtract(instant))
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      val offset: Int = getOffsetToAdd(subtrahendInstant)
      return iField.getDifference(minuendInstant + (if (iTimeField) offset else getOffsetToAdd(minuendInstant)), subtrahendInstant + offset)
    }

    def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      val offset: Int = getOffsetToAdd(subtrahendInstant)
      return iField.getDifferenceAsLong(minuendInstant + (if (iTimeField) offset else getOffsetToAdd(minuendInstant)), subtrahendInstant + offset)
    }

    private def getOffsetToAdd(instant: Long): Int = {
      val offset: Int = this.iZone.getOffset(instant)
      val sum: Long = instant + offset
      if ((instant ^ sum) < 0 && (instant ^ offset) >= 0) {
        throw new ArithmeticException("Adding time zone offset caused overflow")
      }
      return offset
    }

    private def getOffsetFromLocalToSubtract(instant: Long): Int = {
      val offset: Int = this.iZone.getOffsetFromLocal(instant)
      val diff: Long = instant - offset
      if ((instant ^ diff) < 0 && (instant ^ offset) < 0) {
        throw new ArithmeticException("Subtracting time zone offset caused overflow")
      }
      return offset
    }

    private def addOffset(instant: Long): Long = {
      return iZone.convertUTCToLocal(instant)
    }

    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      else if (obj.isInstanceOf[ZonedChronology.ZonedDurationField]) {
        val other: ZonedChronology.ZonedDurationField = obj.asInstanceOf[ZonedChronology.ZonedDurationField]
        return (iField == other.iField) && (iZone == other.iZone)
      }
      return false
    }

    override def hashCode: Int = {
      return iField.hashCode ^ iZone.hashCode
    }
  }

  /**
   * A DateTimeField that decorates another to add timezone behaviour.
   * <p>
   * This class converts passed in instants to local wall time, and vice
   * versa on output.
   */
  @SerialVersionUID(-3968986277775529794L)
  private[chrono] final class ZonedDateTimeField extends BaseDateTimeField {
    private[chrono] final val iField: DateTimeField = null
    private[chrono] final val iZone: DateTimeZone = null
    private[chrono] final val iDurationField: DurationField = null
    private[chrono] final val iTimeField: Boolean = false
    private[chrono] final val iRangeDurationField: DurationField = null
    private[chrono] final val iLeapDurationField: DurationField = null

    private[chrono] def this(field: DateTimeField, zone: DateTimeZone, durationField: DurationField, rangeDurationField: DurationField, leapDurationField: DurationField) {
      this()
      `super`(field.getType)
      if (!field.isSupported) {
        throw new IllegalArgumentException
      }
      iField = field
      iZone = zone
      iDurationField = durationField
      iTimeField = useTimeArithmetic(durationField)
      iRangeDurationField = rangeDurationField
      iLeapDurationField = leapDurationField
    }

    def isLenient: Boolean = {
      return iField.isLenient
    }

    def get(instant: Long): Int = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.get(localInstant)
    }

    override def getAsText(instant: Long, locale: Locale): String = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.getAsText(localInstant, locale)
    }

    override def getAsShortText(instant: Long, locale: Locale): String = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.getAsShortText(localInstant, locale)
    }

    override def getAsText(fieldValue: Int, locale: Locale): String = {
      return iField.getAsText(fieldValue, locale)
    }

    override def getAsShortText(fieldValue: Int, locale: Locale): String = {
      return iField.getAsShortText(fieldValue, locale)
    }

    override def add(instant: Long, value: Int): Long = {
      if (iTimeField) {
        val offset: Int = getOffsetToAdd(instant)
        val localInstant: Long = iField.add(instant + offset, value)
        return localInstant - offset
      }
      else {
        var localInstant: Long = iZone.convertUTCToLocal(instant)
        localInstant = iField.add(localInstant, value)
        return iZone.convertLocalToUTC(localInstant, false, instant)
      }
    }

    override def add(instant: Long, value: Long): Long = {
      if (iTimeField) {
        val offset: Int = getOffsetToAdd(instant)
        val localInstant: Long = iField.add(instant + offset, value)
        return localInstant - offset
      }
      else {
        var localInstant: Long = iZone.convertUTCToLocal(instant)
        localInstant = iField.add(localInstant, value)
        return iZone.convertLocalToUTC(localInstant, false, instant)
      }
    }

    override def addWrapField(instant: Long, value: Int): Long = {
      if (iTimeField) {
        val offset: Int = getOffsetToAdd(instant)
        val localInstant: Long = iField.addWrapField(instant + offset, value)
        return localInstant - offset
      }
      else {
        var localInstant: Long = iZone.convertUTCToLocal(instant)
        localInstant = iField.addWrapField(localInstant, value)
        return iZone.convertLocalToUTC(localInstant, false, instant)
      }
    }

    def set(instant: Long, value: Int): Long = {
      var localInstant: Long = iZone.convertUTCToLocal(instant)
      localInstant = iField.set(localInstant, value)
      val result: Long = iZone.convertLocalToUTC(localInstant, false, instant)
      if (get(result) != value) {
        val cause: IllegalInstantException = new IllegalInstantException(localInstant, iZone.getID)
        val ex: IllegalFieldValueException = new IllegalFieldValueException(iField.getType, Integer.valueOf(value), cause.getMessage)
        ex.initCause(cause)
        throw ex
      }
      return result
    }

    override def set(instant: Long, text: String, locale: Locale): Long = {
      var localInstant: Long = iZone.convertUTCToLocal(instant)
      localInstant = iField.set(localInstant, text, locale)
      return iZone.convertLocalToUTC(localInstant, false, instant)
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      val offset: Int = getOffsetToAdd(subtrahendInstant)
      return iField.getDifference(minuendInstant + (if (iTimeField) offset else getOffsetToAdd(minuendInstant)), subtrahendInstant + offset)
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      val offset: Int = getOffsetToAdd(subtrahendInstant)
      return iField.getDifferenceAsLong(minuendInstant + (if (iTimeField) offset else getOffsetToAdd(minuendInstant)), subtrahendInstant + offset)
    }

    final def getDurationField: DurationField = {
      return iDurationField
    }

    final def getRangeDurationField: DurationField = {
      return iRangeDurationField
    }

    override def isLeap(instant: Long): Boolean = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.isLeap(localInstant)
    }

    override def getLeapAmount(instant: Long): Int = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.getLeapAmount(localInstant)
    }

    final override def getLeapDurationField: DurationField = {
      return iLeapDurationField
    }

    def roundFloor(instant: Long): Long = {
      if (iTimeField) {
        val offset: Int = getOffsetToAdd(instant)
        instant = iField.roundFloor(instant + offset)
        return instant - offset
      }
      else {
        var localInstant: Long = iZone.convertUTCToLocal(instant)
        localInstant = iField.roundFloor(localInstant)
        return iZone.convertLocalToUTC(localInstant, false, instant)
      }
    }

    override def roundCeiling(instant: Long): Long = {
      if (iTimeField) {
        val offset: Int = getOffsetToAdd(instant)
        instant = iField.roundCeiling(instant + offset)
        return instant - offset
      }
      else {
        var localInstant: Long = iZone.convertUTCToLocal(instant)
        localInstant = iField.roundCeiling(localInstant)
        return iZone.convertLocalToUTC(localInstant, false, instant)
      }
    }

    override def remainder(instant: Long): Long = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.remainder(localInstant)
    }

    def getMinimumValue: Int = {
      return iField.getMinimumValue
    }

    override def getMinimumValue(instant: Long): Int = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.getMinimumValue(localInstant)
    }

    override def getMinimumValue(instant: ReadablePartial): Int = {
      return iField.getMinimumValue(instant)
    }

    override def getMinimumValue(instant: ReadablePartial, values: Array[Int]): Int = {
      return iField.getMinimumValue(instant, values)
    }

    def getMaximumValue: Int = {
      return iField.getMaximumValue
    }

    override def getMaximumValue(instant: Long): Int = {
      val localInstant: Long = iZone.convertUTCToLocal(instant)
      return iField.getMaximumValue(localInstant)
    }

    override def getMaximumValue(instant: ReadablePartial): Int = {
      return iField.getMaximumValue(instant)
    }

    override def getMaximumValue(instant: ReadablePartial, values: Array[Int]): Int = {
      return iField.getMaximumValue(instant, values)
    }

    override def getMaximumTextLength(locale: Locale): Int = {
      return iField.getMaximumTextLength(locale)
    }

    override def getMaximumShortTextLength(locale: Locale): Int = {
      return iField.getMaximumShortTextLength(locale)
    }

    private def getOffsetToAdd(instant: Long): Int = {
      val offset: Int = this.iZone.getOffset(instant)
      val sum: Long = instant + offset
      if ((instant ^ sum) < 0 && (instant ^ offset) >= 0) {
        throw new ArithmeticException("Adding time zone offset caused overflow")
      }
      return offset
    }

    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      else if (obj.isInstanceOf[ZonedChronology.ZonedDateTimeField]) {
        val other: ZonedChronology.ZonedDateTimeField = obj.asInstanceOf[ZonedChronology.ZonedDateTimeField]
        return (iField == other.iField) && (iZone == other.iZone) && (iDurationField == other.iDurationField) && (iRangeDurationField == other.iRangeDurationField)
      }
      return false
    }

    override def hashCode: Int = {
      return iField.hashCode ^ iZone.hashCode
    }
  }

}

@SerialVersionUID(-1079258847191166848L)
final class ZonedChronology extends AssembledChronology {
  /**
   * Restricted constructor
   *
   * @param base base chronology to wrap
   * @param zone the time zone
   */
  private def this(base: Chronology, zone: DateTimeZone) {
    this()
    `super`(base, zone)
  }

  override def getZone: DateTimeZone = {
    return getParam.asInstanceOf[DateTimeZone]
  }

  def withUTC: Chronology = {
    return getBase
  }

  def withZone(zone: DateTimeZone): Chronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    if (zone eq getParam) {
      return this
    }
    if (zone eq DateTimeZone.UTC) {
      return getBase
    }
    return new ZonedChronology(getBase, zone)
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    return localToUTC(getBase.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay))
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    return localToUTC(getBase.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond))
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(instant: Long, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    return localToUTC(getBase.getDateTimeMillis(instant + getZone.getOffset(instant), hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond))
  }

  /**
   * @param localInstant  the instant from 1970-01-01T00:00:00 local time
   * @return the instant from 1970-01-01T00:00:00Z
   */
  private def localToUTC(localInstant: Long): Long = {
    val zone: DateTimeZone = getZone
    val offset: Int = zone.getOffsetFromLocal(localInstant)
    val utcInstant: Long = localInstant - offset
    val offsetBasedOnUtc: Int = zone.getOffset(utcInstant)
    if (offset != offsetBasedOnUtc) {
      throw new IllegalInstantException(localInstant, zone.getID)
    }
    return utcInstant
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    val converted: HashMap[AnyRef, AnyRef] = new HashMap[AnyRef, AnyRef]
    fields.eras = convertField(fields.eras, converted)
    fields.centuries = convertField(fields.centuries, converted)
    fields.years = convertField(fields.years, converted)
    fields.months = convertField(fields.months, converted)
    fields.weekyears = convertField(fields.weekyears, converted)
    fields.weeks = convertField(fields.weeks, converted)
    fields.days = convertField(fields.days, converted)
    fields.halfdays = convertField(fields.halfdays, converted)
    fields.hours = convertField(fields.hours, converted)
    fields.minutes = convertField(fields.minutes, converted)
    fields.seconds = convertField(fields.seconds, converted)
    fields.millis = convertField(fields.millis, converted)
    fields.year = convertField(fields.year, converted)
    fields.yearOfEra = convertField(fields.yearOfEra, converted)
    fields.yearOfCentury = convertField(fields.yearOfCentury, converted)
    fields.centuryOfEra = convertField(fields.centuryOfEra, converted)
    fields.era = convertField(fields.era, converted)
    fields.dayOfWeek = convertField(fields.dayOfWeek, converted)
    fields.dayOfMonth = convertField(fields.dayOfMonth, converted)
    fields.dayOfYear = convertField(fields.dayOfYear, converted)
    fields.monthOfYear = convertField(fields.monthOfYear, converted)
    fields.weekOfWeekyear = convertField(fields.weekOfWeekyear, converted)
    fields.weekyear = convertField(fields.weekyear, converted)
    fields.weekyearOfCentury = convertField(fields.weekyearOfCentury, converted)
    fields.millisOfSecond = convertField(fields.millisOfSecond, converted)
    fields.millisOfDay = convertField(fields.millisOfDay, converted)
    fields.secondOfMinute = convertField(fields.secondOfMinute, converted)
    fields.secondOfDay = convertField(fields.secondOfDay, converted)
    fields.minuteOfHour = convertField(fields.minuteOfHour, converted)
    fields.minuteOfDay = convertField(fields.minuteOfDay, converted)
    fields.hourOfDay = convertField(fields.hourOfDay, converted)
    fields.hourOfHalfday = convertField(fields.hourOfHalfday, converted)
    fields.clockhourOfDay = convertField(fields.clockhourOfDay, converted)
    fields.clockhourOfHalfday = convertField(fields.clockhourOfHalfday, converted)
    fields.halfdayOfDay = convertField(fields.halfdayOfDay, converted)
  }

  private def convertField(field: DurationField, converted: HashMap[AnyRef, AnyRef]): DurationField = {
    if (field == null || !field.isSupported) {
      return field
    }
    if (converted.containsKey(field)) {
      return converted.get(field).asInstanceOf[DurationField]
    }
    val zonedField: ZonedChronology.ZonedDurationField = new ZonedChronology.ZonedDurationField(field, getZone)
    converted.put(field, zonedField)
    return zonedField
  }

  private def convertField(field: DateTimeField, converted: HashMap[AnyRef, AnyRef]): DateTimeField = {
    if (field == null || !field.isSupported) {
      return field
    }
    if (converted.containsKey(field)) {
      return converted.get(field).asInstanceOf[DateTimeField]
    }
    val zonedField: ZonedChronology.ZonedDateTimeField = new ZonedChronology.ZonedDateTimeField(field, getZone, convertField(field.getDurationField, converted), convertField(field.getRangeDurationField, converted), convertField(field.getLeapDurationField, converted))
    converted.put(field, zonedField)
    return zonedField
  }

  /**
   * A zoned chronology is only equal to a zoned chronology with the
   * same base chronology and zone.
   *
   * @param obj  the object to compare to
   * @return true if equal
   * @since 1.4
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[ZonedChronology] == false) {
      return false
    }
    val chrono: ZonedChronology = obj.asInstanceOf[ZonedChronology]
    return (getBase == chrono.getBase) && (getZone == chrono.getZone)
  }

  /**
   * A suitable hashcode for the chronology.
   *
   * @return the hashcode
   * @since 1.4
   */
  override def hashCode: Int = {
    return 326565 + getZone.hashCode * 11 + getBase.hashCode * 7
  }

  /**
   * A debugging string for the chronology.
   *
   * @return the debugging string
   */
  def toString: String = {
    return "ZonedChronology[" + getBase + ", " + getZone.getID + ']'
  }
}
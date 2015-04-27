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
import org.joda.time.DateTime
import org.joda.time.DateTimeField
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.MutableDateTime
import org.joda.time.ReadableDateTime
import org.joda.time.field.DecoratedDateTimeField
import org.joda.time.field.DecoratedDurationField
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

/**
 * Wraps another Chronology to impose limits on the range of instants that
 * the fields within a Chronology may support. The limits are applied to both
 * DateTimeFields and DurationFields.
 * <p>
 * Methods in DateTimeField and DurationField throw an IllegalArgumentException
 * whenever given an input instant that is outside the limits or when an
 * attempt is made to move an instant outside the limits.
 * <p>
 * LimitChronology is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(7670866536893052522L)
object LimitChronology {
  /**
   * Wraps another chronology, with datetime limits. When withUTC or
   * withZone is called, the returned LimitChronology instance has
   * the same limits, except they are time zone adjusted.
   *
   * @param base  base chronology to wrap
   * @param lowerLimit  inclusive lower limit, or null if none
   * @param upperLimit  exclusive upper limit, or null if none
   * @throws IllegalArgumentException if chronology is null or limits are invalid
   */
  def getInstance(base: Chronology, lowerLimit: ReadableDateTime, upperLimit: ReadableDateTime): LimitChronology = {
    if (base == null) {
      throw new IllegalArgumentException("Must supply a chronology")
    }
    lowerLimit = if (lowerLimit == null) null else lowerLimit.toDateTime
    upperLimit = if (upperLimit == null) null else upperLimit.toDateTime
    if (lowerLimit != null && upperLimit != null) {
      if (!lowerLimit.isBefore(upperLimit)) {
        throw new IllegalArgumentException("The lower limit must be come before than the upper limit")
      }
    }
    return new LimitChronology(base, lowerLimit.asInstanceOf[DateTime], upperLimit.asInstanceOf[DateTime])
  }
}

@SerialVersionUID(7670866536893052522L)
final class LimitChronology extends AssembledChronology {
  private[chrono] final val iLowerLimit: DateTime = null
  private[chrono] final val iUpperLimit: DateTime = null
  @transient
  private var iWithUTC: LimitChronology = null

  /**
   * Wraps another chronology, with datetime limits. When withUTC or
   * withZone is called, the returned LimitChronology instance has
   * the same limits, except they are time zone adjusted.
   *
   * @param lowerLimit  inclusive lower limit, or null if none
   * @param upperLimit  exclusive upper limit, or null if none
   */
  private def this(base: Chronology, lowerLimit: DateTime, upperLimit: DateTime) {
    this()
    `super`(base, null)
    iLowerLimit = lowerLimit
    iUpperLimit = upperLimit
  }

  /**
   * Returns the inclusive lower limit instant.
   *
   * @return lower limit
   */
  def getLowerLimit: DateTime = {
    return iLowerLimit
  }

  /**
   * Returns the inclusive upper limit instant.
   *
   * @return upper limit
   */
  def getUpperLimit: DateTime = {
    return iUpperLimit
  }

  /**
   * If this LimitChronology is already UTC, then this is
   * returned. Otherwise, a new instance is returned, with the limits
   * adjusted to the new time zone.
   */
  def withUTC: Chronology = {
    return withZone(DateTimeZone.UTC)
  }

  /**
   * If this LimitChronology has the same time zone as the one given, then
   * this is returned. Otherwise, a new instance is returned, with the limits
   * adjusted to the new time zone.
   */
  def withZone(zone: DateTimeZone): Chronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    if (zone eq getZone) {
      return this
    }
    if (zone eq DateTimeZone.UTC && iWithUTC != null) {
      return iWithUTC
    }
    var lowerLimit: DateTime = iLowerLimit
    if (lowerLimit != null) {
      val mdt: MutableDateTime = lowerLimit.toMutableDateTime
      mdt.setZoneRetainFields(zone)
      lowerLimit = mdt.toDateTime
    }
    var upperLimit: DateTime = iUpperLimit
    if (upperLimit != null) {
      val mdt: MutableDateTime = upperLimit.toMutableDateTime
      mdt.setZoneRetainFields(zone)
      upperLimit = mdt.toDateTime
    }
    val chrono: LimitChronology = LimitChronology.getInstance(getBase.withZone(zone), lowerLimit, upperLimit)
    if (zone eq DateTimeZone.UTC) {
      iWithUTC = chrono
    }
    return chrono
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    val instant: Long = getBase.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
    checkLimits(instant, "resulting")
    return instant
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    val instant: Long = getBase.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    checkLimits(instant, "resulting")
    return instant
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(instant: Long, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    checkLimits(instant, null)
    instant = getBase.getDateTimeMillis(instant, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    checkLimits(instant, "resulting")
    return instant
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
    val limitField: LimitChronology#LimitDurationField = new LimitChronology#LimitDurationField(field)
    converted.put(field, limitField)
    return limitField
  }

  private def convertField(field: DateTimeField, converted: HashMap[AnyRef, AnyRef]): DateTimeField = {
    if (field == null || !field.isSupported) {
      return field
    }
    if (converted.containsKey(field)) {
      return converted.get(field).asInstanceOf[DateTimeField]
    }
    val limitField: LimitChronology#LimitDateTimeField = new LimitChronology#LimitDateTimeField(field, convertField(field.getDurationField, converted), convertField(field.getRangeDurationField, converted), convertField(field.getLeapDurationField, converted))
    converted.put(field, limitField)
    return limitField
  }

  private[chrono] def checkLimits(instant: Long, desc: String) {
    var limit: DateTime = null
    if ((({
      limit = iLowerLimit; limit
    })) != null && instant < limit.getMillis) {
      throw new LimitChronology#LimitException(desc, true)
    }
    if ((({
      limit = iUpperLimit; limit
    })) != null && instant >= limit.getMillis) {
      throw new LimitChronology#LimitException(desc, false)
    }
  }

  /**
   * A limit chronology is only equal to a limit chronology with the
   * same base chronology and limits.
   *
   * @param obj  the object to compare to
   * @return true if equal
   * @since 1.4
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[LimitChronology] == false) {
      return false
    }
    val chrono: LimitChronology = obj.asInstanceOf[LimitChronology]
    return (getBase == chrono.getBase) && FieldUtils.equals(getLowerLimit, chrono.getLowerLimit) && FieldUtils.equals(getUpperLimit, chrono.getUpperLimit)
  }

  /**
   * A suitable hashcode for the chronology.
   *
   * @return the hashcode
   * @since 1.4
   */
  override def hashCode: Int = {
    var hash: Int = 317351877
    hash += (if (getLowerLimit != null) getLowerLimit.hashCode else 0)
    hash += (if (getUpperLimit != null) getUpperLimit.hashCode else 0)
    hash += getBase.hashCode * 7
    return hash
  }

  /**
   * A debugging string for the chronology.
   *
   * @return the debugging string
   */
  def toString: String = {
    return "LimitChronology[" + getBase.toString + ", " + (if (getLowerLimit == null) "NoLimit" else getLowerLimit.toString) + ", " + (if (getUpperLimit == null) "NoLimit" else getUpperLimit.toString) + ']'
  }

  /**
   * Extends IllegalArgumentException such that the exception message is not
   * generated unless it is actually requested.
   */
  @SerialVersionUID(-5924689995607498581L)
  private class LimitException extends IllegalArgumentException {
    private final val iIsLow: Boolean = false

    private[chrono] def this(desc: String, isLow: Boolean) {
      this()
      `super`(desc)
      iIsLow = isLow
    }

    override def getMessage: String = {
      val buf: StringBuffer = new StringBuffer(85)
      buf.append("The")
      val desc: String = super.getMessage
      if (desc != null) {
        buf.append(' ')
        buf.append(desc)
      }
      buf.append(" instant is ")
      var p: DateTimeFormatter = ISODateTimeFormat.dateTime
      p = p.withChronology(getBase)
      if (iIsLow) {
        buf.append("below the supported minimum of ")
        p.printTo(buf, getLowerLimit.getMillis)
      }
      else {
        buf.append("above the supported maximum of ")
        p.printTo(buf, getUpperLimit.getMillis)
      }
      buf.append(" (")
      buf.append(getBase)
      buf.append(')')
      return buf.toString
    }

    override def toString: String = {
      return "IllegalArgumentException: " + getMessage
    }
  }

  @SerialVersionUID(8049297699408782284L)
  private class LimitDurationField extends DecoratedDurationField {
    private[chrono] def this(field: DurationField) {
      this()
      `super`(field, field.getType)
    }

    override def getValue(duration: Long, instant: Long): Int = {
      checkLimits(instant, null)
      return getWrappedField.getValue(duration, instant)
    }

    override def getValueAsLong(duration: Long, instant: Long): Long = {
      checkLimits(instant, null)
      return getWrappedField.getValueAsLong(duration, instant)
    }

    override def getMillis(value: Int, instant: Long): Long = {
      checkLimits(instant, null)
      return getWrappedField.getMillis(value, instant)
    }

    override def getMillis(value: Long, instant: Long): Long = {
      checkLimits(instant, null)
      return getWrappedField.getMillis(value, instant)
    }

    override def add(instant: Long, amount: Int): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.add(instant, amount)
      checkLimits(result, "resulting")
      return result
    }

    override def add(instant: Long, amount: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.add(instant, amount)
      checkLimits(result, "resulting")
      return result
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      checkLimits(minuendInstant, "minuend")
      checkLimits(subtrahendInstant, "subtrahend")
      return getWrappedField.getDifference(minuendInstant, subtrahendInstant)
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      checkLimits(minuendInstant, "minuend")
      checkLimits(subtrahendInstant, "subtrahend")
      return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
    }
  }

  @SerialVersionUID(-2435306746995699312L)
  private class LimitDateTimeField extends DecoratedDateTimeField {
    private final val iDurationField: DurationField = null
    private final val iRangeDurationField: DurationField = null
    private final val iLeapDurationField: DurationField = null

    private[chrono] def this(field: DateTimeField, durationField: DurationField, rangeDurationField: DurationField, leapDurationField: DurationField) {
      this()
      `super`(field, field.getType)
      iDurationField = durationField
      iRangeDurationField = rangeDurationField
      iLeapDurationField = leapDurationField
    }

    override def get(instant: Long): Int = {
      checkLimits(instant, null)
      return getWrappedField.get(instant)
    }

    override def getAsText(instant: Long, locale: Locale): String = {
      checkLimits(instant, null)
      return getWrappedField.getAsText(instant, locale)
    }

    override def getAsShortText(instant: Long, locale: Locale): String = {
      checkLimits(instant, null)
      return getWrappedField.getAsShortText(instant, locale)
    }

    override def add(instant: Long, amount: Int): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.add(instant, amount)
      checkLimits(result, "resulting")
      return result
    }

    override def add(instant: Long, amount: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.add(instant, amount)
      checkLimits(result, "resulting")
      return result
    }

    override def addWrapField(instant: Long, amount: Int): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.addWrapField(instant, amount)
      checkLimits(result, "resulting")
      return result
    }

    override def getDifference(minuendInstant: Long, subtrahendInstant: Long): Int = {
      checkLimits(minuendInstant, "minuend")
      checkLimits(subtrahendInstant, "subtrahend")
      return getWrappedField.getDifference(minuendInstant, subtrahendInstant)
    }

    override def getDifferenceAsLong(minuendInstant: Long, subtrahendInstant: Long): Long = {
      checkLimits(minuendInstant, "minuend")
      checkLimits(subtrahendInstant, "subtrahend")
      return getWrappedField.getDifferenceAsLong(minuendInstant, subtrahendInstant)
    }

    override def set(instant: Long, value: Int): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.set(instant, value)
      checkLimits(result, "resulting")
      return result
    }

    override def set(instant: Long, text: String, locale: Locale): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.set(instant, text, locale)
      checkLimits(result, "resulting")
      return result
    }

    final override def getDurationField: DurationField = {
      return iDurationField
    }

    final override def getRangeDurationField: DurationField = {
      return iRangeDurationField
    }

    override def isLeap(instant: Long): Boolean = {
      checkLimits(instant, null)
      return getWrappedField.isLeap(instant)
    }

    override def getLeapAmount(instant: Long): Int = {
      checkLimits(instant, null)
      return getWrappedField.getLeapAmount(instant)
    }

    final override def getLeapDurationField: DurationField = {
      return iLeapDurationField
    }

    override def roundFloor(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.roundFloor(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def roundCeiling(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.roundCeiling(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def roundHalfFloor(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.roundHalfFloor(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def roundHalfCeiling(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.roundHalfCeiling(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def roundHalfEven(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.roundHalfEven(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def remainder(instant: Long): Long = {
      checkLimits(instant, null)
      val result: Long = getWrappedField.remainder(instant)
      checkLimits(result, "resulting")
      return result
    }

    override def getMinimumValue(instant: Long): Int = {
      checkLimits(instant, null)
      return getWrappedField.getMinimumValue(instant)
    }

    override def getMaximumValue(instant: Long): Int = {
      checkLimits(instant, null)
      return getWrappedField.getMaximumValue(instant)
    }

    override def getMaximumTextLength(locale: Locale): Int = {
      return getWrappedField.getMaximumTextLength(locale)
    }

    override def getMaximumShortTextLength(locale: Locale): Int = {
      return getWrappedField.getMaximumShortTextLength(locale)
    }
  }

}
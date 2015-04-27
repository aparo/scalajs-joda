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
import org.joda.time.Chronology
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.DurationField
import org.joda.time.DurationFieldType
import org.joda.time.field.DividedDateTimeField
import org.joda.time.field.FieldUtils
import org.joda.time.field.MillisDurationField
import org.joda.time.field.OffsetDateTimeField
import org.joda.time.field.PreciseDateTimeField
import org.joda.time.field.PreciseDurationField
import org.joda.time.field.RemainderDateTimeField
import org.joda.time.field.ZeroIsMaxDateTimeField

/**
 * Abstract implementation for calendar systems that use a typical
 * day/month/year/leapYear model.
 * Most of the utility methods required by subclasses are package-private,
 * reflecting the intention that they be defined in the same package.
 * <p>
 * BasicChronology is thread-safe and immutable, and all subclasses must
 * be as well.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @author Guy Allard
 * @since 1.2, renamed from BaseGJChronology
 */
@SerialVersionUID(8283225332206808863L)
object BasicChronology {
  private val cMillisField: DurationField = null
  private val cSecondsField: DurationField = null
  private val cMinutesField: DurationField = null
  private val cHoursField: DurationField = null
  private val cHalfdaysField: DurationField = null
  private val cDaysField: DurationField = null
  private val cWeeksField: DurationField = null
  private val cMillisOfSecondField: DateTimeField = null
  private val cMillisOfDayField: DateTimeField = null
  private val cSecondOfMinuteField: DateTimeField = null
  private val cSecondOfDayField: DateTimeField = null
  private val cMinuteOfHourField: DateTimeField = null
  private val cMinuteOfDayField: DateTimeField = null
  private val cHourOfDayField: DateTimeField = null
  private val cHourOfHalfdayField: DateTimeField = null
  private val cClockhourOfDayField: DateTimeField = null
  private val cClockhourOfHalfdayField: DateTimeField = null
  private val cHalfdayOfDayField: DateTimeField = null
  private val CACHE_SIZE: Int = 1 << 10
  private val CACHE_MASK: Int = CACHE_SIZE - 1

  @SerialVersionUID(581601443656929254L)
  private class HalfdayField extends PreciseDateTimeField {
    private[chrono] def this() {
      this()
      `super`(DateTimeFieldType.halfdayOfDay, cHalfdaysField, cDaysField)
    }

    override def getAsText(fieldValue: Int, locale: Locale): String = {
      return GJLocaleSymbols.forLocale(locale).halfdayValueToText(fieldValue)
    }

    override def set(millis: Long, text: String, locale: Locale): Long = {
      return set(millis, GJLocaleSymbols.forLocale(locale).halfdayTextToValue(text))
    }

    override def getMaximumTextLength(locale: Locale): Int = {
      return GJLocaleSymbols.forLocale(locale).getHalfdayMaxTextLength
    }
  }

  private class YearInfo {
    final val iYear: Int = 0
    final val iFirstDayMillis: Long = 0L

    private[chrono] def this(year: Int, firstDayMillis: Long) {
      this()
      iYear = year
      iFirstDayMillis = firstDayMillis
    }
  }

  try {
    cMillisField = MillisDurationField.INSTANCE
    cSecondsField = new PreciseDurationField(DurationFieldType.seconds, DateTimeConstants.MILLIS_PER_SECOND)
    cMinutesField = new PreciseDurationField(DurationFieldType.minutes, DateTimeConstants.MILLIS_PER_MINUTE)
    cHoursField = new PreciseDurationField(DurationFieldType.hours, DateTimeConstants.MILLIS_PER_HOUR)
    cHalfdaysField = new PreciseDurationField(DurationFieldType.halfdays, DateTimeConstants.MILLIS_PER_DAY / 2)
    cDaysField = new PreciseDurationField(DurationFieldType.days, DateTimeConstants.MILLIS_PER_DAY)
    cWeeksField = new PreciseDurationField(DurationFieldType.weeks, DateTimeConstants.MILLIS_PER_WEEK)
    cMillisOfSecondField = new PreciseDateTimeField(DateTimeFieldType.millisOfSecond, cMillisField, cSecondsField)
    cMillisOfDayField = new PreciseDateTimeField(DateTimeFieldType.millisOfDay, cMillisField, cDaysField)
    cSecondOfMinuteField = new PreciseDateTimeField(DateTimeFieldType.secondOfMinute, cSecondsField, cMinutesField)
    cSecondOfDayField = new PreciseDateTimeField(DateTimeFieldType.secondOfDay, cSecondsField, cDaysField)
    cMinuteOfHourField = new PreciseDateTimeField(DateTimeFieldType.minuteOfHour, cMinutesField, cHoursField)
    cMinuteOfDayField = new PreciseDateTimeField(DateTimeFieldType.minuteOfDay, cMinutesField, cDaysField)
    cHourOfDayField = new PreciseDateTimeField(DateTimeFieldType.hourOfDay, cHoursField, cDaysField)
    cHourOfHalfdayField = new PreciseDateTimeField(DateTimeFieldType.hourOfHalfday, cHoursField, cHalfdaysField)
    cClockhourOfDayField = new ZeroIsMaxDateTimeField(cHourOfDayField, DateTimeFieldType.clockhourOfDay)
    cClockhourOfHalfdayField = new ZeroIsMaxDateTimeField(cHourOfHalfdayField, DateTimeFieldType.clockhourOfHalfday)
    cHalfdayOfDayField = new BasicChronology.HalfdayField
  }
}

@SerialVersionUID(8283225332206808863L)
abstract class BasicChronology extends AssembledChronology {
  @transient
  private final val iYearInfoCache: Array[BasicChronology.YearInfo] = new Array[BasicChronology.YearInfo](BasicChronology.CACHE_SIZE)
  private final val iMinDaysInFirstWeek: Int = 0

  private[chrono] def this(base: Chronology, param: AnyRef, minDaysInFirstWeek: Int) {
    this()
    `super`(base, param)
    if (minDaysInFirstWeek < 1 || minDaysInFirstWeek > 7) {
      throw new IllegalArgumentException("Invalid min days in first week: " + minDaysInFirstWeek)
    }
    iMinDaysInFirstWeek = minDaysInFirstWeek
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

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, millisOfDay: Int): Long = {
    var base: Chronology = null
    if ((({
      base = getBase; base
    })) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, millisOfDay)
    }
    FieldUtils.verifyValueBounds(DateTimeFieldType.millisOfDay, millisOfDay, 0, DateTimeConstants.MILLIS_PER_DAY - 1)
    return getDateMidnightMillis(year, monthOfYear, dayOfMonth) + millisOfDay
  }

  @throws(classOf[IllegalArgumentException])
  override def getDateTimeMillis(year: Int, monthOfYear: Int, dayOfMonth: Int, hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int, millisOfSecond: Int): Long = {
    var base: Chronology = null
    if ((({
      base = getBase; base
    })) != null) {
      return base.getDateTimeMillis(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond)
    }
    FieldUtils.verifyValueBounds(DateTimeFieldType.hourOfDay, hourOfDay, 0, 23)
    FieldUtils.verifyValueBounds(DateTimeFieldType.minuteOfHour, minuteOfHour, 0, 59)
    FieldUtils.verifyValueBounds(DateTimeFieldType.secondOfMinute, secondOfMinute, 0, 59)
    FieldUtils.verifyValueBounds(DateTimeFieldType.millisOfSecond, millisOfSecond, 0, 999)
    return getDateMidnightMillis(year, monthOfYear, dayOfMonth) + hourOfDay * DateTimeConstants.MILLIS_PER_HOUR + minuteOfHour * DateTimeConstants.MILLIS_PER_MINUTE + secondOfMinute * DateTimeConstants.MILLIS_PER_SECOND + millisOfSecond
  }

  def getMinimumDaysInFirstWeek: Int = {
    return iMinDaysInFirstWeek
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
    if (obj != null && getClass eq obj.getClass) {
      val chrono: BasicChronology = obj.asInstanceOf[BasicChronology]
      return getMinimumDaysInFirstWeek == chrono.getMinimumDaysInFirstWeek && (getZone == chrono.getZone)
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
    return getClass.getName.hashCode * 11 + getZone.hashCode + getMinimumDaysInFirstWeek
  }

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  def toString: String = {
    val sb: StringBuilder = new StringBuilder(60)
    var name: String = getClass.getName
    val index: Int = name.lastIndexOf('.')
    if (index >= 0) {
      name = name.substring(index + 1)
    }
    sb.append(name)
    sb.append('[')
    val zone: DateTimeZone = getZone
    if (zone != null) {
      sb.append(zone.getID)
    }
    if (getMinimumDaysInFirstWeek != 4) {
      sb.append(",mdfw=")
      sb.append(getMinimumDaysInFirstWeek)
    }
    sb.append(']')
    return sb.toString
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    fields.millis = BasicChronology.cMillisField
    fields.seconds = BasicChronology.cSecondsField
    fields.minutes = BasicChronology.cMinutesField
    fields.hours = BasicChronology.cHoursField
    fields.halfdays = BasicChronology.cHalfdaysField
    fields.days = BasicChronology.cDaysField
    fields.weeks = BasicChronology.cWeeksField
    fields.millisOfSecond = BasicChronology.cMillisOfSecondField
    fields.millisOfDay = BasicChronology.cMillisOfDayField
    fields.secondOfMinute = BasicChronology.cSecondOfMinuteField
    fields.secondOfDay = BasicChronology.cSecondOfDayField
    fields.minuteOfHour = BasicChronology.cMinuteOfHourField
    fields.minuteOfDay = BasicChronology.cMinuteOfDayField
    fields.hourOfDay = BasicChronology.cHourOfDayField
    fields.hourOfHalfday = BasicChronology.cHourOfHalfdayField
    fields.clockhourOfDay = BasicChronology.cClockhourOfDayField
    fields.clockhourOfHalfday = BasicChronology.cClockhourOfHalfdayField
    fields.halfdayOfDay = BasicChronology.cHalfdayOfDayField
    fields.year = new BasicYearDateTimeField(this)
    fields.yearOfEra = new GJYearOfEraDateTimeField(fields.year, this)
    var field: DateTimeField = new OffsetDateTimeField(fields.yearOfEra, 99)
    fields.centuryOfEra = new DividedDateTimeField(field, DateTimeFieldType.centuryOfEra, 100)
    fields.centuries = fields.centuryOfEra.getDurationField
    field = new RemainderDateTimeField(fields.centuryOfEra.asInstanceOf[DividedDateTimeField])
    fields.yearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.yearOfCentury, 1)
    fields.era = new GJEraDateTimeField(this)
    fields.dayOfWeek = new GJDayOfWeekDateTimeField(this, fields.days)
    fields.dayOfMonth = new BasicDayOfMonthDateTimeField(this, fields.days)
    fields.dayOfYear = new BasicDayOfYearDateTimeField(this, fields.days)
    fields.monthOfYear = new GJMonthOfYearDateTimeField(this)
    fields.weekyear = new BasicWeekyearDateTimeField(this)
    fields.weekOfWeekyear = new BasicWeekOfWeekyearDateTimeField(this, fields.weeks)
    field = new RemainderDateTimeField(fields.weekyear, fields.centuries, DateTimeFieldType.weekyearOfCentury, 100)
    fields.weekyearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.weekyearOfCentury, 1)
    fields.years = fields.year.getDurationField
    fields.months = fields.monthOfYear.getDurationField
    fields.weekyears = fields.weekyear.getDurationField
  }

  /**
   * Get the number of days in the year.
   *
   * @return 366
   */
  private[chrono] def getDaysInYearMax: Int = {
    return 366
  }

  /**
   * Get the number of days in the year.
   *
   * @param year  the year to use
   * @return 366 if a leap year, otherwise 365
   */
  private[chrono] def getDaysInYear(year: Int): Int = {
    return if (isLeapYear(year)) 366 else 365
  }

  /**
   * Get the number of weeks in the year.
   *
   * @param year  the year to use
   * @return number of weeks in the year
   */
  private[chrono] def getWeeksInYear(year: Int): Int = {
    val firstWeekMillis1: Long = getFirstWeekOfYearMillis(year)
    val firstWeekMillis2: Long = getFirstWeekOfYearMillis(year + 1)
    return ((firstWeekMillis2 - firstWeekMillis1) / DateTimeConstants.MILLIS_PER_WEEK).toInt
  }

  /**
   * Get the millis for the first week of a year.
   *
   * @param year  the year to use
   * @return millis
   */
  private[chrono] def getFirstWeekOfYearMillis(year: Int): Long = {
    val jan1millis: Long = getYearMillis(year)
    val jan1dayOfWeek: Int = getDayOfWeek(jan1millis)
    if (jan1dayOfWeek > (8 - iMinDaysInFirstWeek)) {
      return jan1millis + (8 - jan1dayOfWeek) * DateTimeConstants.MILLIS_PER_DAY.toLong
    }
    else {
      return jan1millis - (jan1dayOfWeek - 1) * DateTimeConstants.MILLIS_PER_DAY.toLong
    }
  }

  /**
   * Get the milliseconds for the start of a year.
   *
   * @param year The year to use.
   * @return millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getYearMillis(year: Int): Long = {
    return getYearInfo(year).iFirstDayMillis
  }

  /**
   * Get the milliseconds for the start of a month.
   *
   * @param year The year to use.
   * @param month The month to use
   * @return millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getYearMonthMillis(year: Int, month: Int): Long = {
    var millis: Long = getYearMillis(year)
    millis += getTotalMillisByYearMonth(year, month)
    return millis
  }

  /**
   * Get the milliseconds for a particular date.
   *
   * @param year The year to use.
   * @param month The month to use
   * @param dayOfMonth The day of the month to use
   * @return millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getYearMonthDayMillis(year: Int, month: Int, dayOfMonth: Int): Long = {
    var millis: Long = getYearMillis(year)
    millis += getTotalMillisByYearMonth(year, month)
    return millis + (dayOfMonth - 1) * DateTimeConstants.MILLIS_PER_DAY.toLong
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getYear(instant: Long): Int = {
    val unitMillis: Long = getAverageMillisPerYearDividedByTwo
    var i2: Long = (instant >> 1) + getApproxMillisAtEpochDividedByTwo
    if (i2 < 0) {
      i2 = i2 - unitMillis + 1
    }
    var year: Int = (i2 / unitMillis).toInt
    var yearStart: Long = getYearMillis(year)
    val diff: Long = instant - yearStart
    if (diff < 0) {
      year -= 1
    }
    else if (diff >= DateTimeConstants.MILLIS_PER_DAY * 365L) {
      var oneYear: Long = 0L
      if (isLeapYear(year)) {
        oneYear = DateTimeConstants.MILLIS_PER_DAY * 366L
      }
      else {
        oneYear = DateTimeConstants.MILLIS_PER_DAY * 365L
      }
      yearStart += oneYear
      if (yearStart <= instant) {
        year += 1
      }
    }
    return year
  }

  /**
   * @param millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getMonthOfYear(millis: Long): Int = {
    return getMonthOfYear(millis, getYear(millis))
  }

  /**
   * @param millis from 1970-01-01T00:00:00Z
   * @param year precalculated year of millis
   */
  private[chrono] def getMonthOfYear(millis: Long, year: Int): Int

  /**
   * @param millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getDayOfMonth(millis: Long): Int = {
    val year: Int = getYear(millis)
    val month: Int = getMonthOfYear(millis, year)
    return getDayOfMonth(millis, year, month)
  }

  /**
   * @param millis from 1970-01-01T00:00:00Z
   * @param year precalculated year of millis
   */
  private[chrono] def getDayOfMonth(millis: Long, year: Int): Int = {
    val month: Int = getMonthOfYear(millis, year)
    return getDayOfMonth(millis, year, month)
  }

  /**
   * @param millis from 1970-01-01T00:00:00Z
   * @param year precalculated year of millis
   * @param month precalculated month of millis
   */
  private[chrono] def getDayOfMonth(millis: Long, year: Int, month: Int): Int = {
    var dateMillis: Long = getYearMillis(year)
    dateMillis += getTotalMillisByYearMonth(year, month)
    return ((millis - dateMillis) / DateTimeConstants.MILLIS_PER_DAY).toInt + 1
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getDayOfYear(instant: Long): Int = {
    return getDayOfYear(instant, getYear(instant))
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   * @param year precalculated year of millis
   */
  private[chrono] def getDayOfYear(instant: Long, year: Int): Int = {
    val yearStart: Long = getYearMillis(year)
    return ((instant - yearStart) / DateTimeConstants.MILLIS_PER_DAY).toInt + 1
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getWeekyear(instant: Long): Int = {
    val year: Int = getYear(instant)
    val week: Int = getWeekOfWeekyear(instant, year)
    if (week == 1) {
      return getYear(instant + DateTimeConstants.MILLIS_PER_WEEK)
    }
    else if (week > 51) {
      return getYear(instant - (2 * DateTimeConstants.MILLIS_PER_WEEK))
    }
    else {
      return year
    }
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getWeekOfWeekyear(instant: Long): Int = {
    return getWeekOfWeekyear(instant, getYear(instant))
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   * @param year precalculated year of millis
   */
  private[chrono] def getWeekOfWeekyear(instant: Long, year: Int): Int = {
    val firstWeekMillis1: Long = getFirstWeekOfYearMillis(year)
    if (instant < firstWeekMillis1) {
      return getWeeksInYear(year - 1)
    }
    val firstWeekMillis2: Long = getFirstWeekOfYearMillis(year + 1)
    if (instant >= firstWeekMillis2) {
      return 1
    }
    return ((instant - firstWeekMillis1) / DateTimeConstants.MILLIS_PER_WEEK).toInt + 1
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getDayOfWeek(instant: Long): Int = {
    var daysSince19700101: Long = 0L
    if (instant >= 0) {
      daysSince19700101 = instant / DateTimeConstants.MILLIS_PER_DAY
    }
    else {
      daysSince19700101 = (instant - (DateTimeConstants.MILLIS_PER_DAY - 1)) / DateTimeConstants.MILLIS_PER_DAY
      if (daysSince19700101 < -3) {
        return 7 + ((daysSince19700101 + 4) % 7).toInt
      }
    }
    return 1 + ((daysSince19700101 + 3) % 7).toInt
  }

  /**
   * @param instant millis from 1970-01-01T00:00:00Z
   */
  private[chrono] def getMillisOfDay(instant: Long): Int = {
    if (instant >= 0) {
      return (instant % DateTimeConstants.MILLIS_PER_DAY).toInt
    }
    else {
      return (DateTimeConstants.MILLIS_PER_DAY - 1) + ((instant + 1) % DateTimeConstants.MILLIS_PER_DAY).toInt
    }
  }

  /**
   * Gets the maximum number of days in any month.
   *
   * @return 31
   */
  private[chrono] def getDaysInMonthMax: Int = {
    return 31
  }

  /**
   * Gets the maximum number of days in the month specified by the instant.
   *
   * @param instant  millis from 1970-01-01T00:00:00Z
   * @return the maximum number of days in the month
   */
  private[chrono] def getDaysInMonthMax(instant: Long): Int = {
    val thisYear: Int = getYear(instant)
    val thisMonth: Int = getMonthOfYear(instant, thisYear)
    return getDaysInYearMonth(thisYear, thisMonth)
  }

  /**
   * Gets the maximum number of days in the month specified by the instant.
   * The value represents what the user is trying to set, and can be
   * used to optimise this method.
   *
   * @param instant  millis from 1970-01-01T00:00:00Z
   * @param value  the value being set
   * @return the maximum number of days in the month
   */
  private[chrono] def getDaysInMonthMaxForSet(instant: Long, value: Int): Int = {
    return getDaysInMonthMax(instant)
  }

  /**
   * Gets the milliseconds for a date at midnight.
   *
   * @param year  the year
   * @param monthOfYear  the month
   * @param dayOfMonth  the day
   * @return the milliseconds
   */
  private[chrono] def getDateMidnightMillis(year: Int, monthOfYear: Int, dayOfMonth: Int): Long = {
    FieldUtils.verifyValueBounds(DateTimeFieldType.year, year, getMinYear, getMaxYear)
    FieldUtils.verifyValueBounds(DateTimeFieldType.monthOfYear, monthOfYear, 1, getMaxMonth(year))
    FieldUtils.verifyValueBounds(DateTimeFieldType.dayOfMonth, dayOfMonth, 1, getDaysInYearMonth(year, monthOfYear))
    return getYearMonthDayMillis(year, monthOfYear, dayOfMonth)
  }

  /**
   * Gets the difference between the two instants in years.
   *
   * @param minuendInstant  the first instant
   * @param subtrahendInstant  the second instant
   * @return the difference
   */
  private[chrono] def getYearDifference(minuendInstant: Long, subtrahendInstant: Long): Long

  /**
   * Is the specified year a leap year?
   *
   * @param year  the year to test
   * @return true if leap
   */
  private[chrono] def isLeapYear(year: Int): Boolean

  /**
   * Is the specified instant a leap day?
   *
   * @param instant  the instant to test
   * @return true if leap, default is false
   */
  private[chrono] def isLeapDay(instant: Long): Boolean = {
    return false
  }

  /**
   * Gets the number of days in the specified month and year.
   *
   * @param year  the year
   * @param month  the month
   * @return the number of days
   */
  private[chrono] def getDaysInYearMonth(year: Int, month: Int): Int

  /**
   * Gets the maximum days in the specified month.
   *
   * @param month  the month
   * @return the max days
   */
  private[chrono] def getDaysInMonthMax(month: Int): Int

  /**
   * Gets the total number of millis elapsed in this year at the start
   * of the specified month, such as zero for month 1.
   *
   * @param year  the year
   * @param month  the month
   * @return the elapsed millis at the start of the month
   */
  private[chrono] def getTotalMillisByYearMonth(year: Int, month: Int): Long

  /**
   * Gets the millisecond value of the first day of the year.
   *
   * @return the milliseconds for the first of the year
   */
  private[chrono] def calculateFirstDayOfYearMillis(year: Int): Long

  /**
   * Gets the minimum supported year.
   *
   * @return the year
   */
  private[chrono] def getMinYear: Int

  /**
   * Gets the maximum supported year.
   *
   * @return the year
   */
  private[chrono] def getMaxYear: Int

  /**
   * Gets the maximum month for the specified year.
   * This implementation calls getMaxMonth().
   *
   * @param year  the year
   * @return the maximum month value
   */
  private[chrono] def getMaxMonth(year: Int): Int = {
    return getMaxMonth
  }

  /**
   * Gets the maximum number of months.
   *
   * @return 12
   */
  private[chrono] def getMaxMonth: Int = {
    return 12
  }

  /**
   * Gets an average value for the milliseconds per year.
   *
   * @return the millis per year
   */
  private[chrono] def getAverageMillisPerYear: Long

  /**
   * Gets an average value for the milliseconds per year, divided by two.
   *
   * @return the millis per year divided by two
   */
  private[chrono] def getAverageMillisPerYearDividedByTwo: Long

  /**
   * Gets an average value for the milliseconds per month.
   *
   * @return the millis per month
   */
  private[chrono] def getAverageMillisPerMonth: Long

  /**
   * Returns a constant representing the approximate number of milliseconds
   * elapsed from year 0 of this chronology, divided by two. This constant
   * <em>must</em> be defined as:
   * <pre>
   * (yearAtEpoch * averageMillisPerYear + millisOfYearAtEpoch) / 2
   * </pre>
   * where epoch is 1970-01-01 (Gregorian).
   */
  private[chrono] def getApproxMillisAtEpochDividedByTwo: Long

  /**
   * Sets the year from an instant and year.
   *
   * @param instant  millis from 1970-01-01T00:00:00Z
   * @param year  the year to set
   * @return the updated millis
   */
  private[chrono] def setYear(instant: Long, year: Int): Long

  private def getYearInfo(year: Int): BasicChronology.YearInfo = {
    var info: BasicChronology.YearInfo = iYearInfoCache(year & BasicChronology.CACHE_MASK)
    if (info == null || info.iYear != year) {
      info = new BasicChronology.YearInfo(year, calculateFirstDayOfYearMillis(year))
      iYearInfoCache(year & BasicChronology.CACHE_MASK) = info
    }
    return info
  }
}
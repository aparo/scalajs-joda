/*
 *  Copyright 2001-2011 Stephen Colebourne
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
package org.joda.time.base

import java.io.Serializable
import org.joda.time.Chronology
import org.joda.time.DateTimeUtils
import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.MutablePeriod
import org.joda.time.PeriodType
import org.joda.time.ReadWritablePeriod
import org.joda.time.ReadableDuration
import org.joda.time.ReadableInstant
import org.joda.time.ReadablePartial
import org.joda.time.ReadablePeriod
import org.joda.time.chrono.ISOChronology
import org.joda.time.convert.ConverterManager
import org.joda.time.convert.PeriodConverter
import org.joda.time.field.FieldUtils

/**
 * BasePeriod is an abstract implementation of ReadablePeriod that stores
 * data in a <code>PeriodType</code> and an <code>int[]</code>.
 * <p>
 * This class should generally not be used directly by API users.
 * The {@link ReadablePeriod} interface should be used when different
 * kinds of period objects are to be referenced.
 * <p>
 * BasePeriod subclasses may be mutable and not thread-safe.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(-2110953284060001145L)
object BasePeriod {
  /** Serialization version */
  private val DUMMY_PERIOD: ReadablePeriod =
  new class null
  {
    def getValue(index: Int): Int = {
      return 0
    }
    def getPeriodType: PeriodType = {
      return PeriodType.time
    }
  }
}

@SerialVersionUID(-2110953284060001145L)
abstract class BasePeriod extends AbstractPeriod with ReadablePeriod with Serializable {
  /** The type of period */
  private final val iType: PeriodType = null
  /** The values */
  private final val iValues: Array[Int] = null

  /**
   * Creates a period from a set of field values.
   *
   * @param years  amount of years in this period, which must be zero if unsupported
   * @param months  amount of months in this period, which must be zero if unsupported
   * @param weeks  amount of weeks in this period, which must be zero if unsupported
   * @param days  amount of days in this period, which must be zero if unsupported
   * @param hours  amount of hours in this period, which must be zero if unsupported
   * @param minutes  amount of minutes in this period, which must be zero if unsupported
   * @param seconds  amount of seconds in this period, which must be zero if unsupported
   * @param millis  amount of milliseconds in this period, which must be zero if unsupported
   * @param type  which set of fields this period supports
   * @throws IllegalArgumentException if period type is invalid
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def this(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int, `type`: PeriodType) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    iType = `type`
    iValues = setPeriodInternal(years, months, weeks, days, hours, minutes, seconds, millis)
  }

  /**
   * Creates a period from the given interval endpoints.
   *
   * @param startInstant  interval start, in milliseconds
   * @param endInstant  interval end, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   * @param chrono  the chronology to use, null means ISO default
   * @throws IllegalArgumentException if period type is invalid
   */
  protected def this(startInstant: Long, endInstant: Long, `type`: PeriodType, chrono: Chronology) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    chrono = DateTimeUtils.getChronology(chrono)
    iType = `type`
    iValues = chrono.get(this, startInstant, endInstant)
  }

  /**
   * Creates a period from the given interval endpoints.
   *
   * @param startInstant  interval start, null means now
   * @param endInstant  interval end, null means now
   * @param type  which set of fields this period supports, null means standard
   * @throws IllegalArgumentException if period type is invalid
   */
  protected def this(startInstant: ReadableInstant, endInstant: ReadableInstant, `type`: PeriodType) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    if (startInstant == null && endInstant == null) {
      iType = `type`
      iValues = new Array[Int](size)
    }
    else {
      val startMillis: Long = DateTimeUtils.getInstantMillis(startInstant)
      val endMillis: Long = DateTimeUtils.getInstantMillis(endInstant)
      val chrono: Chronology = DateTimeUtils.getIntervalChronology(startInstant, endInstant)
      iType = `type`
      iValues = chrono.get(this, startMillis, endMillis)
    }
  }

  /**
   * Creates a period from the given duration and end point.
   * <p>
   * The two partials must contain the same fields, thus you can
   * specify two <code>LocalDate</code> objects, or two <code>LocalTime</code>
   * objects, but not one of each.
   * As these are Partial objects, time zones have no effect on the result.
   * <p>
   * The two partials must also both be contiguous - see
   * {@link DateTimeUtils#isContiguous(ReadablePartial)} for a
   * definition. Both <code>LocalDate</code> and <code>LocalTime</code> are contiguous.
   *
   * @param start  the start of the period, must not be null
   * @param end  the end of the period, must not be null
   * @param type  which set of fields this period supports, null means standard
   * @throws IllegalArgumentException if the partials are null or invalid
   * @since 1.1
   */
  protected def this(start: ReadablePartial, end: ReadablePartial, `type`: PeriodType) {
    this()
    `super`
    if (start == null || end == null) {
      throw new IllegalArgumentException("ReadablePartial objects must not be null")
    }
    if (start.isInstanceOf[BaseLocal] && end.isInstanceOf[BaseLocal] && start.getClass eq end.getClass) {
      `type` = checkPeriodType(`type`)
      val startMillis: Long = (start.asInstanceOf[BaseLocal]).getLocalMillis
      val endMillis: Long = (end.asInstanceOf[BaseLocal]).getLocalMillis
      var chrono: Chronology = start.getChronology
      chrono = DateTimeUtils.getChronology(chrono)
      iType = `type`
      iValues = chrono.get(this, startMillis, endMillis)
    }
    else {
      if (start.size != end.size) {
        throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
      }
      {
        var i: Int = 0
        val isize: Int = start.size
        while (i < isize) {
          {
            if (start.getFieldType(i) ne end.getFieldType(i)) {
              throw new IllegalArgumentException("ReadablePartial objects must have the same set of fields")
            }
          }
          ({
            i += 1; i - 1
          })
        }
      }
      if (DateTimeUtils.isContiguous(start) == false) {
        throw new IllegalArgumentException("ReadablePartial objects must be contiguous")
      }
      iType = checkPeriodType(`type`)
      val chrono: Chronology = DateTimeUtils.getChronology(start.getChronology).withUTC
      iValues = chrono.get(this, chrono.set(start, 0L), chrono.set(end, 0L))
    }
  }

  /**
   * Creates a period from the given start point and duration.
   *
   * @param startInstant  the interval start, null means now
   * @param duration  the duration of the interval, null means zero-length
   * @param type  which set of fields this period supports, null means standard
   */
  protected def this(startInstant: ReadableInstant, duration: ReadableDuration, `type`: PeriodType) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    val startMillis: Long = DateTimeUtils.getInstantMillis(startInstant)
    val durationMillis: Long = DateTimeUtils.getDurationMillis(duration)
    val endMillis: Long = FieldUtils.safeAdd(startMillis, durationMillis)
    val chrono: Chronology = DateTimeUtils.getInstantChronology(startInstant)
    iType = `type`
    iValues = chrono.get(this, startMillis, endMillis)
  }

  /**
   * Creates a period from the given duration and end point.
   *
   * @param duration  the duration of the interval, null means zero-length
   * @param endInstant  the interval end, null means now
   * @param type  which set of fields this period supports, null means standard
   */
  protected def this(duration: ReadableDuration, endInstant: ReadableInstant, `type`: PeriodType) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    val durationMillis: Long = DateTimeUtils.getDurationMillis(duration)
    val endMillis: Long = DateTimeUtils.getInstantMillis(endInstant)
    val startMillis: Long = FieldUtils.safeSubtract(endMillis, durationMillis)
    val chrono: Chronology = DateTimeUtils.getInstantChronology(endInstant)
    iType = `type`
    iValues = chrono.get(this, startMillis, endMillis)
  }

  /**
   * Creates a period from the given millisecond duration with the standard period type
   * and ISO rules, ensuring that the calculation is performed with the time-only period type.
   * <p>
   * The calculation uses the hour, minute, second and millisecond fields.
   *
   * @param duration  the duration, in milliseconds
   */
  protected def this(duration: Long) {
    this()
    `super`
    iType = PeriodType.standard
    val values: Array[Int] = ISOChronology.getInstanceUTC.get(BasePeriod.DUMMY_PERIOD, duration)
    iValues = new Array[Int](8)
    System.arraycopy(values, 0, iValues, 4, 4)
  }

  /**
   * Creates a period from the given millisecond duration, which is only really
   * suitable for durations less than one day.
   * <p>
   * Only fields that are precise will be used.
   * Thus the largest precise field may have a large value.
   *
   * @param duration  the duration, in milliseconds
   * @param type  which set of fields this period supports, null means standard
   * @param chrono  the chronology to use, null means ISO default
   * @throws IllegalArgumentException if period type is invalid
   */
  protected def this(duration: Long, `type`: PeriodType, chrono: Chronology) {
    this()
    `super`
    `type` = checkPeriodType(`type`)
    chrono = DateTimeUtils.getChronology(chrono)
    iType = `type`
    iValues = chrono.get(this, duration)
  }

  /**
   * Creates a new period based on another using the {@link ConverterManager}.
   *
   * @param period  the period to convert
   * @param type  which set of fields this period supports, null means use type from object
   * @param chrono  the chronology to use, null means ISO default
   * @throws IllegalArgumentException if period is invalid
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def this(period: AnyRef, `type`: PeriodType, chrono: Chronology) {
    this()
    `super`
    val converter: PeriodConverter = ConverterManager.getInstance.getPeriodConverter(period)
    `type` = (if (`type` == null) converter.getPeriodType(period) else `type`)
    `type` = checkPeriodType(`type`)
    iType = `type`
    if (this.isInstanceOf[ReadWritablePeriod]) {
      iValues = new Array[Int](size)
      chrono = DateTimeUtils.getChronology(chrono)
      converter.setInto(this.asInstanceOf[ReadWritablePeriod], period, chrono)
    }
    else {
      iValues = new MutablePeriod(period, `type`, chrono).getValues
    }
  }

  /**
   * Constructor used when we trust ourselves.
   * Do not expose publically.
   *
   * @param values  the values to use, not null, not cloned
   * @param type  which set of fields this period supports, not null
   */
  protected def this(values: Array[Int], `type`: PeriodType) {
    this()
    `super`
    iType = `type`
    iValues = values
  }

  /**
   * Validates a period type, converting nulls to a default value and
   * checking the type is suitable for this instance.
   *
   * @param type  the type to check, may be null
   * @return the validated type to use, not null
   * @throws IllegalArgumentException if the period type is invalid
   */
  protected def checkPeriodType(`type`: PeriodType): PeriodType = {
    return DateTimeUtils.getPeriodType(`type`)
  }

  /**
   * Gets the period type.
   *
   * @return the period type
   */
  def getPeriodType: PeriodType = {
    return iType
  }

  /**
   * Gets the value at the specified index.
   *
   * @param index  the index to retrieve
   * @return the value of the field at the specified index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getValue(index: Int): Int = {
    return iValues(index)
  }

  /**
   * Gets the total millisecond duration of this period relative to a start instant.
   * <p>
   * This method adds the period to the specified instant in order to
   * calculate the duration.
   * <p>
   * An instant must be supplied as the duration of a period varies.
   * For example, a period of 1 month could vary between the equivalent of
   * 28 and 31 days in milliseconds due to different length months.
   * Similarly, a day can vary at Daylight Savings cutover, typically between
   * 23 and 25 hours.
   *
   * @param startInstant  the instant to add the period to, thus obtaining the duration
   * @return the total length of the period as a duration relative to the start instant
   * @throws ArithmeticException if the millis exceeds the capacity of the duration
   */
  def toDurationFrom(startInstant: ReadableInstant): Duration = {
    val startMillis: Long = DateTimeUtils.getInstantMillis(startInstant)
    val chrono: Chronology = DateTimeUtils.getInstantChronology(startInstant)
    val endMillis: Long = chrono.add(this, startMillis, 1)
    return new Duration(startMillis, endMillis)
  }

  /**
   * Gets the total millisecond duration of this period relative to an
   * end instant.
   * <p>
   * This method subtracts the period from the specified instant in order
   * to calculate the duration.
   * <p>
   * An instant must be supplied as the duration of a period varies.
   * For example, a period of 1 month could vary between the equivalent of
   * 28 and 31 days in milliseconds due to different length months.
   * Similarly, a day can vary at Daylight Savings cutover, typically between
   * 23 and 25 hours.
   *
   * @param endInstant  the instant to subtract the period from, thus obtaining the duration
   * @return the total length of the period as a duration relative to the end instant
   * @throws ArithmeticException if the millis exceeds the capacity of the duration
   */
  def toDurationTo(endInstant: ReadableInstant): Duration = {
    val endMillis: Long = DateTimeUtils.getInstantMillis(endInstant)
    val chrono: Chronology = DateTimeUtils.getInstantChronology(endInstant)
    val startMillis: Long = chrono.add(this, endMillis, -1)
    return new Duration(startMillis, endMillis)
  }

  /**
   * Checks whether a field type is supported, and if so adds the new value
   * to the relevant index in the specified array.
   *
   * @param type  the field type
   * @param values  the array to update
   * @param newValue  the new value to store if successful
   */
  private def checkAndUpdate(`type`: DurationFieldType, values: Array[Int], newValue: Int) {
    val index: Int = indexOf(`type`)
    if (index == -1) {
      if (newValue != 0) {
        throw new IllegalArgumentException("Period does not support field '" + `type`.getName + "'")
      }
    }
    else {
      values(index) = newValue
    }
  }

  /**
   * Sets all the fields of this period from another.
   *
   * @param period  the period to copy from, not null
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def setPeriod(period: ReadablePeriod) {
    if (period == null) {
      setValues(new Array[Int](size))
    }
    else {
      setPeriodInternal(period)
    }
  }

  /**
   * Private method called from constructor.
   */
  private def setPeriodInternal(period: ReadablePeriod) {
    val newValues: Array[Int] = new Array[Int](size)
    {
      var i: Int = 0
      val isize: Int = period.size
      while (i < isize) {
        {
          val `type`: DurationFieldType = period.getFieldType(i)
          val value: Int = period.getValue(i)
          checkAndUpdate(`type`, newValues, value)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    setValues(newValues)
  }

  /**
   * Sets the eight standard the fields in one go.
   *
   * @param years  amount of years in this period, which must be zero if unsupported
   * @param months  amount of months in this period, which must be zero if unsupported
   * @param weeks  amount of weeks in this period, which must be zero if unsupported
   * @param days  amount of days in this period, which must be zero if unsupported
   * @param hours  amount of hours in this period, which must be zero if unsupported
   * @param minutes  amount of minutes in this period, which must be zero if unsupported
   * @param seconds  amount of seconds in this period, which must be zero if unsupported
   * @param millis  amount of milliseconds in this period, which must be zero if unsupported
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def setPeriod(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int) {
    val newValues: Array[Int] = setPeriodInternal(years, months, weeks, days, hours, minutes, seconds, millis)
    setValues(newValues)
  }

  /**
   * Private method called from constructor.
   */
  private def setPeriodInternal(years: Int, months: Int, weeks: Int, days: Int, hours: Int, minutes: Int, seconds: Int, millis: Int): Array[Int] = {
    val newValues: Array[Int] = new Array[Int](size)
    checkAndUpdate(DurationFieldType.years, newValues, years)
    checkAndUpdate(DurationFieldType.months, newValues, months)
    checkAndUpdate(DurationFieldType.weeks, newValues, weeks)
    checkAndUpdate(DurationFieldType.days, newValues, days)
    checkAndUpdate(DurationFieldType.hours, newValues, hours)
    checkAndUpdate(DurationFieldType.minutes, newValues, minutes)
    checkAndUpdate(DurationFieldType.seconds, newValues, seconds)
    checkAndUpdate(DurationFieldType.millis, newValues, millis)
    return newValues
  }

  /**
   * Sets the value of a field in this period.
   *
   * @param field  the field to set
   * @param value  the value to set
   * @throws IllegalArgumentException if field is is null or not supported.
   */
  protected def setField(field: DurationFieldType, value: Int) {
    setFieldInto(iValues, field, value)
  }

  /**
   * Sets the value of a field in this period.
   *
   * @param values  the array of values to update
   * @param field  the field to set
   * @param value  the value to set
   * @throws IllegalArgumentException if field is null or not supported.
   */
  protected def setFieldInto(values: Array[Int], field: DurationFieldType, value: Int) {
    val index: Int = indexOf(field)
    if (index == -1) {
      if (value != 0 || field == null) {
        throw new IllegalArgumentException("Period does not support field '" + field + "'")
      }
    }
    else {
      values(index) = value
    }
  }

  /**
   * Adds the value of a field in this period.
   *
   * @param field  the field to set
   * @param value  the value to set
   * @throws IllegalArgumentException if field is is null or not supported.
   */
  protected def addField(field: DurationFieldType, value: Int) {
    addFieldInto(iValues, field, value)
  }

  /**
   * Adds the value of a field in this period.
   *
   * @param values  the array of values to update
   * @param field  the field to set
   * @param value  the value to set
   * @throws IllegalArgumentException if field is is null or not supported.
   */
  protected def addFieldInto(values: Array[Int], field: DurationFieldType, value: Int) {
    val index: Int = indexOf(field)
    if (index == -1) {
      if (value != 0 || field == null) {
        throw new IllegalArgumentException("Period does not support field '" + field + "'")
      }
    }
    else {
      values(index) = FieldUtils.safeAdd(values(index), value)
    }
  }

  /**
   * Merges the fields from another period.
   *
   * @param period  the period to add from, not null
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def mergePeriod(period: ReadablePeriod) {
    if (period != null) {
      setValues(mergePeriodInto(getValues, period))
    }
  }

  /**
   * Merges the fields from another period.
   *
   * @param values  the array of values to update
   * @param period  the period to add from, not null
   * @return the updated values
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def mergePeriodInto(values: Array[Int], period: ReadablePeriod): Array[Int] = {
    {
      var i: Int = 0
      val isize: Int = period.size
      while (i < isize) {
        {
          val `type`: DurationFieldType = period.getFieldType(i)
          val value: Int = period.getValue(i)
          checkAndUpdate(`type`, values, value)
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return values
  }

  /**
   * Adds the fields from another period.
   *
   * @param period  the period to add from, not null
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def addPeriod(period: ReadablePeriod) {
    if (period != null) {
      setValues(addPeriodInto(getValues, period))
    }
  }

  /**
   * Adds the fields from another period.
   *
   * @param values  the array of values to update
   * @param period  the period to add from, not null
   * @return the updated values
   * @throws IllegalArgumentException if an unsupported field's value is non-zero
   */
  protected def addPeriodInto(values: Array[Int], period: ReadablePeriod): Array[Int] = {
    {
      var i: Int = 0
      val isize: Int = period.size
      while (i < isize) {
        {
          val `type`: DurationFieldType = period.getFieldType(i)
          val value: Int = period.getValue(i)
          if (value != 0) {
            val index: Int = indexOf(`type`)
            if (index == -1) {
              throw new IllegalArgumentException("Period does not support field '" + `type`.getName + "'")
            }
            else {
              values(index) = FieldUtils.safeAdd(getValue(index), value)
            }
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return values
  }

  /**
   * Sets the value of the field at the specified index.
   *
   * @param index  the index
   * @param value  the value to set
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  protected def setValue(index: Int, value: Int) {
    iValues(index) = value
  }

  /**
   * Sets the values of all fields.
   * <p>
   * In version 2.0 and later, this method copies the array into the original.
   * This is because the instance variable has been changed to be final to satisfy the Java Memory Model.
   * This only impacts subclasses that are mutable.
   *
   * @param values  the array of values
   */
  protected def setValues(values: Array[Int]) {
    System.arraycopy(values, 0, iValues, 0, iValues.length)
  }
}
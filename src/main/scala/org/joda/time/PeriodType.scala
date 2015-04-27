/*
 *  Copyright 2001-2009 Stephen Colebourne
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
package org.joda.time

import java.io.Serializable
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.List
import java.util.Map
import org.joda.time.field.FieldUtils

/**
 * Controls a period implementation by specifying which duration fields are to be used.
 * <p>
 * The following implementations are provided:
 * <ul>
 * <li>Standard - years, months, weeks, days, hours, minutes, seconds, millis
 * <li>YearMonthDayTime - years, months, days, hours, minutes, seconds, millis
 * <li>YearMonthDay - years, months, days
 * <li>YearWeekDayTime - years, weeks, days, hours, minutes, seconds, millis
 * <li>YearWeekDay - years, weeks, days
 * <li>YearDayTime - years, days, hours, minutes, seconds, millis
 * <li>YearDay - years, days, hours
 * <li>DayTime - days, hours, minutes, seconds, millis
 * <li>Time - hours, minutes, seconds, millis
 * <li>plus one for each single type
 * </ul>
 *
 * <p>
 * PeriodType is thread-safe and immutable, and all subclasses must be as well.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(2274324892792009998L)
object PeriodType {
  /** Cache of all the known types. */
  private val cTypes: Map[PeriodType, AnyRef] = new HashMap[PeriodType, AnyRef](32)
  private[time] var YEAR_INDEX: Int = 0
  private[time] var MONTH_INDEX: Int = 1
  private[time] var WEEK_INDEX: Int = 2
  private[time] var DAY_INDEX: Int = 3
  private[time] var HOUR_INDEX: Int = 4
  private[time] var MINUTE_INDEX: Int = 5
  private[time] var SECOND_INDEX: Int = 6
  private[time] var MILLI_INDEX: Int = 7
  private var cStandard: PeriodType = null
  private var cYMDTime: PeriodType = null
  private var cYMD: PeriodType = null
  private var cYWDTime: PeriodType = null
  private var cYWD: PeriodType = null
  private var cYDTime: PeriodType = null
  private var cYD: PeriodType = null
  private var cDTime: PeriodType = null
  private var cTime: PeriodType = null
  private var cYears: PeriodType = null
  private var cMonths: PeriodType = null
  private var cWeeks: PeriodType = null
  private var cDays: PeriodType = null
  private var cHours: PeriodType = null
  private var cMinutes: PeriodType = null
  private var cSeconds: PeriodType = null
  private var cMillis: PeriodType = null

  /**
   * Gets a type that defines all standard fields.
   * <ul>
   * <li>years
   * <li>months
   * <li>weeks
   * <li>days
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def standard: PeriodType = {
    var `type`: PeriodType = cStandard
    if (`type` == null) {
      `type` = new PeriodType("Standard", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.months, DurationFieldType.weeks, DurationFieldType.days, DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](0, 1, 2, 3, 4, 5, 6, 7))
      cStandard = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines all standard fields except weeks.
   * <ul>
   * <li>years
   * <li>months
   * <li>days
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def yearMonthDayTime: PeriodType = {
    var `type`: PeriodType = cYMDTime
    if (`type` == null) {
      `type` = new PeriodType("YearMonthDayTime", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.months, DurationFieldType.days, DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](0, 1, -1, 2, 3, 4, 5, 6))
      cYMDTime = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines the year, month and day fields.
   * <ul>
   * <li>years
   * <li>months
   * <li>days
   * </ul>
   *
   * @return the period type
   * @since 1.1
   */
  def yearMonthDay: PeriodType = {
    var `type`: PeriodType = cYMD
    if (`type` == null) {
      `type` = new PeriodType("YearMonthDay", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.months, DurationFieldType.days), Array[Int](0, 1, -1, 2, -1, -1, -1, -1))
      cYMD = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines all standard fields except months.
   * <ul>
   * <li>years
   * <li>weeks
   * <li>days
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def yearWeekDayTime: PeriodType = {
    var `type`: PeriodType = cYWDTime
    if (`type` == null) {
      `type` = new PeriodType("YearWeekDayTime", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.weeks, DurationFieldType.days, DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](0, -1, 1, 2, 3, 4, 5, 6))
      cYWDTime = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines year, week and day fields.
   * <ul>
   * <li>years
   * <li>weeks
   * <li>days
   * </ul>
   *
   * @return the period type
   * @since 1.1
   */
  def yearWeekDay: PeriodType = {
    var `type`: PeriodType = cYWD
    if (`type` == null) {
      `type` = new PeriodType("YearWeekDay", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.weeks, DurationFieldType.days), Array[Int](0, -1, 1, 2, -1, -1, -1, -1))
      cYWD = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines all standard fields except months and weeks.
   * <ul>
   * <li>years
   * <li>days
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def yearDayTime: PeriodType = {
    var `type`: PeriodType = cYDTime
    if (`type` == null) {
      `type` = new PeriodType("YearDayTime", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.days, DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](0, -1, -1, 1, 2, 3, 4, 5))
      cYDTime = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines the year and day fields.
   * <ul>
   * <li>years
   * <li>days
   * </ul>
   *
   * @return the period type
   * @since 1.1
   */
  def yearDay: PeriodType = {
    var `type`: PeriodType = cYD
    if (`type` == null) {
      `type` = new PeriodType("YearDay", Array[DurationFieldType](DurationFieldType.years, DurationFieldType.days), Array[Int](0, -1, -1, 1, -1, -1, -1, -1))
      cYD = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines all standard fields from days downwards.
   * <ul>
   * <li>days
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def dayTime: PeriodType = {
    var `type`: PeriodType = cDTime
    if (`type` == null) {
      `type` = new PeriodType("DayTime", Array[DurationFieldType](DurationFieldType.days, DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](-1, -1, -1, 0, 1, 2, 3, 4))
      cDTime = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines all standard time fields.
   * <ul>
   * <li>hours
   * <li>minutes
   * <li>seconds
   * <li>milliseconds
   * </ul>
   *
   * @return the period type
   */
  def time: PeriodType = {
    var `type`: PeriodType = cTime
    if (`type` == null) {
      `type` = new PeriodType("Time", Array[DurationFieldType](DurationFieldType.hours, DurationFieldType.minutes, DurationFieldType.seconds, DurationFieldType.millis), Array[Int](-1, -1, -1, -1, 0, 1, 2, 3))
      cTime = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the years field.
   *
   * @return the period type
   */
  def years: PeriodType = {
    var `type`: PeriodType = cYears
    if (`type` == null) {
      `type` = new PeriodType("Years", Array[DurationFieldType](DurationFieldType.years), Array[Int](0, -1, -1, -1, -1, -1, -1, -1))
      cYears = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the months field.
   *
   * @return the period type
   */
  def months: PeriodType = {
    var `type`: PeriodType = cMonths
    if (`type` == null) {
      `type` = new PeriodType("Months", Array[DurationFieldType](DurationFieldType.months), Array[Int](-1, 0, -1, -1, -1, -1, -1, -1))
      cMonths = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the weeks field.
   *
   * @return the period type
   */
  def weeks: PeriodType = {
    var `type`: PeriodType = cWeeks
    if (`type` == null) {
      `type` = new PeriodType("Weeks", Array[DurationFieldType](DurationFieldType.weeks), Array[Int](-1, -1, 0, -1, -1, -1, -1, -1))
      cWeeks = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the days field.
   *
   * @return the period type
   */
  def days: PeriodType = {
    var `type`: PeriodType = cDays
    if (`type` == null) {
      `type` = new PeriodType("Days", Array[DurationFieldType](DurationFieldType.days), Array[Int](-1, -1, -1, 0, -1, -1, -1, -1))
      cDays = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the hours field.
   *
   * @return the period type
   */
  def hours: PeriodType = {
    var `type`: PeriodType = cHours
    if (`type` == null) {
      `type` = new PeriodType("Hours", Array[DurationFieldType](DurationFieldType.hours), Array[Int](-1, -1, -1, -1, 0, -1, -1, -1))
      cHours = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the minutes field.
   *
   * @return the period type
   */
  def minutes: PeriodType = {
    var `type`: PeriodType = cMinutes
    if (`type` == null) {
      `type` = new PeriodType("Minutes", Array[DurationFieldType](DurationFieldType.minutes), Array[Int](-1, -1, -1, -1, -1, 0, -1, -1))
      cMinutes = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the seconds field.
   *
   * @return the period type
   */
  def seconds: PeriodType = {
    var `type`: PeriodType = cSeconds
    if (`type` == null) {
      `type` = new PeriodType("Seconds", Array[DurationFieldType](DurationFieldType.seconds), Array[Int](-1, -1, -1, -1, -1, -1, 0, -1))
      cSeconds = `type`
    }
    return `type`
  }

  /**
   * Gets a type that defines just the millis field.
   *
   * @return the period type
   */
  def millis: PeriodType = {
    var `type`: PeriodType = cMillis
    if (`type` == null) {
      `type` = new PeriodType("Millis", Array[DurationFieldType](DurationFieldType.millis), Array[Int](-1, -1, -1, -1, -1, -1, -1, 0))
      cMillis = `type`
    }
    return `type`
  }

  /**
   * Gets a period type that contains the duration types of the array.
   * <p>
   * Only the 8 standard duration field types are supported.
   *
   * @param types  the types to include in the array.
   * @return the period type
   * @since 1.1
   */
  def forFields(types: Array[DurationFieldType]): PeriodType = {
    if (types == null || types.length == 0) {
      throw new IllegalArgumentException("Types array must not be null or empty")
    }
    {
      var i: Int = 0
      while (i < types.length) {
        {
          if (types(i) == null) {
            throw new IllegalArgumentException("Types array must not contain null")
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    val cache: Map[PeriodType, AnyRef] = cTypes
    if (cache.isEmpty) {
      cache.put(standard, standard)
      cache.put(yearMonthDayTime, yearMonthDayTime)
      cache.put(yearMonthDay, yearMonthDay)
      cache.put(yearWeekDayTime, yearWeekDayTime)
      cache.put(yearWeekDay, yearWeekDay)
      cache.put(yearDayTime, yearDayTime)
      cache.put(yearDay, yearDay)
      cache.put(dayTime, dayTime)
      cache.put(time, time)
      cache.put(years, years)
      cache.put(months, months)
      cache.put(weeks, weeks)
      cache.put(days, days)
      cache.put(hours, hours)
      cache.put(minutes, minutes)
      cache.put(seconds, seconds)
      cache.put(millis, millis)
    }
    val inPartType: PeriodType = new PeriodType(null, types, null)
    val cached: AnyRef = cache.get(inPartType)
    if (cached.isInstanceOf[PeriodType]) {
      return cached.asInstanceOf[PeriodType]
    }
    if (cached != null) {
      throw new IllegalArgumentException("PeriodType does not support fields: " + cached)
    }
    var `type`: PeriodType = standard
    val list: List[DurationFieldType] = new ArrayList[DurationFieldType](Arrays.asList(types))
    if (list.remove(DurationFieldType.years) == false) {
      `type` = `type`.withYearsRemoved
    }
    if (list.remove(DurationFieldType.months) == false) {
      `type` = `type`.withMonthsRemoved
    }
    if (list.remove(DurationFieldType.weeks) == false) {
      `type` = `type`.withWeeksRemoved
    }
    if (list.remove(DurationFieldType.days) == false) {
      `type` = `type`.withDaysRemoved
    }
    if (list.remove(DurationFieldType.hours) == false) {
      `type` = `type`.withHoursRemoved
    }
    if (list.remove(DurationFieldType.minutes) == false) {
      `type` = `type`.withMinutesRemoved
    }
    if (list.remove(DurationFieldType.seconds) == false) {
      `type` = `type`.withSecondsRemoved
    }
    if (list.remove(DurationFieldType.millis) == false) {
      `type` = `type`.withMillisRemoved
    }
    if (list.size > 0) {
      cache.put(inPartType, list)
      throw new IllegalArgumentException("PeriodType does not support fields: " + list)
    }
    val checkPartType: PeriodType = new PeriodType(null, `type`.iTypes, null)
    val checkedType: PeriodType = cache.get(checkPartType).asInstanceOf[PeriodType]
    if (checkedType != null) {
      cache.put(checkPartType, checkedType)
      return checkedType
    }
    cache.put(checkPartType, `type`)
    return `type`
  }
}

@SerialVersionUID(2274324892792009998L)
class PeriodType extends Serializable {
  /** The name of the type */
  private final val iName: String = null
  /** The array of types */
  private final val iTypes: Array[DurationFieldType] = null
  /** The array of indices */
  private final val iIndices: Array[Int] = null

  /**
   * Constructor.
   *
   * @param name  the name
   * @param types  the types
   * @param indices  the indices
   */
  protected def this(name: String, types: Array[DurationFieldType], indices: Array[Int]) {
    this()
    `super`
    iName = name
    iTypes = types
    iIndices = indices
  }

  /**
   * Gets the name of the period type.
   *
   * @return the name
   */
  def getName: String = {
    return iName
  }

  /**
   * Gets the number of fields in the period type.
   *
   * @return the number of fields
   */
  def size: Int = {
    return iTypes.length
  }

  /**
   * Gets the field type by index.
   *
   * @param index  the index to retrieve
   * @return the field type
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  def getFieldType(index: Int): DurationFieldType = {
    return iTypes(index)
  }

  /**
   * Checks whether the field specified is supported by this period.
   *
   * @param type  the type to check, may be null which returns false
   * @return true if the field is supported
   */
  def isSupported(`type`: DurationFieldType): Boolean = {
    return (indexOf(`type`) >= 0)
  }

  /**
   * Gets the index of the field in this period.
   *
   * @param type  the type to check, may be null which returns -1
   * @return the index of -1 if not supported
   */
  def indexOf(`type`: DurationFieldType): Int = {
    {
      var i: Int = 0
      val isize: Int = size
      while (i < isize) {
        {
          if (iTypes(i) eq `type`) {
            return i
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return -1
  }

  /**
   * Gets a debugging to string.
   *
   * @return a string
   */
  override def toString: String = {
    return "PeriodType[" + getName + "]"
  }

  /**
   * Gets the indexed field part of the period.
   *
   * @param period  the period to query
   * @param index  the index to use
   * @return the value of the field, zero if unsupported
   */
  private[time] def getIndexedField(period: ReadablePeriod, index: Int): Int = {
    val realIndex: Int = iIndices(index)
    return (if (realIndex == -1) 0 else period.getValue(realIndex))
  }

  /**
   * Sets the indexed field part of the period.
   *
   * @param period  the period to query
   * @param index  the index to use
   * @param values  the array to populate
   * @param newValue  the value to set
   * @throws UnsupportedOperationException if not supported
   */
  private[time] def setIndexedField(period: ReadablePeriod, index: Int, values: Array[Int], newValue: Int): Boolean = {
    val realIndex: Int = iIndices(index)
    if (realIndex == -1) {
      throw new UnsupportedOperationException("Field is not supported")
    }
    values(realIndex) = newValue
    return true
  }

  /**
   * Adds to the indexed field part of the period.
   *
   * @param period  the period to query
   * @param index  the index to use
   * @param values  the array to populate
   * @param valueToAdd  the value to add
   * @return true if the array is updated
   * @throws UnsupportedOperationException if not supported
   */
  private[time] def addIndexedField(period: ReadablePeriod, index: Int, values: Array[Int], valueToAdd: Int): Boolean = {
    if (valueToAdd == 0) {
      return false
    }
    val realIndex: Int = iIndices(index)
    if (realIndex == -1) {
      throw new UnsupportedOperationException("Field is not supported")
    }
    values(realIndex) = FieldUtils.safeAdd(values(realIndex), valueToAdd)
    return true
  }

  /**
   * Returns a version of this PeriodType instance that does not support years.
   *
   * @return a new period type that supports the original set of fields except years
   */
  def withYearsRemoved: PeriodType = {
    return withFieldRemoved(0, "NoYears")
  }

  /**
   * Returns a version of this PeriodType instance that does not support months.
   *
   * @return a new period type that supports the original set of fields except months
   */
  def withMonthsRemoved: PeriodType = {
    return withFieldRemoved(1, "NoMonths")
  }

  /**
   * Returns a version of this PeriodType instance that does not support weeks.
   *
   * @return a new period type that supports the original set of fields except weeks
   */
  def withWeeksRemoved: PeriodType = {
    return withFieldRemoved(2, "NoWeeks")
  }

  /**
   * Returns a version of this PeriodType instance that does not support days.
   *
   * @return a new period type that supports the original set of fields except days
   */
  def withDaysRemoved: PeriodType = {
    return withFieldRemoved(3, "NoDays")
  }

  /**
   * Returns a version of this PeriodType instance that does not support hours.
   *
   * @return a new period type that supports the original set of fields except hours
   */
  def withHoursRemoved: PeriodType = {
    return withFieldRemoved(4, "NoHours")
  }

  /**
   * Returns a version of this PeriodType instance that does not support minutes.
   *
   * @return a new period type that supports the original set of fields except minutes
   */
  def withMinutesRemoved: PeriodType = {
    return withFieldRemoved(5, "NoMinutes")
  }

  /**
   * Returns a version of this PeriodType instance that does not support seconds.
   *
   * @return a new period type that supports the original set of fields except seconds
   */
  def withSecondsRemoved: PeriodType = {
    return withFieldRemoved(6, "NoSeconds")
  }

  /**
   * Returns a version of this PeriodType instance that does not support milliseconds.
   *
   * @return a new period type that supports the original set of fields except milliseconds
   */
  def withMillisRemoved: PeriodType = {
    return withFieldRemoved(7, "NoMillis")
  }

  /**
   * Removes the field specified by indices index.
   *
   * @param indicesIndex  the index to remove
   * @param name  the name addition
   * @return the new type
   */
  private def withFieldRemoved(indicesIndex: Int, name: String): PeriodType = {
    val fieldIndex: Int = iIndices(indicesIndex)
    if (fieldIndex == -1) {
      return this
    }
    val types: Array[DurationFieldType] = new Array[DurationFieldType](size - 1)
    {
      var i: Int = 0
      while (i < iTypes.length) {
        {
          if (i < fieldIndex) {
            types(i) = iTypes(i)
          }
          else if (i > fieldIndex) {
            types(i - 1) = iTypes(i)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    val indices: Array[Int] = new Array[Int](8)
    {
      var i: Int = 0
      while (i < indices.length) {
        {
          if (i < indicesIndex) {
            indices(i) = iIndices(i)
          }
          else if (i > indicesIndex) {
            indices(i) = (if (iIndices(i) == -1) -1 else iIndices(i) - 1)
          }
          else {
            indices(i) = -1
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return new PeriodType(getName + name, types, indices)
  }

  /**
   * Compares this type to another object.
   * To be equal, the object must be a PeriodType with the same set of fields.
   *
   * @param obj  the object to compare to
   * @return true if equal
   */
  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[PeriodType] == false) {
      return false
    }
    val other: PeriodType = obj.asInstanceOf[PeriodType]
    return (Arrays.equals(iTypes, other.iTypes))
  }

  /**
   * Returns a hashcode based on the field types.
   *
   * @return a suitable hashcode
   */
  override def hashCode: Int = {
    var hash: Int = 0
    {
      var i: Int = 0
      while (i < iTypes.length) {
        {
          hash += iTypes(i).hashCode
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return hash
  }
}
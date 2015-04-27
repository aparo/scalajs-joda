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
package org.joda.time

import java.io.Serializable

/**
 * Identifies a field, such as year or minuteOfHour, in a chronology-neutral way.
 * <p>
 * A field type defines the type of the field, such as hourOfDay.
 * If does not directly enable any calculations, however it does provide a
 * {@link #getField(org.joda.Chronology)} method that returns the actual calculation engine
 * for a particular chronology.
 * It also provides access to the related {@link DurationFieldType}s.
 * <p>
 * Instances of <code>DateTimeFieldType</code> are singletons.
 * They can be compared using <code>==</code>.
 * <p>
 * If required, you can create your own field, for example a quarterOfYear.
 * You must create a subclass of <code>DateTimeFieldType</code> that defines the field type.
 * This class returns the actual calculation engine from {@link #getField(org.joda.Chronology)}.
 * The subclass should implement equals and hashCode.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-42615285973990L)
object DateTimeFieldType {
  /** Ordinal values for standard field types. */
  private[time] val ERA: Byte = 1
  private[time] val YEAR_OF_ERA: Byte = 2
  private[time] val CENTURY_OF_ERA: Byte = 3
  private[time] val YEAR_OF_CENTURY: Byte = 4
  private[time] val YEAR: Byte = 5
  private[time] val DAY_OF_YEAR: Byte = 6
  private[time] val MONTH_OF_YEAR: Byte = 7
  private[time] val DAY_OF_MONTH: Byte = 8
  private[time] val WEEKYEAR_OF_CENTURY: Byte = 9
  private[time] val WEEKYEAR: Byte = 10
  private[time] val WEEK_OF_WEEKYEAR: Byte = 11
  private[time] val DAY_OF_WEEK: Byte = 12
  private[time] val HALFDAY_OF_DAY: Byte = 13
  private[time] val HOUR_OF_HALFDAY: Byte = 14
  private[time] val CLOCKHOUR_OF_HALFDAY: Byte = 15
  private[time] val CLOCKHOUR_OF_DAY: Byte = 16
  private[time] val HOUR_OF_DAY: Byte = 17
  private[time] val MINUTE_OF_DAY: Byte = 18
  private[time] val MINUTE_OF_HOUR: Byte = 19
  private[time] val SECOND_OF_DAY: Byte = 20
  private[time] val SECOND_OF_MINUTE: Byte = 21
  private[time] val MILLIS_OF_DAY: Byte = 22
  private[time] val MILLIS_OF_SECOND: Byte = 23
  /** The era field type. */
  private val ERA_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("era", ERA, DurationFieldType.eras, null)
  /** The yearOfEra field type. */
  private val YEAR_OF_ERA_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("yearOfEra", YEAR_OF_ERA, DurationFieldType.years, DurationFieldType.eras)
  /** The centuryOfEra field type. */
  private val CENTURY_OF_ERA_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("centuryOfEra", CENTURY_OF_ERA, DurationFieldType.centuries, DurationFieldType.eras)
  /** The yearOfCentury field type. */
  private val YEAR_OF_CENTURY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("yearOfCentury", YEAR_OF_CENTURY, DurationFieldType.years, DurationFieldType.centuries)
  /** The year field type. */
  private val YEAR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("year", YEAR, DurationFieldType.years, null)
  /** The dayOfYear field type. */
  private val DAY_OF_YEAR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("dayOfYear", DAY_OF_YEAR, DurationFieldType.days, DurationFieldType.years)
  /** The monthOfYear field type. */
  private val MONTH_OF_YEAR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("monthOfYear", MONTH_OF_YEAR, DurationFieldType.months, DurationFieldType.years)
  /** The dayOfMonth field type. */
  private val DAY_OF_MONTH_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("dayOfMonth", DAY_OF_MONTH, DurationFieldType.days, DurationFieldType.months)
  /** The weekyearOfCentury field type. */
  private val WEEKYEAR_OF_CENTURY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("weekyearOfCentury", WEEKYEAR_OF_CENTURY, DurationFieldType.weekyears, DurationFieldType.centuries)
  /** The weekyear field type. */
  private val WEEKYEAR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("weekyear", WEEKYEAR, DurationFieldType.weekyears, null)
  /** The weekOfWeekyear field type. */
  private val WEEK_OF_WEEKYEAR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("weekOfWeekyear", WEEK_OF_WEEKYEAR, DurationFieldType.weeks, DurationFieldType.weekyears)
  /** The dayOfWeek field type. */
  private val DAY_OF_WEEK_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("dayOfWeek", DAY_OF_WEEK, DurationFieldType.days, DurationFieldType.weeks)
  /** The halfday field type. */
  private val HALFDAY_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("halfdayOfDay", HALFDAY_OF_DAY, DurationFieldType.halfdays, DurationFieldType.days)
  /** The hourOfHalfday field type. */
  private val HOUR_OF_HALFDAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("hourOfHalfday", HOUR_OF_HALFDAY, DurationFieldType.hours, DurationFieldType.halfdays)
  /** The clockhourOfHalfday field type. */
  private val CLOCKHOUR_OF_HALFDAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("clockhourOfHalfday", CLOCKHOUR_OF_HALFDAY, DurationFieldType.hours, DurationFieldType.halfdays)
  /** The clockhourOfDay field type. */
  private val CLOCKHOUR_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("clockhourOfDay", CLOCKHOUR_OF_DAY, DurationFieldType.hours, DurationFieldType.days)
  /** The hourOfDay field type. */
  private val HOUR_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("hourOfDay", HOUR_OF_DAY, DurationFieldType.hours, DurationFieldType.days)
  /** The minuteOfDay field type. */
  private val MINUTE_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("minuteOfDay", MINUTE_OF_DAY, DurationFieldType.minutes, DurationFieldType.days)
  /** The minuteOfHour field type. */
  private val MINUTE_OF_HOUR_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("minuteOfHour", MINUTE_OF_HOUR, DurationFieldType.minutes, DurationFieldType.hours)
  /** The secondOfDay field type. */
  private val SECOND_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("secondOfDay", SECOND_OF_DAY, DurationFieldType.seconds, DurationFieldType.days)
  /** The secondOfMinute field type. */
  private val SECOND_OF_MINUTE_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("secondOfMinute", SECOND_OF_MINUTE, DurationFieldType.seconds, DurationFieldType.minutes)
  /** The millisOfDay field type. */
  private val MILLIS_OF_DAY_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("millisOfDay", MILLIS_OF_DAY, DurationFieldType.millis, DurationFieldType.days)
  /** The millisOfSecond field type. */
  private val MILLIS_OF_SECOND_TYPE: DateTimeFieldType = new DateTimeFieldType.StandardDateTimeFieldType("millisOfSecond", MILLIS_OF_SECOND, DurationFieldType.millis, DurationFieldType.seconds)

  /**
   * Get the millis of second field type.
   *
   * @return the DateTimeFieldType constant
   */
  def millisOfSecond: DateTimeFieldType = {
    return MILLIS_OF_SECOND_TYPE
  }

  /**
   * Get the millis of day field type.
   *
   * @return the DateTimeFieldType constant
   */
  def millisOfDay: DateTimeFieldType = {
    return MILLIS_OF_DAY_TYPE
  }

  /**
   * Get the second of minute field type.
   *
   * @return the DateTimeFieldType constant
   */
  def secondOfMinute: DateTimeFieldType = {
    return SECOND_OF_MINUTE_TYPE
  }

  /**
   * Get the second of day field type.
   *
   * @return the DateTimeFieldType constant
   */
  def secondOfDay: DateTimeFieldType = {
    return SECOND_OF_DAY_TYPE
  }

  /**
   * Get the minute of hour field type.
   *
   * @return the DateTimeFieldType constant
   */
  def minuteOfHour: DateTimeFieldType = {
    return MINUTE_OF_HOUR_TYPE
  }

  /**
   * Get the minute of day field type.
   *
   * @return the DateTimeFieldType constant
   */
  def minuteOfDay: DateTimeFieldType = {
    return MINUTE_OF_DAY_TYPE
  }

  /**
   * Get the hour of day (0-23) field type.
   *
   * @return the DateTimeFieldType constant
   */
  def hourOfDay: DateTimeFieldType = {
    return HOUR_OF_DAY_TYPE
  }

  /**
   * Get the hour of day (offset to 1-24) field type.
   *
   * @return the DateTimeFieldType constant
   */
  def clockhourOfDay: DateTimeFieldType = {
    return CLOCKHOUR_OF_DAY_TYPE
  }

  /**
   * Get the hour of am/pm (0-11) field type.
   *
   * @return the DateTimeFieldType constant
   */
  def hourOfHalfday: DateTimeFieldType = {
    return HOUR_OF_HALFDAY_TYPE
  }

  /**
   * Get the hour of am/pm (offset to 1-12) field type.
   *
   * @return the DateTimeFieldType constant
   */
  def clockhourOfHalfday: DateTimeFieldType = {
    return CLOCKHOUR_OF_HALFDAY_TYPE
  }

  /**
   * Get the AM(0) PM(1) field type.
   *
   * @return the DateTimeFieldType constant
   */
  def halfdayOfDay: DateTimeFieldType = {
    return HALFDAY_OF_DAY_TYPE
  }

  /**
   * Get the day of week field type.
   *
   * @return the DateTimeFieldType constant
   */
  def dayOfWeek: DateTimeFieldType = {
    return DAY_OF_WEEK_TYPE
  }

  /**
   * Get the day of month field type.
   *
   * @return the DateTimeFieldType constant
   */
  def dayOfMonth: DateTimeFieldType = {
    return DAY_OF_MONTH_TYPE
  }

  /**
   * Get the day of year field type.
   *
   * @return the DateTimeFieldType constant
   */
  def dayOfYear: DateTimeFieldType = {
    return DAY_OF_YEAR_TYPE
  }

  /**
   * Get the week of a week based year field type.
   *
   * @return the DateTimeFieldType constant
   */
  def weekOfWeekyear: DateTimeFieldType = {
    return WEEK_OF_WEEKYEAR_TYPE
  }

  /**
   * Get the year of a week based year field type.
   *
   * @return the DateTimeFieldType constant
   */
  def weekyear: DateTimeFieldType = {
    return WEEKYEAR_TYPE
  }

  /**
   * Get the year of a week based year within a century field type.
   *
   * @return the DateTimeFieldType constant
   */
  def weekyearOfCentury: DateTimeFieldType = {
    return WEEKYEAR_OF_CENTURY_TYPE
  }

  /**
   * Get the month of year field type.
   *
   * @return the DateTimeFieldType constant
   */
  def monthOfYear: DateTimeFieldType = {
    return MONTH_OF_YEAR_TYPE
  }

  /**
   * Get the year field type.
   *
   * @return the DateTimeFieldType constant
   */
  def year: DateTimeFieldType = {
    return YEAR_TYPE
  }

  /**
   * Get the year of era field type.
   *
   * @return the DateTimeFieldType constant
   */
  def yearOfEra: DateTimeFieldType = {
    return YEAR_OF_ERA_TYPE
  }

  /**
   * Get the year of century field type.
   *
   * @return the DateTimeFieldType constant
   */
  def yearOfCentury: DateTimeFieldType = {
    return YEAR_OF_CENTURY_TYPE
  }

  /**
   * Get the century of era field type.
   *
   * @return the DateTimeFieldType constant
   */
  def centuryOfEra: DateTimeFieldType = {
    return CENTURY_OF_ERA_TYPE
  }

  /**
   * Get the era field type.
   *
   * @return the DateTimeFieldType constant
   */
  def era: DateTimeFieldType = {
    return ERA_TYPE
  }

  @SerialVersionUID(-9937958251642L)
  private class StandardDateTimeFieldType extends DateTimeFieldType {
    /** The ordinal of the standard field type, for switch statements */
    private final val iOrdinal: Byte = 0
    /** The unit duration of the field. */
    @transient
    private final val iUnitType: DurationFieldType = null
    /** The range duration of the field. */
    @transient
    private final val iRangeType: DurationFieldType = null

    /**
     * Constructor.
     *
     * @param name  the name to use
     * @param ordinal  the byte value for the oridinal index
     * @param unitType  the unit duration type
     * @param rangeType  the range duration type
     */
    private[time] def this(name: String, ordinal: Byte, unitType: DurationFieldType, rangeType: DurationFieldType) {
      this()
      `super`(name)
      iOrdinal = ordinal
      iUnitType = unitType
      iRangeType = rangeType
    }

    /** @inheritdoc*/
    def getDurationType: DurationFieldType = {
      return iUnitType
    }

    /** @inheritdoc*/
    def getRangeDurationType: DurationFieldType = {
      return iRangeType
    }

    /** @inheritdoc*/
    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DateTimeFieldType.StandardDateTimeFieldType]) {
        return iOrdinal == (obj.asInstanceOf[DateTimeFieldType.StandardDateTimeFieldType]).iOrdinal
      }
      return false
    }

    /** @inheritdoc*/
    override def hashCode: Int = {
      return (1 << iOrdinal)
    }

    /** @inheritdoc*/
    def getField(chronology: Chronology): DateTimeField = {
      chronology = DateTimeUtils.getChronology(chronology)
      iOrdinal match {
        case ERA =>
          return chronology.era
        case YEAR_OF_ERA =>
          return chronology.yearOfEra
        case CENTURY_OF_ERA =>
          return chronology.centuryOfEra
        case YEAR_OF_CENTURY =>
          return chronology.yearOfCentury
        case YEAR =>
          return chronology.year
        case DAY_OF_YEAR =>
          return chronology.dayOfYear
        case MONTH_OF_YEAR =>
          return chronology.monthOfYear
        case DAY_OF_MONTH =>
          return chronology.dayOfMonth
        case WEEKYEAR_OF_CENTURY =>
          return chronology.weekyearOfCentury
        case WEEKYEAR =>
          return chronology.weekyear
        case WEEK_OF_WEEKYEAR =>
          return chronology.weekOfWeekyear
        case DAY_OF_WEEK =>
          return chronology.dayOfWeek
        case HALFDAY_OF_DAY =>
          return chronology.halfdayOfDay
        case HOUR_OF_HALFDAY =>
          return chronology.hourOfHalfday
        case CLOCKHOUR_OF_HALFDAY =>
          return chronology.clockhourOfHalfday
        case CLOCKHOUR_OF_DAY =>
          return chronology.clockhourOfDay
        case HOUR_OF_DAY =>
          return chronology.hourOfDay
        case MINUTE_OF_DAY =>
          return chronology.minuteOfDay
        case MINUTE_OF_HOUR =>
          return chronology.minuteOfHour
        case SECOND_OF_DAY =>
          return chronology.secondOfDay
        case SECOND_OF_MINUTE =>
          return chronology.secondOfMinute
        case MILLIS_OF_DAY =>
          return chronology.millisOfDay
        case MILLIS_OF_SECOND =>
          return chronology.millisOfSecond
        case _ =>
          throw new InternalError
      }
    }

    /**
     * Ensure a singleton is returned.
     *
     * @return the singleton type
     */
    private def readResolve: AnyRef = {
      iOrdinal match {
        case ERA =>
          return ERA_TYPE
        case YEAR_OF_ERA =>
          return YEAR_OF_ERA_TYPE
        case CENTURY_OF_ERA =>
          return CENTURY_OF_ERA_TYPE
        case YEAR_OF_CENTURY =>
          return YEAR_OF_CENTURY_TYPE
        case YEAR =>
          return YEAR_TYPE
        case DAY_OF_YEAR =>
          return DAY_OF_YEAR_TYPE
        case MONTH_OF_YEAR =>
          return MONTH_OF_YEAR_TYPE
        case DAY_OF_MONTH =>
          return DAY_OF_MONTH_TYPE
        case WEEKYEAR_OF_CENTURY =>
          return WEEKYEAR_OF_CENTURY_TYPE
        case WEEKYEAR =>
          return WEEKYEAR_TYPE
        case WEEK_OF_WEEKYEAR =>
          return WEEK_OF_WEEKYEAR_TYPE
        case DAY_OF_WEEK =>
          return DAY_OF_WEEK_TYPE
        case HALFDAY_OF_DAY =>
          return HALFDAY_OF_DAY_TYPE
        case HOUR_OF_HALFDAY =>
          return HOUR_OF_HALFDAY_TYPE
        case CLOCKHOUR_OF_HALFDAY =>
          return CLOCKHOUR_OF_HALFDAY_TYPE
        case CLOCKHOUR_OF_DAY =>
          return CLOCKHOUR_OF_DAY_TYPE
        case HOUR_OF_DAY =>
          return HOUR_OF_DAY_TYPE
        case MINUTE_OF_DAY =>
          return MINUTE_OF_DAY_TYPE
        case MINUTE_OF_HOUR =>
          return MINUTE_OF_HOUR_TYPE
        case SECOND_OF_DAY =>
          return SECOND_OF_DAY_TYPE
        case SECOND_OF_MINUTE =>
          return SECOND_OF_MINUTE_TYPE
        case MILLIS_OF_DAY =>
          return MILLIS_OF_DAY_TYPE
        case MILLIS_OF_SECOND =>
          return MILLIS_OF_SECOND_TYPE
        case _ =>
          return this
      }
    }
  }

}

@SerialVersionUID(-42615285973990L)
abstract class DateTimeFieldType extends Serializable {
  /** The name of the field. */
  private final val iName: String = null

  /**
   * Constructor.
   *
   * @param name  the name to use
   */
  protected def this(name: String) {
    this()
    `super`
    iName = name
  }

  /**
   * Get the name of the field.
   * <p>
   * By convention, names follow a pattern of "dddOfRrr", where "ddd" represents
   * the (singular) duration unit field name and "Rrr" represents the (singular)
   * duration range field name. If the range field is not applicable, then
   * the name of the field is simply the (singular) duration field name.
   *
   * @return field name
   */
  def getName: String = {
    return iName
  }

  /**
   * Get the duration unit of the field.
   *
   * @return duration unit of the field, never null
   */
  def getDurationType: DurationFieldType

  /**
   * Get the duration range of the field.
   *
   * @return duration range of the field, null if unbounded
   */
  def getRangeDurationType: DurationFieldType

  /**
   * Gets a suitable field for this type from the given Chronology.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   * @return a suitable field
   */
  def getField(chronology: Chronology): DateTimeField

  /**
   * Checks whether this field supported in the given Chronology.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   * @return true if supported
   */
  def isSupported(chronology: Chronology): Boolean = {
    return getField(chronology).isSupported
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = {
    return getName
  }
}
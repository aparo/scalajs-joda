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

/**
 * Identifies a duration field, such as years or minutes, in a chronology-neutral way.
 * <p>
 * A duration field type defines the type of the field, such as hours.
 * If does not directly enable any calculations, however it does provide a
 * {@link #getField(org.joda.Chronology)} method that returns the actual calculation engine
 * for a particular chronology.
 * <p>
 * Instances of <code>DurationFieldType</code> are singletons.
 * They can be compared using <code>==</code>.
 * <p>
 * If required, you can create your own field, for example a quarters.
 * You must create a subclass of <code>DurationFieldType</code> that defines the field type.
 * This class returns the actual calculation engine from {@link #getField(org.joda.Chronology)}.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */

object DurationFieldType {
  private[time] val ERAS: Byte = 1
  private[time] val CENTURIES: Byte = 2
  private[time] val WEEKYEARS: Byte = 3
  private[time] val YEARS: Byte = 4
  private[time] val MONTHS: Byte = 5
  private[time] val WEEKS: Byte = 6
  private[time] val DAYS: Byte = 7
  private[time] val HALFDAYS: Byte = 8
  private[time] val HOURS: Byte = 9
  private[time] val MINUTES: Byte = 10
  private[time] val SECONDS: Byte = 11
  private[time] val MILLIS: Byte = 12
  /** The eras field type. */
  private[time] val ERAS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("eras", ERAS)
  /** The centuries field type. */
  private[time] val CENTURIES_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("centuries", CENTURIES)
  /** The weekyears field type. */
  private[time] val WEEKYEARS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("weekyears", WEEKYEARS)
  /** The years field type. */
  private[time] val YEARS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("years", YEARS)
  /** The months field type. */
  private[time] val MONTHS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("months", MONTHS)
  /** The weeks field type. */
  private[time] val WEEKS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("weeks", WEEKS)
  /** The days field type. */
  private[time] val DAYS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("days", DAYS)
  /** The halfdays field type. */
  private[time] val HALFDAYS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("halfdays", HALFDAYS)
  /** The hours field type. */
  private[time] val HOURS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("hours", HOURS)
  /** The minutes field type. */
  private[time] val MINUTES_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("minutes", MINUTES)
  /** The seconds field type. */
  private[time] val SECONDS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("seconds", SECONDS)
  /** The millis field type. */
  private[time] val MILLIS_TYPE: DurationFieldType = new DurationFieldType.StandardDurationFieldType("millis", MILLIS)

  /**
   * Get the millis field type.
   *
   * @return the DateTimeFieldType constant
   */
  def millis: DurationFieldType = {
    return MILLIS_TYPE
  }

  /**
   * Get the seconds field type.
   *
   * @return the DateTimeFieldType constant
   */
  def seconds: DurationFieldType = {
    return SECONDS_TYPE
  }

  /**
   * Get the minutes field type.
   *
   * @return the DateTimeFieldType constant
   */
  def minutes: DurationFieldType = {
    return MINUTES_TYPE
  }

  /**
   * Get the hours field type.
   *
   * @return the DateTimeFieldType constant
   */
  def hours: DurationFieldType = {
    return HOURS_TYPE
  }

  /**
   * Get the halfdays field type.
   *
   * @return the DateTimeFieldType constant
   */
  def halfdays: DurationFieldType = {
    return HALFDAYS_TYPE
  }

  /**
   * Get the days field type.
   *
   * @return the DateTimeFieldType constant
   */
  def days: DurationFieldType = {
    return DAYS_TYPE
  }

  /**
   * Get the weeks field type.
   *
   * @return the DateTimeFieldType constant
   */
  def weeks: DurationFieldType = {
    return WEEKS_TYPE
  }

  /**
   * Get the weekyears field type.
   *
   * @return the DateTimeFieldType constant
   */
  def weekyears: DurationFieldType = {
    return WEEKYEARS_TYPE
  }

  /**
   * Get the months field type.
   *
   * @return the DateTimeFieldType constant
   */
  def months: DurationFieldType = {
    return MONTHS_TYPE
  }

  /**
   * Get the years field type.
   *
   * @return the DateTimeFieldType constant
   */
  def years: DurationFieldType = {
    return YEARS_TYPE
  }

  /**
   * Get the centuries field type.
   *
   * @return the DateTimeFieldType constant
   */
  def centuries: DurationFieldType = {
    return CENTURIES_TYPE
  }

  /**
   * Get the eras field type.
   *
   * @return the DateTimeFieldType constant
   */
  def eras: DurationFieldType = {
    return ERAS_TYPE
  }


  private class StandardDurationFieldType(val name: String, val iOrdinal: Byte) extends DurationFieldType(name) {
    /** The ordinal of the standard field type, for switch statements */
    private final val iOrdinal: Byte = 0

    /** @inheritdoc*/
    override def equals(obj: AnyRef): Boolean = {
      if (this eq obj) {
        return true
      }
      if (obj.isInstanceOf[DurationFieldType.StandardDurationFieldType]) {
        return iOrdinal == (obj.asInstanceOf[DurationFieldType.StandardDurationFieldType]).iOrdinal
      }
      return false
    }

    /** @inheritdoc*/
    override def hashCode: Int = {
      return (1 << iOrdinal)
    }

    def getField(chronology: Chronology): DurationField = {
      chronology = DateTimeUtils.getChronology(chronology)
      iOrdinal match {
        case ERAS =>
          return chronology.eras
        case CENTURIES =>
          return chronology.centuries
        case WEEKYEARS =>
          return chronology.weekyears
        case YEARS =>
          return chronology.years
        case MONTHS =>
          return chronology.months
        case WEEKS =>
          return chronology.weeks
        case DAYS =>
          return chronology.days
        case HALFDAYS =>
          return chronology.halfdays
        case HOURS =>
          return chronology.hours
        case MINUTES =>
          return chronology.minutes
        case SECONDS =>
          return chronology.seconds
        case MILLIS =>
          return chronology.millis
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
        case ERAS =>
          return ERAS_TYPE
        case CENTURIES =>
          return CENTURIES_TYPE
        case WEEKYEARS =>
          return WEEKYEARS_TYPE
        case YEARS =>
          return YEARS_TYPE
        case MONTHS =>
          return MONTHS_TYPE
        case WEEKS =>
          return WEEKS_TYPE
        case DAYS =>
          return DAYS_TYPE
        case HALFDAYS =>
          return HALFDAYS_TYPE
        case HOURS =>
          return HOURS_TYPE
        case MINUTES =>
          return MINUTES_TYPE
        case SECONDS =>
          return SECONDS_TYPE
        case MILLIS =>
          return MILLIS_TYPE
        case _ =>
          return this
      }
    }
  }

}

/**
* Constructor.
*
* @param name  the name to use, which by convention, are plural.
*/

abstract class DurationFieldType(val iName: String) extends Serializable {
  /**
   * Get the name of the field.
   * By convention, names are plural.
   *
   * @return field name
   */
  def getName: String = iName


  /**
   * Gets a suitable field for this type from the given Chronology.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   * @return a suitable field
   */
  def getField(chronology: Chronology): DurationField

  /**
   * Checks whether this field supported in the given Chronology.
   *
   * @param chronology  the chronology to use, null means ISOChronology in default zone
   * @return true if supported
   */
  def isSupported(chronology: Chronology): Boolean = {
    getField(chronology).isSupported
  }

  /**
   * Get a suitable debug string.
   *
   * @return debug string
   */
  override def toString: String = getName

}
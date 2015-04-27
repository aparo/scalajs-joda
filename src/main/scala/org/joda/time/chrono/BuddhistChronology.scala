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

import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeField
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.DurationFieldType
import org.joda.time.field.DelegatedDateTimeField
import org.joda.time.field.DividedDateTimeField
import org.joda.time.field.OffsetDateTimeField
import org.joda.time.field.RemainderDateTimeField
import org.joda.time.field.SkipUndoDateTimeField
import org.joda.time.field.UnsupportedDurationField

/**
 * A chronology that matches the BuddhistCalendar class supplied by Sun.
 * <p>
 * The chronology is identical to the Gregorian/Julian, except that the
 * year is offset by +543 and the era is named 'BE' for Buddhist Era.
 * <p>
 * This class was intended by Sun to model the calendar used in Thailand.
 * However, the actual rules for Thailand are much more involved than
 * this class covers. (This class is accurate after 1941-01-01 ISO).
 * <p>
 * This chronlogy is being retained for those who want a same effect
 * replacement for the Sun class. It is hoped that community support will
 * enable a more accurate chronology for Thailand, to be developed.
 * <p>
 * BuddhistChronology is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-3474595157769370126L)
object BuddhistChronology {
  /**
   * Constant value for 'Buddhist Era', equivalent to the value returned
   * for AD/CE. Note that this differs from the constant in BuddhistCalendar.
   */
  val BE: Int = DateTimeConstants.CE
  /** A singleton era field. */
  private val ERA_FIELD: DateTimeField = new BasicSingleEraDateTimeField("BE")
  /** Number of years difference in calendars. */
  private val BUDDHIST_OFFSET: Int = 543
  /** Cache of zone to chronology */
  private val cCache: ConcurrentHashMap[DateTimeZone, BuddhistChronology] = new ConcurrentHashMap[DateTimeZone, BuddhistChronology]
  /** UTC instance of the chronology */
  private val INSTANCE_UTC: BuddhistChronology = getInstance(DateTimeZone.UTC)

  /**
   * Standard instance of a Buddhist Chronology, that matches
   * Sun's BuddhistCalendar class. This means that it follows the
   * GregorianJulian calendar rules with a cutover date.
   * <p>
   * The time zone of the returned instance is UTC.
   */
  def getInstanceUTC: BuddhistChronology = {
    return INSTANCE_UTC
  }

  /**
   * Standard instance of a Buddhist Chronology, that matches
   * Sun's BuddhistCalendar class. This means that it follows the
   * GregorianJulian calendar rules with a cutover date.
   */
  def getInstance: BuddhistChronology = {
    return getInstance(DateTimeZone.getDefault)
  }

  /**
   * Standard instance of a Buddhist Chronology, that matches
   * Sun's BuddhistCalendar class. This means that it follows the
   * GregorianJulian calendar rules with a cutover date.
   *
   * @param zone  the time zone to use, null is default
   */
  def getInstance(zone: DateTimeZone): BuddhistChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: BuddhistChronology = cCache.get(zone)
    if (chrono == null) {
      chrono = new BuddhistChronology(GJChronology.getInstance(zone, null), null)
      val lowerLimit: DateTime = new DateTime(1, 1, 1, 0, 0, 0, 0, chrono)
      chrono = new BuddhistChronology(LimitChronology.getInstance(chrono, lowerLimit, null), "")
      val oldChrono: BuddhistChronology = cCache.putIfAbsent(zone, chrono)
      if (oldChrono != null) {
        chrono = oldChrono
      }
    }
    return chrono
  }
}

@SerialVersionUID(-3474595157769370126L)
final class BuddhistChronology extends AssembledChronology {
  /**
   * Restricted constructor.
   *
   * @param param if non-null, then don't change the field set
   */
  private def this(base: Chronology, param: AnyRef) {
    this()
    `super`(base, param)
  }

  /**
   * Serialization singleton
   */
  private def readResolve: AnyRef = {
    val base: Chronology = getBase
    return if (base == null) BuddhistChronology.getInstanceUTC else BuddhistChronology.getInstance(base.getZone)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return BuddhistChronology.INSTANCE_UTC
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
    return BuddhistChronology.getInstance(zone)
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
    if (obj.isInstanceOf[BuddhistChronology]) {
      val chrono: BuddhistChronology = obj.asInstanceOf[BuddhistChronology]
      return getZone == chrono.getZone
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
    return "Buddhist".hashCode * 11 + getZone.hashCode
  }

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  def toString: String = {
    var str: String = "BuddhistChronology"
    val zone: DateTimeZone = getZone
    if (zone != null) {
      str = str + '[' + zone.getID + ']'
    }
    return str
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    if (getParam == null) {
      fields.eras = UnsupportedDurationField.getInstance(DurationFieldType.eras)
      var field: DateTimeField = fields.year
      fields.year = new OffsetDateTimeField(new SkipUndoDateTimeField(this, field), BuddhistChronology.BUDDHIST_OFFSET)
      field = fields.yearOfEra
      fields.yearOfEra = new DelegatedDateTimeField(fields.year, fields.eras, DateTimeFieldType.yearOfEra)
      field = fields.weekyear
      fields.weekyear = new OffsetDateTimeField(new SkipUndoDateTimeField(this, field), BuddhistChronology.BUDDHIST_OFFSET)
      field = new OffsetDateTimeField(fields.yearOfEra, 99)
      fields.centuryOfEra = new DividedDateTimeField(field, fields.eras, DateTimeFieldType.centuryOfEra, 100)
      fields.centuries = fields.centuryOfEra.getDurationField
      field = new RemainderDateTimeField(fields.centuryOfEra.asInstanceOf[DividedDateTimeField])
      fields.yearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.yearOfCentury, 1)
      field = new RemainderDateTimeField(fields.weekyear, fields.centuries, DateTimeFieldType.weekyearOfCentury, 100)
      fields.weekyearOfCentury = new OffsetDateTimeField(field, DateTimeFieldType.weekyearOfCentury, 1)
      fields.era = BuddhistChronology.ERA_FIELD
    }
  }
}
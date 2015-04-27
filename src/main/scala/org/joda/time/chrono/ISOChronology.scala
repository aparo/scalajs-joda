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

import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import org.joda.time.Chronology
import org.joda.time.DateTimeFieldType
import org.joda.time.DateTimeZone
import org.joda.time.field.DividedDateTimeField
import org.joda.time.field.RemainderDateTimeField

/**
 * Implements a chronology that follows the rules of the ISO8601 standard,
 * which is compatible with Gregorian for all modern dates.
 * When ISO does not define a field, but it can be determined (such as AM/PM)
 * it is included.
 * <p>
 * With the exception of century related fields, ISOChronology is exactly the
 * same as {@link GregorianChronology}. In this chronology, centuries and year
 * of century are zero based. For all years, the century is determined by
 * dropping the last two digits of the year, ignoring sign. The year of century
 * is the value of the last two year digits.
 * <p>
 * ISOChronology is thread-safe and immutable.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-6212696554273812441L)
object ISOChronology {
  /** Singleton instance of a UTC ISOChronology */
  private val INSTANCE_UTC: ISOChronology = null
  /** Cache of zone to chronology */
  private val cCache: ConcurrentHashMap[DateTimeZone, ISOChronology] = new ConcurrentHashMap[DateTimeZone, ISOChronology]

  /**
   * Gets an instance of the ISOChronology.
   * The time zone of the returned instance is UTC.
   *
   * @return a singleton UTC instance of the chronology
   */
  def getInstanceUTC: ISOChronology = {
    return INSTANCE_UTC
  }

  /**
   * Gets an instance of the ISOChronology in the default time zone.
   *
   * @return a chronology in the default time zone
   */
  def getInstance: ISOChronology = {
    return getInstance(DateTimeZone.getDefault)
  }

  /**
   * Gets an instance of the ISOChronology in the given time zone.
   *
   * @param zone  the time zone to get the chronology in, null is default
   * @return a chronology in the specified time zone
   */
  def getInstance(zone: DateTimeZone): ISOChronology = {
    if (zone == null) {
      zone = DateTimeZone.getDefault
    }
    var chrono: ISOChronology = cCache.get(zone)
    if (chrono == null) {
      chrono = new ISOChronology(ZonedChronology.getInstance(INSTANCE_UTC, zone))
      val oldChrono: ISOChronology = cCache.putIfAbsent(zone, chrono)
      if (oldChrono != null) {
        chrono = oldChrono
      }
    }
    return chrono
  }

  @SerialVersionUID(-6212696554273812441L)
  private final class Stub extends Serializable {
    @transient
    private var iZone: DateTimeZone = null

    private[chrono] def this(zone: DateTimeZone) {
      this()
      iZone = zone
    }

    private def readResolve: AnyRef = {
      return ISOChronology.getInstance(iZone)
    }

    @throws(classOf[IOException])
    private def writeObject(out: ObjectOutputStream) {
      out.writeObject(iZone)
    }

    @throws(classOf[IOException])
    @throws(classOf[ClassNotFoundException])
    private def readObject(in: ObjectInputStream) {
      iZone = in.readObject.asInstanceOf[DateTimeZone]
    }
  }

  try {
    INSTANCE_UTC = new ISOChronology(GregorianChronology.getInstanceUTC)
    cCache.put(DateTimeZone.UTC, INSTANCE_UTC)
  }
}

@SerialVersionUID(-6212696554273812441L)
final class ISOChronology extends AssembledChronology {
  /**
   * Restricted constructor
   */
  private def this(base: Chronology) {
    this()
    `super`(base, null)
  }

  /**
   * Gets the Chronology in the UTC time zone.
   *
   * @return the chronology in UTC
   */
  def withUTC: Chronology = {
    return ISOChronology.INSTANCE_UTC
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
    return ISOChronology.getInstance(zone)
  }

  /**
   * Gets a debugging toString.
   *
   * @return a debugging string
   */
  def toString: String = {
    var str: String = "ISOChronology"
    val zone: DateTimeZone = getZone
    if (zone != null) {
      str = str + '[' + zone.getID + ']'
    }
    return str
  }

  protected def assemble(fields: AssembledChronology.Fields) {
    if (getBase.getZone eq DateTimeZone.UTC) {
      fields.centuryOfEra = new DividedDateTimeField(ISOYearOfEraDateTimeField.INSTANCE, DateTimeFieldType.centuryOfEra, 100)
      fields.centuries = fields.centuryOfEra.getDurationField
      fields.yearOfCentury = new RemainderDateTimeField(fields.centuryOfEra.asInstanceOf[DividedDateTimeField], DateTimeFieldType.yearOfCentury)
      fields.weekyearOfCentury = new RemainderDateTimeField(fields.centuryOfEra.asInstanceOf[DividedDateTimeField], fields.weekyears, DateTimeFieldType.weekyearOfCentury)
    }
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
    if (obj.isInstanceOf[ISOChronology]) {
      val chrono: ISOChronology = obj.asInstanceOf[ISOChronology]
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
    return "ISO".hashCode * 11 + getZone.hashCode
  }

  /**
   * Serialize ISOChronology instances using a small stub. This reduces the
   * serialized size, and deserialized instances come from the cache.
   */
  private def writeReplace: AnyRef = {
    return new ISOChronology.Stub(getZone)
  }
}
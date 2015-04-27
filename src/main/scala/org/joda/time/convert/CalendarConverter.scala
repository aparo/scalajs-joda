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
package org.joda.time.convert

import java.util.Calendar
import java.util.GregorianCalendar
import org.joda.time.Chronology
import org.joda.time.DateTimeZone
import org.joda.time.chrono.BuddhistChronology
import org.joda.time.chrono.GJChronology
import org.joda.time.chrono.GregorianChronology
import org.joda.time.chrono.ISOChronology
import org.joda.time.chrono.JulianChronology

/**
 * CalendarConverter converts a java util Calendar to an instant or partial.
 * The Calendar is converted to milliseconds and the chronology that best
 * matches the calendar.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
object CalendarConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: CalendarConverter = new CalendarConverter
}

final class CalendarConverter extends AbstractConverter with InstantConverter with PartialConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the chronology.
   * <p>
   * If a chronology is specified then it is used.
   * Otherwise, it is the GJChronology if a GregorianCalendar is used,
   * BuddhistChronology if a BuddhistCalendar is used or ISOChronology otherwise.
   * The time zone is extracted from the calendar if possible, default used if not.
   *
   * @param object  the Calendar to convert, must not be null
   * @param chrono  the chronology to use, null means use Calendar
   * @return the chronology, never null
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getChronology(`object`: AnyRef, chrono: Chronology): Chronology = {
    if (chrono != null) {
      return chrono
    }
    val cal: Calendar = `object`.asInstanceOf[Calendar]
    var zone: DateTimeZone = null
    try {
      zone = DateTimeZone.forTimeZone(cal.getTimeZone)
    }
    catch {
      case ex: IllegalArgumentException => {
        zone = DateTimeZone.getDefault
      }
    }
    return getChronology(cal, zone)
  }

  /**
   * Gets the chronology, which is the GJChronology if a GregorianCalendar is used,
   * BuddhistChronology if a BuddhistCalendar is used or ISOChronology otherwise.
   * The time zone specified is used in preference to that on the calendar.
   *
   * @param object  the Calendar to convert, must not be null
   * @param zone  the specified zone to use, null means default zone
   * @return the chronology, never null
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getChronology(`object`: AnyRef, zone: DateTimeZone): Chronology = {
    if (`object`.getClass.getName.endsWith(".BuddhistCalendar")) {
      return BuddhistChronology.getInstance(zone)
    }
    else if (`object`.isInstanceOf[GregorianCalendar]) {
      val gc: GregorianCalendar = `object`.asInstanceOf[GregorianCalendar]
      val cutover: Long = gc.getGregorianChange.getTime
      if (cutover == Long.MIN_VALUE) {
        return GregorianChronology.getInstance(zone)
      }
      else if (cutover == Long.MaxValue) {
        return JulianChronology.getInstance(zone)
      }
      else {
        return GJChronology.getInstance(zone, cutover, 4)
      }
    }
    else {
      return ISOChronology.getInstance(zone)
    }
  }

  /**
   * Gets the millis, which is the Calendar millis value.
   *
   * @param object  the Calendar to convert, must not be null
   * @param chrono  the chronology result from getChronology, non-null
   * @return the millisecond value
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getInstantMillis(`object`: AnyRef, chrono: Chronology): Long = {
    val calendar: Calendar = `object`.asInstanceOf[Calendar]
    return calendar.getTime.getTime
  }

  /**
   * Returns Calendar.class.
   *
   * @return Calendar.class
   */
  def getSupportedType: Class[_] = {
    return classOf[Calendar]
  }
}
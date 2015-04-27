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

import org.joda.time.Chronology
import org.joda.time.DateTimeUtils
import org.joda.time.Period
import org.joda.time.ReadWritableInterval
import org.joda.time.ReadWritablePeriod

/**
 * NullConverter converts null to an instant, partial, duration, period
 * or interval. Null means now for instant/partial, zero for duration/period
 * and from now to now for interval.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object NullConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: NullConverter = new NullConverter
}

class NullConverter extends AbstractConverter with InstantConverter with PartialConverter with DurationConverter with PeriodConverter with IntervalConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the millisecond duration, which is zero.
   *
   * @param object  the object to convert, which is null
   * @return the millisecond duration
   */
  def getDurationMillis(`object`: AnyRef): Long = {
    return 0L
  }

  /**
   * Sets the given ReadWritableDuration to zero milliseconds.
   *
   * @param duration duration to get modified
   * @param object  the object to convert, which is null
   * @param chrono  the chronology to use
   * @throws NullPointerException if the duration is null
   */
  def setInto(duration: ReadWritablePeriod, `object`: AnyRef, chrono: Chronology) {
    duration.setPeriod(null.asInstanceOf[Period])
  }

  /**
   * Extracts interval endpoint values from an object of this converter's
   * type, and sets them into the given ReadWritableInterval.
   *
   * @param writableInterval interval to get modified, not null
   * @param object  the object to convert, which is null
   * @param chrono  the chronology to use, may be null
   * @throws NullPointerException if the interval is null
   */
  def setInto(writableInterval: ReadWritableInterval, `object`: AnyRef, chrono: Chronology) {
    writableInterval.setChronology(chrono)
    val now: Long = DateTimeUtils.currentTimeMillis
    writableInterval.setInterval(now, now)
  }

  /**
   * Returns null.
   *
   * @return null
   */
  def getSupportedType: Class[_] = {
    return null
  }
}
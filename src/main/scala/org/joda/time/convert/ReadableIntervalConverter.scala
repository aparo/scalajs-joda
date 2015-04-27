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
import org.joda.time.ReadWritableInterval
import org.joda.time.ReadWritablePeriod
import org.joda.time.ReadableInterval

/**
 * Converts intervals into durations of any requested period type.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
object ReadableIntervalConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: ReadableIntervalConverter = new ReadableIntervalConverter
}

class ReadableIntervalConverter extends AbstractConverter with IntervalConverter with DurationConverter with PeriodConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the millisecond length of the interval.
   *
   * @param object  the interval
   */
  def getDurationMillis(`object`: AnyRef): Long = {
    return ((`object`.asInstanceOf[ReadableInterval])).toDurationMillis
  }

  /**
   * Sets the values of the mutable duration from the specified interval.
   *
   * @param writablePeriod  the period to modify
   * @param object  the interval to set from
   * @param chrono  the chronology to use
   */
  def setInto(writablePeriod: ReadWritablePeriod, `object`: AnyRef, chrono: Chronology) {
    val interval: ReadableInterval = `object`.asInstanceOf[ReadableInterval]
    chrono = (if (chrono != null) chrono else DateTimeUtils.getIntervalChronology(interval))
    val start: Long = interval.getStartMillis
    val end: Long = interval.getEndMillis
    val values: Array[Int] = chrono.get(writablePeriod, start, end)
    {
      var i: Int = 0
      while (i < values.length) {
        {
          writablePeriod.setValue(i, values(i))
        }
        ({
          i += 1; i - 1
        })
      }
    }
  }

  /**
   * Checks if the input is a ReadableInterval.
   * <p>
   * If it is, then the calling code should cast and copy the fields directly.
   *
   * @param object  the object to convert, must not be null
   * @param chrono  the chronology to use, may be null
   * @return true if the input is a ReadableInterval
   * @throws ClassCastException if the object is invalid
   */
  override def isReadableInterval(`object`: AnyRef, chrono: Chronology): Boolean = {
    return true
  }

  /**
   * Extracts interval endpoint values from an object of this converter's
   * type, and sets them into the given ReadWritableInterval.
   *
   * @param writableInterval interval to get modified, not null
   * @param object  the object to convert, must not be null
   * @param chrono  the chronology to use, may be null
   * @throws ClassCastException if the object is invalid
   */
  def setInto(writableInterval: ReadWritableInterval, `object`: AnyRef, chrono: Chronology) {
    val input: ReadableInterval = `object`.asInstanceOf[ReadableInterval]
    writableInterval.setInterval(input)
    if (chrono != null) {
      writableInterval.setChronology(chrono)
    }
    else {
      writableInterval.setChronology(input.getChronology)
    }
  }

  /**
   * Returns ReadableInterval.class.
   */
  def getSupportedType: Class[_] = {
    return classOf[ReadableInterval]
  }
}
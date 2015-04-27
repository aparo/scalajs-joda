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
import org.joda.time.PeriodType
import org.joda.time.ReadWritablePeriod
import org.joda.time.ReadablePeriod

/**
 * ReadablePeriodConverter extracts milliseconds and chronology from a ReadablePeriod.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object ReadablePeriodConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: ReadablePeriodConverter = new ReadablePeriodConverter
}

class ReadablePeriodConverter extends AbstractConverter with PeriodConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Extracts duration values from an object of this converter's type, and
   * sets them into the given ReadWritablePeriod.
   *
   * @param duration duration to get modified
   * @param object  the object to convert, must not be null
   * @param chrono  the chronology to use
   * @throws NullPointerException if the duration or object is null
   * @throws ClassCastException if the object is an invalid type
   * @throws IllegalArgumentException if the object is invalid
   */
  def setInto(duration: ReadWritablePeriod, `object`: AnyRef, chrono: Chronology) {
    duration.setPeriod(`object`.asInstanceOf[ReadablePeriod])
  }

  /**
   * Selects a suitable period type for the given object.
   *
   * @param object  the object to examine, must not be null
   * @return the period type from the readable duration
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getPeriodType(`object`: AnyRef): PeriodType = {
    val period: ReadablePeriod = `object`.asInstanceOf[ReadablePeriod]
    return period.getPeriodType
  }

  /**
   * Returns ReadablePeriod class.
   *
   * @return ReadablePeriod.class
   */
  def getSupportedType: Class[_] = {
    return classOf[ReadablePeriod]
  }
}
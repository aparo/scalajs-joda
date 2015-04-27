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

/**
 * LongConverter converts a Long to an instant, partial or duration.
 * The Long value represents milliseconds in the ISO chronology.
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object LongConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: LongConverter = new LongConverter
}

class LongConverter extends AbstractConverter with InstantConverter with PartialConverter with DurationConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the millisecond instant, which is the Long value.
   *
   * @param object  the Long to convert, must not be null
   * @param chrono  the chronology to use, which is always non-null
   * @return the millisecond value
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getInstantMillis(`object`: AnyRef, chrono: Chronology): Long = {
    return (`object`.asInstanceOf[Long]).longValue
  }

  /**
   * Gets the millisecond duration, which is the Long value.
   *
   * @param object  the Long to convert, must not be null
   * @return the millisecond duration
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  def getDurationMillis(`object`: AnyRef): Long = {
    return (`object`.asInstanceOf[Long]).longValue
  }

  /**
   * Returns Long.class.
   *
   * @return Long.class
   */
  def getSupportedType: Class[_] = {
    return classOf[Long]
  }
}
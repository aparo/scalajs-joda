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

import java.util.Date
import org.joda.time.Chronology

/**
 * DateConverter converts a java util Date to an instant or partial.
 * The Date is converted to milliseconds in the ISO chronology.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
object DateConverter {
  /**
   * Singleton instance.
   */
  private[convert] val INSTANCE: DateConverter = new DateConverter
}

final class DateConverter extends AbstractConverter with InstantConverter with PartialConverter {
  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
  }

  /**
   * Gets the millis, which is the Date millis value.
   *
   * @param object  the Date to convert, must not be null
   * @param chrono  the non-null result of getChronology
   * @return the millisecond value
   * @throws NullPointerException if the object is null
   * @throws ClassCastException if the object is an invalid type
   */
  override def getInstantMillis(`object`: AnyRef, chrono: Chronology): Long = {
    val date: Date = `object`.asInstanceOf[Date]
    return date.getTime
  }

  /**
   * Returns Date.class.
   *
   * @return Date.class
   */
  def getSupportedType: Class[_] = {
    return classOf[Date]
  }
}
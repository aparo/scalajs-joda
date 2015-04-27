/*
 *  Copyright 2001-2013 Stephen Colebourne
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

import org.joda.time.format.DateTimeFormat

/**
 * Exception thrown when attempting to create an instant or date-time that cannot exist.
 * <p>
 * Classes like {@code DateTime} only store valid date-times.
 * One of the cases where validity is important is handling daylight savings time (DST).
 * In many places DST is used, where the local clock moves forward by an hour in spring and back by an hour in autumn/fall.
 * This means that in spring, there is a "gap" where a local time does not exist.
 * <p>
 * This exception refers to this gap, and it means that your application tried to create
 * a date-time inside the gap - a time that did not exist.
 * Since Joda-Time objects must be valid, this is not allowed.
 * <p>
 * Possible solutions may be as follows:<br />
 * Use <code>LocalDateTime</code>, as all local date-times are valid.<br />
 * When converting a <code>LocalDate</code> to a <code>DateTime</code>, then use <code>toDateTimeAsStartOfDay()</code>
 * as this handles and manages any gaps.<br />
 * When parsing, use <code>parseLocalDateTime()</code> if the string being parsed has no time-zone.
 *
 * @author Stephen Colebourne
 * @since 2.2
 */
@SerialVersionUID(2858712538216L)
object IllegalInstantException {
  private def createMessage(instantLocal: Long, zoneId: String): String = {
    val localDateTime: String = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").print(new Instant(instantLocal))
    val zone: String = (if (zoneId != null) " (" + zoneId + ")" else "")
    return "Illegal instant due to time zone offset transition (daylight savings time 'gap'): " + localDateTime + zone
  }

  /**
   * Checks if the exception is, or has a cause, of {@code IllegalInstantException}.
   *
   * @param ex  the exception to check
   * @return true if an { @code IllegalInstantException}
   */
  def isIllegalInstant(ex: Throwable): Boolean = {
    if (ex.isInstanceOf[IllegalInstantException]) {
      return true
    }
    while (ex.getCause != null && ex.getCause ne ex) {
      return isIllegalInstant(ex.getCause)
    }
    return false
  }
}

@SerialVersionUID(2858712538216L)
class IllegalInstantException extends IllegalArgumentException {
  /**
   * Constructor.
   *
   * @param message  the message
   */
  def this(message: String) {
    this()
    `super`(message)
  }

  /**
   * Constructor.
   *
   * @param instantLocal  the local instant
   * @param zoneId  the time-zone ID, may be null
   */
  def this(instantLocal: Long, zoneId: String) {
    this()
    `super`(IllegalInstantException.createMessage(instantLocal, zoneId))
  }
}
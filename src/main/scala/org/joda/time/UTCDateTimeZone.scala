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
package org.joda.time

/**
 * A DateTimeZone implementation for UTC.
 * <p>
 * This exists instead of using FixedDateTimeZone to avoid deadlocks.
 * https://github.com/JodaOrg/joda-time/issues/171
 */
@SerialVersionUID(-3513011772763289092L)
object UTCDateTimeZone {
  private[time] val INSTANCE: DateTimeZone = new UTCDateTimeZone
}

@SerialVersionUID(-3513011772763289092L)
final class UTCDateTimeZone extends DateTimeZone {
  def this() {
    this()
    `super`("UTC")
  }

  def getNameKey(instant: Long): String = {
    return "UTC"
  }

  def getOffset(instant: Long): Int = {
    return 0
  }

  def getStandardOffset(instant: Long): Int = {
    return 0
  }

  override def getOffsetFromLocal(instantLocal: Long): Int = {
    return 0
  }

  def isFixed: Boolean = {
    return true
  }

  def nextTransition(instant: Long): Long = {
    return instant
  }

  def previousTransition(instant: Long): Long = {
    return instant
  }

  override def toTimeZone: TimeZone = {
    return new SimpleTimeZone(0, getID)
  }

  def equals(obj: AnyRef): Boolean = {
    return (obj.isInstanceOf[UTCDateTimeZone])
  }

  override def hashCode: Int = {
    return getID.hashCode
  }
}
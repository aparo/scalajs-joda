/*
 *  Copyright 2001-2005 Stephen Colebourne
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
package org.joda.time.tz

import org.joda.time.DateTimeZone

/**
 * Basic DateTimeZone implementation that has a fixed name key and offsets.
 * <p>
 * FixedDateTimeZone is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-3513011772763289092L)
final class FixedDateTimeZone extends DateTimeZone {
  private final val iNameKey: String = null
  private final val iWallOffset: Int = 0
  private final val iStandardOffset: Int = 0

  def this(id: String, nameKey: String, wallOffset: Int, standardOffset: Int) {
    this()
    `super`(id)
    iNameKey = nameKey
    iWallOffset = wallOffset
    iStandardOffset = standardOffset
  }

  def getNameKey(instant: Long): String = {
    return iNameKey
  }

  def getOffset(instant: Long): Int = {
    return iWallOffset
  }

  def getStandardOffset(instant: Long): Int = {
    return iStandardOffset
  }

  override def getOffsetFromLocal(instantLocal: Long): Int = {
    return iWallOffset
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

  /**
   * Override to return the correct timzone instance.
   * @since 1.5
   */
  override def toTimeZone: TimeZone = {
    val id: String = getID
    if (id.length == 6 && (id.startsWith("+") || id.startsWith("-"))) {
      return java.util.TimeZone.getTimeZone("GMT" + getID)
    }
    return new SimpleTimeZone(iWallOffset, getID)
  }

  def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[FixedDateTimeZone]) {
      val other: FixedDateTimeZone = obj.asInstanceOf[FixedDateTimeZone]
      return (getID == other.getID) && iStandardOffset == other.iStandardOffset && iWallOffset == other.iWallOffset
    }
    return false
  }

  override def hashCode: Int = {
    return getID.hashCode + 37 * iStandardOffset + 31 * iWallOffset
  }
}
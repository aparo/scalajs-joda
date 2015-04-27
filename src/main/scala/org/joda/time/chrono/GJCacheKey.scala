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

import org.joda.time.DateTimeZone
import org.joda.time.Instant

/**
 * For internal usage in GJChronology only.
 */
class GJCacheKey {
  private final val zone: DateTimeZone = null
  private final val cutoverInstant: Instant = null
  private final val minDaysInFirstWeek: Int = 0

  private[chrono] def this(zone: DateTimeZone, cutoverInstant: Instant, minDaysInFirstWeek: Int) {
    this()
    this.zone = zone
    this.cutoverInstant = cutoverInstant
    this.minDaysInFirstWeek = minDaysInFirstWeek
  }

  override def hashCode: Int = {
    val prime: Int = 31
    var result: Int = 1
    result = prime * result + (if ((cutoverInstant == null)) 0 else cutoverInstant.hashCode)
    result = prime * result + minDaysInFirstWeek
    result = prime * result + (if ((zone == null)) 0 else zone.hashCode)
    return result
  }

  override def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj == null) {
      return false
    }
    if (!(obj.isInstanceOf[GJCacheKey])) {
      return false
    }
    val other: GJCacheKey = obj.asInstanceOf[GJCacheKey]
    if (cutoverInstant == null) {
      if (other.cutoverInstant != null) {
        return false
      }
    }
    else if (!(cutoverInstant == other.cutoverInstant)) {
      return false
    }
    if (minDaysInFirstWeek != other.minDaysInFirstWeek) {
      return false
    }
    if (zone == null) {
      if (other.zone != null) {
        return false
      }
    }
    else if (!(zone == other.zone)) {
      return false
    }
    return true
  }
}
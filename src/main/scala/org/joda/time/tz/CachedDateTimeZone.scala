/*
 *  Copyright 2001-2012 Stephen Colebourne
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
 * Improves the performance of requesting time zone offsets and name keys by
 * caching the results. Time zones that have simple rules or are fixed should
 * not be cached, as it is unlikely to improve performance.
 * <p>
 * CachedDateTimeZone is thread-safe and immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(5472298452022250685L)
object CachedDateTimeZone {
  private val cInfoCacheMask: Int = 0

  /**
   * Returns a new CachedDateTimeZone unless given zone is already cached.
   */
  def forZone(zone: DateTimeZone): CachedDateTimeZone = {
    if (zone.isInstanceOf[CachedDateTimeZone]) {
      return zone.asInstanceOf[CachedDateTimeZone]
    }
    return new CachedDateTimeZone(zone)
  }

  private final class Info {
    final val iPeriodStart: Long = 0L
    final val iZoneRef: DateTimeZone = null
    private[tz] var iNextInfo: CachedDateTimeZone.Info = null
    private var iNameKey: String = null
    private var iOffset: Int = Integer.MIN_VALUE
    private var iStandardOffset: Int = Integer.MIN_VALUE

    private[tz] def this(zone: DateTimeZone, periodStart: Long) {
      this()
      iPeriodStart = periodStart
      iZoneRef = zone
    }

    def getNameKey(millis: Long): String = {
      if (iNextInfo == null || millis < iNextInfo.iPeriodStart) {
        if (iNameKey == null) {
          iNameKey = iZoneRef.getNameKey(iPeriodStart)
        }
        return iNameKey
      }
      return iNextInfo.getNameKey(millis)
    }

    def getOffset(millis: Long): Int = {
      if (iNextInfo == null || millis < iNextInfo.iPeriodStart) {
        if (iOffset == Integer.MIN_VALUE) {
          iOffset = iZoneRef.getOffset(iPeriodStart)
        }
        return iOffset
      }
      return iNextInfo.getOffset(millis)
    }

    def getStandardOffset(millis: Long): Int = {
      if (iNextInfo == null || millis < iNextInfo.iPeriodStart) {
        if (iStandardOffset == Integer.MIN_VALUE) {
          iStandardOffset = iZoneRef.getStandardOffset(iPeriodStart)
        }
        return iStandardOffset
      }
      return iNextInfo.getStandardOffset(millis)
    }
  }

  try {
    var i: Integer = null
    try {
      i = Integer.getInteger("org.joda.time.tz.CachedDateTimeZone.size")
    }
    catch {
      case e: SecurityException => {
        i = null
      }
    }
    var cacheSize: Int = 0
    if (i == null) {
      cacheSize = 512
    }
    else {
      cacheSize = i.intValue
      cacheSize -= 1
      var shift: Int = 0
      while (cacheSize > 0) {
        shift += 1
        cacheSize >>= 1
      }
      cacheSize = 1 << shift
    }
    cInfoCacheMask = cacheSize - 1
  }
}

@SerialVersionUID(5472298452022250685L)
class CachedDateTimeZone extends DateTimeZone {
  private final val iZone: DateTimeZone = null
  @transient
  private final val iInfoCache: Array[CachedDateTimeZone.Info] = new Array[CachedDateTimeZone.Info](CachedDateTimeZone.cInfoCacheMask + 1)

  private def this(zone: DateTimeZone) {
    this()
    `super`(zone.getID)
    iZone = zone
  }

  /**
   * Returns the DateTimeZone being wrapped.
   */
  def getUncachedZone: DateTimeZone = {
    return iZone
  }

  def getNameKey(instant: Long): String = {
    return getInfo(instant).getNameKey(instant)
  }

  def getOffset(instant: Long): Int = {
    return getInfo(instant).getOffset(instant)
  }

  def getStandardOffset(instant: Long): Int = {
    return getInfo(instant).getStandardOffset(instant)
  }

  def isFixed: Boolean = {
    return iZone.isFixed
  }

  def nextTransition(instant: Long): Long = {
    return iZone.nextTransition(instant)
  }

  def previousTransition(instant: Long): Long = {
    return iZone.previousTransition(instant)
  }

  override def hashCode: Int = {
    return iZone.hashCode
  }

  def equals(obj: AnyRef): Boolean = {
    if (this eq obj) {
      return true
    }
    if (obj.isInstanceOf[CachedDateTimeZone]) {
      return iZone == (obj.asInstanceOf[CachedDateTimeZone]).iZone
    }
    return false
  }

  private def getInfo(millis: Long): CachedDateTimeZone.Info = {
    val period: Int = (millis >> 32).toInt
    val cache: Array[CachedDateTimeZone.Info] = iInfoCache
    val index: Int = period & CachedDateTimeZone.cInfoCacheMask
    var info: CachedDateTimeZone.Info = cache(index)
    if (info == null || ((info.iPeriodStart >> 32)).toInt != period) {
      info = createInfo(millis)
      cache(index) = info
    }
    return info
  }

  private def createInfo(millis: Long): CachedDateTimeZone.Info = {
    var periodStart: Long = millis & (0xffffffffL << 32)
    val info: CachedDateTimeZone.Info = new CachedDateTimeZone.Info(iZone, periodStart)
    val end: Long = periodStart | 0xffffffffL
    var chain: CachedDateTimeZone.Info = info
    while (true) {
      val next: Long = iZone.nextTransition(periodStart)
      if (next == periodStart || next > end) {
        break //todo: break is not supported
      }
      periodStart = next
      chain = (({
        chain.iNextInfo = new CachedDateTimeZone.Info(iZone, periodStart); chain.iNextInfo
      }))
    }
    return info
  }
}
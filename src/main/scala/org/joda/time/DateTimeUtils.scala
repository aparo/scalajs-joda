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

import java.lang.reflect.Method
import java.text.DateFormatSymbols
import java.util.Collections
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Locale
import java.util.Map
import java.util.concurrent.atomic.AtomicReference
import org.joda.time.Chronology
import org.joda.time.chrono.ISOChronology

/**
 * DateTimeUtils provide public utility methods for the date-time library.
 * <p>
 * DateTimeUtils uses shared static variables which are declared as volatile
 * for thread-safety. These can be changed during the lifetime of the application
 * however doing so is generally a bad idea.
 *
 * @author Stephen Colebourne
 * @since 1.0
 */
object DateTimeUtils {
  /** The singleton instance of the system millisecond provider. */
  private val SYSTEM_MILLIS_PROVIDER: DateTimeUtils.SystemMillisProvider = new DateTimeUtils.SystemMillisProvider
  /** The millisecond provider currently in use. */
  @volatile
  private var cMillisProvider: DateTimeUtils.MillisProvider = SYSTEM_MILLIS_PROVIDER
  /**
   * The default names.
   * This is lazily initialized to reduce risks of race conditions at startup.
   */
  private val cZoneNames: AtomicReference[Map[Nothing, Nothing]] = new AtomicReference[Map[Nothing, Nothing]]

  /**
   * Gets the current time in milliseconds.
   * <p>
   * By default this returns <code>System.currentTimeMillis()</code>.
   * This may be changed using other methods in this class.
   *
   * @return the current time in milliseconds from 1970-01-01T00:00:00Z
   */
  def currentTimeMillis: Long = {
    return cMillisProvider.getMillis
  }

  /**
   * Resets the current time to return the system time.
   * <p>
   * This method changes the behaviour of {@link #currentTimeMillis()}.
   * Whenever the current time is queried, {@link System#currentTimeMillis()} is used.
   *
   * @throws SecurityException if the application does not have sufficient security rights
   */
  @throws(classOf[SecurityException])
  def setCurrentMillisSystem {
    checkPermission
    cMillisProvider = SYSTEM_MILLIS_PROVIDER
  }

  /**
   * Sets the current time to return a fixed millisecond time.
   * <p>
   * This method changes the behaviour of {@link #currentTimeMillis()}.
   * Whenever the current time is queried, the same millisecond time will be returned.
   *
   * @param fixedMillis  the fixed millisecond time to use
   * @throws SecurityException if the application does not have sufficient security rights
   */
  @throws(classOf[SecurityException])
  def setCurrentMillisFixed(fixedMillis: Long) {
    checkPermission
    cMillisProvider = new DateTimeUtils.FixedMillisProvider(fixedMillis)
  }

  /**
   * Sets the current time to return the system time plus an offset.
   * <p>
   * This method changes the behaviour of {@link #currentTimeMillis()}.
   * Whenever the current time is queried, {@link System#currentTimeMillis()} is used
   * and then offset by adding the millisecond value specified here.
   *
   * @param offsetMillis  the fixed millisecond time to use
   * @throws SecurityException if the application does not have sufficient security rights
   */
  @throws(classOf[SecurityException])
  def setCurrentMillisOffset(offsetMillis: Long) {
    checkPermission
    if (offsetMillis == 0) {
      cMillisProvider = SYSTEM_MILLIS_PROVIDER
    }
    else {
      cMillisProvider = new DateTimeUtils.OffsetMillisProvider(offsetMillis)
    }
  }

  /**
   * Sets the provider of the current time to class specified.
   * <p>
   * This method changes the behaviour of {@link #currentTimeMillis()}.
   * Whenever the current time is queried, the specified class will be called.
   *
   * @param millisProvider  the provider of the current time to use, not null
   * @throws SecurityException if the application does not have sufficient security rights
   * @since 2.0
   */
  @throws(classOf[SecurityException])
  def setCurrentMillisProvider(millisProvider: DateTimeUtils.MillisProvider) {
    if (millisProvider == null) {
      throw new Nothing("The MillisProvider must not be null")
    }
    checkPermission
    cMillisProvider = millisProvider
  }

  /**
   * Checks whether the provider may be changed using permission 'CurrentTime.setProvider'.
   *
   * @throws SecurityException if the provider may not be changed
   */
  @throws(classOf[SecurityException])
  private def checkPermission {
    val sm = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new Nothing("CurrentTime.setProvider"))
    }
  }

  /**
   * Gets the millisecond instant from the specified instant object handling null.
   * <p>
   * If the instant object is <code>null</code>, the {@link #currentTimeMillis()}
   * will be returned. Otherwise, the millis from the object are returned.
   *
   * @param instant  the instant to examine, null means now
   * @return the time in milliseconds from 1970-01-01T00:00:00Z
   */
  def getInstantMillis(instant: Nothing): Long = {
    if (instant == null) {
      return DateTimeUtils.currentTimeMillis
    }
    return instant.getMillis
  }

  /**
   * Gets the chronology from the specified instant object handling null.
   * <p>
   * If the instant object is <code>null</code>, or the instant's chronology is
   * <code>null</code>, {@link ISOChronology#getInstance()} will be returned.
   * Otherwise, the chronology from the object is returned.
   *
   * @param instant  the instant to examine, null means ISO in the default zone
   * @return the chronology, never null
   */
  def getInstantChronology(instant: Nothing): Nothing = {
    if (instant == null) {
      return ISOChronology.getInstance
    }
    val chrono: Nothing = instant.getChronology
    if (chrono == null) {
      return ISOChronology.getInstance
    }
    return chrono
  }

  /**
   * Gets the chronology from the specified instant based interval handling null.
   * <p>
   * The chronology is obtained from the start if that is not null, or from the
   * end if the start is null. The result is additionally checked, and if still
   * null then {@link ISOChronology#getInstance()} will be returned.
   *
   * @param start  the instant to examine and use as the primary source of the chronology
   * @param end  the instant to examine and use as the secondary source of the chronology
   * @return the chronology, never null
   */
  def getIntervalChronology(start: Nothing, end: Nothing): Nothing = {
    var chrono: Nothing = null
    if (start != null) {
      chrono = start.getChronology
    }
    else if (end != null) {
      chrono = end.getChronology
    }
    if (chrono == null) {
      chrono = ISOChronology.getInstance
    }
    return chrono
  }

  /**
   * Gets the chronology from the specified interval object handling null.
   * <p>
   * If the interval object is <code>null</code>, or the interval's chronology is
   * <code>null</code>, {@link ISOChronology#getInstance()} will be returned.
   * Otherwise, the chronology from the object is returned.
   *
   * @param interval  the interval to examine, null means ISO in the default zone
   * @return the chronology, never null
   */
  def getIntervalChronology(interval: Nothing): Nothing = {
    if (interval == null) {
      return ISOChronology.getInstance
    }
    val chrono: Nothing = interval.getChronology
    if (chrono == null) {
      return ISOChronology.getInstance
    }
    return chrono
  }

  /**
   * Gets the interval handling null.
   * <p>
   * If the interval is <code>null</code>, an interval representing now
   * to now in the {@link ISOChronology#getInstance() ISOChronology}
   * will be returned. Otherwise, the interval specified is returned.
   *
   * @param interval  the interval to use, null means now to now
   * @return the interval, never null
   * @since 1.1
   */
  def getReadableInterval(interval: Nothing): Nothing = {
    if (interval == null) {
      val now: Long = DateTimeUtils.currentTimeMillis
      interval = new Nothing(now, now)
    }
    return interval
  }

  /**
   * Gets the chronology handling null.
   * <p>
   * If the chronology is <code>null</code>, {@link ISOChronology#getInstance()}
   * will be returned. Otherwise, the chronology is returned.
   *
   * @param chrono  the chronology to use, null means ISO in the default zone
   * @return the chronology, never null
   */
  def getChronology(chrono: Nothing): Nothing = {
    if (chrono == null) {
      return ISOChronology.getInstance
    }
    return chrono
  }

  /**
   * Gets the zone handling null.
   * <p>
   * If the zone is <code>null</code>, {@link DateTimeZone#getDefault()}
   * will be returned. Otherwise, the zone specified is returned.
   *
   * @param zone  the time zone to use, null means the default zone
   * @return the time zone, never null
   */
  def getZone(zone: Nothing): Nothing = {
    if (zone == null) {
      return DateTimeZone.getDefault
    }
    return zone
  }

  /**
   * Gets the period type handling null.
   * <p>
   * If the zone is <code>null</code>, {@link PeriodType#standard()}
   * will be returned. Otherwise, the type specified is returned.
   *
   * @param type  the time zone to use, null means the standard type
   * @return the type to use, never null
   */
  def getPeriodType(`type`: Nothing): Nothing = {
    if (`type` == null) {
      return PeriodType.standard
    }
    return `type`
  }

  /**
   * Gets the millisecond duration from the specified duration object handling null.
   * <p>
   * If the duration object is <code>null</code>, zero will be returned.
   * Otherwise, the millis from the object are returned.
   *
   * @param duration  the duration to examine, null means zero
   * @return the duration in milliseconds
   */
  def getDurationMillis(duration: Nothing): Long = {
    if (duration == null) {
      return 0L
    }
    return duration.getMillis
  }

  /**
   * Checks whether the partial is contiguous.
   * <p>
   * A partial is contiguous if one field starts where another ends.
   * <p>
   * For example <code>LocalDate</code> is contiguous because DayOfMonth has
   * the same range (Month) as the unit of the next field (MonthOfYear), and
   * MonthOfYear has the same range (Year) as the unit of the next field (Year).
   * <p>
   * Similarly, <code>LocalTime</code> is contiguous, as it consists of
   * MillisOfSecond, SecondOfMinute, MinuteOfHour and HourOfDay (note how
   * the names of each field 'join up').
   * <p>
   * However, a Year/HourOfDay partial is not contiguous because the range
   * field Day is not equal to the next field Year.
   * Similarly, a DayOfWeek/DayOfMonth partial is not contiguous because
   * the range Month is not equal to the next field Day.
   *
   * @param partial  the partial to check
   * @return true if the partial is contiguous
   * @throws IllegalArgumentException if the partial is null
   * @since 1.1
   */
  def isContiguous(partial: Nothing): Boolean = {
    if (partial == null) {
      throw new Nothing("Partial must not be null")
    }
    var lastType: Nothing = null
    {
      var i: Int = 0
      while (i < partial.size) {
        {
          val loopField: Nothing = partial.getField(i)
          if (i > 0) {
            if (loopField.getRangeDurationField == null || loopField.getRangeDurationField.getType ne lastType) {
              return false
            }
          }
          lastType = loopField.getDurationField.getType
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return true
  }

  /**
   * Gets the {@link DateFormatSymbols} based on the given locale.
   * <p>
   * If JDK 6 or newer is being used, DateFormatSymbols.getInstance(locale) will
   * be used in order to allow the use of locales defined as extensions.
   * Otherwise, new DateFormatSymbols(locale) will be used.
   * See JDK 6 {@link DateFormatSymbols} for further information.
   *
   * @param locale  the { @link Locale} used to get the correct { @link DateFormatSymbols}
   * @return the symbols
   * @since 2.0
   */
  def getDateFormatSymbols(locale: Locale): DateFormatSymbols = {
    try {
      val method: Method = classOf[DateFormatSymbols].getMethod("getInstance", Array[Nothing](classOf[Locale]))
      return method.invoke(null, Array[Nothing](locale)).asInstanceOf[DateFormatSymbols]
    }
    catch {
      case ex: Nothing => {
        return new DateFormatSymbols(locale)
      }
    }
  }

  /**
   * Gets the default map of time zone names.
   * <p>
   * This can be changed by {@link #setDefaultTimeZoneNames}.
   * <p>
   * The default set of short time zone names is as follows:
   * <ul>
   * <li>UT - UTC
   * <li>UTC - UTC
   * <li>GMT - UTC
   * <li>EST - America/New_York
   * <li>EDT - America/New_York
   * <li>CST - America/Chicago
   * <li>CDT - America/Chicago
   * <li>MST - America/Denver
   * <li>MDT - America/Denver
   * <li>PST - America/Los_Angeles
   * <li>PDT - America/Los_Angeles
   * </ul>
   *
   * @return the unmodifiable map of abbreviations to zones, not null
   * @since 2.2
   */
  def getDefaultTimeZoneNames: Map[Nothing, Nothing] = {
    var names: Map[Nothing, Nothing] = cZoneNames.get
    if (names == null) {
      names = buildDefaultTimeZoneNames
      if (!cZoneNames.compareAndSet(null, names)) {
        names = cZoneNames.get
      }
    }
    return names
  }

  /**
   * Sets the default map of time zone names.
   * <p>
   * The map is copied before storage.
   *
   * @param names  the map of abbreviations to zones, not null
   * @since 2.2
   */
  def setDefaultTimeZoneNames(names: Map[Nothing, Nothing]) {
    cZoneNames.set(Collections.unmodifiableMap(new HashMap[Nothing, Nothing](names)))
  }

  private def buildDefaultTimeZoneNames: Map[Nothing, Nothing] = {
    val map: Map[Nothing, Nothing] = new LinkedHashMap[Nothing, Nothing]
    map.put("UT", DateTimeZone.UTC)
    map.put("UTC", DateTimeZone.UTC)
    map.put("GMT", DateTimeZone.UTC)
    put(map, "EST", "America/New_York")
    put(map, "EDT", "America/New_York")
    put(map, "CST", "America/Chicago")
    put(map, "CDT", "America/Chicago")
    put(map, "MST", "America/Denver")
    put(map, "MDT", "America/Denver")
    put(map, "PST", "America/Los_Angeles")
    put(map, "PDT", "America/Los_Angeles")
    return Collections.unmodifiableMap(map)
  }

  private def put(map: Map[Nothing, Nothing], name: Nothing, id: Nothing) {
    try {
      map.put(name, DateTimeZone.forID(id))
    }
    catch {
      case ex: Nothing => {
      }
    }
  }

  /**
   * Calculates the astronomical Julian Day for an instant.
   * <p>
   * The <a href="http://en.wikipedia.org/wiki/Julian_day">Julian day</a> is a well-known
   * system of time measurement for scientific use by the astronomy community.
   * It expresses the interval of time in days and fractions of a day since
   * January 1, 4713 BC (Julian) Greenwich noon.
   * <p>
   * Each day starts at midday (not midnight) and time is expressed as a fraction.
   * Thus the fraction 0.25 is 18:00. equal to one quarter of the day from midday to midday.
   * <p>
   * Note that this method has nothing to do with the day-of-year.
   *
   * @param epochMillis  the epoch millis from 1970-01-01Z
   * @return the astronomical Julian Day represented by the specified instant
   * @since 2.2
   */
  def toJulianDay(epochMillis: Long): Double = {
    val epochDay: Double = epochMillis / 86400000d
    return epochDay + 2440587.5d
  }

  /**
   * Calculates the astronomical Julian Day Number for an instant.
   * <p>
   * The {@link #toJulianDay(long)} method calculates the astronomical Julian Day
   * with a fraction based on days starting at midday.
   * This method calculates the variant where days start at midnight.
   * JDN 0 is used for the date equivalent to Monday January 1, 4713 BC (Julian).
   * Thus these days start 12 hours before those of the fractional Julian Day.
   * <p>
   * Note that this method has nothing to do with the day-of-year.
   *
   * @param epochMillis  the epoch millis from 1970-01-01Z
   * @return the astronomical Julian Day represented by the specified instant
   * @since 2.2
   */
  def toJulianDayNumber(epochMillis: Long): Long = {
    return Math.floor(toJulianDay(epochMillis) + 0.5d).asInstanceOf[Long]
  }

  /**
   * Creates a date-time from a Julian Day.
   * <p>
   * Returns the {@code DateTime} object equal to the specified Julian Day.
   *
   * @param julianDay  the Julian Day
   * @return the epoch millis from 1970-01-01Z
   * @since 2.2
   */
  def fromJulianDay(julianDay: Double): Long = {
    val epochDay: Double = julianDay - 2440587.5d
    return (epochDay * 86400000d).toLong
  }

  /**
   * A millisecond provider, allowing control of the system clock.
   *
   * @author Stephen Colebourne
   * @since 2.0 (previously private)
   */
  trait MillisProvider {
    /**
     * Gets the current time.
     * <p>
     * Implementations of this method must be thread-safe.
     *
     * @return the current time in milliseconds
     */
    def getMillis: Long
  }

  /**
   * System millis provider.
   */
  private[time] class SystemMillisProvider extends MillisProvider {
    /**
     * Gets the current time.
     * @return the current time in millis
     */
    def getMillis: Long = {
      return System.currentTimeMillis
    }
  }

  /**
   * Fixed millisecond provider.
   */
  private[time] class FixedMillisProvider extends MillisProvider {
    /** The fixed millis value. */
    private final val iMillis: Long = 0L

    /**
     * Constructor.
     * @param fixedMillis  the millis value
     */
    private[time] def this(fixedMillis: Long) {
      this()
      iMillis = fixedMillis
    }

    /**
     * Gets the current time.
     * @return the current time in millis
     */
    def getMillis: Long = {
      return iMillis
    }
  }

  /**
   * Offset from system millis provider.
   */
  private[time] class OffsetMillisProvider extends MillisProvider {
    /** The millis offset. */
    private final val iMillis: Long = 0L

    /**
     * Constructor.
     * @param offsetMillis  the millis offset
     */
    private[time] def this(offsetMillis: Long) {
      this()
      iMillis = offsetMillis
    }

    /**
     * Gets the current time.
     * @return the current time in millis
     */
    def getMillis: Long = {
      return System.currentTimeMillis + iMillis
    }
  }

}

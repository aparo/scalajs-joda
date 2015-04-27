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

import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamException
import java.io.Serializable
import java.util.Collections
import java.util.HashMap
import java.util.Locale
import java.util.Map
import java.util.Set
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference
import org.joda.convert.FromString
import org.joda.convert.ToString
import org.joda.time.chrono.BaseChronology
import org.joda.time.field.FieldUtils
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.FormatUtils
import org.joda.time.tz.DefaultNameProvider
import org.joda.time.tz.FixedDateTimeZone
import org.joda.time.tz.NameProvider
import org.joda.time.tz.Provider
import org.joda.time.tz.UTCProvider
import org.joda.time.tz.ZoneInfoProvider

/**
 * DateTimeZone represents a time zone.
 * <p>
 * A time zone is a system of rules to convert time from one geographic
 * location to another. For example, Paris, France is one hour ahead of
 * London, England. Thus when it is 10:00 in London, it is 11:00 in Paris.
 * <p>
 * All time zone rules are expressed, for historical reasons, relative to
 * Greenwich, London. Local time in Greenwich is referred to as Greenwich Mean
 * Time (GMT).  This is similar, but not precisely identical, to Universal
 * Coordinated Time, or UTC. This library only uses the term UTC.
 * <p>
 * Using this system, America/Los_Angeles is expressed as UTC-08:00, or UTC-07:00
 * in the summer. The offset -08:00 indicates that America/Los_Angeles time is
 * obtained from UTC by adding -08:00, that is, by subtracting 8 hours.
 * <p>
 * The offset differs in the summer because of daylight saving time, or DST.
 * The following definitions of time are generally used:
 * <ul>
 * <li>UTC - The reference time.
 * <li>Standard Time - The local time without a daylight saving time offset.
 * For example, in Paris, standard time is UTC+01:00.
 * <li>Daylight Saving Time - The local time with a daylight saving time
 * offset. This offset is typically one hour, but not always. It is typically
 * used in most countries away from the equator.  In Paris, daylight saving
 * time is UTC+02:00.
 * <li>Wall Time - This is what a local clock on the wall reads. This will be
 * either Standard Time or Daylight Saving Time depending on the time of year
 * and whether the location uses Daylight Saving Time.
 * </ul>
 * <p>
 * Unlike the Java TimeZone class, DateTimeZone is immutable. It also only
 * supports long format time zone ids. Thus EST and ECT are not accepted.
 * However, the factory that accepts a TimeZone will attempt to convert from
 * the old short id to a suitable long id.
 * <p>
 * There are four approaches to loading time-zone data, which are tried in this order:
 * <ol>
 * <li>load the specific {@link Provider} specified by the system property
 * {@code org.joda.time.DateTimeZone.Provider}.
 * <li>load {@link ZoneInfoProvider} using the data in the filing system folder
 * pointed to by system property {@code org.joda.time.DateTimeZone.Folder}.
 * <li>load {@link ZoneInfoProvider} using the data in the classpath location
 * {@code org/joda/time/tz/data}.
 * <li>load {@link UTCProvider}
 * </ol>
 * <p>
 * Unless you override the standard behaviour, the default if the third approach.
 * <p>
 * DateTimeZone is thread-safe and immutable, and all subclasses must be as
 * well.
 *
 * @author Brian S O'Neill
 * @author Stephen Colebourne
 * @since 1.0
 */
@SerialVersionUID(5546345482340108586L)
object DateTimeZone {
  /** The time zone for Universal Coordinated Time */
  val UTC: DateTimeZone = UTCDateTimeZone.INSTANCE
  /** Maximum offset. */
  private val MAX_MILLIS: Int = (86400 * 1000) - 1
  /**
   * The instance that is providing time zones.
   * This is lazily initialized to reduce risks of race conditions at startup.
   */
  private val cProvider: AtomicReference[Provider] = new AtomicReference[Provider]
  /**
   * The instance that is providing time zone names.
   * This is lazily initialized to reduce risks of race conditions at startup.
   */
  private val cNameProvider: AtomicReference[NameProvider] = new AtomicReference[NameProvider]
  /**
   * The default time zone.
   * This is lazily initialized to reduce risks of race conditions at startup.
   */
  private val cDefault: AtomicReference[DateTimeZone] = new AtomicReference[DateTimeZone]

  /**
   * Gets the default time zone.
   * <p>
   * The default time zone is derived from the system property {@code user.timezone}.
   * If that is {@code null} or is not a valid identifier, then the value of the
   * JDK {@code TimeZone} default is converted. If that fails, {@code UTC} is used.
   * <p>
   * NOTE: If the {@code java.util.TimeZone} default is updated <i>after</i> calling this
   * method, then the change will not be picked up here.
   *
   * @return the default datetime zone object
   */
  def getDefault: DateTimeZone = {
    var zone: DateTimeZone = cDefault.get
    if (zone == null) {
      try {
        try {
          val id: String = System.getProperty("user.timezone")
          if (id != null) {
            zone = forID(id)
          }
        }
        catch {
          case ex: RuntimeException => {
          }
        }
        if (zone == null) {
          zone = forTimeZone(TimeZone.getDefault)
        }
      }
      catch {
        case ex: IllegalArgumentException => {
        }
      }
      if (zone == null) {
        zone = UTC
      }
      if (!cDefault.compareAndSet(null, zone)) {
        zone = cDefault.get
      }
    }
    return zone
  }

  /**
   * Sets the default time zone.
   * <p>
   * NOTE: Calling this method does <i>not</i> set the {@code java.util.TimeZone} default.
   *
   * @param zone  the default datetime zone object, must not be null
   * @throws IllegalArgumentException if the zone is null
   * @throws SecurityException if the application has insufficient security rights
   */
  @throws(classOf[SecurityException])
  def setDefault(zone: DateTimeZone) {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("DateTimeZone.setDefault"))
    }
    if (zone == null) {
      throw new IllegalArgumentException("The datetime zone must not be null")
    }
    cDefault.set(zone)
  }

  /**
   * Gets a time zone instance for the specified time zone id.
   * <p>
   * The time zone id may be one of those returned by getAvailableIDs.
   * Short ids, as accepted by {@link java.util.TimeZone}, are not accepted.
   * All IDs must be specified in the long format.
   * The exception is UTC, which is an acceptable id.
   * <p>
   * Alternatively a locale independent, fixed offset, datetime zone can
   * be specified. The form <code>[+-]hh:mm</code> can be used.
   *
   * @param id  the ID of the datetime zone, null means default
   * @return the DateTimeZone object for the ID
   * @throws IllegalArgumentException if the ID is not recognised
   */
  @FromString def forID(id: String): DateTimeZone = {
    if (id == null) {
      return getDefault
    }
    if (id == "UTC") {
      return DateTimeZone.UTC
    }
    val zone: DateTimeZone = getProvider.getZone(id)
    if (zone != null) {
      return zone
    }
    if (id.startsWith("+") || id.startsWith("-")) {
      val offset: Int = parseOffset(id)
      if (offset == 0L) {
        return DateTimeZone.UTC
      }
      else {
        id = printOffset(offset)
        return fixedOffsetZone(id, offset)
      }
    }
    throw new IllegalArgumentException("The datetime zone id '" + id + "' is not recognised")
  }

  /**
   * Gets a time zone instance for the specified offset to UTC in hours.
   * This method assumes standard length hours.
   * <p>
   * This factory is a convenient way of constructing zones with a fixed offset.
   *
   * @param hoursOffset  the offset in hours from UTC, from -23 to +23
   * @return the DateTimeZone object for the offset
   * @throws IllegalArgumentException if the offset is too large or too small
   */
  @throws(classOf[IllegalArgumentException])
  def forOffsetHours(hoursOffset: Int): DateTimeZone = {
    return forOffsetHoursMinutes(hoursOffset, 0)
  }

  /**
   * Gets a time zone instance for the specified offset to UTC in hours and minutes.
   * This method assumes 60 minutes in an hour, and standard length minutes.
   * <p>
   * This factory is a convenient way of constructing zones with a fixed offset.
   * The hours value must be in the range -23 to +23.
   * The minutes value must be in the range -59 to +59.
   * The following combinations of sign for the hour and minute are possible:
   * <pre>
   * Hour    Minute    Example    Result
   *
   * +ve     +ve       (2, 15)    +02:15
   * +ve     zero      (2, 0)     +02:00
   * +ve     -ve       (2, -15)   IllegalArgumentException
   *
   * zero    +ve       (0, 15)    +00:15
   * zero    zero      (0, 0)     +00:00
   * zero    -ve       (0, -15)   -00:15
   *
   * -ve     +ve       (-2, 15)   -02:15
   * -ve     zero      (-2, 0)    -02:00
   * -ve     -ve       (-2, -15)  -02:15
   * </pre>
   * Note that in versions before 2.3, the minutes had to be zero or positive.
   *
   * @param hoursOffset  the offset in hours from UTC, from -23 to +23
   * @param minutesOffset  the offset in minutes from UTC, from -59 to +59
   * @return the DateTimeZone object for the offset
   * @throws IllegalArgumentException if any value is out of range, the minutes are negative
   *                                  when the hours are positive, or the resulting offset exceeds +/- 23:59:59.000
   */
  @throws(classOf[IllegalArgumentException])
  def forOffsetHoursMinutes(hoursOffset: Int, minutesOffset: Int): DateTimeZone = {
    if (hoursOffset == 0 && minutesOffset == 0) {
      return DateTimeZone.UTC
    }
    if (hoursOffset < -23 || hoursOffset > 23) {
      throw new IllegalArgumentException("Hours out of range: " + hoursOffset)
    }
    if (minutesOffset < -59 || minutesOffset > 59) {
      throw new IllegalArgumentException("Minutes out of range: " + minutesOffset)
    }
    if (hoursOffset > 0 && minutesOffset < 0) {
      throw new IllegalArgumentException("Positive hours must not have negative minutes: " + minutesOffset)
    }
    var offset: Int = 0
    try {
      val hoursInMinutes: Int = hoursOffset * 60
      if (hoursInMinutes < 0) {
        minutesOffset = hoursInMinutes - Math.abs(minutesOffset)
      }
      else {
        minutesOffset = hoursInMinutes + minutesOffset
      }
      offset = FieldUtils.safeMultiply(minutesOffset, DateTimeConstants.MILLIS_PER_MINUTE)
    }
    catch {
      case ex: ArithmeticException => {
        throw new IllegalArgumentException("Offset is too large")
      }
    }
    return forOffsetMillis(offset)
  }

  /**
   * Gets a time zone instance for the specified offset to UTC in milliseconds.
   *
   * @param millisOffset  the offset in millis from UTC, from -23:59:59.999 to +23:59:59.999
   * @return the DateTimeZone object for the offset
   */
  def forOffsetMillis(millisOffset: Int): DateTimeZone = {
    if (millisOffset < -MAX_MILLIS || millisOffset > MAX_MILLIS) {
      throw new IllegalArgumentException("Millis out of range: " + millisOffset)
    }
    val id: String = printOffset(millisOffset)
    return fixedOffsetZone(id, millisOffset)
  }

  /**
   * Gets a time zone instance for a JDK TimeZone.
   * <p>
   * DateTimeZone only accepts a subset of the IDs from TimeZone. The
   * excluded IDs are the short three letter form (except UTC). This
   * method will attempt to convert between time zones created using the
   * short IDs and the full version.
   * <p>
   * This method is not designed to parse time zones with rules created by
   * applications using <code>SimpleTimeZone</code> directly.
   *
   * @param zone  the zone to convert, null means default
   * @return the DateTimeZone object for the zone
   * @throws IllegalArgumentException if the zone is not recognised
   */
  def forTimeZone(zone: TimeZone): DateTimeZone = {
    if (zone == null) {
      return getDefault
    }
    val id: String = zone.getID
    if (id == null) {
      throw new IllegalArgumentException("The TimeZone id must not be null")
    }
    if (id == "UTC") {
      return DateTimeZone.UTC
    }
    var dtz: DateTimeZone = null
    var convId: String = getConvertedId(id)
    val provider: Provider = getProvider
    if (convId != null) {
      dtz = provider.getZone(convId)
    }
    if (dtz == null) {
      dtz = provider.getZone(id)
    }
    if (dtz != null) {
      return dtz
    }
    if (convId == null) {
      convId = id
      if (convId.startsWith("GMT+") || convId.startsWith("GMT-")) {
        convId = convId.substring(3)
        val offset: Int = parseOffset(convId)
        if (offset == 0L) {
          return DateTimeZone.UTC
        }
        else {
          convId = printOffset(offset)
          return fixedOffsetZone(convId, offset)
        }
      }
    }
    throw new IllegalArgumentException("The datetime zone id '" + id + "' is not recognised")
  }

  /**
   * Gets the zone using a fixed offset amount.
   *
   * @param id  the zone id
   * @param offset  the offset in millis
   * @return the zone
   */
  private def fixedOffsetZone(id: String, offset: Int): DateTimeZone = {
    if (offset == 0) {
      return DateTimeZone.UTC
    }
    return new FixedDateTimeZone(id, null, offset, offset)
  }

  /**
   * Gets all the available IDs supported.
   *
   * @return an unmodifiable Set of String IDs
   */
  def getAvailableIDs: Set[String] = {
    return getProvider.getAvailableIDs
  }

  /**
   * Gets the zone provider factory.
   * <p>
   * The zone provider is a pluggable instance factory that supplies the
   * actual instances of DateTimeZone.
   *
   * @return the provider
   */
  def getProvider: Provider = {
    var provider: Provider = cProvider.get
    if (provider == null) {
      provider = getDefaultProvider
      if (!cProvider.compareAndSet(null, provider)) {
        provider = cProvider.get
      }
    }
    return provider
  }

  /**
   * Sets the zone provider factory.
   * <p>
   * The zone provider is a pluggable instance factory that supplies the
   * actual instances of DateTimeZone.
   *
   * @param provider  provider to use, or null for default
   * @throws SecurityException if you do not have the permission DateTimeZone.setProvider
   * @throws IllegalArgumentException if the provider is invalid
   */
  @throws(classOf[SecurityException])
  def setProvider(provider: Provider) {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("DateTimeZone.setProvider"))
    }
    if (provider == null) {
      provider = getDefaultProvider
    }
    else {
      validateProvider(provider)
    }
    cProvider.set(provider)
  }

  /**
   * Sets the zone provider factory without performing the security check.
   *
   * @param provider  provider to use, or null for default
   * @return the provider
   * @throws IllegalArgumentException if the provider is invalid
   */
  private def validateProvider(provider: Provider): Provider = {
    val ids: Set[String] = provider.getAvailableIDs
    if (ids == null || ids.size == 0) {
      throw new IllegalArgumentException("The provider doesn't have any available ids")
    }
    if (!ids.contains("UTC")) {
      throw new IllegalArgumentException("The provider doesn't support UTC")
    }
    if (!(UTC == provider.getZone("UTC"))) {
      throw new IllegalArgumentException("Invalid UTC zone provided")
    }
    return provider
  }

  /**
   * Gets the default zone provider.
   * <p>
   * This tries four approaches to loading data:
   * <ol>
   * <li>loads the provider identifier by the system property
   * <code>org.joda.time.DateTimeZone.Provider</code>.
   * <li>load <code>ZoneInfoProvider</code> using the data in the filing system folder
   * pointed to by system property <code>org.joda.time.DateTimeZone.Folder</code>.
   * <li>loads <code>ZoneInfoProvider</code> using the data in the classpath location
   * <code>org/joda/time/tz/data</code>.
   * <li>loads <code>UTCProvider</code>.
   * </ol>
   * <p>
   * Unless you override the standard behaviour, the default if the third approach.
   *
   * @return the default name provider
   */
  private def getDefaultProvider: Provider = {
    try {
      val providerClass: String = System.getProperty("org.joda.time.DateTimeZone.Provider")
      if (providerClass != null) {
        try {
          val provider: Provider = Class.forName(providerClass).newInstance.asInstanceOf[Provider]
          return validateProvider(provider)
        }
        catch {
          case ex: Exception => {
            throw new RuntimeException(ex)
          }
        }
      }
    }
    catch {
      case ex: SecurityException => {
      }
    }
    try {
      val dataFolder: String = System.getProperty("org.joda.time.DateTimeZone.Folder")
      if (dataFolder != null) {
        try {
          val provider: Provider = new ZoneInfoProvider(new File(dataFolder))
          return validateProvider(provider)
        }
        catch {
          case ex: Exception => {
            throw new RuntimeException(ex)
          }
        }
      }
    }
    catch {
      case ex: SecurityException => {
      }
    }
    try {
      val provider: Provider = new ZoneInfoProvider("org/joda/time/tz/data")
      return validateProvider(provider)
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace
      }
    }
    return new UTCProvider
  }

  /**
   * Gets the name provider factory.
   * <p>
   * The name provider is a pluggable instance factory that supplies the
   * names of each DateTimeZone.
   *
   * @return the provider
   */
  def getNameProvider: NameProvider = {
    var nameProvider: NameProvider = cNameProvider.get
    if (nameProvider == null) {
      nameProvider = getDefaultNameProvider
      if (!cNameProvider.compareAndSet(null, nameProvider)) {
        nameProvider = cNameProvider.get
      }
    }
    return nameProvider
  }

  /**
   * Sets the name provider factory.
   * <p>
   * The name provider is a pluggable instance factory that supplies the
   * names of each DateTimeZone.
   *
   * @param nameProvider  provider to use, or null for default
   * @throws SecurityException if you do not have the permission DateTimeZone.setNameProvider
   * @throws IllegalArgumentException if the provider is invalid
   */
  @throws(classOf[SecurityException])
  def setNameProvider(nameProvider: NameProvider) {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("DateTimeZone.setNameProvider"))
    }
    if (nameProvider == null) {
      nameProvider = getDefaultNameProvider
    }
    cNameProvider.set(nameProvider)
  }

  /**
   * Gets the default name provider.
   * <p>
   * Tries the system property <code>org.joda.time.DateTimeZone.NameProvider</code>.
   * Then uses <code>DefaultNameProvider</code>.
   *
   * @return the default name provider
   */
  private def getDefaultNameProvider: NameProvider = {
    var nameProvider: NameProvider = null
    try {
      val providerClass: String = System.getProperty("org.joda.time.DateTimeZone.NameProvider")
      if (providerClass != null) {
        try {
          nameProvider = Class.forName(providerClass).newInstance.asInstanceOf[NameProvider]
        }
        catch {
          case ex: Exception => {
            throw new RuntimeException(ex)
          }
        }
      }
    }
    catch {
      case ex: SecurityException => {
      }
    }
    if (nameProvider == null) {
      nameProvider = new DefaultNameProvider
    }
    return nameProvider
  }

  /**
   * Converts an old style id to a new style id.
   *
   * @param id  the old style id
   * @return the new style id, null if not found
   */
  private def getConvertedId(id: String): String = {
    return LazyInit.CONVERSION_MAP.get(id)
  }

  /**
   * Parses an offset from the string.
   *
   * @param str  the string
   * @return the offset millis
   */
  private def parseOffset(str: String): Int = {
    return -LazyInit.OFFSET_FORMATTER.parseMillis(str).toInt
  }

  /**
   * Formats a timezone offset string.
   * <p>
   * This method is kept separate from the formatting classes to speed and
   * simplify startup and classloading.
   *
   * @param offSet  the offset in milliseconds
   * @return the time zone string
   */
  private def printOffset(offSet: Int): String = {
    var offset=offSet
    val buf: StringBuffer = new StringBuffer
    if (offset >= 0) {
      buf.append('+')
    }
    else {
      buf.append('-')
      offset = -offset
    }
    val hours: Int = offset / DateTimeConstants.MILLIS_PER_HOUR
    FormatUtils.appendPaddedInteger(buf, hours, 2)
    offset -= hours * DateTimeConstants.MILLIS_PER_HOUR.toInt
    val minutes: Int = offset / DateTimeConstants.MILLIS_PER_MINUTE
    buf.append(':')
    FormatUtils.appendPaddedInteger(buf, minutes, 2)
    offset -= minutes * DateTimeConstants.MILLIS_PER_MINUTE
    if (offset == 0) {
      return buf.toString
    }
    val seconds: Int = offset / DateTimeConstants.MILLIS_PER_SECOND
    buf.append(':')
    FormatUtils.appendPaddedInteger(buf, seconds, 2)
    offset -= seconds * DateTimeConstants.MILLIS_PER_SECOND
    if (offset == 0) {
      return buf.toString
    }
    buf.append('.')
    FormatUtils.appendPaddedInteger(buf, offset, 3)
    buf.toString
  }

  /**
   * Used to serialize DateTimeZones by id.
   */
  @SerialVersionUID(-6471952376487863581L)
  private final class Stub extends Serializable {
    /** The ID of the zone. */
    @transient
    private var iID: String = null

    /**
     * Constructor.
     * @param id  the id of the zone
     */
    private[time] def this(id: String) {
      this()
      iID = id
    }

    @throws(classOf[IOException])
    private def writeObject(out: ObjectOutputStream) {
      out.writeUTF(iID)
    }

    @throws(classOf[IOException])
    private def readObject(in: ObjectInputStream) {
      iID = in.readUTF
    }

    @throws(classOf[ObjectStreamException])
    private def readResolve: AnyRef = {
      return forID(iID)
    }
  }

  /**
   * Lazy initialization to avoid a synchronization lock.
   */
  private[time] object LazyInit {
    /** Cache of old zone IDs to new zone IDs */
    private[time] val CONVERSION_MAP: Map[String, String] = buildMap
    /** Time zone offset formatter. */
    private[time] val OFFSET_FORMATTER: DateTimeFormatter = buildFormatter

    private def buildFormatter: DateTimeFormatter = {
      val chrono: Chronology = new BaseChronology {
        def getZone: DateTimeZone = {
          return null
        }
        def withUTC: Chronology = {
          return this
        }
        def withZone(zone: DateTimeZone): Chronology = {
          return this
        }
        def toString: String = {
          return getClass.getName
        }
      }
      return new DateTimeFormatterBuilder().appendTimeZoneOffset(null, true, 2, 4).toFormatter.withChronology(chrono)
    }

    private def buildMap: Map[String, String] = {
      val map: Map[String, String] = new HashMap[String, String]
      map.put("GMT", "UTC")
      map.put("WET", "WET")
      map.put("CET", "CET")
      map.put("MET", "CET")
      map.put("ECT", "CET")
      map.put("EET", "EET")
      map.put("MIT", "Pacific/Apia")
      map.put("HST", "Pacific/Honolulu")
      map.put("AST", "America/Anchorage")
      map.put("PST", "America/Los_Angeles")
      map.put("MST", "America/Denver")
      map.put("PNT", "America/Phoenix")
      map.put("CST", "America/Chicago")
      map.put("EST", "America/New_York")
      map.put("IET", "America/Indiana/Indianapolis")
      map.put("PRT", "America/Puerto_Rico")
      map.put("CNT", "America/St_Johns")
      map.put("AGT", "America/Argentina/Buenos_Aires")
      map.put("BET", "America/Sao_Paulo")
      map.put("ART", "Africa/Cairo")
      map.put("CAT", "Africa/Harare")
      map.put("EAT", "Africa/Addis_Ababa")
      map.put("NET", "Asia/Yerevan")
      map.put("PLT", "Asia/Karachi")
      map.put("IST", "Asia/Kolkata")
      map.put("BST", "Asia/Dhaka")
      map.put("VST", "Asia/Ho_Chi_Minh")
      map.put("CTT", "Asia/Shanghai")
      map.put("JST", "Asia/Tokyo")
      map.put("ACT", "Australia/Darwin")
      map.put("AET", "Australia/Sydney")
      map.put("SST", "Pacific/Guadalcanal")
      map.put("NST", "Pacific/Auckland")
      return Collections.unmodifiableMap(map)
    }
  }

}

abstract class DateTimeZone(val iID: String) {
//  private final val iID: String = null

  if (iID == null) {
    throw new IllegalArgumentException("Id must not be null")
  }

  /**
   * Constructor.
   *
   * @param id  the id to use
   * @throws IllegalArgumentException if the id is null
   */
//  protected def this(id: String) {
//    this()
//    if (id == null) {
//      throw new IllegalArgumentException("Id must not be null")
//    }
//    iID = id
//  }

  /**
   * Gets the ID of this datetime zone.
   *
   * @return the ID of this datetime zone
   */
  final def getID: String = iID


  /**
   * Returns a non-localized name that is unique to this time zone. It can be
   * combined with id to form a unique key for fetching localized names.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the name for
   * @return name key or null if id should be used for names
   */
  def getNameKey(instant: Long): String

  /**
   * Gets the short name of this datetime zone suitable for display using
   * the default locale.
   * <p>
   * If the name is not available for the locale, then this method returns a
   * string in the format <code>[+-]hh:mm</code>.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the name for
   * @return the human-readable short name in the default locale
   */
  final def getShortName(instant: Long): String = {
    return getShortName(instant, null)
  }

  /**
   * Gets the short name of this datetime zone suitable for display using
   * the specified locale.
   * <p>
   * If the name is not available for the locale, then this method returns a
   * string in the format <code>[+-]hh:mm</code>.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the name for
   * @param locale  the locale to get the name for
   * @return the human-readable short name in the specified locale
   */
  def getShortName(instant: Long, locale: Locale): String = {
    if (locale == null) {
      locale = Locale.getDefault
    }
    val nameKey: String = getNameKey(instant)
    if (nameKey == null) {
      return iID
    }
    var name: String = null
    val np: NameProvider = DateTimeZone.getNameProvider
    if (np.isInstanceOf[DefaultNameProvider]) {
      name = (np.asInstanceOf[DefaultNameProvider]).getShortName(locale, iID, nameKey, isStandardOffset(instant))
    }
    else {
      name = np.getShortName(locale, iID, nameKey)
    }
    if (name != null) {
      return name
    }
    DateTimeZone.printOffset(getOffset(instant))
  }

  /**
   * Gets the long name of this datetime zone suitable for display using
   * the default locale.
   * <p>
   * If the name is not available for the locale, then this method returns a
   * string in the format <code>[+-]hh:mm</code>.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the name for
   * @return the human-readable long name in the default locale
   */
  final def getName(instant: Long): String = {
    return getName(instant, null)
  }

  /**
   * Gets the long name of this datetime zone suitable for display using
   * the specified locale.
   * <p>
   * If the name is not available for the locale, then this method returns a
   * string in the format <code>[+-]hh:mm</code>.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the name for
   * @param locale  the locale to get the name for
   * @return the human-readable long name in the specified locale
   */
  def getName(instant: Long, locale: Locale): String = {
    if (locale == null) {
      locale = Locale.getDefault
    }
    val nameKey: String = getNameKey(instant)
    if (nameKey == null) {
      return iID
    }
    var name: String = null
    val np: NameProvider = DateTimeZone.getNameProvider
    if (np.isInstanceOf[DefaultNameProvider]) {
      name = (np.asInstanceOf[DefaultNameProvider]).getName(locale, iID, nameKey, isStandardOffset(instant))
    }
    else {
      name = np.getName(locale, iID, nameKey)
    }
    if (name != null) {
      return name
    }
    return DateTimeZone.printOffset(getOffset(instant))
  }

  /**
   * Gets the millisecond offset to add to UTC to get local time.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the offset for
   * @return the millisecond offset to add to UTC to get local time
   */
  def getOffset(instant: Long): Int

  /**
   * Gets the millisecond offset to add to UTC to get local time.
   *
   * @param instant  instant to get the offset for, null means now
   * @return the millisecond offset to add to UTC to get local time
   */
  final def getOffset(instant: ReadableInstant): Int = {
    if (instant == null) {
      return getOffset(DateTimeUtils.currentTimeMillis)
    }
    return getOffset(instant.getMillis)
  }

  /**
   * Gets the standard millisecond offset to add to UTC to get local time,
   * when standard time is in effect.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the offset for
   * @return the millisecond offset to add to UTC to get local time
   */
  def getStandardOffset(instant: Long): Int

  /**
   * Checks whether, at a particular instant, the offset is standard or not.
   * <p>
   * This method can be used to determine whether Summer Time (DST) applies.
   * As a general rule, if the offset at the specified instant is standard,
   * then either Winter time applies, or there is no Summer Time. If the
   * instant is not standard, then Summer Time applies.
   * <p>
   * The implementation of the method is simply whether {@link #getOffset(long)}
   * equals {@link #getStandardOffset(long)} at the specified instant.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z to get the offset for
   * @return true if the offset at the given instant is the standard offset
   * @since 1.5
   */
  def isStandardOffset(instant: Long): Boolean = {
    return getOffset(instant) == getStandardOffset(instant)
  }

  /**
   * Gets the millisecond offset to subtract from local time to get UTC time.
   * This offset can be used to undo adding the offset obtained by getOffset.
   *
   * <pre>
   * millisLocal == millisUTC   + getOffset(millisUTC)
   * millisUTC   == millisLocal - getOffsetFromLocal(millisLocal)
   * </pre>
   *
   * NOTE: After calculating millisLocal, some error may be introduced. At
   * offset transitions (due to DST or other historical changes), ranges of
   * local times may map to different UTC times.
   * <p>
   * For overlaps (where the local time is ambiguous), this method returns the
   * offset applicable before the gap. The effect of this is that any instant
   * calculated using the offset from an overlap will be in "summer" time.
   * <p>
   * For gaps, this method returns the offset applicable before the gap, ie "winter" offset.
   * However, the effect of this is that any instant calculated using the offset
   * from a gap will be after the gap, in "summer" time.
   * <p>
   * For example, consider a zone with a gap from 01:00 to 01:59:<br />
   * Input: 00:00 (before gap) Output: Offset applicable before gap  DateTime: 00:00<br />
   * Input: 00:30 (before gap) Output: Offset applicable before gap  DateTime: 00:30<br />
   * Input: 01:00 (in gap)     Output: Offset applicable before gap  DateTime: 02:00<br />
   * Input: 01:30 (in gap)     Output: Offset applicable before gap  DateTime: 02:30<br />
   * Input: 02:00 (after gap)  Output: Offset applicable after gap   DateTime: 02:00<br />
   * Input: 02:30 (after gap)  Output: Offset applicable after gap   DateTime: 02:30<br />
   * <p>
   * NOTE: Prior to v2.0, the DST overlap behaviour was not defined and varied by hemisphere.
   * Prior to v1.5, the DST gap behaviour was also not defined.
   * In v2.4, the documentation was clarified again.
   *
   * @param instantLocal  the millisecond instant, relative to this time zone, to get the offset for
   * @return the millisecond offset to subtract from local time to get UTC time
   */
  def getOffsetFromLocal(instantLocal: Long): Int = {
    val offsetLocal: Int = getOffset(instantLocal)
    val instantAdjusted: Long = instantLocal - offsetLocal
    val offsetAdjusted: Int = getOffset(instantAdjusted)
    if (offsetLocal != offsetAdjusted) {
      if ((offsetLocal - offsetAdjusted) < 0) {
        var nextLocal: Long = nextTransition(instantAdjusted)
        if (nextLocal == (instantLocal - offsetLocal)) {
          nextLocal = Long.MaxValue
        }
        var nextAdjusted: Long = nextTransition(instantLocal - offsetAdjusted)
        if (nextAdjusted == (instantLocal - offsetAdjusted)) {
          nextAdjusted = Long.MaxValue
        }
        if (nextLocal != nextAdjusted) {
          return offsetLocal
        }
      }
    }
    else if (offsetLocal >= 0) {
      val prev: Long = previousTransition(instantAdjusted)
      if (prev < instantAdjusted) {
        val offsetPrev: Int = getOffset(prev)
        val diff: Int = offsetPrev - offsetLocal
        if (instantAdjusted - prev <= diff) {
          return offsetPrev
        }
      }
    }
    offsetAdjusted
  }

  /**
   * Converts a standard UTC instant to a local instant with the same
   * local time. This conversion is used before performing a calculation
   * so that the calculation can be done using a simple local zone.
   *
   * @param instantUTC  the UTC instant to convert to local
   * @return the local instant with the same local time
   * @throws ArithmeticException if the result overflows a long
   * @since 1.5
   */
  def convertUTCToLocal(instantUTC: Long): Long = {
    val offset: Int = getOffset(instantUTC)
    val instantLocal: Long = instantUTC + offset
    if ((instantUTC ^ instantLocal) < 0 && (instantUTC ^ offset) >= 0) {
      throw new ArithmeticException("Adding time zone offset caused overflow")
    }
    return instantLocal
  }

  /**
   * Converts a local instant to a standard UTC instant with the same
   * local time attempting to use the same offset as the original.
   * <p>
   * This conversion is used after performing a calculation
   * where the calculation was done using a simple local zone.
   * Whenever possible, the same offset as the original offset will be used.
   * This is most significant during a daylight savings overlap.
   *
   * @param instantLocal  the local instant to convert to UTC
   * @param strict  whether the conversion should reject non-existent local times
   * @param originalInstantUTC  the original instant that the calculation is based on
   * @return the UTC instant with the same local time,
   * @throws ArithmeticException if the result overflows a long
   * @throws IllegalArgumentException if the zone has no equivalent local time
   * @since 2.0
   */
  def convertLocalToUTC(instantLocal: Long, strict: Boolean, originalInstantUTC: Long): Long = {
    val offsetOriginal: Int = getOffset(originalInstantUTC)
    val instantUTC: Long = instantLocal - offsetOriginal
    val offsetLocalFromOriginal: Int = getOffset(instantUTC)
    if (offsetLocalFromOriginal == offsetOriginal) {
      return instantUTC
    }
    convertLocalToUTC(instantLocal, strict)
  }

  /**
   * Converts a local instant to a standard UTC instant with the same
   * local time. This conversion is used after performing a calculation
   * where the calculation was done using a simple local zone.
   *
   * @param instantLocal  the local instant to convert to UTC
   * @param strict  whether the conversion should reject non-existent local times
   * @return the UTC instant with the same local time,
   * @throws ArithmeticException if the result overflows a long
   * @throws IllegalInstantException if the zone has no equivalent local time
   * @since 1.5
   */
  def convertLocalToUTC(instantLocal: Long, strict: Boolean): Long = {
    val offsetLocal: Int = getOffset(instantLocal)
    var offset: Int = getOffset(instantLocal - offsetLocal)
    if (offsetLocal != offset) {
      if (strict || offsetLocal < 0) {
        var nextLocal: Long = nextTransition(instantLocal - offsetLocal)
        if (nextLocal == (instantLocal - offsetLocal)) {
          nextLocal = Long.MaxValue
        }
        var nextAdjusted: Long = nextTransition(instantLocal - offset)
        if (nextAdjusted == (instantLocal - offset)) {
          nextAdjusted = Long.MaxValue
        }
        if (nextLocal != nextAdjusted) {
          if (strict) {
            throw new IllegalInstantException(instantLocal, getID)
          }
          else {
            offset = offsetLocal
          }
        }
      }
    }
    val instantUTC: Long = instantLocal - offset
    if ((instantLocal ^ instantUTC) < 0 && (instantLocal ^ offset) < 0) {
      throw new ArithmeticException("Subtracting time zone offset caused overflow")
    }
    return instantUTC
  }

  /**
   * Gets the millisecond instant in another zone keeping the same local time.
   * <p>
   * The conversion is performed by converting the specified UTC millis to local
   * millis in this zone, then converting back to UTC millis in the new zone.
   *
   * @param newZone  the new zone, null means default
   * @param oldInstant  the UTC millisecond instant to convert
   * @return the UTC millisecond instant with the same local time in the new zone
   */
  def getMillisKeepLocal(newZone: DateTimeZone, oldInstant: Long): Long = {
    if (newZone == null) {
      newZone = DateTimeZone.getDefault
    }
    if (newZone eq this) {
      return oldInstant
    }
    val instantLocal: Long = convertUTCToLocal(oldInstant)
    return newZone.convertLocalToUTC(instantLocal, false, oldInstant)
  }

  /**
   * Checks if the given {@link LocalDateTime} is within a gap.
   * <p>
   * When switching from standard time to Daylight Savings Time there is
   * typically a gap where a clock hour is missing. This method identifies
   * whether the local datetime refers to such a gap.
   *
   * @param localDateTime  the time to check, not null
   * @return true if the given datetime refers to a gap
   * @since 1.6
   */
  def isLocalDateTimeGap(localDateTime: LocalDateTime): Boolean = {
    if (isFixed) {
      return false
    }
    try {
      localDateTime.toDateTime(this)
      return false
    }
    catch {
      case ex: IllegalInstantException => {
        return true
      }
    }
  }

  /**
   * Adjusts the offset to be the earlier or later one during an overlap.
   *
   * @param instant  the instant to adjust
   * @param earlierOrLater  false for earlier, true for later
   * @return the adjusted instant millis
   */
  def adjustOffset(instant: Long, earlierOrLater: Boolean): Long = {
    val instantBefore: Long = instant - 3 * DateTimeConstants.MILLIS_PER_HOUR
    val instantAfter: Long = instant + 3 * DateTimeConstants.MILLIS_PER_HOUR
    val offsetBefore: Long = getOffset(instantBefore)
    val offsetAfter: Long = getOffset(instantAfter)
    if (offsetBefore <= offsetAfter) {
      return instant
    }
    val diff: Long = offsetBefore - offsetAfter
    val transition: Long = nextTransition(instantBefore)
    val overlapStart: Long = transition - diff
    val overlapEnd: Long = transition + diff
    if (instant < overlapStart || instant >= overlapEnd) {
      return instant
    }
    val afterStart: Long = instant - overlapStart
    if (afterStart >= diff) {
      return if (earlierOrLater) instant else instant - diff
    }
    else {
      return if (earlierOrLater) instant + diff else instant
    }
  }

  /**
   * Returns true if this time zone has no transitions.
   *
   * @return true if no transitions
   */
  def isFixed: Boolean

  /**
   * Advances the given instant to where the time zone offset or name changes.
   * If the instant returned is exactly the same as passed in, then
   * no changes occur after the given instant.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z
   * @return milliseconds from 1970-01-01T00:00:00Z
   */
  def nextTransition(instant: Long): Long

  /**
   * Retreats the given instant to where the time zone offset or name changes.
   * If the instant returned is exactly the same as passed in, then
   * no changes occur before the given instant.
   *
   * @param instant  milliseconds from 1970-01-01T00:00:00Z
   * @return milliseconds from 1970-01-01T00:00:00Z
   */
  def previousTransition(instant: Long): Long

  /**
   * Get the datetime zone as a {@link java.util.TimeZone}.
   *
   * @return the closest matching TimeZone object
   */
  def toTimeZone: TimeZone = {
    return java.util.TimeZone.getTimeZone(iID)
  }

  /**
   * Compare this datetime zone with another.
   *
   * @param object the object to compare with
   * @return true if equal, based on the ID and all internal rules
   */
  override def equals(`object`: AnyRef): Boolean

  /**
   * Gets a hash code compatable with equals.
   *
   * @return suitable hashcode
   */
  override def hashCode: Int = {
    return 57 + getID.hashCode
  }

  /**
   * Gets the datetime zone as a string, which is simply its ID.
   * @return the id of the zone
   */
  override def toString: String = {
    return getID
  }

  /**
   * By default, when DateTimeZones are serialized, only a "stub" object
   * referring to the id is written out. When the stub is read in, it
   * replaces itself with a DateTimeZone object.
   * @return a stub object to go in the stream
   */
  @throws(classOf[ObjectStreamException])
  protected def writeReplace: AnyRef = {
    return new DateTimeZone.Stub(iID)
  }
}
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
package org.joda.time

import java.io.Serializable
import java.util.Comparator
import org.joda.time.convert.ConverterManager
import org.joda.time.convert.InstantConverter

/**
 * DateTimeComparator provides comparators to compare one date with another.
 * <p>
 * Dates may be specified using any object recognised by the
 * {@link org.joda.time.convert.ConverterManager ConverterManager} class.
 * <p>
 * The default objects recognised by the comparator are:
 * <ul>
 * <li>ReadableInstant
 * <li>String
 * <li>Calendar
 * <li>Date
 * <li>Long (milliseconds)
 * <li>null (now)
 * </ul>
 *
 * <p>
 * DateTimeComparator is thread-safe and immutable.
 *
 * @author Guy Allard
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
@SerialVersionUID(-6097339773320178364L)
object DateTimeComparator {
  /** Singleton instance */
  private val ALL_INSTANCE: DateTimeComparator = new DateTimeComparator(null, null)
  /** Singleton instance */
  private val DATE_INSTANCE: DateTimeComparator = new DateTimeComparator(DateTimeFieldType.dayOfYear, null)
  /** Singleton instance */
  private val TIME_INSTANCE: DateTimeComparator = new DateTimeComparator(null, DateTimeFieldType.dayOfYear)

  /**
   * Returns a DateTimeComparator the compares the entire date time value.
   *
   * @return a comparator over all fields
   */
  def getInstance: DateTimeComparator = {
    return ALL_INSTANCE
  }

  /**
   * Returns a DateTimeComparator with a lower limit only. Fields of a
   * magnitude less than the lower limit are excluded from comparisons.
   * <p>
   * The time-zone is considered when using this comparator.
   * The input millis are truncated using the time-zone of that input value.
   * Thus, two inputs with different time-zones will typically not be equal
   *
   * @param lowerLimit  inclusive lower limit for fields to be compared, null means no limit
   * @return a comparator over all fields above the lower limit
   */
  def getInstance(lowerLimit: DateTimeFieldType): DateTimeComparator = {
    return getInstance(lowerLimit, null)
  }

  /**
   * Returns a DateTimeComparator with a lower and upper limit. Fields of a
   * magnitude less than the lower limit are excluded from comparisons.
   * Fields of a magnitude greater than or equal to the upper limit are also
   * excluded from comparisons. Either limit may be specified as null, which
   * indicates an unbounded limit.
   * <p>
   * The time-zone is considered when using this comparator unless both limits are null.
   * The input millis are rounded/truncated using the time-zone of that input value.
   * Thus, two inputs with different time-zones will typically not be equal
   *
   * @param lowerLimit  inclusive lower limit for fields to be compared, null means no limit
   * @param upperLimit  exclusive upper limit for fields to be compared, null means no limit
   * @return a comparator over all fields between the limits
   */
  def getInstance(lowerLimit: DateTimeFieldType, upperLimit: DateTimeFieldType): DateTimeComparator = {
    if (lowerLimit == null && upperLimit == null) {
      return ALL_INSTANCE
    }
    if (lowerLimit eq DateTimeFieldType.dayOfYear && upperLimit == null) {
      return DATE_INSTANCE
    }
    if (lowerLimit == null && upperLimit eq DateTimeFieldType.dayOfYear) {
      return TIME_INSTANCE
    }
    return new DateTimeComparator(lowerLimit, upperLimit)
  }

  /**
   * Returns a comparator that only considers date fields.
   * Time of day is ignored.
   * <p>
   * The time-zone is considered when using this comparator.
   * The input millis are rounded down to the start of the day
   * in the time-zone of that input value. Thus, two inputs with
   * different time-zones will typically not be equal
   *
   * @return a comparator over all date fields
   */
  def getDateOnlyInstance: DateTimeComparator = {
    return DATE_INSTANCE
  }

  /**
   * Returns a comparator that only considers time fields.
   * Date is ignored.
   * <p>
   * The time-zone is considered when using this comparator.
   * The input millis are truncated to be within the day
   * in the time-zone of that input value. Thus, two inputs with
   * different time-zones will typically not be equal
   *
   * @return a comparator over all time fields
   */
  def getTimeOnlyInstance: DateTimeComparator = {
    return TIME_INSTANCE
  }
}

@SerialVersionUID(-6097339773320178364L)
class DateTimeComparator extends Comparator[AnyRef] with Serializable {
  /** The lower limit of fields to compare, null if no limit */
  private final val iLowerLimit: DateTimeFieldType = null
  /** The upper limit of fields to compare, null if no limit */
  private final val iUpperLimit: DateTimeFieldType = null

  /**
   * Restricted constructor.
   *
   * @param lowerLimit  the lower field limit, null means no limit
   * @param upperLimit  the upper field limit, null means no limit
   */
  protected def this(lowerLimit: DateTimeFieldType, upperLimit: DateTimeFieldType) {
    this()
    `super`
    iLowerLimit = lowerLimit
    iUpperLimit = upperLimit
  }

  /**
   * Gets the field type that represents the lower limit of comparison.
   *
   * @return the field type, null if no upper limit
   */
  def getLowerLimit: DateTimeFieldType = {
    return iLowerLimit
  }

  /**
   * Gets the field type that represents the upper limit of comparison.
   *
   * @return the field type, null if no upper limit
   */
  def getUpperLimit: DateTimeFieldType = {
    return iUpperLimit
  }

  /**
   * Compare two objects against only the range of date time fields as
   * specified in the constructor.
   *
   * @param lhsObj  the first object,
   *                logically on the left of a &lt; comparison, null means now
   * @param rhsObj  the second object,
   *                logically on the right of a &lt; comparison, null means now
   * @return zero if order does not matter,
   *         negative value if lhsObj &lt; rhsObj, positive value otherwise.
   * @throws IllegalArgumentException if either argument is not supported
   */
  def compare(lhsObj: AnyRef, rhsObj: AnyRef): Int = {
    var conv: InstantConverter = ConverterManager.getInstance.getInstantConverter(lhsObj)
    val lhsChrono: Chronology = conv.getChronology(lhsObj, null.asInstanceOf[Chronology])
    var lhsMillis: Long = conv.getInstantMillis(lhsObj, lhsChrono)
    conv = ConverterManager.getInstance.getInstantConverter(rhsObj)
    val rhsChrono: Chronology = conv.getChronology(rhsObj, null.asInstanceOf[Chronology])
    var rhsMillis: Long = conv.getInstantMillis(rhsObj, rhsChrono)
    if (iLowerLimit != null) {
      lhsMillis = iLowerLimit.getField(lhsChrono).roundFloor(lhsMillis)
      rhsMillis = iLowerLimit.getField(rhsChrono).roundFloor(rhsMillis)
    }
    if (iUpperLimit != null) {
      lhsMillis = iUpperLimit.getField(lhsChrono).remainder(lhsMillis)
      rhsMillis = iUpperLimit.getField(rhsChrono).remainder(rhsMillis)
    }
    if (lhsMillis < rhsMillis) {
      return -1
    }
    else if (lhsMillis > rhsMillis) {
      return 1
    }
    else {
      return 0
    }
  }

  /**
   * Support serialization singletons.
   *
   * @return the resolved singleton instance
   */
  private def readResolve: AnyRef = {
    return DateTimeComparator.getInstance(iLowerLimit, iUpperLimit)
  }

  /**
   * Compares this comparator to another.
   *
   * @param object  the object to compare to
   * @return true if equal
   */
  override def equals(`object`: AnyRef): Boolean = {
    if (`object`.isInstanceOf[DateTimeComparator]) {
      val other: DateTimeComparator = `object`.asInstanceOf[DateTimeComparator]
      return (iLowerLimit eq other.getLowerLimit || (iLowerLimit != null && (iLowerLimit == other.getLowerLimit))) && (iUpperLimit eq other.getUpperLimit || (iUpperLimit != null && (iUpperLimit == other.getUpperLimit)))
    }
    return false
  }

  /**
   * Gets a suitable hashcode.
   *
   * @return the hashcode
   */
  override def hashCode: Int = {
    return (if (iLowerLimit == null) 0 else iLowerLimit.hashCode) + (123 * (if (iUpperLimit == null) 0 else iUpperLimit.hashCode))
  }

  /**
   * Gets a debugging string.
   *
   * @return a debugging string
   */
  override def toString: String = {
    if (iLowerLimit eq iUpperLimit) {
      return "DateTimeComparator[" + (if (iLowerLimit == null) "" else iLowerLimit.getName) + "]"
    }
    else {
      return "DateTimeComparator[" + (if (iLowerLimit == null) "" else iLowerLimit.getName) + "-" + (if (iUpperLimit == null) "" else iUpperLimit.getName) + "]"
    }
  }
}
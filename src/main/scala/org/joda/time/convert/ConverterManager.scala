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
package org.joda.time.convert

import org.joda.time.JodaTimePermission

/**
 * ConverterManager controls the date and time converters.
 * <p>
 * This class enables additional conversion classes to be added via
 * {@link #addInstantConverter(InstantConverter)}, which may replace an
 * existing converter. Similar methods exist for duration, time period and
 * interval converters.
 * <p>
 * This class is threadsafe, so adding/removing converters can be done at any
 * time. Updating the set of converters is relatively expensive, and so should
 * not be performed often.
 * <p>
 * The default instant converters are:
 * <ul>
 * <li>ReadableInstant
 * <li>String
 * <li>Calendar
 * <li>Date (includes sql package subclasses)
 * <li>Long (milliseconds)
 * <li>null (now)
 * </ul>
 *
 * The default partial converters are:
 * <ul>
 * <li>ReadablePartial
 * <li>ReadableInstant
 * <li>String
 * <li>Calendar
 * <li>Date (includes sql package subclasses)
 * <li>Long (milliseconds)
 * <li>null (now)
 * </ul>
 *
 * The default duration converters are:
 * <ul>
 * <li>ReadableDuration
 * <li>ReadableInterval
 * <li>String
 * <li>Long (milliseconds)
 * <li>null (zero ms)
 * </ul>
 *
 * The default time period converters are:
 * <ul>
 * <li>ReadablePeriod
 * <li>ReadableInterval
 * <li>String
 * <li>null (zero)
 * </ul>
 *
 * The default interval converters are:
 * <ul>
 * <li>ReadableInterval
 * <li>String
 * <li>null (zero-length from now to now)
 * </ul>
 *
 * @author Stephen Colebourne
 * @author Brian S O'Neill
 * @since 1.0
 */
object ConverterManager {
  /**
   * Singleton instance, lazily loaded to avoid class loading.
   */
  private var INSTANCE: ConverterManager = null

  def getInstance: ConverterManager = {
    if (INSTANCE == null) {
      INSTANCE = new ConverterManager
    }
    return INSTANCE
  }
}

final class ConverterManager {
  private var iInstantConverters: ConverterSet = null
  private var iPartialConverters: ConverterSet = null
  private var iDurationConverters: ConverterSet = null
  private var iPeriodConverters: ConverterSet = null
  private var iIntervalConverters: ConverterSet = null

  /**
   * Restricted constructor.
   */
  protected def this() {
    this()
    `super`
    iInstantConverters = new ConverterSet(Array[Converter](ReadableInstantConverter.INSTANCE, StringConverter.INSTANCE, CalendarConverter.INSTANCE, DateConverter.INSTANCE, LongConverter.INSTANCE, NullConverter.INSTANCE))
    iPartialConverters = new ConverterSet(Array[Converter](ReadablePartialConverter.INSTANCE, ReadableInstantConverter.INSTANCE, StringConverter.INSTANCE, CalendarConverter.INSTANCE, DateConverter.INSTANCE, LongConverter.INSTANCE, NullConverter.INSTANCE))
    iDurationConverters = new ConverterSet(Array[Converter](ReadableDurationConverter.INSTANCE, ReadableIntervalConverter.INSTANCE, StringConverter.INSTANCE, LongConverter.INSTANCE, NullConverter.INSTANCE))
    iPeriodConverters = new ConverterSet(Array[Converter](ReadableDurationConverter.INSTANCE, ReadablePeriodConverter.INSTANCE, ReadableIntervalConverter.INSTANCE, StringConverter.INSTANCE, NullConverter.INSTANCE))
    iIntervalConverters = new ConverterSet(Array[Converter](ReadableIntervalConverter.INSTANCE, StringConverter.INSTANCE, NullConverter.INSTANCE))
  }

  /**
   * Gets the best converter for the object specified.
   *
   * @param object  the object to convert
   * @return the converter to use
   * @throws IllegalArgumentException if no suitable converter
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  def getInstantConverter(`object`: AnyRef): InstantConverter = {
    val converter: InstantConverter = iInstantConverters.select(if (`object` == null) null else `object`.getClass).asInstanceOf[InstantConverter]
    if (converter != null) {
      return converter
    }
    throw new IllegalArgumentException("No instant converter found for type: " + (if (`object` == null) "null" else `object`.getClass.getName))
  }

  /**
   * Gets a copy of the set of converters.
   *
   * @return the converters, a copy of the real data, never null
   */
  def getInstantConverters: Array[InstantConverter] = {
    val set: ConverterSet = iInstantConverters
    val converters: Array[InstantConverter] = new Array[InstantConverter](set.size)
    set.copyInto(converters)
    return converters
  }

  /**
   * Adds a converter to the set of converters. If a matching converter is
   * already in the set, the given converter replaces it. If the converter is
   * exactly the same as one already in the set, no changes are made.
   * <p>
   * The order in which converters are added is not relevent. The best
   * converter is selected by examining the object hierarchy.
   *
   * @param converter  the converter to add, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def addInstantConverter(converter: InstantConverter): InstantConverter = {
    checkAlterInstantConverters
    if (converter == null) {
      return null
    }
    val removed: Array[InstantConverter] = new Array[InstantConverter](1)
    iInstantConverters = iInstantConverters.add(converter, removed)
    return removed(0)
  }

  /**
   * Removes a converter from the set of converters. If the converter was
   * not in the set, no changes are made.
   *
   * @param converter  the converter to remove, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def removeInstantConverter(converter: InstantConverter): InstantConverter = {
    checkAlterInstantConverters
    if (converter == null) {
      return null
    }
    val removed: Array[InstantConverter] = new Array[InstantConverter](1)
    iInstantConverters = iInstantConverters.remove(converter, removed)
    return removed(0)
  }

  /**
   * Checks whether the user has permission 'ConverterManager.alterInstantConverters'.
   *
   * @throws SecurityException if the user does not have the permission
   */
  @throws(classOf[SecurityException])
  private def checkAlterInstantConverters {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("ConverterManager.alterInstantConverters"))
    }
  }

  /**
   * Gets the best converter for the object specified.
   *
   * @param object  the object to convert
   * @return the converter to use
   * @throws IllegalArgumentException if no suitable converter
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  def getPartialConverter(`object`: AnyRef): PartialConverter = {
    val converter: PartialConverter = iPartialConverters.select(if (`object` == null) null else `object`.getClass).asInstanceOf[PartialConverter]
    if (converter != null) {
      return converter
    }
    throw new IllegalArgumentException("No partial converter found for type: " + (if (`object` == null) "null" else `object`.getClass.getName))
  }

  /**
   * Gets a copy of the set of converters.
   *
   * @return the converters, a copy of the real data, never null
   */
  def getPartialConverters: Array[PartialConverter] = {
    val set: ConverterSet = iPartialConverters
    val converters: Array[PartialConverter] = new Array[PartialConverter](set.size)
    set.copyInto(converters)
    return converters
  }

  /**
   * Adds a converter to the set of converters. If a matching converter is
   * already in the set, the given converter replaces it. If the converter is
   * exactly the same as one already in the set, no changes are made.
   * <p>
   * The order in which converters are added is not relevent. The best
   * converter is selected by examining the object hierarchy.
   *
   * @param converter  the converter to add, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def addPartialConverter(converter: PartialConverter): PartialConverter = {
    checkAlterPartialConverters
    if (converter == null) {
      return null
    }
    val removed: Array[PartialConverter] = new Array[PartialConverter](1)
    iPartialConverters = iPartialConverters.add(converter, removed)
    return removed(0)
  }

  /**
   * Removes a converter from the set of converters. If the converter was
   * not in the set, no changes are made.
   *
   * @param converter  the converter to remove, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def removePartialConverter(converter: PartialConverter): PartialConverter = {
    checkAlterPartialConverters
    if (converter == null) {
      return null
    }
    val removed: Array[PartialConverter] = new Array[PartialConverter](1)
    iPartialConverters = iPartialConverters.remove(converter, removed)
    return removed(0)
  }

  /**
   * Checks whether the user has permission 'ConverterManager.alterPartialConverters'.
   *
   * @throws SecurityException if the user does not have the permission
   */
  @throws(classOf[SecurityException])
  private def checkAlterPartialConverters {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("ConverterManager.alterPartialConverters"))
    }
  }

  /**
   * Gets the best converter for the object specified.
   *
   * @param object  the object to convert
   * @return the converter to use
   * @throws IllegalArgumentException if no suitable converter
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  def getDurationConverter(`object`: AnyRef): DurationConverter = {
    val converter: DurationConverter = iDurationConverters.select(if (`object` == null) null else `object`.getClass).asInstanceOf[DurationConverter]
    if (converter != null) {
      return converter
    }
    throw new IllegalArgumentException("No duration converter found for type: " + (if (`object` == null) "null" else `object`.getClass.getName))
  }

  /**
   * Gets a copy of the list of converters.
   *
   * @return the converters, a copy of the real data, never null
   */
  def getDurationConverters: Array[DurationConverter] = {
    val set: ConverterSet = iDurationConverters
    val converters: Array[DurationConverter] = new Array[DurationConverter](set.size)
    set.copyInto(converters)
    return converters
  }

  /**
   * Adds a converter to the set of converters. If a matching converter is
   * already in the set, the given converter replaces it. If the converter is
   * exactly the same as one already in the set, no changes are made.
   * <p>
   * The order in which converters are added is not relevent. The best
   * converter is selected by examining the object hierarchy.
   *
   * @param converter  the converter to add, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def addDurationConverter(converter: DurationConverter): DurationConverter = {
    checkAlterDurationConverters
    if (converter == null) {
      return null
    }
    val removed: Array[DurationConverter] = new Array[DurationConverter](1)
    iDurationConverters = iDurationConverters.add(converter, removed)
    return removed(0)
  }

  /**
   * Removes a converter from the set of converters. If the converter was
   * not in the set, no changes are made.
   *
   * @param converter  the converter to remove, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def removeDurationConverter(converter: DurationConverter): DurationConverter = {
    checkAlterDurationConverters
    if (converter == null) {
      return null
    }
    val removed: Array[DurationConverter] = new Array[DurationConverter](1)
    iDurationConverters = iDurationConverters.remove(converter, removed)
    return removed(0)
  }

  /**
   * Checks whether the user has permission 'ConverterManager.alterDurationConverters'.
   *
   * @throws SecurityException if the user does not have the permission
   */
  @throws(classOf[SecurityException])
  private def checkAlterDurationConverters {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("ConverterManager.alterDurationConverters"))
    }
  }

  /**
   * Gets the best converter for the object specified.
   *
   * @param object  the object to convert
   * @return the converter to use
   * @throws IllegalArgumentException if no suitable converter
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  def getPeriodConverter(`object`: AnyRef): PeriodConverter = {
    val converter: PeriodConverter = iPeriodConverters.select(if (`object` == null) null else `object`.getClass).asInstanceOf[PeriodConverter]
    if (converter != null) {
      return converter
    }
    throw new IllegalArgumentException("No period converter found for type: " + (if (`object` == null) "null" else `object`.getClass.getName))
  }

  /**
   * Gets a copy of the list of converters.
   *
   * @return the converters, a copy of the real data, never null
   */
  def getPeriodConverters: Array[PeriodConverter] = {
    val set: ConverterSet = iPeriodConverters
    val converters: Array[PeriodConverter] = new Array[PeriodConverter](set.size)
    set.copyInto(converters)
    return converters
  }

  /**
   * Adds a converter to the set of converters. If a matching converter is
   * already in the set, the given converter replaces it. If the converter is
   * exactly the same as one already in the set, no changes are made.
   * <p>
   * The order in which converters are added is not relevent. The best
   * converter is selected by examining the object hierarchy.
   *
   * @param converter  the converter to add, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def addPeriodConverter(converter: PeriodConverter): PeriodConverter = {
    checkAlterPeriodConverters
    if (converter == null) {
      return null
    }
    val removed: Array[PeriodConverter] = new Array[PeriodConverter](1)
    iPeriodConverters = iPeriodConverters.add(converter, removed)
    return removed(0)
  }

  /**
   * Removes a converter from the set of converters. If the converter was
   * not in the set, no changes are made.
   *
   * @param converter  the converter to remove, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def removePeriodConverter(converter: PeriodConverter): PeriodConverter = {
    checkAlterPeriodConverters
    if (converter == null) {
      return null
    }
    val removed: Array[PeriodConverter] = new Array[PeriodConverter](1)
    iPeriodConverters = iPeriodConverters.remove(converter, removed)
    return removed(0)
  }

  /**
   * Checks whether the user has permission 'ConverterManager.alterPeriodConverters'.
   *
   * @throws SecurityException if the user does not have the permission
   */
  @throws(classOf[SecurityException])
  private def checkAlterPeriodConverters {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("ConverterManager.alterPeriodConverters"))
    }
  }

  /**
   * Gets the best converter for the object specified.
   *
   * @param object  the object to convert
   * @return the converter to use
   * @throws IllegalArgumentException if no suitable converter
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  def getIntervalConverter(`object`: AnyRef): IntervalConverter = {
    val converter: IntervalConverter = iIntervalConverters.select(if (`object` == null) null else `object`.getClass).asInstanceOf[IntervalConverter]
    if (converter != null) {
      return converter
    }
    throw new IllegalArgumentException("No interval converter found for type: " + (if (`object` == null) "null" else `object`.getClass.getName))
  }

  /**
   * Gets a copy of the list of converters.
   *
   * @return the converters, a copy of the real data, never null
   */
  def getIntervalConverters: Array[IntervalConverter] = {
    val set: ConverterSet = iIntervalConverters
    val converters: Array[IntervalConverter] = new Array[IntervalConverter](set.size)
    set.copyInto(converters)
    return converters
  }

  /**
   * Adds a converter to the set of converters. If a matching converter is
   * already in the set, the given converter replaces it. If the converter is
   * exactly the same as one already in the set, no changes are made.
   * <p>
   * The order in which converters are added is not relevent. The best
   * converter is selected by examining the object hierarchy.
   *
   * @param converter  the converter to add, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def addIntervalConverter(converter: IntervalConverter): IntervalConverter = {
    checkAlterIntervalConverters
    if (converter == null) {
      return null
    }
    val removed: Array[IntervalConverter] = new Array[IntervalConverter](1)
    iIntervalConverters = iIntervalConverters.add(converter, removed)
    return removed(0)
  }

  /**
   * Removes a converter from the set of converters. If the converter was
   * not in the set, no changes are made.
   *
   * @param converter  the converter to remove, null ignored
   * @return replaced converter, or null
   */
  @throws(classOf[SecurityException])
  def removeIntervalConverter(converter: IntervalConverter): IntervalConverter = {
    checkAlterIntervalConverters
    if (converter == null) {
      return null
    }
    val removed: Array[IntervalConverter] = new Array[IntervalConverter](1)
    iIntervalConverters = iIntervalConverters.remove(converter, removed)
    return removed(0)
  }

  /**
   * Checks whether the user has permission 'ConverterManager.alterIntervalConverters'.
   *
   * @throws SecurityException if the user does not have the permission
   */
  @throws(classOf[SecurityException])
  private def checkAlterIntervalConverters {
    val sm: SecurityManager = System.getSecurityManager
    if (sm != null) {
      sm.checkPermission(new JodaTimePermission("ConverterManager.alterIntervalConverters"))
    }
  }

  /**
   * Gets a debug representation of the object.
   */
  override def toString: String = {
    return "ConverterManager[" + iInstantConverters.size + " instant," + iPartialConverters.size + " partial," + iDurationConverters.size + " duration," + iPeriodConverters.size + " period," + iIntervalConverters.size + " interval]"
  }
}
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

/**
 * A set of converters, which allows exact converters to be quickly
 * selected. This class is threadsafe because it is (essentially) immutable.
 *
 * @author Brian S O'Neill
 * @since 1.0
 */
object ConverterSet {
  /**
   * Returns the closest matching converter for the given type, but not very
   * efficiently.
   */
  private def selectSlow(set: ConverterSet, `type`: Class[_]): Converter = {
    var converters: Array[Converter] = set.iConverters
    var length: Int = converters.length
    var converter: Converter = null
    {
      var i: Int = length
      while (({
        i -= 1; i
      }) >= 0) {
        converter = converters(i)
        val supportedType: Class[_] = converter.getSupportedType
        if (supportedType eq `type`) {
          return converter
        }
        if (supportedType == null || (`type` != null && !supportedType.isAssignableFrom(`type`))) {
          set = set.remove(i, null)
          converters = set.iConverters
          length = converters.length
        }
      }
    }
    if (`type` == null || length == 0) {
      return null
    }
    if (length == 1) {
      return converters(0)
    }
    {
      var i: Int = length
      while (({
        i -= 1; i
      }) >= 0) {
        converter = converters(i)
        val supportedType: Class[_] = converter.getSupportedType
        {
          var j: Int = length
          while (({
            j -= 1; j
          }) >= 0) {
            if (j != i && converters(j).getSupportedType.isAssignableFrom(supportedType)) {
              set = set.remove(j, null)
              converters = set.iConverters
              length = converters.length
              i = length - 1
            }
          }
        }
      }
    }
    if (length == 1) {
      return converters(0)
    }
    val msg: StringBuilder = new StringBuilder
    msg.append("Unable to find best converter for type \"")
    msg.append(`type`.getName)
    msg.append("\" from remaining set: ")
    {
      var i: Int = 0
      while (i < length) {
        {
          converter = converters(i)
          val supportedType: Class[_] = converter.getSupportedType
          msg.append(converter.getClass.getName)
          msg.append('[')
          msg.append(if (supportedType == null) null else supportedType.getName)
          msg.append("], ")
        }
        ({
          i += 1; i - 1
        })
      }
    }
    throw new IllegalStateException(msg.toString)
  }

  private[convert] class Entry {
    private[convert] final val iType: Class[_] = null
    private[convert] final val iConverter: Converter = null

    private[convert] def this(`type`: Class[_], converter: Converter) {
      this()
      iType = `type`
      iConverter = converter
    }
  }

}

class ConverterSet {
  private final val iConverters: Array[Converter] = null
  private var iSelectEntries: Array[ConverterSet.Entry] = null

  private[convert] def this(converters: Array[Converter]) {
    this()
    iConverters = converters
    iSelectEntries = new Array[ConverterSet.Entry](1 << 4)
  }

  /**
   * Returns the closest matching converter for the given type, or null if
   * none found.
   *
   * @param type type to select, which may be null
   * @throws IllegalStateException if multiple converters match the type
   *                               equally well
   */
  @throws(classOf[IllegalStateException])
  private[convert] def select(`type`: Class[_]): Converter = {
    var entries: Array[ConverterSet.Entry] = iSelectEntries
    val length: Int = entries.length
    var index: Int = if (`type` == null) 0 else `type`.hashCode & (length - 1)
    var e: ConverterSet.Entry = null
    while ((({
      e = entries(index); e
    })) != null) {
      if (e.iType eq `type`) {
        return e.iConverter
      }
      if (({
        index += 1; index
      }) >= length) {
        index = 0
      }
    }
    val converter: Converter = ConverterSet.selectSlow(this, `type`)
    e = new ConverterSet.Entry(`type`, converter)
    entries = entries.clone.asInstanceOf[Array[ConverterSet.Entry]]
    entries(index) = e
    {
      var i: Int = 0
      while (i < length) {
        {
          if (entries(i) == null) {
            iSelectEntries = entries
            return converter
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    val newLength: Int = length << 1
    val newEntries: Array[ConverterSet.Entry] = new Array[ConverterSet.Entry](newLength)
    {
      var i: Int = 0
      while (i < length) {
        {
          e = entries(i)
          `type` = e.iType
          index = if (`type` == null) 0 else `type`.hashCode & (newLength - 1)
          while (newEntries(index) != null) {
            if (({
              index += 1; index
            }) >= newLength) {
              index = 0
            }
          }
          newEntries(index) = e
        }
        ({
          i += 1; i - 1
        })
      }
    }
    iSelectEntries = newEntries
    return converter
  }

  /**
   * Returns the amount of converters in the set.
   */
  private[convert] def size: Int = {
    return iConverters.length
  }

  /**
   * Copies all the converters in the set to the given array.
   */
  private[convert] def copyInto(converters: Array[Converter]) {
    System.arraycopy(iConverters, 0, converters, 0, iConverters.length)
  }

  /**
   * Returns a copy of this set, with the given converter added. If a
   * matching converter is already in the set, the given converter replaces
   * it. If the converter is exactly the same as one already in the set, the
   * original set is returned.
   *
   * @param converter  converter to add, must not be null
   * @param removed  if not null, element 0 is set to the removed converter
   * @throws NullPointerException if converter is null
   */
  private[convert] def add(converter: Converter, removed: Array[Converter]): ConverterSet = {
    val converters: Array[Converter] = iConverters
    val length: Int = converters.length
    {
      var i: Int = 0
      while (i < length) {
        {
          val existing: Converter = converters(i)
          if (converter == existing) {
            if (removed != null) {
              removed(0) = null
            }
            return this
          }
          if (converter.getSupportedType eq existing.getSupportedType) {
            val copy: Array[Converter] = new Array[Converter](length)
            {
              var j: Int = 0
              while (j < length) {
                {
                  if (j != i) {
                    copy(j) = converters(j)
                  }
                  else {
                    copy(j) = converter
                  }
                }
                ({
                  j += 1; j - 1
                })
              }
            }
            if (removed != null) {
              removed(0) = existing
            }
            return new ConverterSet(copy)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    val copy: Array[Converter] = new Array[Converter](length + 1)
    System.arraycopy(converters, 0, copy, 0, length)
    copy(length) = converter
    if (removed != null) {
      removed(0) = null
    }
    return new ConverterSet(copy)
  }

  /**
   * Returns a copy of this set, with the given converter removed. If the
   * converter was not in the set, the original set is returned.
   *
   * @param converter  converter to remove, must not be null
   * @param removed  if not null, element 0 is set to the removed converter
   * @throws NullPointerException if converter is null
   */
  private[convert] def remove(converter: Converter, removed: Array[Converter]): ConverterSet = {
    val converters: Array[Converter] = iConverters
    val length: Int = converters.length
    {
      var i: Int = 0
      while (i < length) {
        {
          if (converter == converters(i)) {
            return remove(i, removed)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    if (removed != null) {
      removed(0) = null
    }
    return this
  }

  /**
   * Returns a copy of this set, with the converter at the given index
   * removed.
   *
   * @param index index of converter to remove
   * @param removed if not null, element 0 is set to the removed converter
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  private[convert] def remove(index: Int, removed: Array[Converter]): ConverterSet = {
    val converters: Array[Converter] = iConverters
    val length: Int = converters.length
    if (index >= length) {
      throw new IndexOutOfBoundsException
    }
    if (removed != null) {
      removed(0) = converters(index)
    }
    val copy: Array[Converter] = new Array[Converter](length - 1)
    val j: Int = 0
    {
      var i: Int = 0
      while (i < length) {
        {
          if (i != index) {
            copy(({
              j += 1; j - 1
            })) = converters(i)
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return new ConverterSet(copy)
  }
}
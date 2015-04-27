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
package org.joda.time.format

/**
 * Adapter between old and new printer interface.
 *
 * @author Stephen Colebourne
 * @since 2.4
 */
object InternalParserDateTimeParser {
  private[format] def of(underlying: InternalParser): DateTimeParser = {
    if (underlying.isInstanceOf[DateTimeParserInternalParser]) {
      return (underlying.asInstanceOf[DateTimeParserInternalParser]).getUnderlying
    }
    if (underlying.isInstanceOf[DateTimeParser]) {
      return underlying.asInstanceOf[DateTimeParser]
    }
    if (underlying == null) {
      return null
    }
    return new InternalParserDateTimeParser(underlying)
  }
}

class InternalParserDateTimeParser extends DateTimeParser with InternalParser {
  private final val underlying: InternalParser = null

  private def this(underlying: InternalParser) {
    this()
    this.underlying = underlying
  }

  def estimateParsedLength: Int = {
    return underlying.estimateParsedLength
  }

  def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
    return underlying.parseInto(bucket, text, position)
  }

  def parseInto(bucket: DateTimeParserBucket, text: String, position: Int): Int = {
    return underlying.parseInto(bucket, text, position)
  }

  override def equals(obj: AnyRef): Boolean = {
    if (obj eq this) {
      return true
    }
    if (obj.isInstanceOf[InternalParserDateTimeParser]) {
      val other: InternalParserDateTimeParser = obj.asInstanceOf[InternalParserDateTimeParser]
      return underlying == other.underlying
    }
    return false
  }
}
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
 * Adapter between old and new parser interface.
 *
 * @author Stephen Colebourne
 * @since 2.4
 */
object DateTimeParserInternalParser {
  private[format] def of(underlying: DateTimeParser): InternalParser = {
    if (underlying.isInstanceOf[InternalParserDateTimeParser]) {
      return underlying.asInstanceOf[InternalParser]
    }
    if (underlying == null) {
      return null
    }
    return new DateTimeParserInternalParser(underlying)
  }
}

class DateTimeParserInternalParser extends InternalParser {
  private final val underlying: DateTimeParser = null

  private def this(underlying: DateTimeParser) {
    this()
    this.underlying = underlying
  }

  private[format] def getUnderlying: DateTimeParser = {
    return underlying
  }

  def estimateParsedLength: Int = {
    return underlying.estimateParsedLength
  }

  def parseInto(bucket: DateTimeParserBucket, text: CharSequence, position: Int): Int = {
    return underlying.parseInto(bucket, text.toString, position)
  }
}
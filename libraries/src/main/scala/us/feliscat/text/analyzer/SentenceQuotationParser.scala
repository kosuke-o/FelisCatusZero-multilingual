package us.feliscat.text.analyzer

import java.nio.file.Paths

import us.feliscat.sentence.ja.JapaneseSentenceSplitter
import us.feliscat.text.normalizer.ja.JapaneseEscapeNoun
import us.feliscat.text.{StringNone, StringOption, StringSome}
import us.feliscat.util.LibrariesConfig
import us.feliscat.util.primitive.StringUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

/**
  * @author K.Sakamoto
  *         Created on 2016/02/21
  */
object SentenceQuotationParser {
  private type Quotation = (String, String)
  private type QuotationSentence = (Quotation, QuotedSentence)

  private final val quotations: Seq[Quotation] = {
    val buffer = ListBuffer.empty[Quotation]
    Source.fromFile(
      Paths.get(LibrariesConfig.resourcesDir, "parser", "quotation.csv").toAbsolutePath.toFile
    ).getLines foreach {
      line: String =>
        val quotations: Array[String] = line.trim.split(',')
        if (2 <= quotations.length) {
          val headOpt = StringOption(quotations.head.trim)
          val lastOpt = StringOption(quotations.last.trim)
          if (headOpt.nonEmpty && lastOpt.nonEmpty) {
            buffer += ((headOpt.get, lastOpt.get))
          }
        }
    }
    buffer.result
  }

  def getQuotations: Seq[Quotation] = quotations

  private def getUnContainedNoun(text: String, nouns: Seq[String]): String = {
    nouns foreach {
      noun: String =>
        if (!text.contains(noun)) {
          return noun
        }
    }
    throw new NoSuchElementException("SentenceQuotationParser.getUnContainedNoun")
  }

  def parse(sentenceOpt: StringOption): Option[QuotedSentence] = {
    sentenceOpt match {
      case StringSome(sentence) =>
        Option(parse(sentence, ListBuffer.empty[String] ++ JapaneseEscapeNoun.objects))
      case StringNone =>
        None
    }
  }

  def getFirstMatchOpt(sentenceOpt: StringOption): Option[(Quotation, Range)] = {
    if (sentenceOpt.isEmpty) {
      return None
    }

    val quotationRangeBuffer = ListBuffer.empty[(Quotation, Range)]
    var firstMatchOpt = Option.empty[(Quotation, Range)]

    quotations foreach {
      quotation: Quotation =>
        val quotationStart: String = {
          if (quotation._1 == "(") {
            """\("""
          } else {
            quotation._1
          }
        }
        val quotationEnd: String = {
          if (quotation._2 == ")") {
            """\)"""
          } else {
            quotation._2
          }
        }
        val pattern: String = s"[^$quotationEnd]+".quote((quotationStart, quotationEnd))
        //println(pattern)
        val regex: Regex = pattern.r
        regex.findFirstMatchIn(sentenceOpt.get) foreach {
          m: Match =>
            quotationRangeBuffer += ((quotation, Range(m.start, m.end)))
        }
    }

    if (quotationRangeBuffer.nonEmpty) {
      quotationRangeBuffer foreach {
        case (quotation, range) =>
          if (firstMatchOpt.isEmpty || (range.start < firstMatchOpt.get._2.start)) {
            firstMatchOpt = Option((quotation, range))
          }
        case _ =>
          //Do nothing
      }
    }

    firstMatchOpt
  }

  private def parse(sentence: String, nouns: ListBuffer[String]): QuotedSentence = {
    var parentSentence: String = sentence
    val childrenSentences = mutable.Map.empty[String, QuotationSentence]

    var firstMatchOpt: Option[(Quotation, Range)] = getFirstMatchOpt(StringOption(sentence))

    while (firstMatchOpt.nonEmpty) {
      val (quotation, range): (Quotation, Range) = firstMatchOpt.get
      val start: Int = range.start
      val end:   Int = range.end
      val prefix:     String = parentSentence.substring(0, start)
      val quotedPart: String = parentSentence.substring(start, end)
      val suffix:     String = parentSentence.substring(end)
      val noun:       String = getUnContainedNoun(parentSentence, nouns)
      nouns -= noun
      parentSentence = noun.quote((prefix, suffix))
      childrenSentences.put(noun, (quotation, parse(quotedPart, nouns)))
      firstMatchOpt = getFirstMatchOpt(StringOption(sentence))
    }

    new QuotedSentence(parentSentence, childrenSentences.toMap)
  }

  class QuotedSentence(val parentSentence: String,
                       val childrenSentences: Map[String, QuotationSentence]) {
    override def toString: String = {
      var text: String = parentSentence
      childrenSentences foreach {
        case (replacement, (quotation, sentence)) =>
          text = text.replaceAllLiteratim(
            replacement,
            sentence.toString.quote(quotation))
        case _ =>
          //Do nothing
      }
      text
    }
  }

  def splitAndQuotationParseJapaneseText(textOpt: StringOption): Seq[NormalizedQuotedSentence] = {
    val buffer = ListBuffer.empty[NormalizedQuotedSentence]
    JapaneseSentenceSplitter.split(textOpt) foreach {
      sentence =>
        parse(StringOption(sentence.text)) match {
          case Some(quotedSentence) =>
            buffer += new NormalizedQuotedSentence(sentence.originalText, quotedSentence)
          case None =>
            //Do nothing
        }
    }
    buffer.result
  }

  class NormalizedQuotedSentence(val originalText: String,
                                 val quotedSentence: QuotedSentence) {
    override def toString: String = quotedSentence.toString
  }
}


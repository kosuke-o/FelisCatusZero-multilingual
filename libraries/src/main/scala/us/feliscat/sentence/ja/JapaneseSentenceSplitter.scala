package us.feliscat.sentence.ja

import java.nio.file.Paths

import us.feliscat.m17n.Japanese
import us.feliscat.sentence.MultiLingualSentenceSplitter
import us.feliscat.text.normalizer.ja.{JapaneseEscapeCharacter, JapaneseNormalizedString, JapaneseSentenceNormalizer}
import us.feliscat.text.{StringNone, StringOption, StringSome}
import us.feliscat.util.LibrariesConfig
import us.feliscat.util.primitive.StringUtils

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.control.Breaks

/**
  * @author K.Sakamoto
  *         Created on 2016/08/08
  */
object JapaneseSentenceSplitter extends MultiLingualSentenceSplitter with Japanese {
  //このようなprivateな型を作成しておかないとobject JapaneseSentenceSplitter内のprivate class NormalizedSentenceをSeqで返すparseメソッドが作成ができない。
  private type NS = NormalizedSentence

  private final val japanesePeriodRegex: String = "[。．]"
  private final val japaneseCommaRegex: String  = "[、，]"
  private final val japanesePeriod: String = "。"
  private final val japanesePeriod2: String = "．"
  private final val japaneseComma: String  = "、"

  private final val properNounsWithJapanesePeriod: Seq[String] = initialize
  private var ghost: String = "妛"
  private var ghost2: String = "彁"
  private val mergedProperNounsWithJapanesePeriods: String = {
    val builder = new StringBuilder()
    properNounsWithJapanesePeriod foreach {
      properNoun: String =>
        builder.append(properNoun.trim)
    }
    builder.result
  }

  private def initialize: Seq[String] = {
    Source.fromFile(
      Paths.get(LibrariesConfig.resourcesDir, "parser", "proper_noun_with_japanese_period.txt").toAbsolutePath.toFile
    ).getLines.toSeq.sortWith((a, b) => a.length > b.length)
  }

  def split(textOpt: StringOption): Seq[NS] = {
    if (textOpt.isEmpty) {
      return Nil
    }
    val sentences = ListBuffer.empty[NS]

    //改行文字により行に分割
    textOpt.get.lines foreach {
      l: String =>
        var line: String = l
        val replacementBuffer = ListBuffer.empty[String]

        val tmp: String = mergedProperNounsWithJapanesePeriods concat line

        if ((tmp contains ghost) || (tmp contains ghost2)) {

          var loopCounter: Int = 2
          val breaks: Breaks = new Breaks()

          //もし句点を含む固有名詞に含まれていない文字（原則、幽霊文字）をghostとghost2に実装
          breaks.breakable {
            JapaneseEscapeCharacter.objects foreach {
              case c if !(tmp contains c) =>
                loopCounter -= 1
                if (0 < loopCounter) {
                  ghost = c
                } else {
                  ghost2 = c
                  breaks.break
                }
              case _ =>
                //Do nothing
            }
            throw new NoSuchElementException("JapaneseSentenceSplitter.split")
          }
        }

        //句点を含む固有名詞の句点を幽霊文字に変換
        for (properNoun: String <- properNounsWithJapanesePeriod
             if line contains properNoun) {

          val replacement: String = properNoun.
            replaceAllLiteratim(japanesePeriod, ghost).
            replaceAllLiteratim(japanesePeriod2, ghost2)
          replacementBuffer += replacement
          line = line.replaceAllLiteratim(
            properNoun,
            replacement
          )
        }

        val replacements: Seq[String] = replacementBuffer.result

        //句点により文単位に分割
        for (sentence: String <- line.trim split japanesePeriodRegex
             if StringOption(sentence).nonEmpty) {
          //正規化処理
          val sOpt: Option[String] = splitSentence(sentence)
          if (sOpt.nonEmpty) {
            var s: String = sOpt.get
            //println(s)
            //幽霊文字を元の句点に戻す
            for (replacement: String <- replacements) {
              val normalizedProperNoun: String = JapaneseNormalizedString(StringOption(replacement)).toString
              val normalizedProperNounWithJapanesePeriod: String = normalizedProperNoun.
                replaceAllLiteratim(ghost, japanesePeriod).
                replaceAllLiteratim(ghost2, japanesePeriod2)
              s = s.replaceAllLiteratim(
                normalizedProperNoun,
                normalizedProperNounWithJapanesePeriod)
            }
            //文頭・文末処理
            JapaneseSentenceNormalizer.normalize(StringOption(s)) match {
              case StringSome(ns) =>
                sentences += new NormalizedSentence(ns, sentence)
              case StringNone =>
                //Do nothing
            }
          }
        }
    }

    sentences.result
  }

  private def splitSentence(sentence: String): Option[String] = {
    val phrases = new StringBuilder()

    //読点により節に分割
    sentence.trim split japaneseCommaRegex foreach {
      phrase: String =>
        phrases.
          //節を正規化
          append(JapaneseNormalizedString(StringOption(phrase.trim)).toString).
          //節末に読点を追加
          append(japaneseComma)
    }

    if (phrases.isEmpty) {
      return None
    }

    Option(
      phrases.
        //文末の読点を削除
        deleteCharAt(phrases.size - 1).
        //文末に句点を追加
        append(japanesePeriod).
        result) map {
      _.replaceAll("\u003D", "\uFF1D").//= ＝
        replaceAll("\u301C", "\uFF5E").//〜 ～
        replaceAll("\u007E", "\uFF5E")//~ ～
    }
  }

  class NormalizedSentence(val text: String,
                                   val originalText: String) {
    override def toString: String = text
  }
}

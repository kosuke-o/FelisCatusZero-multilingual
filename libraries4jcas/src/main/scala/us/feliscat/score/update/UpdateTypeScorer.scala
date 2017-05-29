package us.feliscat.score.update

import us.feliscat.score.{Granularity, ScorerType}
import us.feliscat.types.{Document, Sentence}
import us.feliscat.util.JCasLibrariesConfig
import us.feliscat.util.uima.fsList.FSListUtils

/**
 * <pre>
 * Created on 3/12/15.
 * </pre>
 * @author K.Sakamoto
 */
class UpdateTypeScorer(instruction: Document, scoreIndex: Int) extends Scorer {
  private val scorer: Scorer = JCasLibrariesConfig.updateTypeScorer match {
    case ScorerType.Entailment =>
      new EntailmentScorer(instruction, scoreIndex)
    case ScorerType.Relevance =>
      new RelevanceScorer(instruction, scoreIndex)
    case _ =>
      new RelevanceScorer(instruction, scoreIndex)
  }

  override def scoreBySentence(sentence: Sentence): Double = {
    scorer.scoreBySentence(sentence: Sentence)
  }

  override def scoreBySentence(document: Document): Double = {
    scorer.scoreBySentence(document)
  }

  override def scoreBySentences(document: Document): Double = {
    scorer.scoreBySentences(document)
  }

  override def score(document: Document): Double = {
    scorer.score(document)
  }
}

trait Scorer {
  def score(document: Document): Double = {
    JCasLibrariesConfig.granularity match {
      case Granularity.Sentence =>
        scoreBySentence(document)
      case Granularity.Sentences =>
        scoreBySentences(document)
      case Granularity.None =>
        0D
    }
  }
  def scoreBySentence(sentence: Sentence): Double
  def scoreBySentence(document: Document): Double = {
    var score: Double = 0D
    val sentenceSet: Seq[Sentence] = document.getSentenceSet.toSeq.asInstanceOf[Seq[Sentence]]
    sentenceSet foreach {
      sentence: Sentence =>
        score += scoreBySentence(sentence)
    }
    score / sentenceSet.size
  }
  def scoreBySentences(document: Document): Double
}


package uima.cc.qalab.ja

import us.feliscat.m17n.Japanese
import org.apache.uima.jcas.JCas
import uima.cc.qalab.MultiLingualQALabEvaluationMethodSubtaskCasConsumer
import us.feliscat.util.uima.JCasID

/**
  * <pre>
  * Created on 2017/02/14.
  * </pre>
  *
  * @author K.Sakamoto
  */
object JapaneseQALabEvaluationMethodSubtaskCasConsumer
  extends MultiLingualQALabEvaluationMethodSubtaskCasConsumer with Japanese {
  override protected def process(aJCas: JCas)(implicit id: JCasID): Unit = {

  }
}

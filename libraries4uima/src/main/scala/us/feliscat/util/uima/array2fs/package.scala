package us.feliscat.util.uima

import org.apache.uima.jcas.cas._

/**
  * @author K. Sakamoto
  *         Created on 2017/05/25
  */
package object array2fs {
  /**
    * @author K.Sakamoto
    * @param repr array
    * @tparam T type
    */
  implicit class ArrayUtils[T <: TOP](repr: Array[T]) extends UimaUtils {
    def toFSArray: FSArray = {
      val size: Int = repr.length
      val fsArray = new FSArray(aJCas, size)
      for (i <- 0 until size) {
        fsArray.set(i, repr(i))
      }
      fsArray
    }

    def toFSList: FSList = {
      if (repr.isEmpty) {
        //return an empty list
        return new EmptyFSList(aJCas)
      }

      var head = new NonEmptyFSList(aJCas)
      val list: NonEmptyFSList = head
      val it: Iterator[T] = repr.iterator
      while (it.hasNext) {
        head.setHead(it.next)
        if (it.hasNext) {
          head.setTail(new NonEmptyFSList(aJCas))
          head = head.getTail.asInstanceOf[NonEmptyFSList]
        } else {
          head.setTail(new EmptyFSList(aJCas))
        }
      }
      list
    }
  }

  /**
    * @author K.Sakamoto
    * @param repr string array
    */
  implicit class ArrayStringUtils(repr: Array[String]) extends UimaUtils {
    def toStringArray: StringArray = {
      val size: Int = repr.length
      val stringArray = new StringArray(aJCas, size)
      for (i <- 0 until size) {
        stringArray.set(i, repr(i))
      }
      stringArray
    }

    def toStringList: StringList = {
      if (repr.isEmpty) {
        //return an empty list
        return new EmptyStringList(aJCas)
      }

      var head = new NonEmptyStringList(aJCas)
      val list: NonEmptyStringList = head
      val it: Iterator[String] = repr.iterator
      while (it.hasNext) {
        head.setHead(it.next)
        if (it.hasNext) {
          head.setTail(new NonEmptyStringList(aJCas))
          head = head.getTail.asInstanceOf[NonEmptyStringList]
        } else {
          head.setTail(new EmptyStringList(aJCas))
        }
      }
      list
    }
  }

  /**
    * @author K.Sakamoto
    * @param repr integer array
    */
  implicit class ArrayIntegerUtils(repr: Array[Int]) extends UimaUtils {
    def toIntegerArray: IntegerArray = {
      val size: Int = repr.length
      val integerArray = new IntegerArray(aJCas, size)
      for (i <- 0 until size) {
        integerArray.set(i, size)
      }
      integerArray
    }

    def toIntegerList: IntegerList = {
      if (repr.isEmpty) {
        //return an empty list
        return new EmptyIntegerList(aJCas)
      }

      var head = new NonEmptyIntegerList(aJCas)
      val list: NonEmptyIntegerList = head
      val it: Iterator[Int] = repr.iterator
      while (it.hasNext) {
        head.setHead(it.next)
        if (it.hasNext) {
          head.setTail(new NonEmptyIntegerList(aJCas))
          head = head.getTail.asInstanceOf[NonEmptyIntegerList]
        } else {
          head.setTail(new EmptyIntegerList(aJCas))
        }
      }
      list
    }
  }

  /**
    * @author K.Sakamoto
    * @param repr float array
    */
  implicit class ArrayFloatUtils(repr: Array[Float]) extends UimaUtils {
    def toFloatArray: FloatArray = {
      val size: Int = repr.length
      val floatArray = new FloatArray(aJCas, size)
      for (i <- 0 until size) {
        floatArray.set(i, size)
      }
      floatArray
    }

    def toFloatList: FloatList = {
      if (repr.isEmpty) {
        //return an empty list
        return new EmptyFloatList(aJCas)
      }

      var head = new NonEmptyFloatList(aJCas)
      val list: NonEmptyFloatList = head
      val it: Iterator[Float] = repr.iterator
      while (it.hasNext) {
        if (it.hasNext) {
          head.setTail(new NonEmptyFloatList(aJCas))
          head = head.getTail.asInstanceOf[NonEmptyFloatList]
        } else {
          head.setTail(new EmptyFloatList(aJCas))
        }
      }
      list
    }
  }
}

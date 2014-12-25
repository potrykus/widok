package org.widok

import cgta.otest.FunSuite

object AggregateSpec extends FunSuite {
  import ChannelSpec._

  def forallBuf[T](f: Buffer[Int] => (ReadChannel[T], ReadChannel[T]), testEmptyList: Boolean = true) {
    val elems = Seq(1, 2, 3)

    val buffer = Buffer[Int]()

    elems.foreach { elem =>
      val (lch, rch) = f(buffer)
      buffer += elem
      assertEquals(lch, rch)
      tick()
    }

    if (testEmptyList) {
      val buffer2 = Buffer[Int]()
      val (lch, rch) = f(buffer2)
      assertEquals(lch, rch)
      tick()
    }
  }

  def forallBufSeq[T](f: Buffer[Int] => (ReadBuffer[T], () => Seq[T])) {
    val elems = Seq(1, 2, 3)

    val buffer = Buffer[Int]()

    /** Set up handler before insertion */
    elems.foreach { elem =>
      val (buf, seq) = f(buffer)
      buffer += elem
      Assert.isEquals(buf.values, seq())
    }

    /** Set up handler after insertion */
    buffer.clear()
    elems.foreach { elem =>
      buffer += elem
      val (buf, seq) = f(buffer)
      Assert.isEquals(buf.values, seq())
    }

    /** Inserting after */
    buffer.clear()
    elems.foreach { elem =>
      val (buf, seq) = f(buffer)

      val fst = buffer += elem
      val snd = buffer += elem + 1
      val trd = buffer.insertAfter(fst, elem + 2)

      Assert.isEquals(buf.values, seq())
    }

    /** Inserting before */
    buffer.clear()
    elems.foreach { elem =>
      val (buf, seq) = f(buffer)

      val fst = buffer += elem
      val snd = buffer += elem + 1
      val trd = buffer.insertBefore(snd, elem + 2)

      Assert.isEquals(buf.values, seq())
    }

    /** Deleting */
    buffer.clear()
    elems.foreach { elem =>
      val (buf, seq) = f(buffer)

      val fst = buffer += elem
      val snd = buffer += elem
      Assert.isEquals(buf.values, seq())

      buffer -= fst
      Assert.isEquals(buf.values, seq())
    }

    /** TODO Also check updating. */
  }

  test("head") {
    forallBuf(buffer => (buffer.head.isEmpty, buffer.isEmpty), testEmptyList = false)
  }

  test("headOption") {
    forallBuf(buffer => (buffer.headOption.partialMap { case Some(v) => v }, buffer.head))
  }

  test("lastOption") {
    forallBuf(buffer => (buffer.lastOption.partialMap { case Some(v) => v }, buffer.last))
  }

  test("last") {
    forallBuf(buffer => (buffer.last.isEmpty, buffer.isEmpty), testEmptyList = false)
  }

  test("map") {
    forallBufSeq(buffer => (buffer.map(_ * 3), () => buffer.values.map(_ * 3)))
  }

  test("filter") {
    forallBufSeq(buffer => (buffer.filter(_ > 1), () => buffer.values.filter(_ > 1)))
  }
}
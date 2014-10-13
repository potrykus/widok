package org.widok

import org.scalajs.dom
import org.scalajs.dom.extensions.KeyCode
import org.scalajs.dom.{HTMLInputElement, KeyboardEvent}

import org.widok.bindings._

import scala.collection.mutable

object Widget {
  object List {
    trait Item[T <: Item[T]] extends Widget[Item[T]]
  }

  trait List[V <: List[V]] extends Widget[List[V]] { self: V =>
    def bind[T, U <: Seq[T]](channel: Channel[U])(f: T => List.Item[_]) = {
      channel.attach(list => {
        DOM.clear(rendered)

        list.foreach { cur =>
          rendered.appendChild(f(cur).rendered)
        }
      })

      self
    }

    def bind[T, X <: List.Item[X]](aggregate: Aggregate[T])(f: Channel[T] => List.Item[X]) = {
      var map = mutable.Map[Channel[T], List.Item[_]]()

      aggregate.attach(new Aggregate.Observer[T] {
        def append(cur: Channel[T]) {
          val li = f(cur)
          rendered.appendChild(li.rendered)
          map += (cur -> li)
        }

        def remove(cur: Channel[T]) {
          rendered.removeChild(map(cur).rendered)
          map -= cur
        }
      })

      self
    }
  }

  object Input {
    trait Text[V <: Text[V]] extends Widget[Text[V]] { self: V =>
      val rendered: HTMLInputElement

      /**
       * Provides two-way binding.
       *
       * @param data
       *              The channel to read from and to.
       * @param flush
       *              If the channel produces data, this flushes the current
       *              value of the input field.
       * @param live
       *             Produce every single character if true, otherwise
       *             produce only if enter was pressed.
       * @return
       */
      def bind(data: Channel[String], flush: Channel[Unit] = Channel(), live: Boolean = false) = {
        val obs = (text: String) => rendered.value = text

        data.attach(obs)
        flush.attach(_ => data.produce(rendered.value, obs))

        rendered.onkeyup = (e: KeyboardEvent) =>
          if (e.keyCode == KeyCode.enter || live)
            data.produce(rendered.value, obs)

        self
      }
    }

    trait Checkbox[V <: Checkbox[V]] extends Widget[Checkbox[V]] { self: V =>
      val rendered: HTMLInputElement

      def bind(data: Channel[Boolean], flush: Channel[Unit] = Channel()) = {
        val obs = (checked: Boolean) => rendered.checked = checked

        data.attach(obs)
        flush.attach(_ => data.produce(rendered.checked, obs))

        rendered.onchange = (e: dom.Event) => data.produce(rendered.checked, obs)
        self
      }
    }

    trait Select[V <: Select[V]] extends Widget[Select[V]] { self: V =>
      // TODO define bind()
    }
  }

  trait Button[V <: Button[V]] extends Widget[Button[V]] { self: V =>
    def bind(data: Channel[Unit]) = {
      rendered.onclick = (e: dom.Event) => data.produce(())
      self
    }
  }

  trait Anchor[V <: Anchor[V]] extends Widget[Anchor[V]] { self: V =>
    def bind(data: Channel[Unit]) = {
      rendered.onclick = (e: dom.Event) => data.produce(())
      self
    }
  }

  trait Container[V <: Container[V]] extends Widget[Container[V]] { self: V =>
    def bindString[T <: String](value: Channel[T]) = {
      value.attach(cur => rendered.textContent = cur.toString)
      self
    }

    def bindInt[T <: Int](value: Channel[T]) = {
      value.attach(cur => rendered.textContent = cur.toString)
      self
    }

    def bindDouble[T <: Double](value: Channel[T]) = {
      value.attach(cur => rendered.textContent = cur.toString)
      self
    }

    def bindBoolean[T <: Boolean](value: Channel[T]) = {
      value.attach(cur => rendered.textContent = cur.toString)
      self
    }

    def bindWidget[T <: Widget[_]](value: Channel[T]) = {
      value.attach(cur => {
        if (rendered.firstChild != null) rendered.removeChild(rendered.firstChild)
        rendered.appendChild(cur.rendered)
      })

      self
    }

    def bindOptWidget[T <: Option[Widget[_]]](value: Channel[T]) = {
      value.attach(cur => {
        if (rendered.firstChild != null) rendered.removeChild(rendered.firstChild)
        if (cur.isDefined) rendered.appendChild(cur.get.rendered)
      })

      self
    }

    // Bind HTML.
    def bindRaw[T](value: Channel[String]) = {
      value.attach(cur => rendered.innerHTML = cur)
      self
    }
  }
}

object Event {
  trait Mouse
  object Mouse {
    case object Click extends Mouse
    case object DoubleClick extends Mouse
    case object Leave extends Mouse
    case object Enter extends Mouse
    case object Out extends Mouse
    case object Up extends Mouse
    case object Over extends Mouse
    case object Down extends Mouse
    case object Move extends Mouse
    case object ContextMenu extends Mouse
  }

  trait Touch
  object Touch {
    case object Start extends Touch
    case object Move extends Touch
    case object End extends Touch
  }

  trait Key
  object Key {
    case object Down extends Key
    case object Up extends Key
    case object Press extends Key
  }
}

trait Widget[T <: Widget[T]] { self: T =>
  val rendered: dom.HTMLElement

  // May only be used once.
  // TODO Add assertions
  def bindMouse(event: Event.Mouse, writeChannel: Channel[dom.MouseEvent]) = {
    import Event.Mouse._
    event match {
      case Click => rendered.onclick = (e: dom.MouseEvent) => writeChannel.produce(e)
      case DoubleClick => rendered.ondblclick = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Leave => rendered.onmouseleave = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Enter => rendered.onmouseenter = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Out => rendered.onmouseout = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Up => rendered.onmouseup = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Over => rendered.onmouseover = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Down => rendered.onmousedown = (e: dom.MouseEvent) => writeChannel.produce(e)
      case Move => rendered.onmousemove = (e: dom.MouseEvent) => writeChannel.produce(e)
      case ContextMenu => rendered.oncontextmenu = (e: dom.MouseEvent) => writeChannel.produce(e)
    }

    self
  }

  // May only be used once.
  // TODO Add assertions
  def bindKey(event: Event.Key, writeChannel: Channel[dom.KeyboardEvent]) = {
    import Event.Key._
    event match {
      case Up => rendered.onkeyup = (e: dom.KeyboardEvent) => writeChannel.produce(e)
      case Down => rendered.onkeydown = (e: dom.KeyboardEvent) => writeChannel.produce(e)
      case Press => rendered.onkeypress = (e: dom.KeyboardEvent) => writeChannel.produce(e)
    }

    self
  }

  // May only be used once.
  // TODO Add assertions
  def bindTouch(event: Event.Touch, writeChannel: Channel[dom.TouchEvent]) = {
    import Event.Touch._
    val ev = event match {
      case Start => "ontouchstart"
      case Move => "ontouchmove"
      case End => "ontouchend"
    }

    rendered.addEventListener(
      ev,
      (e: dom.Event) => writeChannel.produce(e.asInstanceOf[dom.TouchEvent]),
      useCapture = false)

    self
  }

  def id(id: String) = {
    rendered.id = id
    self
  }

  def cursor(cursor: HTML.Cursor) = {
    rendered.style.cursor = cursor.toString
    self
  }

  def css(cssTags: String*) = {
    val tags = rendered.className.split(" ").toSet
    rendered.className = (tags ++ cssTags).mkString(" ")
    self
  }

  def css(state: Boolean, cssTags: String*) = {
    val tags = rendered.className.split(" ").toSet

    val changed =
      if (state) tags ++ cssTags
      else tags.diff(cssTags.toSet)

    rendered.className = changed.mkString(" ")
    self
  }

  def cssCh(tag: Channel[String]) = {
    var cur: Option[String] = None

    tag.attach(value => {
      val tags = rendered.className.split(" ").toSet
      val changed =
        if (cur.isDefined) tags - cur.get + value
        else tags + value
      cur = Some(value)

      rendered.className = changed.mkString(" ")
    })

    self
  }

  def cssCh(state: Channel[Boolean], cssTags: String*) = {
    state.attach(value => css(value, cssTags: _*))
    self
  }

  def attribute(key: String, value: String) = {
    rendered.setAttribute(key, value)
    self
  }

  def show(value: Channel[Boolean], remove: Boolean = true) = {
    value.attach(cur =>
      if (remove) {
        rendered.style.display =
          if (cur) "block" else "none"
      } else {
        rendered.style.visibility =
          if (cur) "visible" else "hidden"
      })

    self
  }
}
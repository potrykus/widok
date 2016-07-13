package org.widok.bindings

import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLAudioElement, HTMLVideoElement, HTMLMediaElement, HTMLSourceElement}

import pl.metastack.metarx._

import org.widok._

object HTML {
  trait Cursor
  object Cursor {
    case object Wait extends Cursor { override def toString = "wait" }
    case object Pointer extends Cursor { override def toString = "pointer" }
    case object Move extends Cursor { override def toString = "move" }
  }

  sealed trait Floating
  object Floating {
    /** Default. */
    case object None extends Floating { override def toString = "none"}
    case object Left extends Floating { override def toString = "left"}
    case object Right extends Floating { override def toString = "right"}
    case object Initial extends Floating { override def toString = "initial"}
    case object Inherit extends Floating { override def toString = "inherit"}
  }

  sealed trait Clear
  object Clear {
    case object None extends Clear { override def toString = "none"}
    case object Left extends Clear { override def toString = "left"}
    case object Right extends Clear { override def toString = "right"}
    case object Initial extends Clear { override def toString = "initial"}
    case object Inherit extends Clear { override def toString = "inherit"}

    /** No floating elements allowed on either the left or the right side */
    case object Both extends Clear { override def toString = "both"}
  }

  sealed trait Overflow
  object Overflow {
    case object Visible extends Overflow { override def toString = "visible"}
    case object Hidden extends Overflow { override def toString = "hidden"}
    case object Scroll extends Overflow { override def toString = "scroll"}
    case object Auto extends Overflow { override def toString = "auto"}
    case object Initial extends Overflow { override def toString = "initial"}
    case object Inherit extends Overflow { override def toString = "inherit"}
  }

  object Heading {
    case class Level1(contents: View*) extends Widget[Level1] {
      val rendered = DOM.createElement("h1", contents)
    }

    case class Level2(contents: View*) extends Widget[Level2] {
      val rendered = DOM.createElement("h2", contents)
    }

    case class Level3(contents: View*) extends Widget[Level3] {
      val rendered = DOM.createElement("h3", contents)
    }

    case class Level4(contents: View*) extends Widget[Level4] {
      val rendered = DOM.createElement("h4", contents)
    }

    case class Level5(contents: View*) extends Widget[Level5] {
      val rendered = DOM.createElement("h5", contents)
    }

    case class Level6(contents: View*) extends Widget[Level6] {
      val rendered = DOM.createElement("h6", contents)
    }
  }

  case class Paragraph(contents: View*) extends Widget[Paragraph] {
    val rendered = DOM.createElement("p", contents)
  }

  object Text {
    case class Bold(contents: View*) extends Widget[Bold] {
      val rendered = DOM.createElement("b", contents)
    }

    case class Italic(contents: View*) extends Widget[Italic] {
      val rendered = DOM.createElement("i", contents)
    }

    case class Small(contents: View*) extends Widget[Small] {
      val rendered = DOM.createElement("small", contents)
    }
  }

  case class Text(value: String) extends Widget[Text] {
    val rendered = dom.document.createTextNode(value)
      .asInstanceOf[dom.html.Element]
  }

  case class Raw(html: String) extends Widget[Raw] {
    val rendered = DOM.createElement("span")
    rendered.innerHTML = html

    def bind(innerHtml: ReadChannel[String]): Raw = {
      innerHtml.attach(rendered.innerHTML = _)
      this
    }
  }

  case class Image(source: String) extends Widget[Image] {
    val rendered = DOM.createElement("img")
    rendered.setAttribute("src", source)
  }

  case class LineBreak() extends Widget[LineBreak] {
    val rendered = DOM.createElement("br")
  }

  sealed trait ButtonType { val tpe: String }
  object ButtonType {
    case object Button extends ButtonType { val tpe = "button" }
    case object Submit extends ButtonType { val tpe = "submit" }
    case object Reset extends ButtonType { val tpe = "reset" }
  }

  trait ButtonBase[T] extends Widget[T] { self: T =>
    def element: dom.html.Element
    override val rendered = element

    def tpe(value: ButtonType) = attribute("type", value.tpe)
  }

  case class Button(contents: View*) extends ButtonBase[Button] {
    def element = DOM.createElement("button", contents)
  }

  case class Section(contents: View*) extends Widget[Section] {
    val rendered = DOM.createElement("section", contents)
  }

  case class Header(contents: View*) extends Widget[Header] {
    val rendered = DOM.createElement("header", contents)
  }

  case class Footer(contents: View*) extends Widget[Footer] {
    val rendered = DOM.createElement("footer", contents)
  }

  case class Navigation(contents: View*) extends Widget[Navigation] {
    val rendered = DOM.createElement("nav", contents)
  }

  sealed trait Target { def html: String }
  object Target {
    case object Blank extends Target { override def html = "_blank" }
    case object Self extends Target { override def html = "_self" }
    case object Parent extends Target { override def html = "_parent" }
    case object Top extends Target { override def html = "_top" }
    case class FrameName(value: String) extends Target {
      override def html = value
    }
  }

  trait AnchorBase[T] extends Widget[T] { self: T =>
    def url(value: String) = {
      rendered.setAttribute("href", value)
      self
    }

    def target(value: Target) = {
      rendered.setAttribute("target", value.html)
      self
    }
  }

  case class Anchor(contents: View*) extends AnchorBase[Anchor] {
    val rendered = DOM.createElement("a", contents)
  }

  case class Form(contents: View*) extends Widget[Form] {
    val rendered = DOM.createElement("form", contents)
  }

  case class Label(contents: View*) extends Widget[Label] {
    val rendered = DOM.createElement("label", contents)

    def forId(value: String) = {
      rendered.setAttribute("for", value)
      this
    }
  }

  object Input {
    trait Textual[T] extends Widget.Input.Text[T] { self: T =>
      def autofocus(value: Boolean) =
        attribute("autofocus", "")

      def placeholder(value: String) =
        attribute("placeholder", value)

      def placeholder(value: ReadChannel[String]) =
        attribute("placeholder", value)
    }

    trait TextBase[T] extends Textual[T] { self: T =>
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]
      rendered.setAttribute("type", "text")

      def autocomplete(value: Boolean) = {
        rendered.setAttribute("autocomplete", if (value) "on" else "off")
        self
      }
    }

    case class Text() extends TextBase[Text]

    trait PasswordBase[T] extends Textual[T] { self: T =>
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]
      rendered.setAttribute("type", "password")
    }

    case class Password() extends PasswordBase[Password]

    case class Checkbox() extends Widget.Input.Checkbox[Checkbox] {
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]
      rendered.setAttribute("type", "checkbox")
    }

    case class Radio() extends Widget.Input.Checkbox[Radio] {
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]
      rendered.setAttribute("type", "radio")
    }

    trait NumberBase[T] extends Textual[T] { self: T =>
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]
      rendered.setAttribute("type", "number")
    }

    case class Number() extends NumberBase[Number]

    trait TextareaBase[T] extends Textual[T] { self: T =>
      val rendered = DOM.createElement("textarea")
        .asInstanceOf[dom.html.Input]

      def cols(value: Int) = attribute("cols", value.toString)
      def rows(value: Int) = attribute("rows", value.toString)
    }

    case class Textarea() extends TextareaBase[Textarea]

    case class File() extends Textual[File] {
      val rendered = DOM.createElement("input")
        .asInstanceOf[dom.html.Input]

      def accept(value: String) = {
        rendered.setAttribute("accept", value)
        this
      }

      def bind(writeChannel: WriteChannel[String]) = {
        rendered.addEventListener(
          "change",
          (e: dom.Event) => writeChannel.produce(rendered.value),
          useCapture = false)
        this
      }

      rendered.setAttribute("type", "file")
    }

    object Select {
      case class Option(caption: String) extends Widget[Option] {
        val rendered = DOM.createElement("option", Seq(HTML.Text(caption)))

        def bind(ch: ReadChannel[Boolean]) = {
          ch.attach { selected =>
            if (selected) attributes.insertOrUpdate("selected", "")
            else attributes.removeIfExists("selected")
          }
        }
      }
    }

    trait SelectBase[T] extends Widget.Input.Select[T] { self: T =>
      val rendered = DOM.createElement("select")

      def options(options: Seq[HTML.Input.Select.Option], selected: Int = -1) = {
        options.zipWithIndex.foreach { case (elem, idx) =>
          if (idx == selected) elem.rendered.setAttribute("selected", "")
          rendered.appendChild(elem.rendered)
        }

        self
      }
    }

    case class Select() extends SelectBase[Select]
  }

  case class HorizontalLine() extends Widget[HorizontalLine] {
    val rendered = DOM.createElement("hr")
  }

  object List {
    case class Unordered(contents: Widget.List.Item[_]*) extends Widget.List[Unordered] {
      val rendered = DOM.createElement("ul", contents)
    }

    case class Ordered(contents: Widget.List.Item[_]*) extends Widget.List[Ordered] {
      val rendered = DOM.createElement("ol", contents)
    }

    case class Item(contents: View*) extends Widget.List.Item[Item] {
      val rendered = DOM.createElement("li", contents)
    }

    case class Items(buf: DeltaBuffer[Widget[_]]) extends Widget.List.Item[Items] {
      val rendered = DOM.createNullElement()

      override def render(parent: dom.Node, offset: dom.Node) {
        import Buffer.Delta
        import Buffer.Position

        DOM.insertAfter(parent, offset, rendered)

        var last: dom.Node = rendered

        buf.changes.attach {
          case Delta.Insert(Position.Head(), element) =>
            DOM.insertAfter(parent, rendered, element.rendered)
            if (last == rendered) last = element.rendered

          case Delta.Insert(Position.Last(), element) =>
            DOM.insertAfter(parent, last, element.rendered)
            last = element.rendered

          case Delta.Insert(Position.Before(reference), element) =>
            parent.insertBefore(element.rendered, reference.rendered)

          case Delta.Insert(Position.After(reference), element) =>
            DOM.insertAfter(parent, reference.rendered, element.rendered)
            if (last == reference.rendered) last = element.rendered

          case Delta.Replace(reference, element) =>
            parent.replaceChild(element.rendered, reference.rendered)

          case Delta.Remove(element) =>
            if (last == element.rendered) last = element.rendered.previousSibling
            parent.removeChild(element.rendered)

          case Delta.Clear() =>
            if (last != rendered) {
              DOM.remove(parent, rendered.nextSibling, last)
              last = rendered
            }
        }
      }
    }
  }

  object Table {
    case class Head(contents: Widget.List.Item[_]*) extends Widget.List[Head] {
      val rendered = DOM.createElement("thead", contents)
    }

    case class HeadColumn(contents: View*) extends Widget[HeadColumn] {
      val rendered = DOM.createElement("th", contents)
    }

    case class Body(contents: Widget.List.Item[_]*) extends Widget.List[Body] {
      val rendered = DOM.createElement("tbody", contents)
    }

    /** May contain either HeadColumn or Column. */
    case class Row(contents: View*) extends Widget.List.Item[Row] {
      val rendered = DOM.createElement("tr", contents)
    }

    case class Column(contents: View*) extends Widget.List.Item[Column] {
      val rendered = DOM.createElement("td", contents)
    }
  }

  case class Time(contents: View*) extends Widget[Time] {
    val rendered = DOM.createElement("time", contents)

    def dateTime(value: String) = {
      rendered.setAttribute("datetime", value)
      this
    }
  }

  case class Table(contents: View*) extends Widget[Table] {
    val rendered = DOM.createElement("table", contents)
  }

  object Container {
    case class Generic(contents: View*) extends Widget.Container[Generic] {
      val rendered = DOM.createElement("div", contents)
    }

    case class Inline(contents: View*) extends Widget.Container[Inline] {
      val rendered = DOM.createElement("span", contents)
    }

  }

  case class Source() extends Widget[Source] {
    val rendered = DOM.createElement("source").asInstanceOf[HTMLSourceElement]

    def src(s: String) = { rendered.src = s; this }
    def src(s: ReadChannel[String]) = { attribute("src", s); this }

    def media(m: String) = { rendered.media = m; this }
    def media(m: ReadChannel[String]) = { attribute("media", m); this }

    def tpe(t: String) = { rendered.`type` = t; this }
    def tpe(t: ReadChannel[String]) = { attribute("type", t); this }
  }

  trait MediaBase[T] extends Widget[T] {
    self: T =>
    override val rendered: HTMLMediaElement

    def autoplay(a: Boolean) = { rendered.autoplay = a; self }
    def controls(c: Boolean) = { rendered.controls = c; self }
    def loop(l: Boolean) = { rendered.loop = l; self }
    def muted(m: Boolean) = { rendered.muted = m; self }
  }

  case class Video(sources: Source*) extends MediaBase[Video] {
    override val rendered = DOM.createElement("video", sources).asInstanceOf[HTMLVideoElement]

    def videoWidth(w: Int) = { rendered.videoWidth = w; this }
    def videoHeight(h: Int) = { rendered.videoHeight = h; this }
    def poster(url: String) = { rendered.poster = url; this }
  }

  case class Audio(sources: Source*) extends MediaBase[Audio] {
    override val rendered = DOM.createElement("audio", sources).asInstanceOf[HTMLAudioElement]
  }
}

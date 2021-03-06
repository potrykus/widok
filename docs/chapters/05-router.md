# Router
When developing applications that consist of more than one page, a routing system becomes inevitable.

The router observes the fragment identifier of the browser URL. For example, in ``application.html#/page`` the part after the hash mark denotes the fragment identifier, that is ``/page``. The router is initialised with a set of known routes. A fallback route may also be specified.

## Interface
The router may be used as follows:

```scala
object Main extends Application {
  val main = Route("/", pages.Main)
  val test = Route("/test/:param", pages.Test)
  val test2 = Route("/test/:param/:param2", pages.Test)
  val notFound = Route("/404", pages.NotFound)

  val routes = Set(main, test, notFound)

  def main() {
    val router = Router(enabled, fallback = Some(notFound))
    router.listen()
  }
}
```

``routes`` denotes the set of enabled routes. It should also contain the ``notFound`` route. Otherwise, the page could not be displayed when ``#/404`` is loaded.

## Routes
To construct a new route, pass the path and its reference to ``Route()``. Pages may be overloaded with different paths as above with ``test`` and ``test2``.

A path consists of *parts* which are separated by slashes. For instance, the ``test`` route above has two parts: ``test`` and ``:param``. A part beginning with a colon is a *placeholder*. Its purpose is to match the respective value in the fragment identifier and to bind it to the placeholder name. Note that a placeholder always refers to the whole part.

A route can be *instantiated* by calling it, setting all of its placeholders:

```scala
// Zero parameters
val route: InstantiatedRoute = Main.main()

// One parameter
val route = Main.test("param", "value")

// Multiple parameters
val route: InstantiatedRoute =
  Main.test2(
    Map(
      "param" -> "value",
      "param2" -> "value2"
    )
  )

// Redirect to `route`
route.go()
```

To query the instantiated parameters, access the ``args`` field in the first parameter passed to ``ready()``.

```scala
case class Test() extends Page {
  ...
  def ready(route: InstantiatedRoute) {
    log(route.args("param"))

    // Accessing optional parameters with get()
    // This returns an Option[String]
    log(route.args.get("param2"))
  }
}
```

### Design decisions
Due to its limitations, the router could be efficiently implemented. Matching string-only parts in routes allows for better reasoning than regular expressions. When the router is constructed, it sorts all routes by their length and checks whether there are any conflicts. Also, the restriction that each parameter must be named makes code more readable when referring to parameters of an instantiated route. If validation of parameters is desired, this must be done in ``ready()``.

## Application provider
As the router defines usually the entry point of an application, Widok provides an application provider that enforces better separation:

```scala
object Routes {
  val main = Route("/", pages.Main)
  ...
  val notFound = Route("/404", pages.NotFound)

  val routes = Set(main, ..., notFound)
}

object Main extends RoutingApplication(
  Routes.routes
, Routes.notFound
)
```

This is to be preferred when no further logic should be executed in the entry point prior to setting up the router.


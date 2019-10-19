# Virtua

A Simple virtual DOM library in pure ClojureScript

## Overview

Virtua is an implementation of virtual dom algorithms in pure ClojureScript.
Originally developed as part of a presentation at
[Clojure.MN](https://www.clojure.mn) (way back in 2016), this has been expanded
to be a jumping off point for developing a pure-ClojureScript.

## Usage

### Adding to Your Project

Virtua is not (yet) published to a repostory, but you can add it to your project
as a git dependency.  In `deps.edn`:

```clojure
{:deps  {org.clojure/clojurescript {:mvn/version "1.10.520"}
         virtua {:git/url "https://github.com/peterschwarz/virtua"
                 :sha "<current master>"}}}
```

Similar usage can be had via Leinigen or boot.

### How to Use

A Virtua component is an function that take the current snapshot of the state
value. It must return hiccup-style markup.

For example:

```clojure
(let [app-state (atom {:count 0})
      parent (. js/document (getElementById "app"))]
(attach!
  (fn [state]
    [:div#counter
     [:div#count (get state :count 0)]
     [:div#count-button
       [:button
        {:on-click #(swap! app-state update-in [:count] inc)}
        "Count!" ]]])
   app-state
   parent))
```

The value may be an arbitrary data, though any non-Atom values will be wrapped
in an atom. Though, in this case, it would be difficult to update the state, as
no state modifier methods are provided.

## Development

Virtua is configured to use [Figwheel](https://figwheel.org) during development,
with an [example](dev/virtua/main.cljs) as the main, and auto-testing enabled.

Running

```
$ clj -A:build-dev
```

will open up the example app at [localhost:9500/](http://localhost:9500/), and
the test runner at
[localhost:9500/figwheel-extra-main/auto-testing](http://localhost:9500/figwheel-extra-main/auto-testing)

## License

Virtua is licensed under the [Apache License Version 2.0](LICENSE) software
license.

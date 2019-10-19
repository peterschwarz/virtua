(ns virtua.dom
  (:require [goog.dom :as gdom]
            [goog.object :as obj]))

(defn- event-handler? [k]
  (= "on-" (-> (name k) (subs 0 3))))

(defn set-attribute
  "Sets an attribute on the given element.

  Keys can be symbols, strings or keywords. Boolean values will be handled
  appropriately."
  [el k v]
  (cond
    (boolean? v) (obj/set el (name k) v)
    :else (.setAttribute el (name k) v)))

(defn set-props
  "Sets the given map of properties or attributes on an element.

  Note that any event handlers or nil-valued entries are not set."
  [el m]
  (doseq [[attr v] m]
    (when (and v (not (event-handler? attr)))
      (set-attribute el attr v)))
  el)

(defn remove-props
  "Removes the properties or attributes specified in the map.

  Note that any event handlers are ignored."
  [el m]
  (doseq [attr (cljs.core/remove event-handler? (keys m))]
    (.removeAttribute el (name attr)))
  el)

(defn update-props
  "Updates the properties on a element. Properties in the first map are removed,
  properties in the second map are applied."
  [el to-remove to-add]
  (-> el
      (remove-props to-remove)
      (set-props to-add)))

(defn- node-list-seq
  "Converts a JS NodeList into a sequence."
  [node-list]
  (when node-list
    (map (fn [index] (aget node-list index)) (range (.-length node-list)))))

(defn children
  "Returns a sequence of the given element's children."
  [el]
  (when el
    (node-list-seq (.-childNodes el))))

(defn child-count
  "Returns the count of the given element's children."
  [el]
  (if el
    (.. (.-childNodes el) -length)
    0))

(defn child-at
  "Returns the child of the given element at the given index."
  [el index]
  (when (and el (<= index (child-count el)))
    (aget (.-childNodes el) index)))

(defn append
  "Appends the children to a given element."
  ([el child]
   (when child
     (gdom/appendChild el child))
   el)
  ([el child & children]
   (append el child)
   (doseq [c children]
     (append el c))
   el))

(defn insert-at
  "Inserts a child element at the given index of of the parent element's
  children."
  [el child i]
  (gdom/insertChildAt el child i))

(defn create-text
  "Creates a Text Node with the given string content."
  [s]
  (gdom/createTextNode s))

(defn create
  "Creates an HTML element of the given tag. Tag may be a string, a symbol or a
  keyword."
  [tag]
  (gdom/createDom (name tag)))

(defn remove [el]
  (when el
    (gdom/removeNode el)))

(defn remove-children [el]
  (gdom/removeChildren el))

(defn replace [a b]
  (when (and a b)
    (gdom/replaceNode a b)))

(ns virtua.dom
  (:require [goog.dom :as gdom]
            [goog.object :as obj]
            #_[cljs.pprint :refer [pprint]])
  (:refer-clojure :exclude [remove replace]))

(defn- event-handler? [k]
  (= "on-" (-> (name k) (subs 0 3))))

(defn set-attribute [el k v]
  (.setAttribute el (name k) v)
  (when (boolean? v)
    (obj/set el (name k) v)))

(defn set-props [el m]
  (doseq [[attr v] m]
    (when (and v (not (event-handler? attr)))
      (set-attribute el attr v)))
  el)

(defn remove-props [el m]
  (doseq [attr (cljs.core/remove event-handler? (keys m))]
    (.removeAttribute el (name attr)))
  el)

(defn update-props [el to-remove to-add]
  (-> el
      (remove-props to-remove)
      (set-props to-add)))

(defn node-list-seq [node-list]
  (when node-list
    (map (fn [index] (aget node-list index)) (range (.-length node-list)))))

(defn children [el]
  (when el
    (node-list-seq (.-childNodes el))))

(defn child-count [el]
  (if el
    (.. (.-childNodes el) -length)
    0))

(defn child-at [el index]
  (when (and el (<= index (child-count el)))
    (aget (.-childNodes el) index)))

(defn append
  ([el child]
   (when child
     (gdom/appendChild el child))
   el)
  ([el child & children]
   (append el child)
   (doseq [c children]
     (append el c))
   el))

(defn insert-at [el child i]
  (gdom/insertChildAt el child i))

(defn create-text [s]
  (gdom/createTextNode s))

(defn create
  [tag]
  (gdom/createDom tag))

(defn remove [el]
  (when el
    (gdom/removeNode el)))

(defn remove-children [el]
  (gdom/removeChildren el))

(defn replace [a b]
  (when (and a b)
    (gdom/replaceNode a b)))

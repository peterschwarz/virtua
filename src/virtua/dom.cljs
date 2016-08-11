(ns virtua.dom
  (:require [goog.dom :as gdom])
  (:refer-clojure :exclude [remove replace]))

(defn set-props [el m]
  (doseq [[attr v] m]
    (when (and v (not= "on-" (-> (name attr) (subs 0 3))))
      (.setAttribute el (name attr) (str v))))
  el)

(defn update-props [el remove-props add-props])

(defn remove-props [el])

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

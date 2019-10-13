(ns virtua.test-utils
  (:require [goog.dom :as gdom]))

(defn new-container! [id]
  (let [component (gdom/createDom "div" #js {:id id})
        body (-> (gdom/getElementsByTagName "body") (aget 0))]
    (gdom/appendChild body component)
    component))

(defn remove-container! [el]
  (gdom/removeNode el))

(ns virtua.test-utils
  (:require [goog.dom :as gdom]))

(defn new-container! [id]
  (let [component (gdom/createDom "div" #js {:id id})]
    (gdom/appendChild (gdom/getElement "tests") component)
    component))

(defn remove-container! [el]
  (gdom/removeNode el))

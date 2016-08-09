(ns virtua.main
  (:require [virtua.core :as v]))

(enable-console-print!)


;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn a-component [state]
  [:div
   [:p "I'm a sub componnet"]
   [:p "My state of interest: " (:text state)]])

(v/attach!
  (fn [state]
    [:div {:class "container"} "Hello"
     a-component
     [:ul
      (map (fn [i] [:li i]) (:list state))]])
  app-state
  (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

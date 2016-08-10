(ns virtua.main
  (:require [virtua.core :as v]))

(enable-console-print!)


;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn a-component [state]
  [:div.row
   [:p "I'm a sub component"]
   [:p "My state of interest: " (:text state)]])

(v/attach!
  (fn [state]
    [:div {:class "container"}
     [:h1 "Hello, Virtua"]
     [:p "Hello there, we're using Virtua to render this page."]
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

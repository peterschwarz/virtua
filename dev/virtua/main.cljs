(ns virtua.main
  (:require [virtua.core :as v]))

(enable-console-print!)


;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:content "Hello world!"}))

(defn a-component [state]
  [:div.row
   [:p "I'm a sub component"]
   [:p "My state of interest: " (:content state)]])

(v/attach!
  (fn [state]
    [:div.container
     [:h1 "Hello, Virtua"]
     [:p "Hello there, we're using Virtua to render this page."]
     a-component
     [:ul
      (map (fn [i] [:li i]) (:list state))]
     [:div.row
      [:div.col-xs-1 (get state :count 0)]
      [:div.col-xs-1
       [:button.btn.btn-default
        {:on-click #(swap! app-state update-in [:count] inc)}
        "Count!" ]]]])
  app-state
  (. js/document (getElementById "app")))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

(ns ^:figwheel-hooks virtua.main
  (:require [virtua.core :as v]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:content "Hello world!" :checked? true}))

(defn a-component [state]
  [:section#sub-component
   [:p "I'm a sub component.  You can modify the following state by setting the value "
    [:code ":content"] " in the " [:code "app-state"] " atom from the REPL."]
   [:p "My state of interest: " (:content state)]])

(v/attach!
  (fn [state]
    [:div
     [:header [:h1 "Hello, Virtua"]]

     [:section#demo-description
      [:article "Hello there, we're using Virtua to render this page."]]

      a-component

    [:section#item-list
     [:ul
      (map (fn [i] [:li i]) (:list state))]]

     [:section#counter-demo
      (let [checked? (get state :enabled? true)]
        [:div#counter
         [:div#count (get state :count 0)]
         [:div#count-button
          [:button
           {:on-click #(swap! app-state update-in [:count] inc)
            :disabled (not checked?)}
           "Count!" ]]
         [:div#count-enabled
          [:label
           [:input {:type "checkbox"
                    :checked checked?
                    :on-change #(swap! app-state assoc :enabled? (not checked?))}]
           "Enable Counting"]]])]]
      )

  app-state
  (. js/document (getElementById "app")))


(defn ^:after-load on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (println "src updated")
)

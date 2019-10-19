; Copyright 2016-2019 Peter Schwarz
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
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

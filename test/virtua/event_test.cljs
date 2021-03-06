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
(ns virtua.event-test
  (:require [cljs.test :refer-macros [deftest testing async is are use-fixtures]]
            [virtua.core :as v]
            [virtua.test-utils :refer-macros [with-container]]
            [goog.dom :as gdom]
            [goog.events :as evt]))

(defn- trigger! [el evt-type]
  (let [event (js/document.createEvent "HTMLEvents")]
    (.initEvent event evt-type, true, false)
    (.dispatchEvent el event)))

(defn- click! [el]
  (trigger! el "click"))

(defn- change! [el value]
  (set! (.-value el) value)
  (trigger! el "change"))

(deftest test-click
  (let [clicked? (atom false)]
    (with-container el
      (v/attach!
        [:div {:id "test"
               :on-click #(reset! clicked? true)}]
        {}
        el)

      (click! (gdom/getRequiredElement "test")))
    (is (= true @clicked?))))

(deftest test-change
  (let [new-value (atom nil)]
    (with-container el
      (v/attach!
        [:div {:id "test"
               :on-change #(reset! new-value (.. % -target -value))}]
        {}
        el)
      (change! (gdom/getRequiredElement "test") "changed!"))
    (is (= "changed!" @new-value))))

(deftest test-update-change-handler
  (let [clicked-value (atom 0)
        app-state (atom {:click-fn #(swap! clicked-value inc)})]
    (with-container el
      (v/attach!
        (fn [state]
          [:button {:id "btn"
                    :on-click (:click-fn state)}])
        app-state
        el)

      (click! (gdom/getRequiredElement "btn"))
      (is (= 1 @clicked-value))

      (swap! app-state assoc :click-fn #(swap! clicked-value dec))

      (click! (gdom/getRequiredElement "btn"))
      (is (= 0 @clicked-value)))))

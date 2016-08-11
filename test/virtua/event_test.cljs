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

(ns virtua.core-test
  (:require [cljs.test :refer-macros [deftest testing async is are use-fixtures]]
            [virtua.dom :refer [child-at child-count]]
            [virtua.core :as v]
            [virtua.test-utils :refer-macros [wait with-container]]
            [goog.dom :as gdom]))

(defn text [el]
  (when el
    (gdom/getTextContent el)))

(defn tag [el]
  (when el
  (.. el -tagName toLowerCase)))

(deftest test-render-single-element
  (testing "rendering a simple element"
    (with-container el
      (v/attach! [:div "hello"] {} el)
      (is (= 1 (child-count el)))
      (is (= "div" (tag (child-at el 0))))
      (is (= "hello" (-> (child-at el 0)
                         text))))))

(deftest test-render-nested-elements
  (testing "rendering nested elements"
    (with-container el
      (v/attach!
        [:div
         [:p "paragraph1"]
         [:p "paragraph2"]]
        {}
        el)

      (is (= 1 (child-count el)))
      (let [div (child-at el 0)]
        (is (= "div" (tag div)))
        (is (= 2 (child-count div)))

        (is (= "paragraph1"
               (-> (child-at div 0)
                   text)))
        (is (= "paragraph2"
               (-> (child-at div 1)
                   text)))))))

(deftest test-render-with-fn
  (testing "rendering a function"
    (with-container el
      (v/attach!
        (fn [_]
          [:div
           [:p "paragraph1"]])
        {}
        el)
      (is (= 1 (child-count el)))
      (let [div (child-at el 0)]
        (is (= "div" (tag div)))
        (is (= 1 (child-count div)))

        (is (= "paragraph1"
               (-> (child-at div 0)
                   text)))))))

(deftest test-render-with-fn-using-state
  (testing "rendering a function using the state"
    (with-container el
      (v/attach!
        (fn [state]
          [:div (:text state)])
        {:text "Test Text"}
        el)
      (is (= 1 (child-count el)))
      (is (= "div" (tag (child-at el 0))))
      (is (= "Test Text"
             (-> (child-at el 0)
                 text))))))

(deftest test-render-with-nested-fn
  (testing "rendering a function nested in the tree"
    (with-container el
      (let [a-component (fn [state] [:div (:text state)])]
        (v/attach!
          [:div a-component]
          {:text "Component Text"}
          el)

        (is (= 1 (child-count el)))
        (let [div (child-at el 0)]
          (is (= "div" (tag div)))
          (is (= 1 (child-count div)))
          (is (= "div" (-> (child-at div 0) tag)))
          (is (= "Component Text"
                 (-> (child-at div 0)
                     text))))))))

(deftest test-render-with-state-updates
  (let [done identity] ; async done
    (testing "rendering a function using atom state"
      (with-container el
        (let [app-state (atom {:text "Test Text"})]
          (v/attach!
            (fn [state]
              [:div (:text state)])
            app-state
            el)
          (is (= 1 (child-count el)))
          (is (= "div" (tag (child-at el 0))))
          (is (= "Test Text"
                 (-> (child-at el 0)
                     text)))

          (swap! app-state assoc :text "Changed Text")
          (do ;wait
            (is (= "Changed Text"
                   (-> (child-at el 0)
                       text)))
            (done)))))))

(deftest test-render-with-state-updates-additive
  (let [done identity] ;async done
    (testing "rendering a function using additive state change"
      (with-container el
        (let [app-state (atom {:show-it? false})]
          (v/attach!
            (fn [state]
              [:ul
               [:li "always shown"]
               (when (:show-it? state) [:li "Now showing: additive"])])
            app-state
            el)

          (is (= 1 (child-count el)))
          (let [ul (child-at el 0)]
            (is (= 1 (child-count ul)))
            (is (= "always shown"
                   (-> (child-at ul 0)
                       text)))

            (swap! app-state assoc :show-it? true)
            (do; wait
              (is (= 2 (child-count ul)))
              (is (= "always shown"
                     (-> (child-at ul 0)
                         text)))
              (is (= "Now showing: additive"
                     (-> (child-at ul 1)
                         text)))
              (done))))))))

(deftest test-render-with-state-updates-reductive
  (let [done identity] ;async done
    (testing "rendering a function using a reductive state change"
      (with-container el
        (let [app-state (atom {:show-it? true})]
          (v/attach!
            (fn [state]
              [:ul
               [:li "always shown"]
               (when (:show-it? state) [:li "Now showing: reductive"])])
            app-state
            el)

          (is (= 1 (child-count el)))
          (let [ul (child-at el 0)]
            (is (=  (child-count ul)))
            (is (= "always shown"
                   (-> (child-at ul 0)
                       text)))
            (is (= "Now showing: reductive"
                   (-> (child-at ul 1)
                       text)))

            (swap! app-state assoc :show-it? false)
            (do; wait
              (is (= 1 (child-count ul)))
              (is (= "always shown"
                     (-> (child-at ul 0)
                         text)))
              (done))))))))

(deftest test-render-with-state-updates-insertive
  (let [done identity] ;async done
    (testing "rendering a function using a insertive state change"
      (with-container el
        (let [app-state (atom {:show-it? false})]
          (v/attach!
            (fn [state]
              [:ul
               (when (:show-it? state) [:li "Now showing: insertive"])
               [:li "always shown"]])
            app-state
            el)

          (is (= 1 (child-count el)))
          (let [ul (child-at el 0)]
            (is (= 1 (child-count ul)))
            (is (= "always shown"
                   (-> (child-at ul 0)
                       text)))

            (swap! app-state assoc :show-it? true)
            (do; wait
              (is (= 2 (child-count ul)))
              (is (= "Now showing: insertive"
                     (-> (child-at ul 0)
                         text)))
              (is (= "always shown"
                     (-> (child-at ul 1)
                         text)))
              (done))))))))

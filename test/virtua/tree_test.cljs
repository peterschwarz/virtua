(ns virtua.tree-test
  (:require [cljs.test :refer-macros [deftest testing async is are use-fixtures]]
            [virtua.core :as v]))


(deftest test-intermediate-state
  (testing "Intermediate state transformation"
    (are [x state y] (= (v/apply-state x state) y)
      ; primitives
      "String" {}
      "String"

      1 {}
      1

      ; empty node
      [:div] {}
      {:tag :div}

      ; children
      [:div "Foo"] {}
      {:tag :div :children ["Foo"]}

      ; multiple children
      [:div "foo" "bar"] {}
      {:tag :div :children ["foo" "bar"]}

      ; deep children
      [:div [:span "Foo"]] {}
      {:tag :div :children [{:tag :span
                             :children ["Foo"]}]}

      ; test lazy sequences
      [:div (range 1 4)] {}
      {:tag :div :children [1 2 3]}

      ; test state change
      [:div (fn [s] (:text s))] {:text "Test Text"}
      {:tag :div :children ["Test Text"]}

      ; Test attributes
      [:div {:class "foo"} "Test"] {}
      {:tag :div
       :attributes {:class "foo"}
       :children ["Test"]}

      ; Test nil removals
      ;[:div (fn [s] (:text s))] {}
      ;{:tag :div :children []}


      )))

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

      [:div [:p "one"] [:p "two"] ] {}
      {:tag :div :children [{:tag :p
                             :children ["one"]}
                            {:tag :p
                             :children ["two"]}]}

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

      ; test hiccup-like classes
      [:div.foo] {}
      {:tag :div
       :attributes {:class "foo"}}

      [:div.foo.bar] {}
      {:tag :div
       :attributes {:class "foo bar"}}

      [:div#foo] {}
      {:tag :div
       :attributes {:id "foo"}}

      [:div#foo.bar] {}
      {:tag :div
       :attributes {:id "foo" :class "bar"}}

      ; hiccup style with merge of params
      [:div.foo {:class "bar"}] {}
      {:tag :div
       :attributes {:class "foo bar"}}

      ; Preservs content
      [:div.foo "Content"] {}
      {:tag :div
       :attributes {:class "foo"}
       :children ["Content"]}

      )))

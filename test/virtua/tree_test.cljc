; Copyright 2016-2020 Peter Schwarz
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
  (:require [clojure.test :refer [deftest testing are]]
            [virtua.tree :as vtree]))

(defn apply-state [state decl]
  (->> decl vtree/->tree-seq (vtree/expand-tree-seq state)))

(deftest test-intermediate-state
  (testing "Intermediate state transformation"
    (are [x state y] (= (apply-state state x) y)
      ; primitives
      "String" nil
      '("String")

      1 nil
      '(1)

      ; empty node
      [:div] nil
      '([:div nil 0])

      ; children
      [:div "Foo"] {}
      '([:div nil 1] "Foo")

      ; multiple children
      [:div "foo" "bar"] {}
      '([:div nil 2] "foo" "bar")

      ; deep children
      [:div [:span "Foo"]] {}
      '([:div nil 1] [:span nil 1] "Foo")

      [:div [:p "one"] [:p "two"] ] {}
      '([:div nil 2] [:p nil 1] "one" [:p nil 1] "two")

      ; test lazy sequences
      [:div (range 1 4)] {}
      '([:div nil 1] [:virtua/seq nil 3] 1 2 3)

      ; test state change
      [:div (fn [s] (:text s))] {:text "Test Text"}
      '([:div nil 1] "Test Text")

      ; Test attributes
      [:div {:class "foo"} "Test"] {}
      '([:div {:class "foo"} 1] "Test")

      ; test hiccup-like classes
      [:div.foo] {}
      '([:div {:class "foo"} 0])

      [:div.foo.bar] {}
      '([:div {:class "foo bar"} 0])

      [:div#foo] {}
      '([:div {:id "foo"} 0])

      [:div#foo.bar] {}
      '([:div {:id "foo" :class "bar"} 0])

      ; hiccup style with merge of params
      [:div.foo {:class "bar"}] {}
      '([:div {:class "foo bar"} 0])

      ; Preserves content
      [:div.foo "Content"] {}
      '([:div {:class "foo"} 1] "Content")

      ; lists are walked correctly
      [[:div.a "one"] [:div.b "two"]] {}
      '([:virtua/seq nil 2] [:div {:class "a"} 1] "one" [:div {:class "b"} 1] "two")

      )))

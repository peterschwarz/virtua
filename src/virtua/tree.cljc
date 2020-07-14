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
(ns virtua.tree
  (:require [clojure.string :refer [join split]]))

(defn- merge-attr
  "Merges attributes, with appropriate joining of merge-able attributes.
  For example, two maps that define :class will merge their value.

  ```
  (let [a {:class \"foo\"}
        b {:class \"bar\"}]
    (merge-attr a b))

  => {:class \"foo bar\"}
  ```
  "
  [a b]
  (let [class-a (:class a)
        class-b (:class b)
        classes (cond
                  (and class-a class-b) (str class-a " " class-b)
                  class-a class-a
                  class-b class-b)]
    (cond-> (merge (dissoc a :class) (dissoc b :class))
      classes (assoc :class classes))))

(defn- extract-keyword-attr
  "Extracts hiccup-style attributes, namely css class and element ids."
  [node-type]
  (let [[element & classes] (split (name node-type) #"\.")
        [etype id] (split element #"#")]
    [(keyword etype)
     (cond-> nil
       (not (empty? classes)) (assoc :class (join " " classes))
       (not (empty? id)) (assoc :id id))]))

(defn tag?
  "return true if the given value is a tag element declaration.

  This is the format `[:tag ...]`"
  [v]
  (and (vector? v) (keyword? (first v))))

(defn markup-branch?
  [v]
  (or (tag? v)
      (sequential? v)
      (seq? v)))

(defn markup-children
  [node]
  (cond
    ; format [:tag ...]
    (tag? node)
    (let [children (subvec node 1)]
      (if (map? (first children))
        ; format [:tag {:attributes...}]
        (subvec children 1)
        children))

    :default
    node))

(defn- non-nil-nchildren
  [coll]
  (->> coll (filter (complement nil?)) count))

(defn markup-data
  [node]
  (cond
    (tag? node)
    (let [[tag tag-attr] (extract-keyword-attr (first node))]
      (if (map? (second node))
        ; format [:tag {:attributes...}]
        [tag (merge-attr tag-attr (second node)) (non-nil-nchildren (subvec node 2))]
        [tag tag-attr (non-nil-nchildren (subvec node 1))]))

    (or (sequential? node) (seq? node))
    [:virtua/seq nil (non-nil-nchildren node)]

    :default
    node))

(defn ->tree-seq
  [vdom]
  (->>
    vdom
    (tree-seq markup-branch? markup-children)
    (map (fn [node]
           (if (markup-branch? node)
             (markup-data node)
             node)))
    (filter (complement nil?))))

(defn expand-tree-seq
  [state t]
  (for [node t
        out (if (fn? node)
              (->> (node state)
                   ->tree-seq
                   (expand-tree-seq state))
              #?(:clj (cons node nil)
                 :cljs #js [node]))]
    out))

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
(ns virtua.render
  (:require [clojure.core.match :refer [match]]
            [virtua.dom :as dom]
            [virtua.events :as evts]
            [virtua.tree :refer [tag?]]))

(defn- create-element
  [node]
  (when node
    (if (tag? node)
      (let [[tag attributes _] node
            el (dom/create tag)]
        (dom/set-props el attributes)
        (evts/configure-event-handlers el attributes)
        el)
      (dom/create-text (str node)))))

(defn- child-count [left right same]
  (letfn [(local-count [node]
            (if (sequential? node)
              (last node)
              0))]
    (max
      (local-count left)
      (local-count right)
      (local-count same))))

(defn- operations
  [left right same]
  (match [left right same]
    ; no chnage
    [nil nil _] nil

    [nil new-el nil]
    [[::add new-el]]

    [old-el nil nil]
    [[::remove old-el]]

    [old-el new-el nil]
    [[::remove old-el]
     [::add new-el]]

    [([nil old-attr & more-old] :seq)
     ([nil new-attr & more-new] :seq)
     [tag same-attr _]]
    [[::change-attr old-attr new-attr]]

    :else nil))

(defn- apply-operations [f el index ops]
  (doseq [op ops]
    (let [effect (first op)]
      (case effect
        ::remove
        (let [child (dom/child-at el index)]
          (f [:virtua/remove (last op) child])
          (dom/remove! child))

        ::add
        (let [child (create-element (last op))]
          (f [:virtua/add (last op) child])
          (dom/insert-at el child index))

        ::change-attr
        (let [[_ remove-attrs add-attrs] op
              child (dom/child-at el index)]
          (dom/update-props child remove-attrs add-attrs)
          (evts/update-event-handlers child remove-attrs add-attrs))))))

(defn- is-pseudo-tag?
  [left right same]
  (letfn [(check [node]
            (when (vector? node)
              (= :virtua/seq (first node))))]
    (or (check left) (check right) (check same))))

(defn- render-step
  [f element-stack left right same]
  (if (is-pseudo-tag? left right same)
    ; pseudo tag, append the appropriate length to the items, but don't
    ; advance the index.
    (let [[parent nchildren index] (first element-stack)

          new-stack-top
          [parent (+ nchildren (child-count left right same) -1) index]]
      [(cons new-stack-top (rest element-stack)) 1])

    ; standard element
    (let [[parent nchildren index] (first element-stack)
          ops (operations left right same)
          _ (apply-operations f parent index ops)
          child (dom/child-at parent index)

          next-nchildren (child-count left right same)

          new-stack (cond->> (rest element-stack)
                      (> nchildren (inc index))
                      (cons [parent nchildren (inc index)])

                      (and child (> next-nchildren 0))
                      (cons [child next-nchildren 0]))

          advance (if (and (not child) (> next-nchildren 0))
                    ; drop old children, if the parent was dropped
                    next-nchildren
                    1)]
      [new-stack advance])))

(defn render
  ""
  ([parent dom-diff] (render parent identity dom-diff))
  ([parent f dom-diff]
   (loop [[left right same] dom-diff
          stack (cons [parent 1 0] nil)]
       (let [node-left (first left)
             node-right (first right)
             node-same (first same)]
         (when (and (or node-left node-right node-same)
                    (not (empty? stack)))
           (let [[new-stack advance]
                 (render-step f stack node-left node-right node-same)]
             (recur [(nthrest left advance)
                     (nthrest right advance)
                     (nthrest same advance)]
                     new-stack)))))))

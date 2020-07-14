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
(ns virtua.core
   (:require [clojure.data :refer [diff]]
             [virtua.dom :as dom]
             [virtua.events :as evts]
             [virtua.tree :as tree]
             [virtua.render :refer [render]]))

(defn- apply-state [state decl]
  (->> decl tree/->tree-seq (tree/expand-tree-seq state)))

(defn- update-render-state
  [render-state event]
  (let [[evt-type data el] event]
    (case evt-type
      :virtua/add
      (when-let [on-mount-fn (get-in data [1 :virtua/on-mount])]
        (swap! render-state update-in [::on-mount-fns] conj [on-mount-fn el]))

      :virtua/remove
      (when-let [on-unmount-fn (get-in data [1 :virtua/on-unmount])]
        (swap! render-state update-in [::on-unmount-fns] conj on-unmount-fn)))))

(defn- apply-tree-updates
  [el changes]
  (let [render-state (atom {})]
    (render el #(update-render-state render-state %) changes)

    ; Call the on-unmount fn's in order recevied.
    (doseq [f (-> @render-state ::on-unmount-fns reverse)]
      (f))

    ; Call the on-mount fn's in order received.
    (doseq [[f el] (-> @render-state
                       ::on-mount-fns
                       reverse)]
      (f el))))

(defn- update-tree
  "Update the DOM, using the given virtua tree based on the old state and the
  new state."
  [el decl old-state new-state]
  ; NOTE, this is pretty inefficient, and could presumably could be optimized
  ; by diffing the state, and then walking the virtua tree for effected
  ; elements.
  (let [changes (diff (apply-state old-state decl)
                      (apply-state new-state decl))
        [left right same] changes]

    (when (or left right)
      (apply-tree-updates el changes))))

(defn- wrap-state
  "Wraps the given state value in an Atom, if necessary."
  [value]
  (if (satisfies? IAtom value)
    value
    (atom value)))

(defn attach!
  "Attach a virtua component, with the given state value to a parent element.

  A virtua component is an function that take the current snapshot of the state
  value. It must return hiccup-style markup.

  For example:

  ```
  (let [app-state (atom {:count 0})
        parent (. js/document (getElementById \"app\"))]
    (attach!
      (fn [state]
        [:div#counter
         [:div#count (get state :count 0)]
         [:div#count-button
           [:button
            {:on-click #(swap! app-state update-in [:count] inc)}
            \"Count!\" ]]])
       app-state
       parent))
  ```

  The value may be an arbitrary data, though any non-Atom values will be wrapped
  in an atom. Though, in this case, it would be difficult to update the state,
  as no state modifier methods are provided."
  [virtua-component value parent]
  (let [state (wrap-state value)
        initial-node (apply-state @state virtua-component)]
    (dom/remove-children! parent)
    (add-watch state ::virtua-component
               (fn [_ _ old-state new-state]
                 ; TODO: This should probably use requestAnimationFrame
                 (update-tree parent virtua-component old-state new-state)))
    (apply-tree-updates parent [nil initial-node nil])))

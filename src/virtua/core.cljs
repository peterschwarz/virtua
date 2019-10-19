(ns virtua.core
   (:require [virtua.dom :as dom]
             [virtua.events :as evts]
             [clojure.string :refer [join]]
             [clojure.data :refer [diff]])
   (:import [goog.ui IdGenerator]))


(defn- primitive? [x]
  (or (string? x)
      (number? x)
      (= (type x) (type true))))

(defn- unique-id []
  (.. IdGenerator getInstance getNextUniqueId))

(defn- create-element
  [node]
  (when node
    (cond

      (primitive? node)
      (dom/create-text (str node))

      (map? node)
      (let [{:keys [tag attributes children]} node
            el (dom/create tag)]
        (when-not (:id attributes)
          (dom/set-attribute el "id" (unique-id)))
        (dom/set-props el attributes)
        (evts/configure-event-handlers el attributes)
        (->> children
             (map create-element)
             (apply dom/append el)))

      :default
      (dom/create-text (str node)))))

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
  (let [[element & classes] (.split (name node-type) ".")
        [etype id] (.split element "#")]
    [(keyword etype)
     (cond-> nil
       (not (empty? classes)) (assoc :class (join " " classes))
       (not (empty? id)) (assoc :id id))]))

(defn apply-state
  "Applies state to the given node in the virtual DOM.  The resulting node may
  contain new children, attributes, etc."
  [node state]
  (when node
    (cond
      (primitive? node)
      node

      (fn? node) (apply-state (node state) state)

      (vector? node)
      (let [[node-type & children] node
            [tag tag-attr] (extract-keyword-attr node-type)
            has-attr? (map? (first children))
            attr (if has-attr?
                   (merge-attr tag-attr (first children))
                   tag-attr)
            children (if has-attr? (rest children) children)]
        (cond-> {:tag tag}

          attr
          (assoc :attributes attr)

          (not (empty? children))
          (assoc :children (->> children
                                (map #(apply-state % state))
                                flatten
                                vec))))

      (coll? node)
      (map #(apply-state % state) node)

      :default
      node)))

(defn- changed?
  "Evaluate the two items to see if they have changed tag type or, if primitive,
  evaluate if the value has changed."
  [left right]
  (and left right
    (or (not= (:tag left) (:tag right))
        (and (primitive? left) (not= left right)))))

(defn- attr-changed?
  "Evaluate the two items to see if the attributes have changed."
  [left right]
  (and left right
       (map? left) (map? right)
       (not= (:attributes left) (:attributes right))))

(defn- update-elements
  "Update the elements based on the diff of left and right.  The changes are
  applied to the provided element, which would be considered their parent. The
  provided index would be the starting index within the given element's
  children."
  [el left right index]
  (cond
    (and (not left) right)
    (dom/insert-at el (create-element right) index)

    (and left (not right))
    (dom/remove! (dom/child-at el index))

    (changed? left right)
    (let [child (dom/child-at el index)]
      (dom/remove! child)
      (dom/insert-at el (create-element right) index))

    (:children right)
    (doseq [i (range (max (count (:children left))
                          (count (:children right))))]
      (update-elements
        (dom/child-at el index)
        (get-in left [:children i])
        (get-in right [:children i])
        i)))

  (when (attr-changed? left right)
    (let [[remove-attrs add-attrs _] (diff (:attributes left) (:attributes right))
          child (dom/child-at el index)]
      (dom/update-props child  remove-attrs add-attrs)
      (evts/update-event-handlers child remove-attrs add-attrs))))

(defn- update-tree
  "Update the DOM, using the given virtua tree based on the old state and the
  new state."
  [el virtua-tree old-state new-state]
  ; NOTE, this is pretty inefficient, and could presumably could be optimized
  ; by diffing the state, and then walking the virtua tree for effected
  ; elements.
  (let [before (apply-state virtua-tree old-state)
        after (apply-state virtua-tree new-state)
        [left right same] (diff before after)]
    (when (and left right)
      (update-elements el before after 0))))

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
        initial-node (apply-state virtua-component @state)]
    (dom/remove-children! parent)
    (add-watch state ::virtua-component
               (fn [_ _ old-state new-state]
                 ; TODO: This should probably use requestAnimationFrame
                 (update-tree parent virtua-component old-state new-state)))
    (dom/append
      parent
      (create-element initial-node))))

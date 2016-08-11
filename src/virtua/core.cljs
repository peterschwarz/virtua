(ns virtua.core
   (:require [virtua.dom :as dom]
             [virtua.events :as evts]
             #_[cljs.pprint :refer [pprint]]
             [clojure.string :refer [join]]
             [clojure.data :refer [diff]]))


(defn primitive? [x]
  (or (string? x)
      (number? x)
      (= (type x) (type true))))

(defn create-element
  [node]
  (when node
    (cond

      (primitive? node)
      (dom/create-text (str node))

      (map? node)
      (let [{:keys [tag attributes children]} node
            el (dom/create (name tag))]
        (dom/set-props el attributes)
        (evts/configure-event-handlers el attributes)
        (->> children
             (map create-element)
             (apply dom/append el)))

      :default
      (dom/create-text (str node)))))

(defn- merge-attr
  "Merges attributes, with approprate joining of mergeable attributes.
  For example, two maps that define :class will merge their value.

  (let [a {:class \"foo\"}
        b {:class \"bar\"}]
    (merge-attr a b))

  => {:class \"foo bar\"}
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

(defn apply-state [node state]
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
                                #_(remove nil?)
                                vec))))

      (coll? node)
      (map #(apply-state % state) node)

      :default
      node)))

(defn- changed? [left right]
  (and left right
    (or (not= (:tag left) (:tag right))
        (and (primitive? left) (not= left right)))))

(defn- update-elements
  [el left right index]
  (cond
    (and (not left) right)
    (dom/insert-at el (create-element right) index)

    (and left (not right))
    (dom/remove (dom/child-at el index))

    (changed? left right)
    (let [child (dom/child-at el index)]
      (dom/remove child)
      (dom/insert-at el (create-element right) index))

    (:children right)
    (doseq [i (range (max (count (:children left))
                          (count (:children right))))]
      (update-elements (dom/child-at el index)
                       (get-in left [:children i])
                       (get-in right [:children i])
                       i))))

(defn update-tree [el node old-state new-state]
  (let [before (apply-state node old-state)
        after (apply-state node new-state)
        [left right same] (diff before after)]
    (when (and left right)
      (update-elements el before after 0))))

(defn- wrap-state [value]
  (if (satisfies? IAtom value)
    value
    (atom value)))

(defn attach! [virtua-component value parent]
  (let [state (wrap-state value)
        initial-node (apply-state virtua-component @state)]
    (dom/remove-children parent)
    (add-watch state ::virtua-component
               (fn [_ _ old-state new-state]
                 ; TODO: This should probably use requestAnimationFrame
                 (update-tree parent virtua-component old-state new-state)))
    (dom/append
      parent
      (create-element initial-node))))

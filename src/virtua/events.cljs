(ns virtua.events
  (:require [goog.dom :as gdom]
            [goog.object :as obj]
            [goog.events :as evt])
  (:import [goog.events EventType]))

(def types
  {:on-click (.-CLICK EventType)
   :on-change (.-CHANGE EventType)
   :on-key-up (.-KEYUP EventType)
   :on-key-down (.-KEYDOWN EventType)
   :on-key-press (.-KEYPRESS EventType)
   :on-mouse-down (.-MOUSEDOWN EventType)
   :on-mouse-enter (.-MOUSEENTER EventType)
   :on-mouse-leave (.-MOUSELEAVE EventType)
   :on-mouse-move (.-MOUSEMOVE EventType)
   :on-mouse-out (.-MOUSEOUT EventType)
   :on-mouse-over (.-MOUSEOVER EventType)
   :on-mouse-up (.-MOUSEUP EventType)
   ; TODO: More
   })

(defrecord Handler [key handler])

(defn configure-event-handlers
  "Configures event handlers for an object"
  [el attributes]
  (doseq [[event handler-fn] attributes]
    (when (types event)
      (let [handler-key (evt/listen el (types event) handler-fn)]
        (obj/set el (name event) (Handler. handler-key handler-fn))))))


(defn remove-event-handlers
  [el attributes-to-remove]
  (doseq [event (->> (keys attributes-to-remove)
                           (filter types)
                           (map name))]
    (when-let [handler-info (obj/get el event)]
      (evt/unlistenByKey (.-key handler-info))
      (obj/remove el event))))

(defn update-event-handlers
  [el attributes-to-remove attributes-to-add]
  (remove-event-handlers el attributes-to-remove)
  (configure-event-handlers el attributes-to-add))

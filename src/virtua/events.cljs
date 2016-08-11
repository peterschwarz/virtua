(ns virtua.events
  (:require [goog.dom :as gdom]
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

(defn configure-event-handlers
  "Configures event handlers for an object"
  [el attributes]
  (doseq [[event handler] attributes]
    (when (types event)
      (evt/listen el (types event) handler))))


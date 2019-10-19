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
(ns virtua.events
  (:require [goog.dom :as gdom]
            [goog.object :as obj]
            [goog.events :as evt])
  (:import [goog.events EventType]))

(def ^:private types
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

(defrecord ^:private Handler [key handler])

(defn configure-event-handlers
  "Configures event handlers for an object, using the attribute map provided.

  Any attributes in the map that are not event handlers will be ignored."
  [el attributes]
  (doseq [[event handler-fn] attributes]
    (when (types event)
      (let [handler-key (evt/listen el (types event) handler-fn)]
        (obj/set el (name event) (Handler. handler-key handler-fn))))))


(defn remove-event-handlers
  "Remove the event handlers based on the attribute map provided.

  Any attributes in the map that are not event handlers will be ignored."
  [el attributes-to-remove]
  (doseq [event (->> (keys attributes-to-remove)
                           (filter types)
                           (map name))]
    (when-let [handler-info (obj/get el event)]
      (evt/unlistenByKey (.-key handler-info))
      (obj/remove el event))))

(defn update-event-handlers
  "Update the event handlers based on the attribute maps provided. The first
  map contains the event handlers to remove.  The second map should contain the
  event handlers to add.

  Any attributes in the map that are not event handlers will be ignored."
  [el attributes-to-remove attributes-to-add]
  (remove-event-handlers el attributes-to-remove)
  (configure-event-handlers el attributes-to-add))

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
(ns virtua.test-utils)

(defmacro wait
  [& forms]
  (let [has-millis? (number? (first forms))
        millis (if has-millis? (first forms) 100)
        forms (if has-millis? (rest forms) forms)]
    `(js/setTimeout
       (fn []
         ~@forms)
       ~millis)))


(defmacro with-container
  [container & forms]
  `(let [~container (virtua.test-utils/new-container! ~(str (gensym "test-container")))]
     (try
     ~@forms
     (finally
      (virtua.test-utils/remove-container! ~container)))))

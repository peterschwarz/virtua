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

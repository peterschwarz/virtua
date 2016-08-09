(ns ^:figwheel-always virtua.test-suite
  (:require [cljs.test :as test
             :refer [report]
             :refer-macros [run-tests]]
            [figwheel.client :as fw]
            [cljs.pprint :as pprint]
            [goog.dom :as gdom]
            ; add tests after
            virtua.core-test
            virtua.tree-test))

(enable-console-print!)

(defonce test-state (atom {}))

(defn ^:export run []
  (println "Running tests...")
  (swap! test-state assoc :run-summary nil :run-results [])
  (run-tests
    ; add tests to run here
    'virtua.core-test
    'virtua.tree-test))

(defn color-favicon-data-url [color]
  (let [cvs (.createElement js/document "canvas")]
    (set! (.-width cvs) 16)
    (set! (.-height cvs) 16)
    (let [ctx (.getContext cvs "2d")]
      (set! (.-fillStyle ctx) color)
      (.fillRect ctx 0 0 16 16))
    (.toDataURL cvs)))

(defn change-favicon-to-color [color]
  (let [icon (.getElementById js/document "favicon")]
    (set! (.-href icon) (color-favicon-data-url color))))

(defn- format-summary-title [{:keys [test pass fail error]}]
 (str "Ran " test " tests containing "
      (+ pass fail error)
      " assertions."))

(defn- format-summary-failures [{:keys [fail error]}]
  (str fail " failures, "
       error " errors."))

(defmethod report [:cljs.test/default :summary] [m]
  (swap! test-state assoc :run-summary m )
  (println)
  (println (format-summary-title m))
  (println (format-summary-failures m))
  (if (< 0 (+ (:fail m) (:error m)))
    (change-favicon-to-color "#d00")
    (change-favicon-to-color "#0d0"))) ;;<<-- change color

(defmethod report [:cljs.test/default :begin-test-ns] [m]
  (let [report-map (-> m
                       (select-keys [:ns])
                       (assoc :failures []))]
    (println "\nTesting " (:ns report-map))
    (swap! test-state update-in [:run-results]
           #(conj % report-map))))

(defn- print-comparison [m]
  (let [formatter-fn (or (:formatter (test/get-current-env)) pr-str)]
    (println "expected:" (formatter-fn (:expected m)))
    (println "  actual:" (formatter-fn (:actual m)))))

(defn- last-index [l]
  (dec (count l)))

(def fail-label {:fail "FAIL" :error "ERROR"})

(defn- report-failure [fail-type m]
  (test/inc-report-counter! :fail)
  (println "\n" (fail-label fail-type) "in" (test/testing-vars-str m))
  (when (seq (:testing-contexts (test/get-current-env)))
    (println (test/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (print-comparison m)
  (swap! test-state update-in [:run-results (last-index (get @test-state :run-results)) :failures]
         #(conj % {:type fail-type
                   :location (test/testing-vars-str m)
                   :context (test/testing-contexts-str)
                   :message (:message m)
                   :expected (:expected m)
                   :actual (:actual m)})))

(defmethod report [:cljs.test/default :fail] [m]
  (report-failure :fail m))

(defmethod report [:cljs.test/default :error] [m]
  (report-failure :error m))

(defmethod report [:cljs.test/default :begin-test-var] [m]
  (println "\t" (test/testing-vars-str m)))

; Connect to figwheel to run the tests on updates
(fw/start {:websocket-url "ws://localhost:4000/figwheel-ws"
           :build-id "test"
           :on-jsload run})

(run)

(defproject virtua "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]]

  :plugins [[lein-figwheel "0.5.4-7"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]
  :test-paths []

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "resources/public/js/test"
                                    "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src" "dev"]

                :figwheel {:on-jsload "virtua.core/on-js-reload"}

                :compiler {:main virtua.main
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/virtua.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true}}

               {:id "test"
                :source-paths ["src" "test"]

                :figwheel {}
                :compiler {:main virtua.test-suite
                           :asset-path "js/test/out"
                           :output-to "resources/public/js/test/test.js"
                           :output-dir "resources/public/js/test/out"
                           :source-map-timestamp true }}

               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/virtua.js"
                           :main virtua.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             :server-port 4000
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             }


  ;; setting up nREPL for Figwheel and ClojureScript dev
  ;; Please see:
  ;; https://github.com/bhauman/lein-figwheel/wiki/Using-the-Figwheel-REPL-within-NRepl


  :profiles {:dev {:dependencies [[figwheel-sidecar "0.5.4-7"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {; for nREPL dev you really need to limit output
                                  :init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

)

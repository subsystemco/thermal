(defproject thermal "0.1.0-SNAPSHOT"
  :description "A generator for receipts as returned by Apple's StoreKit APIs and verifyReceipt endpoints."
  :url "https://github.com/leppert/thermal"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript  "1.9.293"]
                 [com.cemerick/piggieback    "0.2.1"]
                 [clj-time "0.12.0"]
                 [com.andrewmcveigh/cljs-time "0.5.0-alpha2"]
                 [org.clojure/data.codec "0.1.0"]
                 [cheshire "5.6.3"]
                 [apple-receipt "0.1.0-SNAPSHOT"]]
  :plugins [[s3-wagon-private "1.2.0"]
            [lein-cljsbuild "1.1.3"]
            [lein-npm       "0.6.2"]
            [lein-doo       "0.1.7"]]
  :npm {:dependencies [[source-map-support "0.4.0"]]}

  :repositories {"snapshots" {:url "s3p://libs.subsystem.co/snapshots"
                              :username :env/aws_libs_access_key
                              :passphrase :env/aws_libs_secret_key}
                 "releases" {:url "s3p://libs.subsystem.co/releases"
                             :username :env/aws_libs_access_key
                             :passphrase :env/aws_libs_secret_key}}

  :doo {:build "test"
        :alias {:default [:node]}}

  :cljsbuild
  {:builds {:production {:source-paths ["src"]
                         :compiler {:output-to     "target/thermal/thermal.js"
                                    :output-dir    "target/thermal"
                                    :source-map    "target/thermal/thermal.js.map"
                                    :target        :nodejs
                                    :language-in   :ecmascript5
                                    :optimizations :advanced
                                    }}
            :test {:source-paths ["src" "test"]
                   :compiler {:output-to     "target/thermal-test/thermal.js"
                              :output-dir    "target/thermal-test"
                              :target        :nodejs
                              :language-in   :ecmascript5
                              :optimizations :none
                              :main          thermal.test-runner}}}}
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})

(defproject thermal "0.1.0-SNAPSHOT"
  :description "A generator for receipts as returned by Apple's StoreKit APIs and verifyReceipt endpoints."
  :url "https://github.com/leppert/thermal"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-time "0.12.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/data.json "0.2.6"]
                 [apple-receipt "0.1.0-SNAPSHOT"]]
  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"snapshots" {:url "s3p://libs.subsystem.co/snapshots"
                              :username :env/aws_libs_access_key
                              :passphrase :env/aws_libs_secret_key}
                 "releases" {:url "s3p://libs.subsystem.co/releases"
                             :username :env/aws_libs_access_key
                             :passphrase :env/aws_libs_secret_key}})

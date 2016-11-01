(ns thermal.utils
  (:require #?@(:clj  [[cheshire.core :as che]
                       [clojure.data.codec.base64 :as b64]]
                :cljs [[goog.crypt.base64 :as b64]])))

(defn to-json
  [m]
  #?(:clj  (che/generate-string m)
     :cljs (.stringify js/JSON (clj->js m))))

(defn string->base64 [input]
  "Encodes an UTF-8 string into a base64 UTF-8 string"
  #?(:clj  (String. (b64/encode (.getBytes input)) "UTF-8")
     :cljs (b64/encodeString input)))

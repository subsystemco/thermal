(ns thermal.test-runner
 (:require [doo.runner :refer-macros [doo-tests]]
           [thermal.core-test]
           [cljs.nodejs :as nodejs]))

(try
  (.install (nodejs/require "source-map-support"))
  (catch :default _))

(doo-tests
 'thermal.core-test)

(ns thermal.apple-test
  (:require [thermal.apple :refer [response]]
            #?@(:clj  [[clojure.test :refer :all]
                       [clj-time.core :as t]]
                :cljs [[cljs.test :refer-macros [deftest is testing]]
                       [cljs-time.core :as t]])))

(deftest generates-receipts
  (testing "generates an active monthly subscription receipt"
    (response {:product "com.subsystem.subscription.monthly"
               :plan_duration (t/months 1)
               :start_date (t/date-time 2016 8 14 4 3 27 456)}))

  (testing "generates a monthly subscription with a trial receipt"
    (response {:product "com.subsystem.subscription.monthly"
               :plan_duration (t/months 1)
               :start_date (t/date-time 2016 8 14 4 3 27 456)
               :trialed true}))

  (testing "generates an expired monthly subscription receipt"
    (response {:product "com.subsystem.subscription.monthly"
               :plan_duration (t/months 1)
               :start_date (t/minus (t/now) (t/months 3))
               :end_date (t/minus (t/now) (t/months 1))}))

  (testing "generates a cancelled monthly subscription receipt"
    (response {:product "com.subsystem.subscription.monthly"
               :plan_duration (t/months 1)
               :start_date (t/minus (t/now) (t/months 3))
               :end_date (t/minus (t/now) (t/months 1))
               :canceled true}))

  (testing "generates a receipt with a plan change"
    (response {:product "com.subsystem.subscription.monthly"
               :plan_duration (t/months 1)
               :start_date (t/plus (t/now) (t/days 1) (t/years -1) (t/months -6))}
              {:product "com.subsystem.subscription.yearly"
               :plan_duration (t/years 1)
               :start_date (t/plus (t/now) (t/days 1) (t/years -1) (t/months -3))})))

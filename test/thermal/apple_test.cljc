(ns thermal.apple-test
  (:require [thermal.apple :refer [response]]
            [thermal.test-utils :refer [rfc3339-date?]]
            #?@(:clj  [[clojure.test :refer :all]
                       [clj-time.core :as t]
                       [clj-time.coerce :as c]]
                :cljs [[cljs.test :refer-macros [deftest is testing]]
                       [cljs-time.core :as t]
                       [cljs-time.coerce :as c]])))

(defn in-same-sec
  [ts1 ts2]
  (= (subs (str ts1) 0 10)
     (subs (str ts2) 0 10)))

(deftest defaults
  (testing "sets general defaults"
    (let [resp (response {:product "com.subsystem.subscription.monthly"
                          :plan_duration (t/months 1)
                          :start_date (t/now)})
          receipt (:receipt resp)]
      (is (= (:status resp) 0))
      (is (= (:adam_id receipt) 0))
      (is (= (:app_item_id receipt) 0))
      (is (= (:application_version receipt) "1"))
      (is (= (:download_id receipt) 0))
      (is (= (:version_external_identifier receipt) 0))
      (is (rfc3339-date? (:request_date receipt) "Etc/GMT"))
      (is (in-same-sec (:request_date_ms receipt) (c/to-long (t/now))))
      (is (rfc3339-date? (:request_date_pst receipt) "America/Los_Angeles"))
      (is (= (:original_application_version receipt) "1.0"))))

  (testing "sets sandbox specific defaults"
    (let [resp (response {:product "com.subsystem.subscription.monthly"
                          :plan_duration (t/months 1)
                          :start_date (t/now)})
          receipt (:receipt resp)]
      (is (= (:environment resp) "Sandbox"))
      (is (= (:receipt_type receipt) "ProductionSandbox"))
      (is (= (:original_purchase_date receipt) "2013-08-01 07:00:00 Etc/GMT"))
      (is (= (:original_purchase_date_ms receipt) "1375340400000"))
      (is (= (:original_purchase_date_pst receipt) "2013-08-01 00:00:00 America/Los_Angeles")))))

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
               :start_date (t/plus (t/now) (t/days 1) (t/years -1) (t/months -3))}))

  (testing "sets request_date to the current time"
    (let [resp (response {:product "com.subsystem.subscription.monthly"
                          :plan_duration (t/months 1)
                          :start_date (t/date-time 2016 8 14 4 3 27 456)})]
      (is (in-same-sec (get-in resp [:receipt :request_date_ms])
                       (c/to-long (t/now))))))

  (testing "changes latest_receipt based on current time"
    (let [resp1 (response {:product "com.subsystem.subscription.monthly"
                           :plan_duration (t/months 1)
                           :start_date (t/date-time 2016 8 14 4 3 27 456)})
          resp2 (response {:product "com.subsystem.subscription.monthly"
                           :plan_duration (t/months 1)
                           :start_date (t/date-time 2016 8 14 4 3 27 456)})]
      (is (not= (:latest_receipt resp2) (:latest_receipt resp1))))))

(ns thermal.apple
  (:require [thermal.utils :as utils]
            [apple-receipt.record :as record]
            [apple-receipt.status-code :as status-code]
            #?@(:clj  [[clj-time.core :as t]
                       [clj-time.coerce :as c]
                       [clj-time.local :as l]
                       [clj-time.periodic :as p]]
                :cljs [[cljs.nodejs :as nodejs]
                       [cljs-time.core :as t]
                       [cljs-time.coerce :as c]
                       [cljs-time.local :as l]
                       [cljs-time.periodic :as p]
                       [cljs-time.extend :as extend]]))
  (:import #?(:cljs goog.i18n.TimeZone)))

#?(:cljs (def ^:private tz (nodejs/require "timezone/loaded")))

(def ENVIRONMENT "Sandbox")
(def BUNDLE-ID "test-bundle-id")
(def PRODUCT-ID "test-product-id")

(defn transaction-id
  "Generate a transaction id from a date."
  [date]
  (str "1000000" (subs (str (c/to-epoch date)) 1)))

(defn web-order-line-item-id
  "Generate a web order line item id from a date."
  [date]
  (str "10000000" (subs (str (c/to-epoch date)) 2)))

(defn time-zone-for-id
  [id]
  #?(:clj  (t/time-zone-for-id id)
     :cljs (.createTimeZone goog.i18n.TimeZone (js-obj "id" id))))

(defn format-date
  [date zone]
  #?(:clj  (l/format-local-time (t/to-time-zone date) :mysql)
     :cljs (tz date "%F %T" "en_US" (.getTimeZoneId zone))))

(def gmt-tz-id "Etc/GMT")
(def gmt-tz (time-zone-for-id gmt-tz-id))
(def la-tz-id "America/Los_Angeles")
(def la-tz (time-zone-for-id la-tz-id))
(def original-purchase-date #?(:clj  (t/from-time-zone (t/date-time 2013 8 1 7) gmt-tz)
                               :cljs (t/date-time 2013 8 1 7)))

(defn dates
  "Return a map of dates in Apple's three formats: timestamp, GMT, and PST."
  [prop-name date]
  (let [prop-name (name prop-name)]
    {(keyword (str prop-name "_date")) (str (format-date date gmt-tz) " " gmt-tz-id)
     (keyword (str prop-name "_date_ms")) (str (c/to-epoch date))
     (keyword (str prop-name "_date_pst")) (str (format-date date la-tz) " " la-tz-id)}))

(defn iap
  "Generate an In App Purchase receipt."
  [{:keys [product-id duration is-trial-period
           purchase-date original-purchase-date
           cancellation-date]}]
  (record/map->IAPReceipt
   (merge
    {:quantity "1"
     :product_id product-id
     :transaction_id (transaction-id purchase-date)
     :original_transaction_id (transaction-id original-purchase-date)
     :web_order_line_item_id (web-order-line-item-id purchase-date)
     :is_trial_period (if is-trial-period "true" "false")}
    (dates :purchase purchase-date)
    (dates :original_purchase original-purchase-date)
    (dates :expires (t/plus purchase-date duration))
    (if cancellation-date (dates :cancellation cancellation-date)))))

(defn receipt
  "Generate an app receipt."
  [date iaps]
  (record/map->AppReceipt
   (merge
    {:receipt_type "ProductionSandbox"
     :adam_id 0
     :app_item_id 0
     :bundle_id BUNDLE-ID
     :application_version "12345"
     :download_id 0
     :version_external_identifier 0
     :original_application_version "1.0"
     :in_app iaps}
    (dates :original_purchase original-purchase-date)
    (dates :receipt_creation date)
    (dates :request (t/now)))))

(defn response
  "Generate an Apple receipt validation response."
  [& subs]
  (let [now (t/now)
        defaults {:product PRODUCT-ID
                  :canceled false
                  :trialed false
                  :plan_duration (t/months 1)
                  :start_date now}
        subs (->> subs
                  (sort-by :start_date)
                  (#(concat % (list {})))
                  (partition 2 1)
                  (map
                   #(merge (assoc defaults :end_date (:start_date (last %) now)) (first %))))
        iaps (->> subs
                  (mapcat (fn [sub]
                            (for [date (p/periodic-seq (:start_date sub) (:plan_duration sub))
                                  :while (t/after? (:end_date sub) date)]
                              (iap {:product-id (:product sub)
                                    :duration (:plan_duration sub)
                                    :is-trial-period (and (:trialed sub) (= date (:start_date sub)))
                                    :purchase-date date
                                    :original-purchase-date (:start_date sub)
                                    :cancellation-date (let [end (t/plus date (:plan_duration sub))]
                                                         (if (and (:canceled sub)
                                                                  (not (t/after? (:end_date sub) end)))
                                                           end))})))))]

    (record/api-json->Response
     {:status status-code/success
      :environment ENVIRONMENT
      :receipt (receipt (-> subs first :start_date) iaps)
      :latest_receipt_info iaps
      :latest_receipt (utils/string->base64 (utils/to-json iaps))})))

(defn scratch []
  (.toLocaleString (js/Date.) "en-US" (js-obj "timeZone" "Pacific/Chatham"))
  (tz "2012-01-01")
  (tz (tz "2012-01-01"), "%c", "fr_FR", "America/Montreal")
  (.toLocaleString (t/now) "en-US" (js-obj "timeZone" "Pacific/Chatham"))
  (tz (t/now), "%c", "en_US", "Pacific/Chatham")
  )

(ns thermal.apple
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.local :as l]))

(def BUNDLE_ID "test-bundle-id")

(defn transaction-id
  "Generate a transaction id from a date."
  [date]
  (str "1000000" (subs (str (c/to-epoch date)) 1)))

(defn web-order-line-item-id
  "Generate a web order line item id from a date."
  [date]
  (str "10000000" (subs (str (c/to-epoch date)) 2)))

(defn format-date
  [date]
  (l/format-local-time date :mysql))

(def gmt-tz-id "Etc/GMT")
(def gmt-tz (t/time-zone-for-id gmt-tz-id))
(def la-tz-id "America/Los_Angeles")
(def la-tz (t/time-zone-for-id la-tz-id))

(defn dates
  "Return a map of dates in Apple's three formats: timestamp, GMT, and PST."
  [prop-name date]
  (let [prop-name (name prop-name)]
    {(keyword (str prop-name "_date")) (str (format-date (t/to-time-zone date gmt-tz)) " " gmt-tz-id)
     (keyword (str prop-name "_date_ms")) (str (c/to-epoch date))
     (keyword (str prop-name "_date_pst")) (str (format-date (t/to-time-zone date la-tz)) " " la-tz-id)}))

(defn iap
  "Generate an In App Purchase receipt."
  ([product duration date org-date]
   (iap product duration date org-date false))
  ([product duration date org-date trial]
   (merge
     {:quantity "1"
      :product_id product
      :transaction_id (transaction-id date)
      :original_transaction_id (transaction-id org-date)
      :web_order_line_item_id (web-order-line-item-id date)
      :is_trial_period (str trial)}
     (dates :purchase date)
     (dates :original_purchase org-date)
     (dates :expires (t/plus date duration)))))

(defn receipt
  "Generate an app receipt."
  [date iaps]
  (merge
    {:receipt_type "ProductionSandbox"
     :adam_id 0
     :app_item_id 0
     :bundle_id BUNDLE_ID
     :application_version "12345"
     :download_id 0
     :version_external_identifier 0
     :original_application_version "1.0"
     :in_app iaps}
    ;; original_purchase is always the same date
    (dates :original_purchase (t/from-time-zone (t/date-time 2013 8 1 7) gmt-tz))
    (dates :receipt_creation date)
    (dates :request (t/now))))

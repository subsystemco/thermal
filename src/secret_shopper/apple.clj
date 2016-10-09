(ns secret-shopper.apple
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clj-time.local :as l]))

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

(defn dates
  "Return a map of dates in Apple's three formats: timestamp, GMT, and PST."
  [prop-name date]
  (let [prop-name (name prop-name)
        gmt-tz-id "Etc/GMT"
        gmt-tz (t/time-zone-for-id gmt-tz-id)
        la-tz-id "America/Los_Angeles"
        la-tz (t/time-zone-for-id la-tz-id)]
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

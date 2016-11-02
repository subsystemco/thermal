(ns thermal.test-utils)

(defn rfc3339-date?
  [s zone]
  (not (= nil (re-pattern (str "\\d{4}(-\\d\\d){2} \\d{2}(:\\d{2}){2} " zone)))))

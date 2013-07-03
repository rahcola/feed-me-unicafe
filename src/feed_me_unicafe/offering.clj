(ns feed-me-unicafe.offering
  (:require [feed-me-unicafe.food :as f])
  (:require [feed-me-unicafe.restaurant :as r]))

(defrecord AOffering [food restaurant time])

(defn by-restaurants
  [restaurant-keys]
  (fn [{:keys [restaurant]}]
    (contains? (set restaurant-keys)
               (:key restaurant))))

(defn ^{:private true}
  today! []
  (let [c (doto (java.util.Calendar/getInstance)
            (.set java.util.Calendar/HOUR_OF_DAY 0)
            (.set java.util.Calendar/MINUTE 0)
            (.set java.util.Calendar/SECOND 0)
            (.set java.util.Calendar/MILLISECOND 0))]
    (.getTime c)))

(def today
  (fn [{:keys [time]}]
    (= time (today!))))

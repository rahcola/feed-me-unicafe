(ns feed-me-unicafe.core
  (:require [feed-me-unicafe.menu-parser :as p]))

(defn is [x]
  (fn [y] (= x y)))

(defn subset [sub]
  (fn [super]
    (clojure.set/subset? sub super)))

(defn since [date]
  (fn [date']
    (>= (compare date' date) 0)))

(defn today! []
  (let [c (doto (java.util.Calendar/getInstance)
            (.set java.util.Calendar/HOUR_OF_DAY 0)
            (.set java.util.Calendar/MINUTE 0)
            (.set java.util.Calendar/SECOND 0)
            (.set java.util.Calendar/MILLISECOND 0))]
    (.getTime c)))

(defn select-by-map [map offerings]
  (clojure.set/select (fn [offering]
                        (reduce (fn [b [key pred]]
                                  (and b (pred (get offering key))))
                                true
                                map))
                      offerings))

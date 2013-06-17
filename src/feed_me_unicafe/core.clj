(ns feed-me-unicafe.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [clojure.data.json :as json])
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

(extend-type java.util.Date
  json/JSONWriter
  (-write [date out]
    (let [date-format (doto (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ssZ")
                        (.setTimeZone (java.util.TimeZone/getTimeZone "UTC")))]
      (.write out (str "\"" (.format date-format date) "\"")))))

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (p/menus! :finnish) :escape-unicode false :escape-slash false)})

(defonce server (jetty/run-jetty #'handler {:port 8080 :join? false}))

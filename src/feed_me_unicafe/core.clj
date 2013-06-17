(ns feed-me-unicafe.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [ring.middleware.params :as params])
  (:require [clojure.data.json :as json])
  (:require [feed-me-unicafe.menu-parser :as p]))

(defn is [x]
  (fn [y] (= x y)))

(def any (constantly true))

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

(defn ensure-coll [x]
  (if (coll? x) x [x]))

(defn restaurant-pred [restaurants]
  (set (map keyword (ensure-coll restaurants))))

(defn build-select [params]
  {:restaurant (if (contains? params "restaurant")
                 (restaurant-pred (get params "restaurant"))
                 any)
   :date (if (contains? params "today")
           (is (today!))
           any)})

(defn project-menus [menus params]
  (if (contains? params "project")
    (let [keys (ensure-coll (get params "project"))]
      (clojure.set/project menus (map keyword keys)))
    menus))

(def cache
  (atom {:fetched (new java.util.Date)
         :menus (p/menus! :finnish)}))

(defn time-delta [from to]
  (- (.getTime to) (.getTime from)))

(defn update-cache! [cache]
  (if (> (time-delta (:fetched @cache) (new java.util.Date)) 600000)
    (swap! cache (fn [_] {:menus (p/menus! :finnish)
                          :fetched (new java.util.Date)}))))

(def handler
  (params/wrap-params
   (fn [{:keys [params]}]
     (update-cache! cache)
     (let [menus (:menus @cache)
           selected-menus (select-by-map (build-select params) menus)
           projected-menus (project-menus selected-menus params)]
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (str (json/write-str projected-menus
                                   :escape-unicode false
                                   :escape-slash false))}))))

(defonce server (jetty/run-jetty #'handler {:port 8080 :join? false}))

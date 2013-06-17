(ns feed-me-unicafe.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [ring.middleware.params :as params])
  (:require [clojure.data.json :as json])
  (:require [feed-me-unicafe.menu-parser :as p]))

(defn today! []
  (let [c (doto (java.util.Calendar/getInstance)
            (.set java.util.Calendar/HOUR_OF_DAY 0)
            (.set java.util.Calendar/MINUTE 0)
            (.set java.util.Calendar/SECOND 0)
            (.set java.util.Calendar/MILLISECOND 0))]
    (.getTime c)))

(extend-type java.util.Date
  json/JSONWriter
  (-write [date out]
    (let [df (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ssZ")
          utc-tz (java.util.TimeZone/getTimeZone "UTC")]
      (.setTimeZone df utc-tz)
      (.write out (str "\"" (.format df date) "\"")))))

(defn ensure-coll [x]
  (if (coll? x) x [x]))

(defn pred-and
  ([pred pred']
     (fn [x] (and (pred x) (pred' x))))
  ([pred pred' & preds]
     (reduce pred-and (pred-and pred pred') preds)))

(defn restaurant-pred [params]
  (if (contains? params "restaurant")
    (let [r-set (set (map keyword (ensure-coll (get params "restaurant"))))]
      (fn [{:keys [restaurant]}]
        (r-set restaurant)))
    (constantly true)))

(defn date-pred [params]
  (let [today (today!)]
    (if (contains? params "today")
      (fn [{:keys [date]}]
        (= date today))
      (fn [{:keys [date]}]
        (>= (compare date today) 0)))))

(defn select-menus [menus params]
  (clojure.set/select (pred-and (restaurant-pred params)
                                (date-pred params))
                      menus))

(defn project-menus [menus params]
  (if (contains? params "project")
    (let [keys (ensure-coll (get params "project"))]
      (clojure.set/project menus (map keyword keys)))
    menus))

(defn distance [location location']
  (let [latitude-delta (- (:latitude location) (:latitude location'))
        longitude-delta (- (:longitude location) (:longitude location'))]
    (java.lang.Math/sqrt (+ (* latitude-delta latitude-delta)
                            (* longitude-delta longitude-delta)))))

(defn sort-menus [menus params]
  (if (and (contains? params "latitude")
           (contains? params "longitude"))
    (let [query-location {:latitude (new Double (get params "latitude"))
                          :longitude (new Double (get params "longitude"))}]
      (map (fn [{:keys [location] :as menu-item}]
             (assoc menu-item :distance
                    (distance query-location location)))
           menus))
    menus))

(def cache
  (atom {:fetched (new java.util.Date)
         :menus (p/menus! :finnish)}))

(defn time-delta [from to]
  (- (.getTime to) (.getTime from)))

(defn update-cache! [cache]
  (if (> (time-delta (:fetched @cache) (new java.util.Date)) 600000)
    (swap! cache (constantly {:menus (p/menus! :finnish)
                              :fetched (new java.util.Date)}))))

(def foo (atom nil))

(def handler
  (params/wrap-params
   (fn [{:keys [params]}]
     (update-cache! cache)
     (let [menus (-> (:menus @cache)
                     (select-menus params)
                     (project-menus params)
                     (sort-menus params))]
       (swap! foo (constantly params))
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/write-str menus
                              :escape-unicode false
                              :escape-slash false)}))))

(defonce server (jetty/run-jetty #'handler {:port 8080 :join? false}))

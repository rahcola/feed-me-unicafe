(ns feed-me-unicafe.core
  (:require [ring.adapter.jetty :as jetty])
  (:require [ring.middleware.params :as params])
  (:require [clojure.data.json :as json])
  (:require [feed-me-unicafe.offering :as o])
  (:require [feed-me-unicafe.menu-parser :as p]))

(extend-type java.util.Date
  json/JSONWriter
  (-write [date out]
    (let [df (new java.text.SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ssZ")
          utc-tz (java.util.TimeZone/getTimeZone "UTC")]
      (.setTimeZone df utc-tz)
      (.write out (str "\"" (.format df date) "\"")))))

(defn ensure-coll [x]
  (if (coll? x) x [x]))

(defn restaurant? [params]
  (if-let [restaurants (get params "restaurant")]
    (o/by-restaurants (set (map keyword (ensure-coll restaurants))))
    (constantly true)))

(defn today? [params]
  (if (contains? params "today") o/today (constantly true)))

(def cache
  (atom {:fetched (new java.util.Date)
         :menus (p/parse-page (p/page! :finnish))}))

(defn time-delta [from to]
  (- (.getTime to) (.getTime from)))

(defn update-cache! [cache]
  (if (> (time-delta (:fetched @cache) (new java.util.Date)) 600000)
    (swap! cache (constantly {:menus (p/parse-page (p/page! :finnish))
                              :fetched (new java.util.Date)}))))

(def handler
  (params/wrap-params
   (fn [{:keys [params]}]
     (update-cache! cache)
     (let [menus (->> (:menus @cache)
                      (clojure.set/select (restaurant? params))
                      (clojure.set/select (today? params)))]
       {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/write-str menus
                              :escape-unicode false
                              :escape-slash false)}))))

(defonce server (jetty/run-jetty #'handler {:port 8080 :join? false}))

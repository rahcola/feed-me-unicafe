(ns feed-me-unicafe.menu-parser
  (:require [net.cgrand.enlive-html :as html])
  (:import java.net.URI)
  (:import java.net.URL)
  (:import java.text.SimpleDateFormat))

(def restaurant-ids
  [1 2 3 4 5 15 6 7 8 16 10 11 12 13 14 18 19 21])

(def unicafe-domain "www.hyyravintolat.fi")

(def language-path
  {:finnish "/unicafe/lounashaku"
   :english "/en/unicafe/lunch-search"})

(defn page! [language]
  (let [restaurant-query (apply str (interpose "&" (map (fn [id] (str "r[" id "]=1"))
                                                        restaurant-ids)))
        days-query (apply str (interpose "&" (map (fn [day] (str "v[" day "]=1"))
                                                  [1 2 3 4 5])))
        url (.toURL (URI. "http" unicafe-domain (get language-path language)
                          (str restaurant-query "&" days-query "&adv_get=Etsi") nil))]
    (html/html-resource url)))

(defn menu-table [dom]
  (first (html/select dom [:table.weeklymenu])))

(defn restaurant-names [menu-table]
  (map html/text (html/select menu-table [:thead :tr :th html/first-child])))

(defn daily-rows [menu-table]
  (html/select menu-table [:table.weeklymenu :> :tr]))

(defn row-date [row]
  (html/text (first (html/select row [:th.weekday :span]))))

(defn row-menus [row]
  (html/select row [:td]))

(defn menu-items [menu]
  (html/select menu [:p]))

(defn menu-item-title [menu-item]
  (html/text (first (html/select menu-item [:p html/first-child]))))

(defn menu-item-price [menu-item]
  (html/text (first (html/select menu-item [:span.priceinfo]))))

(defn parse-date [date-str]
  (let [sdf (SimpleDateFormat. "d.M.y")]
    (.parse sdf date-str)))

(defn parse-menu [dom]
  (let [table (menu-table dom)
        restaurant-names (restaurant-names table)]
    (set (flatten (map (fn [row]
                         (let [date (parse-date (row-date row))]
                           (map (fn [menu restaurant]
                                  (map (fn [menu-item]
                                         {:date date
                                          :restaurant restaurant
                                          :title (menu-item-title menu-item)
                                          :price (menu-item-price menu-item)})
                                       (menu-items menu)))
                                (row-menus row)
                                restaurant-names)))
                       (daily-rows table))))))

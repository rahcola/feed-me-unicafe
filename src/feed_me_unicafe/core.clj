(ns feed-me-unicafe.core
  (:require [clojure.data.xml :as xml])
  (:import java.net.URL))

(defn filter-by-tag [tag elements]
  (filter (fn [e] (= tag (:tag e))) elements))

(defn filter-by-class [class elements]
  (filter (fn [e] (= class (:class (:attrs e)))) elements))

(def language-url-fragment
  {:finnish "fi/"
   :english "eng/"})

(defn sanitize-feed-str [feed-str]
  (-> (.replace feed-str " &euro;" "€")
      (.replace " &#128;&euro;" "€")))

(defn feed [id language]
  (let [url (-> (URL. "http://www.hyyravintolat.fi/rss/")
                (URL. (get language-url-fragment language))
                (URL. (str id "/")))]
    (with-open [stream (.openStream url)]
      (xml/parse-str (sanitize-feed-str (slurp stream))))))

(defn feed-title [feed]
  (first (filter-by-tag :title (:content (first (:content feed))))))

(defn feed-items [feed]
  (filter-by-tag :item (:content (first (:content feed)))))

(defn item-title [item]
  (first (filter-by-tag :title (:content item))))

(defn item-descriptions [item]
  (let [raw (first (:content (first (filter-by-tag :description (:content item)))))
        html (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                  "<description>"
                  raw
                  "</description>")]
    (:content (xml/parse-str html))))

(defn description-meal [description]
  (first (filter-by-class "meal" (:content description))))

(defn description-special [description]
  (first (filter-by-tag :em (:content description))))

(defn description-price [description]
  (first (filter-by-class "priceinfo" (:content description))))

(defn parse-description [description]
  {:meal (first (:content (description-meal description)))
   :special (first (:content (description-special description)))
   :price (first (:content (description-price description)))})

(defn parse-item [item]
  (let [title (item-title item)
        description (item-descriptions item)]
    {:date (first (:content title))
     :menu (map parse-description description)}))

(defn parse-feed [feed]
  (let [title (feed-title feed)
        items (feed-items feed)]
    {:restaurant (first (:content title))
     :menus (map parse-item items)}))

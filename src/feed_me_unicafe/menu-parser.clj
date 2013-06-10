(ns feed-me-unicafe.menu-parser
  (:require [net.cgrand.enlive-html :as html])
  (:import java.net.URI)
  (:import java.text.SimpleDateFormat))

(def symbols
  {"[S]" :recommended-by-KELA
   "vl" :low-lactose
   "l" :lactose-free
   "g" :gluten-free
   "v" :contains-garlic
   "k" :vegetable-diet
   "ve" :vegan
   "se" :contains-celery
   "pä" :contains-nuts
   "m" :milk-free
   "so" :contains-soy})

(def prices
  {"Edullisesti" :budget
   "Maukkaasti" :tasty})

(def restaurant-ids
  [1 2 3 4 5 15 6 7 8 16 10 11 12 13 14 18 19 21])

(def unicafe-domain "www.hyyravintolat.fi")

(def language-fragments
  {:finnish "/unicafe/lounashaku"
   :english "/en/unicafe/lunch-search"})

(defn build-query [keys values]
  (apply str (interpose "&" (map (fn [key value] (str key "=" value))
                                 keys
                                 values))))

(defn restaurant-names [dom]
  (map html/text
       (html/select dom [:#weeksearch
                         [:tr html/first-child]
                         :th html/first-child])))

(defn rows [dom]
  (drop 2 (html/select dom [:#weeksearch :tr])))

(defn parse-date [date-str]
  (let [sdf (SimpleDateFormat. "d.M.y")]
    (.parse sdf date-str)))

(defn date-of-row [row]
  (parse-date (html/text (first (html/select row [:th.weekday :> :span])))))

(defn parse-symbols [symbols-str]
  (set (map symbols
            (-> (clojure.string/replace symbols-str #"\(|\)" "")
                (clojure.string/split #",")))))

(defn parse-menu-item [menu-item]
  (let [name (html/text (first (html/select menu-item
                                            [[:span html/first-child]])))
        symbols (html/text (first (html/select menu-item
                                               [:em])))
        price (html/text (first (html/select menu-item
                                             [:span.priceinfo])))]
    {:name name
     :symbols (parse-symbols symbols)
     :price (get prices price price)}))

(defn menus-of-row [row]
  (map (fn [menu]
         (if (= [" "] (:content menu))
           []
           (map parse-menu-item (html/select menu [:p]))))
       (html/select row [:td])))

(defn parse-page [dom]
  (let [restaurants (restaurant-names dom)]
    (set (flatten (map (fn [row]
                         (let [date (date-of-row row)]
                           (map (fn [menu restaurant]
                                  (map (fn [menu-item]
                                         (assoc menu-item
                                           :restaurant restaurant
                                           :date date))
                                       menu))
                                (menus-of-row row)
                                restaurants)))
                       (rows dom))))))

(defn page! [language]
  (let [restaurant-query (build-query (map (fn [id] (str "r[" id "]"))
                                           restaurant-ids)
                                      (repeat 1))
        days-query (build-query (map (fn [day] (str "v[" day "]"))
                                     [1 2 3 4 5])
                                (repeat 1))
        url (.toURL (URI. "http" unicafe-domain
                          (get language-fragments language)
                          (str restaurant-query "&"
                               days-query
                               "&adv_get=Etsi")
                          nil))]
    (html/html-resource url)))

(defn menus! [language]
  (parse-page (page! language)))

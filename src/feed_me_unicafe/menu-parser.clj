(ns feed-me-unicafe.menu-parser
  (:require [net.cgrand.enlive-html :as html])
  (:import java.net.URI)
  (:import java.text.SimpleDateFormat))

(def html-escape-pattern
  #"(?:&(?:[A-Za-z_:][\w:.-]*|\#(?:[0-9]+|x[0-9a-fA-F]+));)")

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

(def restaurant-id->internal-name
  {1 :metsatalo
   2 :olivia
   3 :porthania
   4 :paarakennus
   5 :rotunda
   6 :topelias
   7 :valtiotiede
   8 :ylioppilasaukio
   10 :chemicum
   11 :exactum
   12 :physicum
   13 :meilahti
   14 :ruskeasuo
   15 :soc-and-kom
   18 :biokeskus
   19 :korona
   21 :viikuna})

(def restaurant-ids (keys restaurant-id->internal-name))

(def restaurant-name->internal-name
  {"Metsätalo" :metsatalo
   "Olivia" :olivia
   "Porthania" :porthania
   "Päärakennus" :paarakennus
   "Rotunda" :rotunda
   "Topelias" :topelias
   "Valtiotiede" :valtiotiede
   "Ylioppilasaukio" :ylioppilasaukio
   "Chemicum" :chemicum
   "Exactum" :exactum
   "Physicum" :physicum
   "Meilahti" :meilahti
   "Ruskeasuo" :ruskeasuo
   "Soc&Kom" :soc-and-kom
   "Biokeskus" :biokeskus
   "Korona" :korona
   "Viikuna" :viikuna})

(def unicafe-domain "www.hyyravintolat.fi")

(def language-fragments
  {:finnish "/unicafe/lounashaku"
   :english "/en/unicafe/lunch-search"})

(defn build-query [keys values]
  (apply str (interpose "&" (map (fn [key value] (str key "=" value))
                                 keys
                                 values))))

(defn restaurant-query [restaurant-ids]
  (build-query (map (fn [id] (str "r[" id "]"))
                    restaurant-ids)
               (repeat 1)))

(defn days-query [days]
  (build-query (map (fn [day] (str "v[" day "]"))
                    days)
               (repeat 1)))

(defn restaurant-names [dom]
  (map html/text
       (html/select dom [:#weeksearch
                         [:tr html/first-child]
                         :th html/first-child])))

(defn rows [dom]
  (drop 2 (html/select dom [:#weeksearch :tr])))

(defn date-of-row [row]
  (html/text (first (html/select row [:th.weekday :> :span]))))

(defn menus-of-row [row]
  (map (fn [menu]
         (if (= [" "] (:content menu))
           []
           (html/select menu [:p])))
       (html/select row [:td])))

(defn parse-symbols [symbols-strs]
  (set (map symbols
            (flatten (map #(-> (clojure.string/replace % #"\(|\)" "")
                               (clojure.string/split #","))
                          symbols-strs)))))

(defn parse-date [date-str]
  (let [sdf (SimpleDateFormat. "d.M.y")]
    (.parse sdf date-str)))

(defn parse-menu-item [menu-item restaurant date]
  (let [name (html/text (first (html/select menu-item
                                            [[:span html/first-child]])))
        symbols (map html/text (html/select menu-item
                                            [:em]))
        price (html/text (first (html/select menu-item
                                             [:span.priceinfo])))]
    {:restaurant (get restaurant-name->internal-name restaurant :unknown)
     :date (parse-date date)
     :name name
     :symbols (parse-symbols symbols)
     :price (get prices price
                 (clojure.string/replace price html-escape-pattern ""))}))

(defn parse-page [dom]
  (let [restaurants (restaurant-names dom)]
    (set (flatten (map (fn [row]
                         (let [date (date-of-row row)]
                           (map (fn [menu restaurant]
                                  (map (fn [menu-item]
                                         (parse-menu-item menu-item
                                                          restaurant
                                                          date))
                                       menu))
                                (menus-of-row row)
                                restaurants)))
                       (rows dom))))))

(defn page! [language]
  (let [url (.toURL (URI. "http" unicafe-domain
                          (get language-fragments language)
                          (str (restaurant-query restaurant-ids) "&"
                               (days-query [1 2 3 4 5])
                               "&adv_get=Etsi")
                          nil))]
    (html/html-resource url)))

(defn menus! [language]
  (parse-page (page! language)))

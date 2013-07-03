(ns feed-me-unicafe.restaurant
  (:require [feed-me-unicafe.location :as l]))

(defrecord ARestaurant [key name location])

(def restaurant-data
  {:metsatalo {:name "Metsätalo"
               :location {:latitude 60.172577
                          :longitude 24.948878}}
   :olivia {:name "Olivia"
            :location {:latitude 60.175077
                       :longitude 24.952979}}
   :porthania {:name "Porthania"
               :location {:latitude 60.169878
                          :longitude 24.948669}}
   :paarakennus {:name "Päärakennus"
                 :location {:latitude 60.169178
                            :longitude 24.949297}}
   :rotunda {:name "Rotunda"
             :location {:latitude 60.170332
                        :longitude 24.950791}}
   :topelias {:name "Topelias"
              :location {:latitude 60.171806
                         :longitude 24.95067}}
   :valtiotiede {:name "Valtiotiede"
                 :location {:latitude 60.173897
                            :longitude 24.953095}}
   :ylioppilasaukio {:name "Ylioppilasaukio"
                     :location {:latitude 60.169092
                                :longitude 24.93992}}
   :chemicum {:name "Chemicum"
              :location {:latitude 60.205108
                         :longitude 24.963357}}
   :exactum {:name "Exactum"
             :location {:latitude 60.20509
                        :longitude 24.961209}}
   :physicum {:name "Physicum"
              :location {:latitude 60.204755
                         :longitude 24.963200}}
   :meilahti {:name "Meilahti"
              :location {:latitude 60.190212
                         :longitude 24.908911}}
   :ruskeasuo {:name "Ruskeasuo"
               :location {:latitude 60.206341
                          :longitude 24.895871}}
   :soc-and-kom {:name "Soc&Kom"
                 :location {:latitude 60.173054
                            :longitude 24.95049}}
   :kookos {:name "Kookos"
            :location {:latitude 60.181034
                       :longitude 24.958652}}
   :biokeskus {:name "Biokeskus"
               :location {:latitude 60.226922
                          :longitude 25.013707}}
   :korona {:name "Korona"
            :location {:latitude 60.226922
                       :longitude 25.013707}}
   :viikuna {:name "Viikuna"
             :location {:latitude 60.23049
                        :longitude 25.020544}}})

(def restaurants
  (map (fn [[r-key {:keys [name location]}]]
         (map->ARestaurant
          {:key r-key
           :name name
           :location (l/map->ALocation location)}))
       restaurant-data))

(defn restaurant-of-name [name]
  (first (filter #(= name (:name %)) restaurants)))

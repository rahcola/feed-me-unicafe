(ns feed-me-unicafe.restaurant)

(defrecord ARestaurant
    [name location])

(def restaurants
  {:metsatalo (->ARestaurant "Metsätalo" {:latitude 60.172577
                                          :longitude 24.948878})
   :olivia (->ARestaurant "Olivia" {:latitude 60.175077
                                    :longitude 24.952979})
   :porthania (->ARestaurant "Porthania" {:latitude 60.169878
                                          :longitude 24.948669})
   :paarakennus (->ARestaurant "Päärakennus" {:latitude 60.169178
                                              :longitude 24.949297})
   :rotunda (->ARestaurant "Rotunda" {:latitude 60.170332
                                      :longitude 24.950791})
   :topelias (->ARestaurant "Topelias" {:latitude 60.171806
                                        :longitude 24.95067})
   :valtiotiede (->ARestaurant "Valtiotiede" {:latitude 60.173897
                                              :longitude 24.953095})
   :ylioppilasaukio (->ARestaurant "Ylioppilasaukio" {:latitude 60.169092
                                                      :longitude 24.93992})
   :chemicum (->ARestaurant "Chemicum" {:latitude 60.205108
                                        :longitude 24.963357})
   :exactum (->ARestaurant "Exactum" {:latitude 60.20509
                                      :longitude 24.961209})
   :physicum (->ARestaurant "Physicum" {:latitude 60.204755
                                        :longitude 24.963200})
   :meilahti (->ARestaurant "Meilahti" {:latitude 60.190212
                                        :longitude 24.908911})
   :ruskeasuo (->ARestaurant "Ruskeasuo" {:latitude 60.206341
                                          :longitude 24.895871})
   :soc-and-kom (->ARestaurant "Soc&Kom" {:latitude 60.173054
                                          :longitude 24.95049})
   :kookos (->ARestaurant "Kookos" {:latitude 60.181034
                                    :longitude 24.958652})
   :biokeskus (->ARestaurant "Biokeskus" {:latitude 60.226922
                                          :longitude 25.013707})
   :korona (->ARestaurant "Korona" {:latitude 60.226922
                                    :longitude 25.013707})
   :viikuna (->ARestaurant "Viikuna" {:latitude 60.23049
                                      :longitude 25.020544})})

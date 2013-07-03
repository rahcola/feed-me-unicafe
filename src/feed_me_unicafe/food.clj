(ns feed-me-unicafe.food)

(def prices
  {"Edullisesti" :budget
   "Maukkaasti" :tasty})

(def diet-symbols
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

(defrecord AFood [name price diet-symbols])

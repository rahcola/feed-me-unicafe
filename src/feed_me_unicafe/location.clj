(ns feed-me-unicafe.location)

(defn ^{:private true}
  squared [x]
  (* x x))

(defprotocol Location
  (distance [location location']))

(defrecord ALocation [latitude longitude]
  Location
  (distance [_ location']
    (java.lang.Math/sqrt
     (+ (squared (- latitude (:latitude location')))
        (squared (- longitude (:longitude location')))))))

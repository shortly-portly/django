(ns django.view.django
  (:require [django.view.bootstrap :as bs]
            [integrant.core :as ig]))

(defn error [data]
  (bs/layout
  [:h1 data]))


(defmethod ig/init-key :django.view.django/index [_ _]
(fn [data]
  (bs/layout
   [:div
    [:h1 "Results"]

    (bs/table ["first name" "last name" "age"]
              (for [entry data]
                [:tr
                 [:td (entry :firstname)]
                 [:td (entry :lastname)]
                 [:td (entry :age)]]))])))

(defmethod ig/init-key :django.view.django/detail [_ _]
  (fn [data]
  (bs/layout
   [:div
    [:h1 "Details"]])))

(ns django.handler.django
  (:require [ataraxy.response :as response]
            [clojure.java.jdbc :as jdbc]
            duct.database.sql
            [integrant.core :as ig]
            [ring.util.response :as ring]
            [django.view.django :as view]))

; Boundaries
(defprotocol Database
  (query-database [db sql]))

(extend-protocol Database
  duct.database.sql.Boundary
  (query-database [{:keys [spec]} sql]
    (try
      (let [result (jdbc/query spec sql)]
        [nil result])
      (catch Exception e
        ["Something went wrong with the query", nil]))))

(defn convert-route-params [sql route-params]
  (try
  (let [sql-keys (rest sql)
        sql-params (map (fn [key]
                           (Integer/parseInt (route-params key))) sql-keys)]

    [nil (concat [(first sql)] (vec sql-params))])
  (catch Exception e
    [(str "Conversion Error sql parameters: " (rest sql) " from :" route-params) nil])))

; Prep Keys
(defmethod ig/prep-key :django.handler.django/list [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)
    :view (ig/ref :django.view.django/list)} config))

(defmethod ig/prep-key :django.handler.django/detail [_ config]
  (merge
   {:db (ig/ref :duct.database/sql)} config))

; Initialise Keys
(defmethod ig/init-key :django.handler.django/list [_ {:keys [db sql view]}]
  (fn [{:keys [route-params]}]_
    (let [[error new-sql] (convert-route-params sql route-params)
          [error result] (if-not error (query-database db new-sql) [error nil])]
      (if error
        [::response/ok (view/error error)]
        [::response/ok (view result)]))))

(defmethod ig/init-key :django.handler.django/detail [_ {:keys [db sql view]}]
  (fn [{:keys [route-params]}]
    (let [[error new-sql] (convert-route-params sql route-params)
          [error result] (if-not error (query-database db new-sql) [error nil])]
      (if error
        [::response/ok (view/error error)]
        [::response/ok (view "placeholder")]))))

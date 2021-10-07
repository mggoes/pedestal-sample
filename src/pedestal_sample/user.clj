(ns pedestal-sample.user
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as bp]
            [io.pedestal.http.route :as route])
  (:import [java.util UUID]))

(defn one-user
  [_]
  {:status 200
   :body   "OK"})

(defn save-user
  [request]
  (let [user-data (:json-params request)
        user (into {:id (UUID/randomUUID)} user-data)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/write-str user)}))

(def routes
  (route/expand-routes
    #{["/users" :get one-user :route-name :one-user]
      ["/users" :post [(bp/body-params) save-user] :route-name :save-user]}))

(defn create-server
  []
  (http/create-server
    {::http/routes routes
     ::http/type   :jetty
     ::http/port   8080}))

(defn start
  []
  (http/start (create-server)))

(defn -main
  []
  (start))

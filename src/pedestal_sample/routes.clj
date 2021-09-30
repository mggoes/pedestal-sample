(ns pedestal-sample.routes
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn echo-path-params
  [request]
  {:status 200 :body (:path-params request)})

(def routes
  (route/expand-routes
    ;Table syntax
    #{["/users" :get echo-path-params :route-name :all-users]
      ;Constraints
      ["/users/:id" :get echo-path-params :route-name :one-user :constraints {:id #"[0-9]+"}]
      ;Catch-all parameters
      ["/users/:id/scopes/*scopes" :get echo-path-params :route-name :user-scopes]}))

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

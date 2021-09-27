(ns pedestal-sample.greet
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(def denied #{"Gomes" "Goes"})

(defn ok
  [body]
  {:status 200 :body body})

(defn not-found
  []
  {:status 404 :body "Not found!"})

(defn greet
  [name]
  (cond
    (denied name) nil
    (empty? name) "Hello!"
    :else (str "Hello " name)))

;Creates a handler function
(defn handle-greet
  [request]
  (let [name (get-in request [:query-params :name])
        result (greet name)]
    (if result
      (ok result)
      (not-found))))

;Defines routes using table syntax
(def routes
  (route/expand-routes
    #{["/greet" :get handle-greet :route-name :greet]}))

;Creates a webserver with the routes
(defn create-server
  []
  (http/create-server
    {::http/routes routes
     ::http/type   :jetty
     ::http/port   8080}))

;Starts the webserver
(defn start
  []
  (http/start (create-server)))

(defn -main
  []
  (start))

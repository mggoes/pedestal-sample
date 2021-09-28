(ns pedestal-sample.greet
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.content-negotiation :as con-neg]))

(def denied #{"Gomes" "Goes"})
(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])
(def content-neg-interceptor (con-neg/negotiate-content supported-types))

(defn ok
  [body]
  {:status 200 :body body})

(defn not-found
  []
  {:status 404 :body "Not found!"})

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html" body
    "text/plain" body
    "application/edn" (pr-str body)
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
         (fn [context]
           (cond-> context
                   (nil? (get-in context [:response :headers "Content-Type"]))
                   (update-in [:response] coerce-to (accepted-type context))))})

(defn greet
  [name]
  (cond
    (denied name) nil
    (empty? name) "Hello!"
    :else (str "Hello " name)))

(defn handle-greet
  [request]
  (let [name (get-in request [:query-params :name])
        result (greet name)]
    (if result
      (ok result)
      (not-found))))

(def echo
  {:name  ::echo
   :enter #(assoc % :response (ok (:request %)))})

;Defines routes using table syntax
(def routes
  (route/expand-routes
    #{["/greet" :get [coerce-body content-neg-interceptor handle-greet] :route-name :greet]
      ["/echo" :get echo]}))

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

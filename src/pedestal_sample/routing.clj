(ns pedestal-sample.routing
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as bp]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as interceptor])
  (:import [java.util UUID]))

(defn all-products
  [_]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str [{:id (UUID/randomUUID) :name "Product 1"}
                             {:id (UUID/randomUUID) :name "Product 2"}])})

(def all-products-interceptor
  (interceptor/interceptor {:name :all-products-interceptor
                            :enter
                                  (fn [context]
                                    (let [response {:status  200
                                                    :headers {"Content-Type" "application/json"}
                                                    :body    (json/write-str [{:id (UUID/randomUUID) :name "Product 1"}
                                                                              {:id (UUID/randomUUID) :name "Product 2"}])}]
                                      (assoc context :response response)))}))

(defn save-product
  [request]
  (let [data (:json-params request)
        product (into {:id (UUID/randomUUID)} data)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/write-str product)}))

(defn one-product
  [{:keys [path-params]}]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body    (json/write-str {:id (:id path-params) :name "Product 1"})})

;Verbose syntax
;(def routes
;  (route/expand-routes
;    {:route-name        :all-products
;     :app-name          :pedestal-sample
;     :path              "/products"
;     :method            :get
;     :scheme            :http
;     :host              "localhost"
;     :port              "8080"
;     :interceptors      [(bp/body-params) all-products]
;     :path-re           #"/\Quser\E/([^/]+)/(.+)"
;     :path-parts        ["products" :id :blah]
;     :path-params       [:id :blah]
;     :path-constraints  {:id   "([^/]+)"
;                         :blah "(.+)"}
;     :query-constraints {:name   #".+"
;                         :search #"[0-9]+"}
;     }))

;Terse syntax
;(def routes
;  (route/expand-routes
;    [[:pedestal-sample :http "localhost" 8080
;      ["/products"
;       ^:interceptors [(bp/body-params)]
;       {:get [:all-products `all-products]}
;       ["/:id"
;        {:get [:one-product `one-product]}]]]]))

;Table syntax
(def routes
  (route/expand-routes
    #{["/products" :get [(bp/body-params) all-products] :route-name :all-products]
      ["/products" :post [(bp/body-params) save-product] :route-name :save-product]
      ["/products/:id" :get [(bp/body-params) one-product] :route-name :one-product]}))

(def service-map {::http/routes         routes
                  ::http/type           :immutant
                  ;Serving static resources
                  ::http/resource-path  "/public"
                  ;Disables logging interceptor
                  ;::http/request-logger nil
                  ::http/port           8080})

(defn create-server
  []
  (http/create-server service-map))

(defn start
  []
  (http/start (create-server)))

(defn -main
  []
  (start))

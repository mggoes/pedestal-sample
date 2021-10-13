(ns routing
  (:require [clojure.test :refer :all]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor.chain :as chain]
            [io.pedestal.test :as pedestal.test]
            [matcher-combinators.test :refer :all]
            [pedestal-sample.routing :as r]))

;Simple unit test
(testing "all-products"
  (is (match? {:status  200
               :headers {"Content-Type" "application/json"}
               :body    string?} (r/all-products nil))))

;Using chain/execute
(testing "all-products with chain"
  (is (match? {:status  200
               :headers {"Content-Type" "application/json"}
               :body    string?} (:response (chain/execute {} [r/all-products-interceptor])))))

;Using response-for to test an endpoint
(def service (:io.pedestal.http/service-fn (http/create-servlet r/service-map)))

(testing "all-products with response-for"
  ;GET
  (is (match? {:status  200
               :headers {"Content-Type" "application/json"}
               :body    string?} (pedestal.test/response-for service :get "/products")))

  ;POST
  (is (match? {:status  200
               :headers {"Content-Type" "application/json"}
               :body    string?} (pedestal.test/response-for service
                                                             :post "/products"
                                                             :headers {"Content-Type" "application/json"}
                                                             :body "{\"name\":\"Product 100\"}"))))

;Using url-for-routes to construct test urls
(def url-for (route/url-for-routes r/routes))

(testing "one-product"
  ;GET
  (is (match? {:status  200
               :headers {"Content-Type" "application/json"}
               :body    string?} (pedestal.test/response-for service :get (url-for :one-product
                                                                                   :path-params {:id 12345}
                                                                                   :query-params {:sort "ASC"})))))

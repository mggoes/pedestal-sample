(ns pedestal
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :refer [response-for]]
            [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [pedestal-sample.pedestal :as pedestal]))

(def url-for (route/url-for-routes
               (route/expand-routes pedestal/routes)))

(defn service-fn
  [system]
  (get-in system [:pedestal :service ::http/service-fn]))

(defmacro with-system
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

(deftest greeting-test
  (with-system [sut (pedestal/new-system :test)]
               (let [service (service-fn sut)
                     {:keys [status body]} (response-for service :get (url-for :greet))]
                 (is (= 200 status))
                 (is (= "Hello, world!" body)))))

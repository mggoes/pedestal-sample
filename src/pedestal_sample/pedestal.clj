(ns pedestal-sample.pedestal
  (:require [io.pedestal.http :as http]
            [com.stuartsierra.component :as component]
            [io.pedestal.interceptor :as interceptor])
  (:import (java.util Random)))

;Tools
(def components nil)

(defn- test?
  [service-map]
  (= :test (:env service-map)))

;Default interceptor
(def components-ctx-interceptor
  (interceptor/interceptor {:name :components-ctx-interceptor
                            :enter
                                  (fn [context]
                                    (println "Injecting components...")
                                    (assoc-in context [:request :components] components))}))

;Components
(defrecord ^:private Pedestal [service-map service]
  component/Lifecycle
  (start [this]
    (if service
      this
      (cond-> service-map
              true http/default-interceptors
              true (update ::http/interceptors conj components-ctx-interceptor)
              true http/create-server
              (not (test? service-map)) http/start
              true ((partial assoc this :service)))))
  (stop [this]
    (when (and service (not (test? service-map)))
      (http/stop service))
    (assoc this :service nil)))

(defn- new-pedestal
  []
  (map->Pedestal {}))

(defrecord ^:private Database [service-map connection]
  component/Lifecycle
  (start [this]
    (if connection
      this
      (assoc this :connection (str "Database connection for " (name (:env service-map))))))
  (stop [this]
    (if connection
      (dissoc this :connection)
      this)))

(defn- new-database
  []
  (map->Database {}))

;Routes
(defn- respond-hello
  [{{:keys [service-map pedestal database]} :components}]
  (println (.getName (Thread/currentThread)) "Has service-map?" (boolean service-map))
  (println (.getName (Thread/currentThread)) "Has pedestal?" (boolean pedestal))
  (println (.getName (Thread/currentThread)) "Has database?" (boolean database))
  (println (.getName (Thread/currentThread)) (:connection database))
  {:status 200 :body "Hello, world!"})

(def ^:private routes
  #{["/greet" :get respond-hello :route-name :greet]})

;System map
(defn- system-map
  [env]
  (component/system-map
    :service-map {:env          env
                  ::http/routes routes
                  ::http/type   :immutant
                  ::io-threads  10
                  ::http/port   8080}
    :pedestal (component/using (new-pedestal) [:service-map])
    :database (component/using (new-database) [:service-map])))

;Environments
(def env {:prod (system-map :prod)
          :test (system-map :test)})

(defn- start-prod
  [_]
  (component/start (:prod env)))

(defn -main
  []
  (alter-var-root #'components start-prod)
  (assert components))

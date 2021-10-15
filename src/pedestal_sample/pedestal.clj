(ns pedestal-sample.pedestal
  (:require [io.pedestal.http :as http]
            [com.stuartsierra.component :as component]))

;Component
(defn test?
  [service-map]
  (= :test (:env service-map)))

(defrecord Pedestal [service-map service]
  component/Lifecycle
  (start [this]
    (println service-map)
    (println service)
    (println this)
    (if service
      this
      (cond-> service-map
              true http/create-server
              (not (test? service-map)) http/start
              true ((partial assoc this :service)))))
  (stop [this]
    (when (and service (not (test? service-map)))
      (http/stop service))
    (assoc this :service nil)))

(defn new-pedestal
  []
  (map->Pedestal {}))

;Routes
(defn respond-hello [request]
  {:status 200 :body "Hello, world!"})

(def routes
  #{["/greet" :get respond-hello :route-name :greet]})

;System map
(defn new-system
  [env]
  (component/system-map
    :service-map {:env          env
                  ::http/routes routes
                  ::http/type   :immutant
                  ::http/port   8080}
    :pedestal (component/using (new-pedestal) [:service-map])))

(defn -main
  []
  (component/start (new-system :prod)))

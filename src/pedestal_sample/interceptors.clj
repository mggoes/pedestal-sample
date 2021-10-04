(ns pedestal-sample.interceptors
  (:require [io.pedestal.http :as http]
            [io.pedestal.interceptor :as i]
            [io.pedestal.interceptor.chain :as chain]
            [io.pedestal.interceptor.error :as err]))

(def odds
  (i/interceptor
    {:name  ::odds
     :enter (fn [context]
              (assoc context :response {:body   "I handle odd numbers\n"
                                        :status 200}))}))

(def evens
  (i/interceptor
    {:name  ::evens
     :enter (fn [context]
              (assoc context :response {:body   "Even numbers are my bag\n"
                                        :status 200}))}))

(def say-hello
  {:name  ::say-hello
   :enter (fn [context]
            (assoc context :response {:body   "Hello, world!"
                                      :status 200}))})

(def chooser
  {:name  ::chooser
   :enter (fn [context]
            (try
              (let [param (get-in context [:request :query-params :n])
                    n (Integer/parseInt param)
                    nxt (if (even? n) evens odds)]
                (chain/enqueue context [nxt]))
              (catch NumberFormatException _
                (assoc context :response {:body   "Not a number!\n"
                                          :status 400}))))})

(def chooser2
  {:name  ::chooser
   :enter (fn [context]
            (let [n (-> context :request :query-params :n Integer/parseInt)
                  nxt (if (even? n) evens odds)]
              (chain/enqueue context [nxt])))})

;Creates an interceptor to handle the error
(def errors
  (err/error-dispatch [ctx ex]
                      [{:exception-type :java.lang.NumberFormatException}]
                      (assoc ctx :response {:status 400 :body "Not a number!\n"})))

(def routes
  #{["/hello" :get say-hello]
    ["/data-science" :get chooser :route-name :ds1]
    ["/data-science2" :get [errors chooser2] :route-name :ds2]})

(defn start
  []
  (-> {::http/port   8080
       ;Do not block
       ::http/join?  false
       ::http/type   :jetty
       ::http/routes routes}
      http/create-server
      http/start))

(defn -main
  []
  (start))

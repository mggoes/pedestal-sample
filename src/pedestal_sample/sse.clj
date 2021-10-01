(ns pedestal-sample.sse
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.protocols :as chan]
            [hiccup.core :as hiccup]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.sse :as sse]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]))

(def js-string
  "
var eventSource = new EventSource(\"http://localhost:8080/counter\");
eventSource.addEventListener(\"counter\", function(e) {
  console.log(e);
  var counterEl = document.getElementById(\"counter\");
  counter.innerHTML = e.data;
});
")

(defn home-page
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (hiccup/html [:html
                          [:head
                           [:script {:type "text/javascript"}
                            js-string]]
                          [:body
                           [:div
                            [:span "Counter: "]
                            [:span#counter]]]])})

(defn stream-ready
  [event-chan _]
  (dotimes [i 10]
    (when-not (chan/closed? event-chan)
      (async/>!! event-chan {:name "counter" :data i})
      (Thread/sleep 1000)))
  (async/close! event-chan))

(def routes #{["/" :get home-page :route-name :home-page]
              ["/counter" :get (sse/start-event-stream stream-ready) :route-name :stream-ready]})

(def service {:env                  :prod
              ::http/routes         routes
              ::http/type           :jetty
              ::http/port           8080
              ::http/secure-headers {:content-security-policy-settings {:object-src "none"}}})

(defn create-server
  []
  (http/create-server service))

(defn start
  []
  (http/start (create-server)))

(defn -main
  []
  (start))

(ns pedestal-sample.main
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn response
  [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok (partial response 200))
(def created (partial response 201))
(def accepted (partial response 202))

(def entity-render
  {:name :entity-render
   :leave
         (fn [context]
           (if-let [item (:result context)]
             (assoc context :response (ok item))
             context))})

(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [request (:request context)
                 response (ok context)]
             (assoc context :response response)))})

;Database
(defonce database (atom {}))
(def db-interceptor
  {:name :database-interceptor
   :enter
         (fn [context]
           (update context :request assoc :database @database))
   :leave
         (fn [context]
           (if-let [[op & args] (:tx-data context)]
             (do
               (apply swap! database op args)
               (assoc-in context [:request :database] @database))
             context))})

;Create a List
(defn make-list
  [id nm]
  {:id    id
   :name  nm
   :items {}})

(defn make-list-item
  [nm]
  {:name  nm
   :done? false})

(def list-create
  {:name :list-create
   :enter
         (fn [context]
           (let [nm (get-in context [:request :query-params :name] "Unnamed List")
                 db-id (str (gensym "l"))
                 new-list (make-list db-id nm)
                 url (route/url-for :list-view :params {:list-id db-id})]
             (assoc context
               :response (created new-list "Location" url)
               :tx-data [assoc db-id new-list])))})

;Retrieve a List
(defn find-list-by-id
  [dbval db-id]
  (get dbval db-id))

(def list-view
  {:name :list-view
   :enter
         (fn [context]
           (if-let [db-id (get-in context [:request :path-params :list-id])]
             (if-let [the-list (find-list-by-id (get-in context [:request :database]) db-id)]
               (assoc context :result the-list)
               context)
             context))})

;Retrieve a List Item
(defn find-list-item-by-id [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(def list-item-view
  {:name :list-item-view
   :leave
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (if-let [item-id (get-in context [:request :path-params :item-id])]
               (if-let [item (find-list-item-by-id (get-in context [:request :database]) list-id item-id)]
                 (assoc context :result item)
                 context)
               context)
             context))})

;Create a List Item
(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(def list-item-create
  {:name :list-item-create
   :enter
         (fn [context]
           (if-let [list-id (get-in context [:request :path-params :list-id])]
             (let [nm (get-in context [:request :query-params :name] "Unnamed Item")
                   new-item (make-list-item nm)
                   item-id (str (gensym "i"))]
               (-> context
                   (assoc :tx-data [list-item-add list-id item-id new-item])
                   (assoc-in [:request :path-params :item-id] item-id)))
             context))})

(def routes
  (route/expand-routes
    #{["/todo" :post [db-interceptor list-create]]
      ["/todo" :get echo :route-name :list-query-form]
      ["/todo/:list-id" :get [entity-render db-interceptor list-view]]
      ["/todo/:list-id" :post [entity-render list-item-view db-interceptor list-item-create]]
      ["/todo/:list-id/:item-id" :get [entity-render list-item-view db-interceptor]]
      ["/todo/:list-id/:item-id" :put echo :route-name :list-item-update]
      ["/todo/:list-id/:item-id" :delete echo :route-name :list-item-delete]}))

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

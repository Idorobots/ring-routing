(ns ring.util.routing
  (:require [clout.core :as clout]))


(defn path
  "Creates a request path matching middleware."
  [handler path-part]
  (fn [{:keys [params uri] :as request}]
    (let [uri-rest (or (:* params) uri)]
      (if-let [p (clout/route-matches path-part
                                      (assoc request :uri uri-rest))]
        (-> request
            (assoc :params (merge (dissoc params :*) p))
            handler)
        {:status 404
         :retry-fork true}))))

(defn method
  "Creates a request method matching middleware"
  [handler m]
  (fn [{:keys [request-method] :as request}]
    (if (or (= request-method m)
            (and (= request-method :head)
                 (= m :get)))
      (handler request)
      {:status 405
       :retry-fork true})))

(defmacro -->
  "Route - a convenience macro that runs middleware
  in listed instead of reverse order."
  [& exprs]
  `(-> ~@(reverse exprs)))

(defn- run-with-retry [request forks]
  (if-let [[fork & rest] forks]
    (let [{:keys [retry-fork] :as response} (fork request)]
      (if retry-fork
        (run-with-retry request rest)
        response))
    {:status 404
     :retry-fork true}))

(defn- build-forks
  ([] nil)
  ([_] (throw (IllegalArgumentException. "'--<' needs even number of parameters!")))
  ([cond fork & rest]
     (cons `(--> ~cond ~fork)
           (apply build-forks rest))))

(defn- wrap--< [& forks]
  (fn [request]
    (run-with-retry request forks)))

(defmacro --<
  "Fork - runs several forks in listed order until one
  of them successfully returns a response."
  [& forks]
  `(~wrap--< ~@(apply build-forks forks)))


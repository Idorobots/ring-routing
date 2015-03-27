(ns ring.util.routing-test
  (:require [clojure.test :refer :all]
            [ring.util.routing :refer [--> --<] :as route]
            [ring.mock.request :as ring]))


(defn- body-handler [{:keys [body]}]
  {:status 200
   :body body})

(defn- params-handler [{:keys [params]}]
  {:status 200
   :body params})

(defn- middleware [handler key]
  (fn [{:keys [body] :as request}]
    (-> request
        (assoc :body (cons key body))
        handler)))

(deftest -->-test
  (testing "Middleware runs in a correct order."
    (let [result ((--> (middleware :a)
                       (middleware :b)
                       (middleware :c)
                       body-handler)
                  (ring/request :get "/"))]
      (is (= (:body result) '(:c :b :a))))))

(deftest method-test
  (testing "Method is dispatched correctly."
    (let [get ((--> (route/method :get) body-handler)
               (ring/request :get "/"))
          head ((--> (route/method :get) body-handler)
                (ring/request :head "/"))
          not-post ((--> (route/method :post) body-handler)
                    (ring/request :put "/"))]
      (is (= (:status get) 200))
      (is (= (:status head) 200))
      (is (= (:status not-post) 405)))))

(deftest path-test
  (testing "Paths are dispatched correctly."
    (let [root ((--> (route/path "/") body-handler)
                (ring/request :get "/"))
          not-root ((--> (route/path "/") body-handler)
                    (ring/request :get "/not-root"))
          wild ((--> (route/path "*") body-handler)
                (ring/request :get "/some/path"))
          wild2 ((--> (route/path "*") body-handler)
                 (ring/request :get "/some/other/path"))
          wild3 ((--> (route/path "/*/test") body-handler)
                 (ring/request :get "/path/test"))
          wild4 ((--> (route/path "/*/test") body-handler)
                 (ring/request :get "/path/testing"))]
      (is (= (:status root) 200))
      (is (= (:status not-root) 404))
      (is (= (:status wild) 200))
      (is (= (:status wild2) 200))
      (is (= (:status wild3) 200))
      (is (= (:status wild4) 404)))))

(deftest --<-test
  (testing "Fork retries correctly."
    (let [routes (--< (route/path "/a")
                      (--> (middleware :a)
                           body-handler)
                      (route/path "/b")
                      (--> (middleware :b)
                           body-handler))
          a (routes (ring/request :get "/a"))
          b (routes (ring/request :get "/b"))
          c (routes (ring/request :get "/c"))]
      (is (= (:status a) 200))
      (is (= (:body a) '(:a)))
      (is (= (:status b) 200))
      (is (= (:body b) '(:b)))
      (is (= (:status c) 404)))))

(deftest --<-methods-test
  (testing "Fork can dispatch on request method."
    (let [routes (--< (route/method :get)
                      (--> (middleware :get)
                           body-handler)
                      (route/method :post)
                      (--> (middleware :post)
                           body-handler))
          get (routes (ring/request :get "/"))
          post (routes (ring/request :post "/"))
          delete (routes (ring/request :delete "/"))]
      (is (= (:status get) 200))
      (is (= (:body get) '(:get)))
      (is (= (:status post) 200))
      (is (= (:body post) '(:post)))
      (is (= (:status delete) 404)))))

(deftest malformed---<-test
  (testing "Malformed fork won't compile."
    (is (thrown? IllegalArgumentException
                 (eval '(--< :a :b :c))))))

(deftest path-params-test
  (testing "Can use path parameters."
    (let [routes (--> (route/path "/:a/:b/") params-handler)
          ab (routes (ring/request :get "/a/b/"))
          ba (routes (ring/request :get "/b/a/"))
          long (routes (ring/request :get "/foo/bar/baz/"))]
      (is (= (:status ab) 200))
      (is (= (:body ab) {:a "a" :b "b"}))
      (is (= (:status ba) 200))
      (is (= (:body ba) {:a "b" :b "a"}))
      (is (= (:status long) 404)))))

(deftest path-prefix-test
  (testing "Can prefix paths."
    (let [routes (--> (route/path "/prefix/*")
                      (--< (route/path "a/")
                           (--> (middleware :a)
                                body-handler)
                           (route/path "b/")
                           (--> (middleware :b)
                                body-handler)))
          a (routes (ring/request :get "/prefix/a/"))
          b (routes (ring/request :get "/prefix/b/"))
          bad (routes (ring/request :get "/foo/a/"))]
      (is (= (:status a) 200))
      (is (= (:body a) '(:a)))
      (is (= (:status b) 200))
      (is (= (:body b) '(:b)))
      (is (= (:status bad) 404)))))

# ring-routing

A tiny Clojure library for [Ring](https://github.com/ring-clojure/ring) routing emphasizing simplicity & readability. `ring-routing` gives you fine-grained control over your middleware & routes - you can precisely tell how to dispatch incomming requests.

## Usage

Here's an example routes definition for a simple car API:

``` clojure
(ns car.core
  (:require [car.middleware :as middleware]
            [car.handlers :as app]
            [ring.util.routing :refer [--> --<] :as route]))

(def routes
  (--> middleware/wrap-parse-body
       middleware/wrap-parse-cookies
       middleware/wrap-log-request

       (--< (route/path "/cars/:id/")
            (--> middleware/wrap-polish-cars
                 (--< (route/method :get) app/get-car
                      (route/method :post) app/post-car
                      (route/method :delete) app/delete-car))

            (route/path "/parts/*")
            (--< (route/path "wheels/") app/handle-wheels
                 (route/path "seats/") app/handle-seats
                 (route/path "windows/") app/install-linux))))
```

### Running middleware:
Use route macro (`-->`) to combine middleware and path definitions. The following middleware functions will run in order they are listed, so first the body will be parsed, next the cookies and then the request will be logged.

Middleware position is not constrained in any fashion, so if the request path matches `/cars/`, the `wrap-polish-cars` middleware will be run as well.


``` clojure
(--> wrap-parse-body
     wrap-parse-cookies
     wrap-log-request

     (--> (route/path "/cars/")
          wrap-polish-cars
          handle-cars))
```


### Path parameters:
You can parameterize your paths and access path parameters useing the `:params` key in the Ring request map:

``` clojure
(defn get-car [{:keys [params]}]
  ;; Do someting with (:id params)
  )

;; ...

(--> (route/path "/cars/:id/")
     (route/method :get)
     get-car)
```

### Multiple endpoints:
Use fork macro (`--<`) to define alternate paths, for example:

``` clojure
(--< (route/path "/cars/:id") handle-cars
     (route/path "/parts/*") handle-parts)
```

### Multiple endpoints with common prefix:
Sometimes you might want to add a common prefix to your API (for example for API versioning). Here's how to achieve that, note that you have to explicitly define your paths' wildcard:

``` clojure
(--> (route/path "/parts/*")
     (--< (route/path "wheels/") handle-wheels
          (route/path "seats/") handle-steats
          (route/path "windows/") install-linux))
```

### Multiple methods on a single endpoint:
Dispatching on the request method works in a similar fashion:

``` clojure
(--> (route/path "/cars/:id")
     (--< (route/method :get) get-car
          (route/method :post) post-car
          (route/method :delete) delete-car))
```

## License

Copyright © 2015 kajtek@idorobots.org

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

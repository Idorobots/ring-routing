(defproject ring-routing "0.1.4"
  :description "A tiny Ring routing library."
  :url "http://github.com/Idorobots/ring-routing"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clout "2.1.2"]
                 [org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.3.0"]]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}}
  :plugins [[jonase/eastwood "0.2.4"]
            [lein-ancient "0.6.10"]
            [lein-cloverage "1.0.9"]
            [lein-codox "0.10.3"]]
  :codox {:metadata {:doc/format :markdown}
          :source-uri "https://github.com/Idorobots/ring-routing/blob/{version}/{filepath}#L{line}"})

(defproject ring-routing "0.1.3"
  :description "A tiny Ring routing library."
  :url "http://github.com/Idorobots/ring-routing"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[clout "2.1.0"]
                 [org.clojure/clojure "1.6.0"]]
  :profiles {:dev {:dependencies [[ring/ring-mock "0.2.0"]]}}
  :plugins [[jonase/eastwood "0.1.5"]
            [lein-ancient "0.5.4"]
            [lein-cloverage "1.0.2"]
            [lein-codox "0.9.1"]]
  :codox {:metadata {:doc/format :markdown}
          :source-uri "https://github.com/Idorobots/ring-routing/blob/{version}/{filepath}#L{line}"})

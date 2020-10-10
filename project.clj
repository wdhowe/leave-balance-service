(defproject leave-balance-service "0.2.1"
  :description "HTTP service that calculates end of year leave/pto balance."
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.logging "1.1.0"]
                 [clojure.java-time "0.3.2"]
                 [compojure "1.6.1"]
                 [environ "1.2.0"]
                 [http-kit "2.4.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [trptcolin/versioneer "0.2.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler leave-balance-service.handler/app}
  :main ^:skip-aot leave-balance-service.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}
   :uberjar {:aot :all}})

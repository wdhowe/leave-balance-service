(ns leave-balance-service.handler
  (:require [clojure.tools.logging :as log]
            [compojure.core :as compojure]
            [compojure.route :as route]
            [environ.core :refer [env]]
            [org.httpkit.server :as http-server]
            [ring.middleware.defaults :as ring-defaults]
            [ring.middleware.json :as ring-json]
            [leave-balance-service.date-utils :as date-utils]
            [trptcolin.versioneer.core :as version])
  (:gen-class))

(def config
  "Load server configuration from the environment."
  {:max-bal (Integer. (or (:max-bal env) 200))
   :http-port (Integer. (or (:http-port env) 8080))
   :year-end (or (:year-end env) :calendar)})

(comment (println "Config is:" config))

(def version "Project version."
  (version/get-version "leave-balance-service" "leave-balance-service"))

(comment (println "Version is:" version))

(defn health
  "Determine if application is running ok."
  [request]
  (log/debug (:request-method request) (:uri request))
  {:status 200 :body {:healthy true}})

(comment (health {:request-method :get :uri "/health"}))

(defn show-help
  "Return a help string."
  [request]
  (log/debug (:request-method request) (:uri request))
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (str "Leave/PTO Calculator API (" version "). Send requests to:\n"
              "GET / or GET /help -> This help dialog.\n"
              "GET /config -> Config settings.\n"
              "GET /health -> Application health check.\n"
              "POST /calc rate=NUM bal=NUM -> Calculate end of year balance given the rate and current balance.\n"
              "  Example: http http://HOST:" (:http-port config) "/calc rate=3.0 bal=143.5")})

(comment (show-help {:request-method :get :uri "/"}))

(defn show-config
  "Show the service config settings."
  [request]
  (log/debug (:request-method request) (:uri request))
  {:status 200
   :body config})

(comment (show-config {:request-method :get :uri "/config"}))

(defn float-two
  "Format a two decimal precision floating point number."
  [x]
  (Float. (format "%.2f" (float x))))

(comment (float-two 10)
         (float-two 10.123))

(defn leave-info
  "Build a map of leave inputs."
  [{:keys [rate bal]
    :or {rate 4.0 bal 250.0}}]
  (let [max-bal (:max-bal config)
        accrual-rate (Float. (format "%.2f" (Float. rate)))
        current-bal (Float. (format "%.2f" (Float. bal)))]
    {:max-bal max-bal
     :rate accrual-rate
     :bal current-bal}))

(comment (leave-info {:rate "3.001", :bal "230"}))

(defn cur-bal-info
  "Build a map of the current leave balance information."
  [leave-inputs]
  (let [today (date-utils/date-now-string)
        end-year (if (= :calendar (:year-end config))
                   (date-utils/end-of-year-str)
                   (:year-end config))
        weeks (float-two
               (date-utils/weeks-left-year
                (date-utils/str->date end-year)))]
    (assoc leave-inputs
           :today today
           :weeks-left-in-year weeks
           :end-year end-year)))

(comment (cur-bal-info {:max-bal 200, :rate 3.0, :bal 230.0}))

(defn new-leave
  "Determine the new leave hours accrued."
  [rate weeks-left]
  (if (< weeks-left 0)
    0.0
    (* weeks-left rate)))

(comment (new-leave 3.0 22.14))

(defn total-leave
  "Total leave balance after new hours are accrued."
  [{:keys [bal rate weeks-left-in-year]}]
  (+ bal (new-leave rate weeks-left-in-year)))

(comment
  (total-leave {:max-bal 200, :rate 3.0, :bal 143.0, :today "2020-07-29", :weeks-left-in-year 22.14}))

(defn leave-lost
  "Calculate how much leave will be lost if none is taken."
  [{:keys [bal max-bal]} end-bal]
  (if (> end-bal bal) (- end-bal max-bal) 0))

(comment
  (leave-lost {:max-bal 200, :rate 3.0, :bal 143.0, :today "2020-07-29", :weeks-left-in-year 22.14} 209.42))

(defn rec-leave
  "Recommended leave to take each week to avoid losing any."
  [{:keys [weeks-left-in-year]} lost-leave]
  (/ lost-leave weeks-left-in-year))

(comment
  (rec-leave {:max-bal 200, :rate 3.0, :bal 143.0, :today "2020-07-29", :weeks-left-in-year 22.14} 9.42))

(defn calc-leave
  "Calculate end of year leave/pto balance."
  [{:keys [body]
    :as request}]
  (log/debug (:request-method request) (:uri request) body)
  (let [leave-inputs (leave-info body)
        today-info (cur-bal-info leave-inputs)
        end-bal (total-leave today-info)
        lost-leave (leave-lost today-info end-bal)
        take-leave (rec-leave today-info lost-leave)]
    {:status 200
     :body (assoc today-info
                  :end-year-bal (float-two end-bal)
                  :lost-if-none-taken (float-two lost-leave)
                  :use-per-week (float-two take-leave))}))

(comment
  (calc-leave {:ssl-client-cert nil, :protocol "HTTP/1.1", :remote-addr "127.0.0.1", :params {}, :headers {"accept" "application/json, */*;q=0.5", "user-agent" "HTTPie/2.2.0", "connection" "keep-alive", "host" "localhost:3000", "accept-encoding" "gzip, deflate", "content-length" "30", "content-type" "application/json"}, :server-port 3000, :content-length 30, :form-params {}, :query-params {}, :content-type "application/json", :character-encoding "UTF-8", :uri "/calc", :server-name "localhost", :query-string nil, :body {:rate "3.0", :bal "230"}, :scheme :http, :request-method :post}))

(compojure/defroutes app-routes
  (compojure/context "/" request
    (compojure/GET "/" [] (show-help request))
    (compojure/POST "/calc" [] (calc-leave request))
    (compojure/GET "/config" [] (show-config request))
    (compojure/GET "/health" [] (health request))
    (compojure/GET "/help" [] (show-help request))
    (route/not-found (str "Path Not Found: " (:uri request)))))

(def app
  (-> app-routes
      (ring-json/wrap-json-body {:keywords? true})
      (ring-json/wrap-json-response)
      (ring-defaults/wrap-defaults ring-defaults/api-defaults)))

(defn -main
  []
  (log/info (format "Starting http-server on port %d." (:http-port config)))
  (if (http-server/run-server app {:port (:http-port config)})
    (log/info "Http-server started.")
    (log/error "Problem starting http-server!")))
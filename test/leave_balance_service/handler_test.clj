(ns leave-balance-service.handler-test
  (:require [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [leave-balance-service.handler :as handler]
            [ring.mock.request :as mock]))

(deftest root-route-test
  (testing "route '/'"
    (let [response (handler/app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (= true (string/starts-with? (:body response)
                                       "Leave/PTO Calculator API"))))))

(deftest calc-route-test
  (testing "route '/calc'"
    (let [response (handler/app (mock/request :post "/calc"))]
      (is (= (:status response) 200))
      (is (= true (string/includes? (:body response) "end-year-bal")))
      (is (= true (string/includes? (:body response) "lost-if-none-taken")))
      (is (= true (string/includes? (:body response) "use-per-week"))))))

(deftest config-route-test
  (testing "route '/config'"
    (let [response (handler/app (mock/request :get "/config"))]
      (is (= (:status response) 200))
      (is (= true (string/includes? (:body response) "max-bal")))
      (is (= true (string/includes? (:body response) "http-port")))
      (is (= true (string/includes? (:body response) "year-end"))))))

(deftest health-route-test
  (testing "route '/health'"
    (let [response (handler/app (mock/request :get "/health"))]
      (is (= (:status response) 200))
      (is (= (:body response) "{\"healthy\":true}")))))

(deftest help-route-test
  (testing "route '/help'"
    (let [response (handler/app (mock/request :get "/help"))]
      (is (= (:status response) 200))
      (is (= true (string/starts-with? (:body response)
                                       "Leave/PTO Calculator API"))))))

(deftest invalid-route-test
  (testing "route '/invalid'"
    (let [response (handler/app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

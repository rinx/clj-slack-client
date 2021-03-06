(ns clj-slack-client.connectivity-test
  (:require
   [clojure.test :refer :all]
   [clj-slack-client.connectivity :refer :all]
   [clj-slack-client.web :refer :all]
   [manifold.stream :as stream]
   [cheshire.core :as json]))


(def test-api-token (->> "test-api-token.txt"
                         slurp
                         clojure.string/trim))

(def get-api-response @#'clj-slack-client.web/get-api-response)

(deftest web-api-connectivity-test

  (testing "send web api.test, receive reply"
    (let [test-args {:foo "bar", :fizz "buzz"}
          http-response (api-test test-args)
          api-response (get-api-response http-response)]
      (is (= (:status http-response) 200))
      (is (:ok api-response))
      (map #((is (= (get-in [:args (key %)] api-response)
                    (val %))))
           test-args))))


(deftest rtm-api-connectivity-test

  (testing "rtm.start response is ok"
    (let [rtm-start-response (rtm-start test-api-token)]
      (is (:ok rtm-start-response))

      (testing "websocket connection is successful"
        (let [ws-stream (connect-websocket-stream (:url rtm-start-response))
              first-event-json @(stream/take! ws-stream)
              first-event (json/parse-string first-event-json true)]
          (is (= (:type first-event) "hello")))))))


(ns onyx-issues.web
  (:require [cheshire.core :as json])
  (:use ring.middleware.json)
  (:use compojure.core)
  (:use [ring.util.response :only [redirect]])
  (:use ring.mock.request)
  (:use [ring.middleware session])
  (:use [onyx-issues.core :only [with-token login get-issues set-issue-rank]])) 

;; Set the base url for our api
(def base-url "/api")

(defn url [uri]
  (str base-url uri)) 

;; Helper function for responses
(defn response [body]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body body})

(defroutes api-routes
  (context "/api" []
    (GET "/" [session]
      (response {:message "Hello World"}))

    (POST "/login" {{username "username" password "password"} :params session :session}
      (let [token (login username password)]
        (-> (redirect (url "/issues"))
            (assoc :session (assoc session :token token)))))

    (GET "/issues" []
      (response (get-issues)))
           
    (POST "/issues/rank-issue" {{id "id" rank "rank"} :params}
      (set-issue-rank rank id)
      (redirect (url "/issues")))))

;; Middleware

(defn wrap-authentication [app]
  (fn [req]
    (let [token (:token (:session req))
          uri   (:uri req)]
    (if (or (= (url "/login") uri) token)
      (with-token token (app req))
      {:status 403}))))

(defn wrap-debug [app]
  (fn [req]
    (println req)
    (app req)))

(defn wrap-exception [app]
  (fn [req]
    (try (app req)
      (catch Exception e
         {:status 500
          :body "Exception caught"}))))

;; Define fully wrapped app

(def app (-> api-routes
           wrap-json-params 
           wrap-json-response
           wrap-authentication
           wrap-debug
           wrap-session 
           ;;wrap-exception
           identity
           ))

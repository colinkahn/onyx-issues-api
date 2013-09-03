(ns onyx-issues.core
  (:require [clj-http.client :as client]
            [aleph.redis :as redis])) 

;; Requests to GitHub

(def ^{:private true :dynamic true} *token* nil)

(defmacro with-token [token & body] 
 `(binding [*token* ~token] ~@body))

(defn login [username password]
  (-> (client/post "https://api.github.com/authorizations"
        {:basic-auth [username password]
         :body "{\"scopes\":[\"repo\"]}"
         :content-type :json 
         :as :json
         :throw-entire-message? true})
    :body
    :token))

(defn github-issues []
  (:body
    (client/get "https://api.github.com/issues"
      {:headers {"Authorization" (str "token " *token*)}
       :as :json
       :throw-entire-message? true})))

;; Helper Functions

(defn pairs [coll]
  (lazy-seq
    (when-let [s (seq coll)]
      (cons (take 2 s) (pairs (nthnext s 2))))))

(defn tuples-into-map [coll]
  (->> coll (map vec) (into {})))

;; Database operations

(def db (redis/redis-client {:host "localhost"}))

(defn get-issues-with-rank []
  @(db [:zrange :ranked-issues 0 -1 :withscores]))

(defn set-issue-rank [rank url]
  @(db [:zadd :ranked-issues rank url]))

;; Getting Ranked Issues

(defn assoc-rank [issue rankings]
  (if-let [rank (get rankings (:url issue))]
    (assoc issue :rank (read-string rank))
    (assoc issue :rank 0 :is_new true)))

(defn get-issues []
  (let [issues (github-issues)
        rankings (-> (get-issues-with-rank) pairs tuples-into-map)]
    (->> issues
      (map #(-> % (assoc-rank rankings))) 
      (sort-by :rank)
      reverse)))

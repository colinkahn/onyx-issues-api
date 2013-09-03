(defproject onyx-issues "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-ring "0.8.3"]
            [lein-iclojure "1.2"]]
  :ring {:handler onyx-issues.web/app}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.7.6"]
                 [clj-redis "0.0.12"]
                 [aleph "0.3.0"]
                 [cupboard "1.0beta1"]
                 [compojure "1.1.5"]
                 [ring-mock "0.1.3"] 
                 [alembic "0.2.0"]
                 [ring/ring-json "0.2.0"]
                 [cheshire "5.2.0"] 
                 ])

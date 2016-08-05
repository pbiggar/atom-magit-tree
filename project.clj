(defproject treecommit "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.89"]
                 [funcool/cuerdas "0.8.0"]
                 [binaryage/devtools "0.8.0"]]

  :npm {:dependencies [[source-map-support "*"]
                       [gift "0.9.0"]]
        :root :root}

  :clean-targets ["target"
                  "out"
                  "treecommit.js"
                  "node_modules"]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-npm "0.6.1"]]

  :source-paths ["src/cljs" "src/clj" "test/cljs" "test/clj"]
  :profiles {:dev {:source-paths ["src/dev"]
         :dependencies [[thheller/shadow-build "1.0.215"]
                        [thheller/shadow-devtools "0.1.42"]]}}

  :cljsbuild
  {:builds
   [{:id :plugin
     :source-paths ["src/cljs" "src/clj" "test/cljs" "test/clj"]
     :compiler {:optimizations :whitespace
                :target :nodejs
                :pretty-print true
                :main treecommit.core
                :cache-analysis true
                ;                :source-map true
                :parallel-build true
                ;                :output-dir "./plugin/lib/out"
                :output-to "./plugin/lib/main.js"}}]})

(ns build
  (:require [shadow.cljs.build :as cljs]
            [shadow.cljs.umd :as umd]
            [shadow.devtools.server :as devtools]
            [clojure.java.io :as io]))

(defn- plugin-setup []
  (-> (cljs/init-state)
      (cljs/find-resources-in-classpath)
      (umd/create-module
       {:activate 'treecommit.core/activate
        :deactivate 'treecommit.core/deactivate
        :serialize 'treecommit.core/serialize}
       {:output-to "plugin/lib/treecommit.js"})))


(defn release []
  (-> (plugin-setup)
      (cljs/compile-modules)
      (cljs/closure-optimize :simple)
      (umd/flush-module))
  :done)

(defn dev-repl []
  (-> (plugin-setup)
      (devtools/start-loop
        {:before-load 'treecommit.core/stop
         :after-load 'treecommit.core/start
         :reload-with-state true
         :console-support true
         :node-eval false}
        (fn [state modified]
          (-> state
              (cljs/compile-modules)
              (umd/flush-unoptimized-module))))))

(defn dev []
  (-> (plugin-setup)
      (cljs/enable-source-maps)
      (cljs/watch-and-repeat!
       (fn [state modified]
         (-> state
             (cljs/compile-modules)
             (umd/flush-unoptimized-module))))))


(defn -main [& args]
  (dev-repl))

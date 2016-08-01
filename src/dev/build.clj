(ns build
  (:require [shadow.cljs.build :as cljs]
            [shadow.cljs.umd :as umd]
            [shadow.devtools.server :as devtools]
            [clojure.java.io :as io]))

(defn- plugin-setup []
  (-> (cljs/init-state)
      (cljs/set-build-options
       {:node-global-prefix "global.treecommit"})
      (cljs/find-resources-in-classpath)
      (umd/create-module
       {:activate 'treecommit.core/activate
        :deactivate 'treecommit.core/deactivate}
       {:output-to "plugin/lib/treecommit.js"})))

(defn release []
  (-> (plugin-setup)
      (cljs/compile-modules)
      (cljs/closure-optimize :simple)
      (umd/flush-module))
  :done)

(defn dev []
  (-> (plugin-setup)
      (cljs/enable-source-maps)
      (cljs/watch-and-repeat!
       (fn [state modified]
         (-> state
             (cljs/compile-modules)
             (umd/flush-unoptimized-module))))))

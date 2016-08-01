(ns treecommit.core
  (:require [cljs.nodejs :as nodejs]))

(nodejs/enable-util-print!)

(def ^:dynamic active-editor ; for testing
  (fn []
    (-> js/atom .workspace .getActiveEditor)))

(defn stage [])

(defn activate [state]
  (println "Activating treecommit")
  (.add js/atom.commands "atom-workspace" "treeview:stage", stage))

(defn deactivate [state]
  (println "Deactivating from treeview"))


(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "run_tests" #(treeview.test-core/run-tests)))

(defn -main [& args]
  (println "running main()"))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly 0))

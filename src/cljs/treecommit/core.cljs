(ns treecommit.core
  (:require-macros [util.inspect :refer [inspect]])
  (:require [cljs.nodejs :as nodejs]
            [devtools.core :as devtools]
            [cljs.pprint]
            [cuerdas.core :as str]))

(nodejs/enable-util-print!)
(devtools/install!)

(def ^:dynamic active-editor ; for testing
  (fn []
    (-> js/atom .workspace .getActiveEditor)))

(def gift (nodejs/require "gift"))

(defn firstA [fake-array]
  (aget fake-array 0))

(defn in-treeview? [e]
  (-> e .-target .-classList firstA (= "tree-view")))

(defn get-repo []
  (-> js/atom .-project .getRepositories firstA))

(defn get-selected-path [e]
  (-> e
      .-target
      (.querySelector ".tree-view .selected [data-path]")
      (.getAttribute "data-path")))

(defn callback [op file]
  (fn [err stdout stderr]
    (when err
      (println (str "Error found when doing '" op "' on " file ": \n" err))
      (println (str "stdout was: " stdout))
      (println (str "stderr was: " stderr)))))

(defn stage [e]
  (when (in-treeview? e)
    (let [repo (get-repo)
          gifted (gift (-> repo .-path (str/strip-suffix "/.git")))
          file (get-selected-path e)]

      (.add gifted #js[file]
            (callback "stage" file)))))

(defn unstage [e]
  (when (in-treeview? e)
    (let [repo (get-repo)
          gifted (gift (-> repo .-path (str/strip-suffix "/.git")))
          file (get-selected-path e)]

      (.reset gifted (str "HEAD " #js[file])
              (callback "unstage" file)))))

(defn commit []
  (println "commiting"))

(defn open-chunks []
  (println "open-chunks"))

(def CompositeDisposable (-> "atom" nodejs/require .-CompositeDisposable))

(def disposables (CompositeDisposable.))

(defn register-commands []
  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:stage", stage))

  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:unstage", unstage))

  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:commit", commit))

  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:open-chunks", open-chunks)))

(defn unregister-commands []
  (.dispose disposables))

(defn activate [state]
  (set! js/saved nil)
  (register-commands))


(defn deactivate []
  (println "Deactivating from treecommit")
  (unregister-commands))

(defn serialize [])

(defn stop []
  (let [state (serialize)]
    (deactivate)
    state))

(defn start [state]
  (activate state))

(defn -main [& args]
  (println "running main()"))


;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly 0))

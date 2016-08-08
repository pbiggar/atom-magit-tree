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
    (-> js/atom .-workspace .getActiveTextEditor)))

(def gift (nodejs/require "gift"))


(defn firstA [fake-array]
  (aget fake-array 0))

(defn in-treeview? [e]
  (-> e .-target .-classList firstA (= "tree-view")))

;; TODO: it won't always be the first repo
;;  for projectPath, i in atom.project.getPaths()
;;     if goalPath is projectPath or goalPath.indexOf(projectPath + path.sep) is 0
;;       return atom.project.getRepositories()[i]
(defn get-repo []
  (-> js/atom .-project .getRepositories firstA))

(defn get-path []
  (-> (active-editor)
      .getPath))

(defn callback [op file]
  (fn [err stdout stderr]
    (when err
      (println (str "Error found when doing '" op "' on " file ": \n" err))
      (println (str "stdout was: " stdout))
      (println (str "stderr was: " stderr)))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn stage []
  (let [repo (get-repo)
        gifted (gift (-> repo .-path (str/strip-suffix "/.git")))
        path (get-path)]

    (.add gifted #js[path]
          (callback "stage" path))))

(defn unstage []
  (let [repo (get-repo)
        gifted (gift (-> repo .-path (str/strip-suffix "/.git")))
        path (get-path)]

    (.reset gifted (str "HEAD " #js[path])
            (callback "unstage" path))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; disposables and event handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CompositeDisposable (-> "atom" nodejs/require .-CompositeDisposable))
(def disposables (CompositeDisposable.))

(defn register-commands []
  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:stage-file", stage)
        (.add js/atom.commands "atom-workspace" "treecommit:unstage-file", unstage)))


(defn free-disposibles []
  (.dispose disposables))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Initialization and deinitialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn activate [state]
  (set! js/saved nil)
  (register-commands))

(defn deactivate []
  (println "Deactivating from treecommit")
  (free-disposibles))

(defn serialize [])



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; REPL stuff
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stop []
  (let [state (serialize)]
    (deactivate)
    state))

(defn start [state]
  (activate state))

(defn -main [& args]
  (println "running main()"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; noop - needed for :nodejs CLJS build
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(set! *main-cli-fn* (constantly 0))

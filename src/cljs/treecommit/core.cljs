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

;; TODO: it won't always be the first repo
;;  for projectPath, i in atom.project.getPaths()
;;     if goalPath is projectPath or goalPath.indexOf(projectPath + path.sep) is 0
;;       return atom.project.getRepositories()[i]
(defn get-repo []
  (-> js/atom .-project .getRepositories firstA))

(defn get-selected-path []
  (-> js/atom
      .-document
      (.querySelector ".tree-view .selected [data-path]")
      (.getAttribute "data-path")))

(defn callback [op file]
  (fn [err stdout stderr]
    (when err
      (println (str "Error found when doing '" op "' on " file ": \n" err))
      (println (str "stdout was: " stdout))
      (println (str "stderr was: " stderr)))))


;; TODO: there must be a better way to get this.
(defn get-treeview-target []
  (-> js/atom
      .-document
      (.querySelector "ol.tree-view")))


(defn dispatch-treeview-command [command]
  (let [target (get-treeview-target)]
    (-> js/atom
        .-commands
        (.dispatch target command))))

(defn get-gifted []
  (gift (-> (get-repo) .-path (str/strip-suffix "/.git"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn stage []
  (let [gifted (get-gifted)
        path (get-selected-path)]
    (.add gifted #js[path]
          (callback "stage" path))))

(defn unstage []
  (let [gifted (get-gifted)
        path (get-selected-path)]
    (.reset gifted (str "HEAD " #js[path])
            (callback "unstage" path))))

(defn move-up []
  (dispatch-treeview-command "core:move-up"))

(defn move-down []
  (dispatch-treeview-command "core:move-down"))

(defn open-selected []
  (dispatch-treeview-command "tree-view:open-selected-entry"))

(defn toggle-modified-files []
  (println "trigger")
  (dispatch-treeview-command "tree-view:toggle-vcs-unmodified-files"))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Initialization and deinitialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CompositeDisposable (-> "atom" nodejs/require .-CompositeDisposable))
(def disposables (CompositeDisposable.))

(defn register-commands []
  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:stage-selected", stage)
        (.add js/atom.commands "atom-workspace" "treecommit:unstage-selected", unstage)
        (.add js/atom.commands "atom-workspace" "treecommit:move-treeview-up", move-up)
        (.add js/atom.commands "atom-workspace" "treecommit:move-treeview-down", move-down)
        (.add js/atom.commands "atom-workspace" "treecommit:open-treeview-selected", open-selected)
        (.add js/atom.commands "atom-workspace" "treecommit:toggle-vcs-unmodified-files", toggle-modified-files)))


(defn free-disposibles []
  (.dispose disposables))


(defn activate [state]
  (set! js/saved nil)
  (register-commands))

(defn deactivate []
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

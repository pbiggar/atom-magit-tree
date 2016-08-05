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

  ;  for projectPath, i in atom.project.getPaths()
  ;     if goalPath is projectPath or goalPath.indexOf(projectPath + path.sep) is 0
  ;       return atom.project.getRepositories()[i]
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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; views
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;           [MD]   not updated
; M        [ MD]   updated in index
; A        [ MD]   added to index
; D         [ M]   deleted from index
; R        [ MD]   renamed in index
; C        [ MD]   copied in index
; [MARC]           index and work tree matches
; [ MARC]     M    work tree changed since index
; [ MARC]     D    deleted in work tree
; -------------------------------------------------
; D           D    unmerged, both deleted
; A           U    unmerged, added by us
; U           D    unmerged, deleted by them
; U           A    unmerged, added by them
; D           U    unmerged, deleted by us
; A           A    unmerged, both added
; U           U    unmerged, both modified
; -------------------------------------------------
; ?           ?    untracked
; !           !    ignored

(def GIT_STATUS_INDEX_NEW        0)
(def GIT_STATUS_INDEX_MODIFIED   1)
(def GIT_STATUS_INDEX_DELETED    2)
(def GIT_STATUS_INDEX_RENAMED    3)
(def GIT_STATUS_INDEX_TYPECHANGE 4)
(def GIT_STATUS_WT_NEW           7)
(def GIT_STATUS_WT_MODIFIED      8)
(def GIT_STATUS_WT_DELETED       9)
(def GIT_STATUS_WT_TYPECHANGE    10)
(def GIT_STATUS_WT_RENAMED       11)
(def GIT_STATUS_WT_UNREADABLE    12)
(def GIT_STATUS_IGNORED          14)
(def GIT_STATUS_CONFLICTED       15)

(defn bit-test-flipped [i s]
  (bit-test s i))

(defn update-statuses [& args]
  (inspect args))

(defn update-status [pathInfo]
  (let [path (.-path pathInfo)
        status (.-pathStatus pathInfo)
        col1 (condp bit-test-flipped status
                    GIT_STATUS_INDEX_NEW "A"
                    GIT_STATUS_INDEX_MODIFIED "M"
                    GIT_STATUS_INDEX_DELETED "D"
                    GIT_STATUS_INDEX_RENAMED "R"
                    GIT_STATUS_IGNORED "!"
                    GIT_STATUS_WT_NEW "?"
                    " ")
        col2 (condp bit-test-flipped status
                    GIT_STATUS_WT_NEW "?"
                    GIT_STATUS_WT_MODIFIED "M"
                    GIT_STATUS_WT_DELETED "D"
                    GIT_STATUS_WT_RENAMED "R"
                    GIT_STATUS_CONFLICTED "C"
                    GIT_STATUS_IGNORED "!"
                    " ")
        msg (str col1 col2)]

    (inspect msg))

  ;; TODO: find the data-path for the file, set the class from there. Remove old classes first

  )


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; disposables and event handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CompositeDisposable (-> "atom" nodejs/require .-CompositeDisposable))
(def disposables (CompositeDisposable.))

(defn register-commands []
  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:stage", stage)
        (.add js/atom.commands "atom-workspace" "treecommit:unstage", unstage)))

(defn subscribe-to-repo []
  (let [repo (get-repo)]
    (.add disposables
          (.onDidChangeStatuses repo update-statuses)
          (.onDidChangeStatus repo update-status))))

(defn free-disposibles []
  (.dispose disposables))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Initialization and deinitialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn activate [state]
  (set! js/saved nil)
  (register-commands)
  (subscribe-to-repo))

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

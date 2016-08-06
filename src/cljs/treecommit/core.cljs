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

;; TODO: it won't always be the first repo
;;  for projectPath, i in atom.project.getPaths()
;;     if goalPath is projectPath or goalPath.indexOf(projectPath + path.sep) is 0
;;       return atom.project.getRepositories()[i]
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

(defn update-status* [pathInfo]
  (println "updating status" pathInfo)
  (let [path (.-path pathInfo)
        status (.-pathStatus pathInfo)
        index-status (condp bit-test-flipped (inspect status)
                            GIT_STATUS_INDEX_NEW "A"
                            GIT_STATUS_INDEX_MODIFIED "M"
                            GIT_STATUS_INDEX_DELETED "D"
                            GIT_STATUS_INDEX_RENAMED "R"
                            GIT_STATUS_IGNORED "!"
                            GIT_STATUS_WT_NEW "?"
                            nil)
        workdir-status (condp bit-test-flipped status
                              GIT_STATUS_WT_NEW "?"
                              GIT_STATUS_WT_MODIFIED "M"
                              GIT_STATUS_WT_DELETED "D"
                              GIT_STATUS_WT_RENAMED "R"
                              GIT_STATUS_CONFLICTED "C"
                              GIT_STATUS_IGNORED "!"
                              nil)
        workdir-class (when workdir-status (str "treecommit-working-" workdir-status))
        index-class (when index-status (str "treecommit-index-" index-status))
        query-string (str ".tree-view [data-path=\"" path "\"]")

        ;; elem will be null if it's hidden
        ]
    (when-let [elem (-> js/atom .-document (.querySelector query-string))]
      (let [existing (-> elem .-className (.split " "))
            existing (filter #(not (.startsWith % "treecommit")) existing)
            existing (concat existing [workdir-class index-class])]
        (aset elem "className" (.join (clj->js existing) " "))))))


(defn update-status [source]
  (fn [p]
    (println "updating status - " source)
    (when p

      (update-status* p))))

(defn set-initial-statuses []
  (let [repo (get-repo)]
    (doseq [[path status] (-> repo .-statuses js->clj)]
      ((update-status "initial") (js-obj "path" (-> repo .-path (str/strip-suffix ".git") (str path))
                             "pathStatus" status)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; disposables and event handlers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CompositeDisposable (-> "atom" nodejs/require .-CompositeDisposable))
(def disposables (CompositeDisposable.))

(defn register-commands []
  (.add disposables
        (.add js/atom.commands "atom-workspace" "treecommit:stage", stage)
        (.add js/atom.commands "atom-workspace" "treecommit:unstage", unstage)))

; onDidStatusChange on a file
; onDidStatusChange
; dir -> onDidAddEntries
; onDidRemoveEntries

; dirs emit:
  ;   @emitter.emit('did-destroy')
  ;
  ; onDidDestroy: (callback) ->
  ;   @emitter.on('did-destroy', callback)
  ;
  ; onDidStatusChange: (callback) ->
  ;   @emitter.on('did-status-change', callback)
  ;
  ; onDidAddEntries: (callback) ->
  ;   @emitter.on('did-add-entries', callback)
  ;
  ; onDidRemoveEntries: (callback) ->
  ;   @emitter.on('did-remove-entries', callback)
  ;
  ; onDidCollapse: (callback) ->
  ;   @emitter.on('did-collapse', callback)
  ;
  ; onDidExpand: (callback) ->
  ;   @emitter.on('did-expand', callback)


(defn subscribe-to-repo []
  (let [repo (get-repo)]
    (.add disposables
          (.onDidChangeStatuses repo (update-status "changeStatuses - plural"))
          (.onDidChangeStatus repo (update-status "changeStatus")))))

(defn free-disposibles []
  (.dispose disposables))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Initialization and deinitialization
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn activate [state]
  (set! js/saved nil)
  (register-commands)
  (subscribe-to-repo)
  (set-initial-statuses))

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

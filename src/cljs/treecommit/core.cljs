(ns treecommit.core
  (:require-macros [util.inspect :refer [inspect]])
  (:require [cljs.nodejs :as nodejs]
            [cljs.pprint]))

(nodejs/enable-util-print!)

(def ^:dynamic active-editor ; for testing
  (fn []
    (-> js/atom .workspace .getActiveEditor)))

(defn stage []
  (println "staging")
  ; get the panel we're on - that's treeview
  (inspect (.getActivePaneItem js/atom.workspace))

  ;  entries = @getShortNames @treeView.getSelectedEntries()
  ;   if @getRepository()
  ;     options =
  ;       cwd: @getRepository().getWorkingDirectory()
  ;       timeout: 30000
  ;       maxBuffer: 1048576
  ;     me = @
  ;     exec 'git add '+entries.join(' '),options, (err,stdout,stderr) =>
  ;       if err
  ;         me.showMessage '<pre>'+'ERROR '+err+'</pre>', 5000
  ;       else if stderr
  ;         me.showMessage '<pre>'+'ERROR '+stderr+" "+stdout+'</pre>', 5000
  ;       else
  ;         me.showMessage '<pre>'+'file added to stage'+"\n"+'</pre>', 1000
  ;       (me.tualoGitContextView.gitStatus(entry) for entry in entries)


  (println "staging"))

(defn unstage []
  (println "unstaging"))

(defn commit []
  (println "commiting"))

(defn open-chunks []
  (println "open-chunks"))


(defn activate [state]
  (println "Activating treecommit")
  (.add js/atom.commands "atom-workspace" "treecommit:stage", stage)
  (.add js/atom.commands "atom-workspace" "treecommit:unstage", unstage)
  (.add js/atom.commands "atom-workspace" "treecommit:commit", commit)
  (.add js/atom.commands "atom-workspace" "treecommit:open-chunks", open-chunks))

(defn deactivate [state]
  (println "Deactivating from treecommit"))


(set! js/module.exports
  (js-obj "activate" activate
          "deactivate" deactivate
          "stage" stage
          "unstage" unstage
          "commit" commit
          "open-chunks" open-chunks
          "run_tests" #(treecommit.test-core/run-tests)))

(defn -main [& args]
  (println "running main()"))

;; noop - needed for :nodejs CLJS build
(set! *main-cli-fn* (constantly 0))

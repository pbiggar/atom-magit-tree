(ns treecommit.test-core
  (:require-macros [jasmine.macros :refer [describe it expect]]
                   [util.inspect :refer [inspect]])
  (:require [cljs.nodejs :as nodejs]
            [treecommit.core :as tc]
            [clojure.string :as str]))


(defn setup-tests []
  (js/beforeEach
   (fn []
     (js/waitsForPromise
      #(js/atom.packages.activatePackage "language-clojure"))
     (js/waitsForPromise
      #(js/atom.workspace.open "a.clj")))))


(defn run_tests []
  (setup-tests)

  (describe "test suite should work"
            (it "should activate"
                (expect (= true (js/atom.packages.isPackageLoaded "language-clojure")))
                (expect (= true (js/atom.packages.isPackageActive "language-clojure"))))))

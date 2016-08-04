(ns util.inspect)

(defmacro with-ns [ns & body]
  `(do
     (create-ns ~ns)
     (binding [*ns* (the-ns ~ns)]
       (refer 'clojure.core)
       ~@(map (fn [form] `(eval '~form)) body))))

(defmacro inspect [value]
  `(do
      (when (= nil (aget js/global "saved"))
        (set! js/saved (cljs.core/clj->js []))
        (set! js/saved_clj (cljs.core/clj->js [])))

    (let [value# (quote ~value)
          result# ~value]

      (.push js/saved_clj result#)
      (.push js/saved (cljs.core/clj->js result#))

      (println (str value# " "
                    "is "
                    (with-out-str (cljs.pprint/pprint result#))
                    "\n"))
      result#)))

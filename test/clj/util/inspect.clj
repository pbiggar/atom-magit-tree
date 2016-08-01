(ns util.inspect)

(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace
  object or a symbol.  This makes it possible to define functions in
  namespaces other than the current one."
  [ns & body]
  `(do
     (create-ns ~ns)
     (binding [*ns* (the-ns ~ns)]
       (refer 'clojure.core)
       ~@(map (fn [form] `(eval '~form)) body))))

(defmacro inspect
  "prints the expression '<name> is <value>', and returns the value.
    Increments a metric if called in a production environment."
  [value]
  `(do
    (let [value# (quote ~value)
          result# ~value]
      (println (str value# " "
                       "is "
                       (with-out-str (cljs.pprint/pprint result#))
                       "\n"))
      result#)))

(ns klipse.common.stopify
  (:refer-clojure :exclude [eval])
  (:require-macros
   [gadjett.core :refer [dbg]]
   [purnam.core :refer [!> ! ?]])
  (:require
   [cljs.core.async :refer [<! chan put!]]))

(defn compile [cb source]
  (let [asyncRun (!> js/stopify.stopifyLocally source)]
    (do
      ;; Set the function called on the last expression
      (! asyncRun.g.callbackLast cb)
      asyncRun)))

;; Stopify runtime captures exceptions. The callback handles them correctly.
(defn- stopify-cb [cb result]
  (when (= (? result.type) "exception")
    (cb (str "Exception: " (? result.value)))))

(defn run [cb asyncRun]
  (!> asyncRun.run (partial stopify-cb cb))
  "")

(defn eval [source cb]
  (->> source
       (compile cb)
       (run cb)))


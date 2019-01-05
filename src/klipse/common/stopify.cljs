(ns klipse.common.stopify
  (:require-macros
   [gadjett.core :refer [dbg]]
   [purnam.core :refer [!> ! ?]])
  (:require
   [cljs.core.async :refer [<! chan put!]]))

(defn stopify-compile [cb source]
  (let [asyncRun (!> js/stopify.stopifyLocally source)]
    (do
      ;; Set the function called on the last expression
      (! asyncRun.g.callbackLast cb)
      asyncRun)))

;; Stopify runtime captures exceptions. The callback handles them correctly.
(defn stopify-cb [cb result]
  (when (= (? result.type) "exception")
    (cb (str "Exception: " (? result.value)))))

(defn stopify-run [c asyncRun]
  (!> asyncRun.run (partial stopify-cb c))
  "")


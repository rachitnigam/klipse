(ns klipse.common.stopify
  (:require-macros
   [gadjett.core :refer [dbg]]
   [purnam.core :refer [!> !]]
   [cljs.core.async.macros :refer [go go-loop]])
    (:require
   [cljs.core.async :refer [<! chan put!]]))

(defn stopify-compile [source]
  (let [asyncRun (!> js/stopify.stopifyLocally source)]
    (do
      ;; Set the function called on the last expression
      (! asyncRun.g.callbackLast js/console.log)
      asyncRun)))

;; Stopify runtime captures exceptions. The callback handles them correctly.
(defn stopify-cb [c result]
  (if (= (aget result "type") "exception")
    (put! c (str "Exception: " (aget result "value")))
    (put! c (str result))))

(defn stopify-run [c asyncRun]
  (!> asyncRun.run (partial stopify-cb c))
  "")


(ns klipse.common.stopify
  (:refer-clojure :exclude [eval])
  (:require-macros
   [gadjett.core :refer [dbg]]
   [purnam.core :refer [!> ! ?]])
  (:require
   [cljs.core.async :refer [<! chan put!]]))

(defn compile [cb source]
  (let [asyncRun (!> js/stopify.stopifyLocally source)]
    #_(!> js/console.info asyncRun.code)
    (! asyncRun.g.callbackLast cb)
    (! asyncRun.g.setTimeout   (fn [cb timeout]
                                 (js/console.info "aaaa setTimeout")))
    asyncRun))

;; Stopify runtime captures exceptions. The callback handles them correctly.
(defn- stopify-cb [cb result]
  (when (= (? result.type) "exception")
    (cb (str "Exception: " (? result.value)))))

(defn run [cb asyncRun]
  (!> asyncRun.run (partial stopify-cb cb))
  asyncRun)

(defn stop [asyncRun]
  (!> asyncRun.pause #(js/console.info "paused")))

(defn eval [source cb]
  (->> source
       (compile cb)
       (run cb)))

(defn eval-async [source]
  (let [c (chan)]
    (eval source #(put! c %))
    c))


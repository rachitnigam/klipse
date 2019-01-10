(ns klipse.lang.clojure
  (:require-macros
   [gadjett.core :refer [dbg]]
   [cljs.core.async.macros :refer [go]])
  (:require [klipse-clj.lang.clojure :refer [str-eval-async str-compile-async]]
            [klipse-clj.repl :refer [reset-state-compile! reset-ns-compile!]]
            [klipse-clj.lang.clojure.io :refer [*klipse-settings* *verbose?* *cache-buster?*]]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [<! chan put!]]
            [klipse.common.stopify :as stopify]
            [cljs.js :as cljsjs]
            [klipse.common.registry :refer [codemirror-mode-src register-mode scripts-src]]
            [klipse.utils :refer [url-parameters verbose? klipse-settings setup-container!]]))


(set! *klipse-settings* (klipse-settings))
(set! *verbose?* (verbose?))
(set! *cache-buster?* (boolean (read-string (or (:cache-buster (url-parameters)) "false"))))

(defn eval-clj [exp opts]
  (let [c (chan)]
    (go
      (let [source (<! (str-eval-async exp (assoc opts
                                                  :js-eval (fn [source]
                                                             (stopify/eval-async source))
                                                  :setup-container-fn setup-container!
                                                  :verbose (verbose?))))]))
    c))

 (def eval-opts {:editor-in-mode   "clojure"
                :editor-out-mode  "clojure"
                :eval-fn          (fn [exp opts] (eval-clj exp opts))
                :external-scripts [(codemirror-mode-src "clojure")
                                   "https://viebel.github.io/klipse/repo/js/stopify-full.bundle.js"]
                :comment-str      ";"})

(def compile-opts {:editor-in-mode   "clojure"
                   :editor-out-mode  "javascript"
                   :external-scripts [(codemirror-mode-src "clojure") (codemirror-mode-src "javascript")]
                   :eval-fn          (fn [exp opts] (str-compile-async exp (assoc opts :verbose (verbose?))))
                   :comment-str      ";"})

(register-mode "eval-clojure" "selector" eval-opts)
(register-mode "transpile-clojurescript" "selector_js" compile-opts)

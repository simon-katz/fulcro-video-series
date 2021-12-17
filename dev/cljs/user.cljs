(ns cljs.user
  (:require
   [app.client :as client]))

;;;; ___________________________________________________________________________
;;;; `post-interactive-eval-hook`

(def cljs-hook-count
  ;; This helps with testing `post-interactive-eval-hook` functionality.
  (atom 0))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn post-interactive-eval-hook []
  (swap! cljs-hook-count inc)
  (client/init))

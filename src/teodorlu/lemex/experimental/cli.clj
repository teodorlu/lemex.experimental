;; # lemex CLI
;;
;; An example CLI for your own little memex.

(ns teodorlu.lemex.experimental.cli
  (:require
   [teodorlu.lemex.experimental.api :as lemex]
   [babashka.cli :as cli]))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn ^:private requiring-resolve-orelse
  [sym orelse]
  ;; Lemex supports JVM Clojure and Babashka.
  ;;
  ;; Clerk is used to develop Lemex. But we don't want to bundle Clerk in shell
  ;; scripts. So we use this variant of requiring-resolve that allows a
  ;; fallback. We'll use this to show values with Clerk when Clerk is available,
  ;; and do nothing when Clerk is not loaded.
  (try
    (requiring-resolve sym)
    (catch Exception _ orelse)))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def table (requiring-resolve-orelse 'nextjournal.clerk/table (constantly nil)))
^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def code (requiring-resolve-orelse 'nextjournal.clerk/code (constantly nil)))

;; Required operations:

^{:nextjournal.clerk/visibility {:code :hide}}
(table
 {:head ["operation" "definition"]
  :rows (map (fn [[op def]]
               [(code op) def])
             [["lemex create" "create a new doc"]
              ["lemex link"   "select a link"]
              ["lemex docs"   "list all documents"]
              ["lemex build"  "make static html"]])})

;; Using lemex from a CLI will be a bit different from using Lemex from a UI.

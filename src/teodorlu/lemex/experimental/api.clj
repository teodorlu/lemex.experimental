;; # lemex API
;;
;; A toolkit for creating your own little memex.

(ns teodorlu.lemex.experimental.api
  (:require
   [clojure.edn :as edn]
   [babashka.process :as process]
   [babashka.fs :as fs]))

;; ## lemex components
;;
;; lemex is a toolbox for building your own little memex out of simple pieces:
;;
;;  - prose in plaintext
;;  - metadata in EDN
;;  - Pandoc for document conversion
;;  - Babasha & Clojure for composition
;;
;; lemex requires a specific folder organization:
;;
;;     $ tree example
;;     example
;;     ├── babashka
;;     │   ├── doc.edn
;;     │   └── index.md
;;     └── memex
;;         ├── doc.edn
;;         └── index.md
;;
;; Each document is a folder. There is no nesting. Each document folder contains
;; a `doc.edn` (metadata) and an `index.md` (the doc content).

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn ^:private requiring-resolve-orelse [sym orelse]
  (try
    (requiring-resolve sym)
    (catch Exception _ orelse)))

(def table (requiring-resolve-orelse 'nextjournal.clerk/table (constantly nil)))

;; ## lemex vocabulary

^{:nextjournal.clerk/visibility {:code :hide}}
(table
 {:head ["term" "definition"]
  :rows [[:slug    "Identifies the doc, is used in URLs. Unique. May change. Follows [A-Za-z0-9-]+."]
         [:uuid    "Identifies the doc. Is used in internal references. Immutable, unique."]
         [:title   "Identifies the doc for humans. Normal prose. Uniqueness recommended."]
         [:created "Created date. Example: 2023-06-11."]]})

(defn ^:private expand-meta [{:keys [root slug]}]
  (assert root) (assert slug)
  (assert (fs/directory? (fs/file root slug)))
  (fs/exists? (fs/file root slug "doc.edn"))
  (assoc (edn/read-string (slurp (fs/file root slug "doc.edn")))
         :slug slug))

(defn docs
  "Given a lemex root, list all documents with metadata"
  [{:keys [root]}]
  (assert root)
  (let [root (-> root fs/absolutize fs/canonicalize)]
    (->> (fs/glob root "*/doc.edn")
         (map fs/parent)
         (map fs/file-name)
         (map (fn [slug]
                {:slug slug
                 :root root}))
         (map expand-meta))))

(docs {:root "example/"})

;; # lemex API
;;
;; A toolkit for creating your own little memex.

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns teodorlu.lemex.experimental.api
  (:require
   [clojure.edn :as edn]
   [babashka.process :as process]
   [babashka.fs :as fs]
   [clojure.data.json :as json]))

;; ## lemex is plain text, plain data and some code
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
;; a `doc.edn` (metadata) and a document. Readers for Markdown (`index.md`) and
;; Org-mode (`index.org`) are provided.

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(defn ^:private requiring-resolve-orelse [sym orelse]
  (try
    (requiring-resolve sym)
    (catch Exception _ orelse)))

^{:nextjournal.clerk/visibility {:code :hide :result :hide}}
(def table (requiring-resolve-orelse 'nextjournal.clerk/table (constantly nil)))

;; ## lemex vocabulary

^{:nextjournal.clerk/visibility {:code :hide}}
(table
 {:head ["term" "definition"]
  :rows [[:slug    "Identifies the doc, is used in URLs. Unique. May change. Follows [A-Za-z0-9-]+."]
         [:uuid    "Identifies the doc. Is used in internal references. Immutable, unique."]
         [:title   "Identifies the doc for humans. Normal prose. Uniqueness recommended."]
         [:created "Created date. Example: 2023-06-11."]]})

;; ## listing documents

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

;; ## lemex readers: into Pandoc
;;
;; a lemex reader takes a file path.
;; If the file path is something the reader can convert to Pandoc data, it returns that data.
;; Otherwise, it returns nil.
;;
;; In other words, a lemex reader is a function from a file path to pandoc data or nil.

(defn markdown-reader [path]
  (when (re-matches #".*md" path)
    (let [path (-> path fs/absolutize fs/canonicalize)
          process-output (process/shell {:out :string} "pandoc" "-i" (str path) "-t" "json")]
      (when (= 0 (:exit process-output))
        (json/read-str (:out process-output) :key-fn keyword)))))

(markdown-reader "example/babashka/index.md")

(defn orgmode-reader [path]
  (when (re-matches #".*org" path)
    (let [path (-> path fs/absolutize fs/canonicalize)
          process-output (process/shell {:out :string} "pandoc" "-i" (str path) "-t" "json")]
      (when (= 0 (:exit process-output))
        (json/read-str (:out process-output) :key-fn keyword)))))

(orgmode-reader "example/simple-made-easy/index.org")

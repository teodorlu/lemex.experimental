;; # lemex API
;;
;; A toolkit for creating your own little memex.

^{:nextjournal.clerk/toc true
  :nextjournal.clerk/visibility {:code :hide :result :hide}}
(ns teodorlu.lemex.experimental.api
  (:require
   [babashka.fs :as fs]
   [babashka.process :as process]
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [clojure.walk]))

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
;;     ├── memex
;;     │   ├── doc.edn
;;     │   └── index.md
;;     └── simple-made-easy
;;         ├── doc.edn
;;         └── index.org
;;
;; Each document is a folder. There is no nesting. Each document folder contains
;; a `doc.edn` (metadata) and a document. Readers for Markdown (`index.md`) and
;; Org-mode (`index.org`) are provided.

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

;; ## lemex vocabulary

^{:nextjournal.clerk/visibility {:code :hide}}
(table
 {:head ["term" "definition"]
  :rows [[:slug    "Identifies the doc, is used in URLs. Unique. May change. Follows [A-Za-z0-9-]+."]
         [:uuid    "Identifies the doc, is used in internal references. Immutable, unique."]
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
(markdown-reader "example/borkdude/index.md")

(defn orgmode-reader [path]
  (when (re-matches #".*org" path)
    (let [path (-> path fs/absolutize fs/canonicalize)
          process-output (process/shell {:out :string} "pandoc" "-i" (str path) "-t" "json")]
      (when (= 0 (:exit process-output))
        (json/read-str (:out process-output) :key-fn keyword)))))

(orgmode-reader "example/simple-made-easy/index.org")

;; ## links that support moving documents

(defn metadata-index
  "Indexes all metadata on UUID"
  [{:keys [root]}]
  (let [documents (docs {:root root})]
    (into (sorted-map)
          (for [d (filter :uuid documents)]
            [(:uuid d) (dissoc d :uuid)]))))

{:nextjournal.clerk/visibility {:code :show :result :show}}
(metadata-index {:root "example/"})

(defn resolve-links [pandocjson index]
  (let [uuid->slug (fn [uuid] (:slug (get index uuid)))
        pandoc-link? (fn [pandoc] (= "Link" (:t pandoc)))
        pandoc-link-target-path [:c 2 0]
        pandoc-link-target (fn [link] (get-in link pandoc-link-target-path))
        replace-link (fn [x]
                       (if (pandoc-link? x)
                         (let [link x
                               uuid (last (str/split (pandoc-link-target link) #":"))
                               slug (uuid->slug uuid)]
                           (if slug
                             (assoc-in link pandoc-link-target-path (str "../" slug "/"))
                             link))
                         x))]
    (update pandocjson :blocks (fn [blocks] (clojure.walk/postwalk replace-link blocks)))))

(resolve-links (markdown-reader "example/borkdude/index.md")
               (metadata-index {:root "example/"}))

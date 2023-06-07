;; # lemex API
;;
;; A toolkit for creating your own little memex.

(ns teodorlu.lemex.experimental.api
  (:require
   [clojure.edn :as edn]
   [babashka.process :as process]
   [babashka.fs :as fs]))

;; lemex is a toolbox for building your own little memex out of simple pieces:
;;
;;  - prose in plaintext
;;  - metadata in EDN
;;  - Pandoc for document conversion
;;  - Babasha & Clojure for composition

(defn docs
  "Given a lemex root, list all documents with metadata"
  [{:keys [root]}]
  (assert root)
  (let [root (-> root fs/absolutize fs/canonicalize)
        doc-folders (map fs/parent (fs/glob root "**/doc.edn"))]

    doc-folders))

(docs {:root "example/"})


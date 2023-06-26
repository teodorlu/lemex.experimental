(ns teodorlu.lemex.experimental.api2
  (:require
   [babashka.fs :as fs]
   [clojure.edn :as edn]))

;; I just made api2.
;; Why?
;;
;; I wrote api.clj before I had any real use cases in mind.
;; And for the first use case I wanted, I needed to change something.
;; Specifically, I had a memex where the metadata files were named meta.edn, not doc.edn.
;;
;; This became a problem when I wanted to start rewriting links!
;;
;; Examples:
;;
;;     :lemex/meta-edn-file "doc.edn"
;;     :lemex/meta-edn-file "meta.edn"

(defn validate! [opts]
  (assert (:root opts))
  ;; (assert (:meta-edn-glob opts))
  (assert (:meta-edn-file opts))
  (assert (fs/exists? (:root opts))))

(defn conform [lemex]
  (cond-> lemex
    true
    (update :root (comp fs/canonicalize fs/absolutize))

    (not (:meta-edn-glob lemex))
    (assoc :meta-edn-glob (str "*/" (:meta-edn-file lemex)))))

(defn lemex [opts]
  (validate! opts)
  (conform opts))

(comment
  ;; normal call:
  (lemex {:root "example/"
          :meta-edn-file "doc.edn"})

  ;; override the glob to scan recursively, not just one level:
  (lemex {:root "example/"
          :meta-edn-file "doc.edn"})

  (lemex {:root "example/"
          :meta-edn-file "doc.edn"
          :meta-edn-glob "**/doc.edn"}))

(defn ^:private expand-meta [lemex slug]
  (validate! lemex)
  (when (fs/exists? (fs/file (:root lemex) slug "doc.edn"))
    (assoc (edn/read-string (slurp (fs/file (:root lemex) slug "doc.edn")))
           :slug slug)))

(defn docs [lemex]
  (->> (fs/glob (:root lemex) (:meta-edn-glob lemex))
       (map fs/parent)
       (map fs/file-name)
       (map (fn [slug] (expand-meta lemex slug)))))

(comment
  (let [example (lemex {:root "example/"
                        :meta-edn-file "doc.edn"})]
    (docs example))

  )
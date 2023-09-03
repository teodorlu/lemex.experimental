(ns teodorlu.lemex.experimental.cli2
  (:require
   [babashka.process]
   [clojure.string :as str]
   [babashka.fs :as fs]
   [clojure.edn :as edn]))

;; link management

;; link                   - displays a link
;; link --format markdown - display a link, format as markdown
;; link --browse          - browse a link

(defn fzf
  "Choose a thing with fzf

  (fzf [\"apples\" \"pears\" \"pizza\")
  ;; => \"apples\"     ; depending on the user's choice!

  returns nil on failure."
  [choices]
  (let [fzf-result (babashka.process/shell {:out :string
                                            :in (str/join "\n" choices)
                                            :continue true}
                                           "fzf")]
    (when (= 0 (:exit fzf-result))
      (str/trim (:out fzf-result)))))

;; .config/teodorlu.lemex.experimental/provider.d/ ... edn files ...
;;
;;   provider for Benjamin
;;   provider for iterbart
;;   provider for play.teod.eu
;;   provider for Sindre.me
;;   provider for Mikrobloggeriet
;;
;; strip EDN, get ID

;; Provider

{:fn '(fn [] links)}

;; Provider must return a map with a :url, rest is optional.

;; lemex link --format markdown
;; lemex link --format markdown --provider iterapp
;; lemex provider add URL_OR_FILE

(fs/xdg-config-home "teodorlu.lemex.experimental")

(defn provider-links [provider]
  (when-let [provider-fn (:fn provider)]
    ((eval provider-fn))))

(defn providers-path []
  (fs/file (fs/xdg-config-home "teodorlu.lemex.experimental")
           "providers.d"))

(defn provider-path [slug]
  (fs/file (providers-path)
           (str slug ".edn")))

(defn providers []
  (->>
   (fs/list-dir (providers-path))
   (map #(-> {:slug (str/replace (fs/file-name %) #"\.edn$" "")}))))


(defn add-provider [slug url]
  (fs/create-dirs (providers-path))
  (spit (provider-path slug) (slurp url)))

(defn provider-links2 [provider]
  (let [path (provider-path (:slug provider))]
    (when (fs/exists? path)
      (when-let [provider-fn (:fn (edn/read-string (slurp path)))] ((eval provider-fn))))))

(defn -main [& args]
  (println "lemex"))

(comment
  (add-provider "rich-hickey-greatest-hits"
                "https://raw.githubusercontent.com/teodorlu/lemex.experimental/master/contrib/provider.d/rich-hickey-greatest-hits.edn")
  (add-provider "play.teod.eu"
                "https://raw.githubusercontent.com/teodorlu/lemex.experimental/master/contrib/provider.d/play.teod.eu.edn")

  (->> (providers)
       shuffle
       first
       provider-links2)
  )

;; lemex roulette --browse -- get dropped into a random place

;; to install this file with bbin:
;;
;;   bbin install . --as lemex --main-opts '["-m" "teodorlu.lemex.experimental.cli2"]'

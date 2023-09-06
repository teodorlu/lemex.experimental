(ns teodorlu.lemex.experimental.cli2
  (:require
   [babashka.fs :as fs]
   [babashka.process]
   [clojure.data.json :as json]
   [clojure.edn :as edn]
   [clojure.string :as str]))

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

(defn add-provider [slug slurpable]
  (fs/create-dirs (providers-path))
  (spit (provider-path slug) (slurp slurpable)))

(defn provider-links2
  ([provider] (provider-links2 provider {}))
  ([provider env]
   (let [path (provider-path (:slug provider))]
     (when (fs/exists? path)
       (when-let [provider-fn (:fn (edn/read-string (slurp path)))]
         ((eval provider-fn) env))))))

(let [f
      (fn [env]
        (->> (:links ((get env 'clojure.data.json/read-str) (slurp "https://iterbart.app.iterate.no/data/links.json") :key-fn keyword))
             (map (fn [l]
                    {:url (:href l)
                     :title (:title l)}))))]
  (f {'clojure.data.json/read-str clojure.data.json/read-str}))

(->> (:links (clojure.data.json/read-str (slurp "https://iterbart.app.iterate.no/data/links.json") :key-fn keyword))
     (map (fn [l]
            {:url (:href l)
             :title (:title l)})))

(defn -main [& args]
  (println "lemex"))

(def provider-rich-hickey-greatest-hits {:slug "rich-hickey-greatest-hits"})
(def provider-iterbart {:slug "iterbart"})
(def provider-play-teod-eu {:slug "play.teod.eu"})

(comment
  (defn github-raw-path [path] (str "https://raw.githubusercontent.com/teodorlu/lemex.experimental/master/" path))

  (add-provider "rich-hickey-greatest-hits" (github-raw-path "contrib/provider.d/rich-hickey-greatest-hits.edn"))
  (add-provider "rich-hickey-greatest-hits" "contrib/provider.d/rich-hickey-greatest-hits.edn")

  (add-provider "play.teod.eu" (github-raw-path "contrib/provider.d/play.teod.eu.edn"))
  (add-provider "play.teod.eu" "contrib/provider.d/play.teod.eu.edn")

  (add-provider "iterbart" (github-raw-path "contrib/provider.d/iterbart.edn"))
  (add-provider "iterbart" "contrib/provider.d/iterbart.edn")

  (provider-links2 provider-rich-hickey-greatest-hits)
  (provider-links2 provider-iterbart)
  (provider-links2 provider-play-teod-eu)

  (->> (providers)
       shuffle
       first
       provider-links2)

  (providers)
  ;; => ({:slug "rich-hickey-greatest-hits"} {:slug "iterbart"} {:slug "play.teod.eu"})

  (provider-links2
   (second
    (providers)))
  )

;; lemex roulette --browse -- get dropped into a random place

;; to install this file with bbin:
;;
;;   bbin install . --as lemex --main-opts '["-m" "teodorlu.lemex.experimental.cli2"]'

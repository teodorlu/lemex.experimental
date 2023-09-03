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

(let [example-provider (quote {:fn (fn []
                                     (let [data [["Simple Made Easy" "SxdOUGdseq4"]
                                                 ["The Language of the System" "ROor6_NGIWU"]
                                                 ["Maybe Not" "YR5WdGrpoug"]
                                                 ["Design, Composition, and Performance" "QCwqnjxqfmY"]
                                                 ["Design in Practice" "c5QF2HjHLSE"]
                                                 ["The Value of Values" "-I-VpPMzG7c"]]]
                                       (for [[title url] data]
                                         {:title title :url url})))})]
  (provider-links example-provider))

(let [baseurl "https://play.teod.eu"
      index-url (str baseurl "/index/big.edn")
      page-url (fn [slug]
                 (str baseurl "/" slug "/"))]
  (->>
   (edn/read-string (slurp index-url))
   (filter (fn [page]
             (and
              (= :en (:lang page))
              (= :ready-for-comments
                 (:readiness page)))))
   (map (fn [page] (select-keys page [:title :slug])))
   (map (fn [{:keys [title slug]}]
          {:title title
           :url (page-url slug)}))))

(let [example-provider (quote {:fn
                               (fn []
                                 (let [baseurl "https://play.teod.eu"
                                       index-url (str baseurl "/index/big.edn")
                                       page-url (fn [slug]
                                                  (str baseurl "/" slug "/"))]
                                   (->>
                                    (edn/read-string (slurp index-url))
                                    (filter (fn [page]
                                              (and
                                               (= :en (:lang page))
                                               (= :ready-for-comments
                                                  (:readiness page)))))
                                    (map (fn [page] (select-keys page [:title :slug])))
                                    (map (fn [{:keys [title slug]}]
                                           {:title title
                                            :url (page-url slug)})))))})]
  (provider-links example-provider))

;; => (nil :deprecated :forever-incomplete :noindex :ready-for-comments :wtf-is-this)

(let [links-url "https://play.teod.eu/index/big.edn"
      example-provider (quote {:fn (fn []

                                     (let [data [["Simple Made Easy" "SxdOUGdseq4"]
                                                 ["The Language of the System" "ROor6_NGIWU"]
                                                 ["Maybe Not" "YR5WdGrpoug"]
                                                 ["Design, Composition, and Performance" "QCwqnjxqfmY"]
                                                 ["Design in Practice" "c5QF2HjHLSE"]
                                                 ["The Value of Values" "-I-VpPMzG7c"]]]
                                       (for [[title url] data]
                                         {:title title :url url})))})]
  (provider-links example-provider))

(defn url->inferred-slug [url]
  (-> url
      (str/split #"/")
      last
      (str/replace #"\.edn$" "")))

(defn providers-path []
  (fs/file (fs/xdg-config-home "teodorlu.lemex.experimental")
           "providers.d"))

(defn provider-path [slug]
  (fs/file (providers-path)
           (str slug ".edn")))


(->>
 (fs/list-dir (providers-path))
 (map #(-> {:slug (str/replace (fs/file-name %) #"\.edn$" "")})))

(defn providers []
  (->>
   (fs/list-dir (providers-path))
   (map #(-> {:slug (str/replace (fs/file-name %) #"\.edn$" "")}))))


(defn add-provider [url]
  (fs/create-dirs (providers-path))
  (let [slug (url->inferred-slug url)]
    (spit (provider-path slug) (slurp url))))

(defn -main [& args]
  (println "lemex"))

(comment
  (add-provider "https://raw.githubusercontent.com/teodorlu/lemex.experimental/master/contrib/provider.d/rich-hickey-greates-hits.edn")
  (providers))

;; lemex roulette --browse -- get dropped into a random place

;; to install this file with bbin:
;;
;;   bbin install . --as lemex --main-opts '["-m" "teodorlu.lemex.experimental.cli2"]'

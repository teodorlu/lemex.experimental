;; # lemex CLI
;;
;; An example CLI for your own little memex.

(ns teodorlu.lemex.experimental.cli
  (:require
   [teodorlu.lemex.experimental.api :as lemex]
   [babashka.cli :as cli]))

(cli/parse-opts ["--name" "teodor"])

;; # lemex API
;;
;; A toolkit for creating your own little memex.

(ns teodorlu.lemex.experimental.api
  (:require
   [clojure.edn :as edn]
   [babashka.process :as process]
   [babashka.fs :as fs]))

;;

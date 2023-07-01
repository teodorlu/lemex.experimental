;;; lemex.el --- Description -*- lexical-binding: t; -*-
;;
;; Copyright (C) 2023 Teodor Heggelund

;; Dear reader!
;;
;; Per 2023-07-01, this file does not work as a standalone emacs-lisp module.
;;
;; It depends on teod-play.el, which again depends on org-roam. teod-play.el is
;; per 2023-07-01 available publicly, and there are no plans to release it
;; publicly.
;;
;; That's because I plan to migrate /away/ from that code.
;;
;; Planned steps:
;;
;; 1. stop using M-x teod-play-..., and start using M-x lemex-... instead
;; 2. gradually rewrite the code here to cut the dependency on teod-play
;; 3. then share with others.

(require 'teod-play)

(defun lemex-find ()
  "Navigate to a lemex doc"
  (interactive)
  (teod-play-page-find))

(defun lemex-link ()
  "Link to a lemex doc"
  (interactive)
  (teod-play-page-insert-link))

(provide 'lemex)
;;; lemex.el ends here

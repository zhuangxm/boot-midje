(ns zhuangxm.boot-midje
  {:boot/export-tasks true}
  (:require [boot.core :refer :all]))

;;original code copy from http://hoplon.discoursehosting.net/t/boot-and-midje-autotest/372
;; by Michal Buczko http://hoplon.discoursehosting.net/users/mbuczko
;;add throw exception when midje test failure to make boot-clj failure too.

(defn failure-count [lines]
  (reduce (fn [[fail-count error-count] line]
            [(+ fail-count (if (re-matches #"\e(.*?)FAIL\e.*" line) 1 0))
             (+ error-count (if (re-matches #"\e(.*?)ERROR\e.*" line) 1 0))] )
          [0 0] lines))

(deftask midje
         "Run midje tests in boot."
         [f filters FILTER edn "midje filters. Only facts matching one or more of the arguments are loaded. Filter arguments:
         :keyword    -- Does the metadata have a truthy value for the keyword?
         \"string\"  -- Does the fact's name contain the given string?
         #\"regex\"  -- Does any part of the fact's name match the regex?
         a function  -- Does the function return a truthy value when given the fact's metadata?"]
         (let [worker  (boot.pod/make-pod (get-env))
               filters  (or filters :all)]
           (cleanup (worker :shutdown))
           (with-pre-wrap fileset
                          (let [result-str (boot.pod/with-eval-in worker
                                                                  (require 'midje.repl 'clojure.test)
                                                                  (with-out-str
                                                                    (binding [clojure.test/*test-out* *out*]
                                                                      (midje.repl/load-facts ~filters))))
                                [fail-count error-count] (failure-count (clojure.string/split-lines result-str))]
                            (println result-str)
                            (when (or (> fail-count 0) (> error-count 0))
                              (throw (ex-info "midje test failure." {:failed  fail-count
                                                                     :error error-count}))))
                          fileset)))
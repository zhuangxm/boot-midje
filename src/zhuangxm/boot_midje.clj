(ns zhuangxm.boot-midje
  {:boot/export-tasks true}
  (:require [boot.core :refer :all]))

;;original code copy from http://hoplon.discoursehosting.net/t/boot-and-midje-autotest/372
;; by Michal Buczko http://hoplon.discoursehosting.net/users/mbuczko
;;add throw exception when midje test failure to make boot-clj failure too.

(defn failure-count [lines]
  (reduce (fn [[fail-count error-count] line]
            [(+ fail-count (if (re-matches #"\e(.*?)FAIL\e.*" line) 1 0))
             (+ error-count (if (re-matches #"\e(.*?)ERROR\e.*" line) 1 0))])
          [0 0] lines))

(deftask old-midje
         "Run midje tests in boot."
         [f filters FILTER edn "midje filters. Only facts matching one or more of the arguments are loaded. Filter arguments:
         :keyword    -- Does the metadata have a truthy value for the keyword?
         \"string\"  -- Does the fact's name contain the given string?
         #\"regex\"  -- Does any part of the fact's name match the regex?
         a function  -- Does the function return a truthy value when given the fact's metadata?"]
         (let [worker  (boot.pod/make-pod (get-env))
               dirs (get-env :directories)]
               ;;filters  (or filters :all)]
           (cleanup (worker :shutdown))
           (with-pre-wrap fileset
                          (let [result-str (boot.pod/with-eval-in worker
                                                                  (require 'midje.repl 'clojure.test 'clojure.tools.namespace.repl)
                                                                  (with-out-str
                                                                    (binding [#'*ns* *ns* clojure.test/*test-out* *out*]
                                                                      (do
                                                                        (apply clojure.tools.namespace.repl/set-refresh-dirs
                                                                                  ~dirs)
                                                                        (clojure.tools.namespace.repl/refresh)
                                                                        (prn "hello test")
                                                                        (prn "sdfkasdjfasd")
                                                                        (midje.repl/load-facts)))))
                                [fail-count error-count] (failure-count (clojure.string/split-lines result-str))]
                            (println result-str)
                            (when (or (> fail-count 0) (> error-count 0))
                              (throw (ex-info "midje test failure." {:failed  fail-count
                                                                     :error error-count}))))
                          fileset)))

(require '[clojure.tools.namespace.repl :as tns])
(require '[midje.emission.state :as s])
(require '[midje.emission.api :as emit])
(require 'clojure.test)

;;refresh code come from https://github.com/samestep/boot-refresh

(deftask midje
  "Reload all changed namespaces on the classpath.
  Throws an exception in the case of failure."
  []
  (with-pass-thru _
    (apply tns/set-refresh-dirs (get-env :directories))
    (with-bindings {#'*ns* *ns* #'clojure.test/*test-out* *out*}
      (let [result (tns/refresh)
            test-result (clojure.test/run-all-tests)
            midje-result (s/output-counters)
            all-result (merge test-result midje-result)]
        (prn all-result)
        (swap! s/output-counters-atom #(merge {:midje-failures 0 :midje-passes 0} %))
        (emit/fact-stream-summary test-result)
        (reset! s/output-counters-atom {})
        (when (instance? Throwable result)
          (throw result))
        (when (or (> (get all-result :midje-failures 0) 0)
                  (> (get all-result :fail 0) 0)
                  (> (get all-result :error 0) 0))
          (throw (ex-info "midje test failure." {})))))))

(set-env!
  :resource-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.7.0"     :scope "provided"]
                  [boot/core           "2.5.5"     :scope "provided"]]
  :repositories [["clojars" {:name "clojars" :url "https://clojars.org/repo"}]])

(task-options!
  pom {:project     'zhuangxm/boot-midje
       :version     "0.1.0"
       :description "Boot task to execute midje test."
       :url         "https://github.com/zhuangxm/boot-midje"
       :scm         {:url "https://github.com/zhuangxm/boot-midje"}
       :license     {"Eclipse Public License"
                     "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build []
         (comp
           (pom)
           (jar)
           (install)))

(deftask deploy []
         (comp
           (build)
           (push :repo "clojars" :gpg-sign false)))

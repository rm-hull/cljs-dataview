(defproject rm-hull/cljs-dataview "0.0.1-SNAPSHOT"
  :clojurescript? true
  :description "A ClojureScript library for asynchronously fetching & dicing remote binary objects"
  :source-paths ["src"]
  :url "https://github.com/rm-hull/cljs-dataview"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:url "git@github.com:rm-hull/cljs-dataview"}
  :min-lein-version "2.3.4"
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-cljsbuild "1.0.3"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.278.0-76b25b-alpha"]
                 [rm-hull/cljs-test "0.0.8-SNAPSHOT"]]
  :cljsbuild {
    :test-commands {"phantomjs" ["phantomjs" "target/unit-test.js"]}
    :builds {
      :dev {
        :source-paths ["src" "example"]
        :incremental? true
        :compiler {
          :output-to "target/example.js"
          :source-map "target/example.map"
          :static-fns true
          :optimizations :whitespace
          :pretty-print true }}
      :test {
        :source-paths ["src" "test"]
        :incremental? true
        :compiler {
          :output-to "target/unit-test.js"
          :source-map "target/unit-test.map"
          :static-fns true
          :optimizations :whitespace
          :pretty-print true }}}})
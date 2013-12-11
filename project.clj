(defproject rm-hull/cljs-dataview "0.0.1-SNAPSHOT"
  :description "A ClojureScript library for asynchronously fetching & dicing remote binary objects"
  :source-paths ["src/cljs"]
  :url "https://github.com/rm-hull/cljs-dataview"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :min-lein-version "2.3.2"
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-cljsbuild "0.3.4"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]]
  :cljsbuild {
    :builds [{
      :id "cljs-dataview"
        :source-paths ["src"]
        :compiler {
          :optimizations :whitespace
          :static-fns true
          :pretty-print false
          :output-to "target/cljs-dataview.js"
          :source-map "target/cljs-dataview.js.map"}}]})
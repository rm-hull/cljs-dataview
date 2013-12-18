(defproject rm-hull/cljs-dataview "0.0.1-SNAPSHOT"
  :clojurescript? true
  :description "A ClojureScript library for asynchronously fetching & dicing remote binary objects"
  :source-paths ["src/cljs"]
  :url "https://github.com/rm-hull/cljs-dataview"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :scm {:url "git@github.com:rm-hull/cljs-dataview"}
  :min-lein-version "2.3.2"
  :global-vars {*warn-on-reflection* true}
  :plugins [[lein-cljsbuild "1.0.0"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
		 [org.clojure/clojurescript "0.0-2120"]
		 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
		 [prismatic/cljs-test "0.0.6"]]
  :cljsbuild {
    :test-commands {"phantomjs" ["phantomjs" "target/unit-test.js"]}
    :builds {
      :dev {:source-paths ["src" "example"]
	    :incremental? true
	    :compiler {:output-to "target/example.js"
		       :optimizations :whitespace
		       :pretty-print true
                       ;:source-map true
                       }}
      :test {:source-paths ["src" "test"]
             :incremental? true
             :compiler {:output-to "target/unit-test.js"
                        :optimizations :whitespace
                        :pretty-print true }}}
              })
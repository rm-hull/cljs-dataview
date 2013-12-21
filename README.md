# cljs-dataview  [![Build Status](https://secure.travis-ci.org/rm-hull/cljs-dataview.png)](http://travis-ci.org/rm-hull/cljs-dataview)

A ClojureScript library for asynchronously fetching &amp; dicing remote binary objects

_PREAMBLE_: TODO

### Pre-requisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.3.4 or above installed.

### Building

To build and install the library locally, run:

    $ lein clean 
    $ lein cljsbuild once
    $ lein install

### Testing

To run the tests in a browser, ensure the generated javascript files are up-to-date,
and open ```resources/run-tests.html``` in a browser - this executes the tests and
the test results are displayed on the page.

Alternatively, to run using PhantomJS, execute:

    $ lein cljsbuild test

This will only show a tally of failed tests.

### Including in your project

There _will be_ an 'alpha-quality' version hosted at [Clojars](https://clojars.org/rm-hull/cljs-dataview).
For leiningen include a dependency:

```clojure
[rm-hull/cljs-dataview "0.0.1-SNAPSHOT"]
```

For maven-based projects, add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>rm-hull</groupId>
  <artifactId>cljs-dataview</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Basic Usage

### Example: Reading binary STL files

[Binary STL](https://en.wikipedia.org/wiki/STL_\(file_format\)#Binary_STL) files
can be read in and processed with the following code sample (see 
[example/binary_stl.cljs](https://github.com/rm-hull/cljs-dataview/blob/master/example/binary_stl.cljs)
for fully working example). Starting with some necessary pre-amble:

```clojure
(ns binary-stl
  (:require [cljs.dataview.loader :refer [fetch-blob]]
            [cljs.dataview.ops :refer [create-reader read-fixed-string read-float32-le
                                       read-uint16-le read-uint32-le]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def local-url "http://localhost/~rhu/test/torus.stl")
```

The ```torus.stl``` contains polygons for the classic/ubiquitous 3D torus as per:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/shaded/torus.png)

In order to read the binary STL data, we must first define some decoders; so
to create a 3D point, a ```point-spec``` generates an ordered map of _x_, _y_
and _z_ floating-point components from a reader:

```clojure
(defn point-spec [reader]
  (array-map
    :x (read-float32-le reader)
    :y (read-float32-le reader)
    :z (read-float32-le reader)))
```
A _reader_ is a stateful implementation of an 
[IReader](https://github.com/rm-hull/cljs-dataview/blob/master/src/cljs/dataview/ops.cljs#L47)
protocol -- this has methods that traverse a javascript 
[DataView](https://developer.mozilla.org/en-US/docs/Web/API/DataView?redirectlocale=en-US&redirectslug=Web%2FJavaScript%2FTyped_arrays%2FDataView)
sequentially as bytes, 16-bit & 32-bit integers, floating-point numbers and 
fixed-width strings. The reified reader object may also implement 
[IRandomAccess](https://github.com/rm-hull/cljs-dataview/blob/master/src/cljs/dataview/ops.cljs#L55) 
so that _seek_/_rewind_/_tell_ operations (similar to that used with Unix file
descriptors) are also available.

Secondly, a triangle is composed of a [surface normal](https://en.wikipedia.org/wiki/Surface_normal),
followed by 3 vertex co-ordinates and some attributes in the form of a 16-bit
word -- the normal and the vertexes are constructed out of repeated application
of the ```point-spec``` above. Note that since the ```point-spec``` has side-effects
it is important to call _doall_ for force evaluation, otherwise _repeatedly_ will
act lazily.

```clojure
(defn triangle-spec [reader]
  (array-map
    :normal (point-spec reader)
    :points (doall (repeatedly 3 #(point-spec reader)))
    :attributes (read-uint16-le reader)))
```
Finally, the overall STL spec header consists of 80 padded characters, 
followed by a triangle count: notice how this determines how many times the
```triangle-spec``` is subsequently invoked in the body:

```clojure
(defn stl-spec [reader]
  (array-map
    :header (read-fixed-string reader 80 :ascii)
    :triangles (doall (repeatedly
                 (read-uint32-le reader) ; <== triangle-count
                 #(triangle-spec reader)))))
```
So in order to fetch the binary data, ```fetch-blob``` below returns a 
_core.async_ channel, from which a javascript DataView is produced. In order 
to then _sort-of_ treat the DataView object as an input stream, it is wrapped in a 
reader (by virtue of the ```create-reader``` function), which is then passed on
to the ```stl-spec```: hence the binary data is progressively diced 
into a persistent map structure.

```clojure
(go
  (->
    (<! (fetch-blob local-url))
    (create-reader)
    (stl-spec)
    (println)))
```
The resulting output (curtailed and slightly formatted):

```clojure
{:header "Torus, created with https://github/rm-hull/wireframes [October 16 2013]         ", 
 :triangles (
   {:normal {:x -0.9972646832466125, :y -0.05226442590355873, :z 0.05226442590355873}, 
    :points ({:x 4, :y 0, :z 0} 
             {:x 3.9945218563079834, :y 0.10452846437692642, :z 0} 
             {:x 3.972639560699463, :y 0.10452846437692642, :z -0.4175412356853485}), 
    :attributes 0} 
   {:normal {:x -0.9972646832466125, :y -0.05226442590355873, :z 0.05226442590355873},
    :points ({:x 4, :y 0, :z 0} 
             {:x 3.972639560699463, :y 0.10452846437692642, :z -0.4175412356853485} 
             {:x 3.9780876636505127, :y 0, :z -0.4181138575077057}), 
    :attributes 0} 
   {:normal {:x -0.9863678216934204, :y -0.1562253087759018, :z 0.05169334635138512}, 
    :points ({:x 3.9945218563079834, :y 0.10452846437692642, :z 0} 
             {:x 3.978147506713867, :y 0.2079116851091385, :z 0} 
             {:x 3.956354856491089, :y 0.2079116851091385, :z -0.4158296585083008}), 
    :attributes 0} 

...
)}
```

## TODO

* ~~Implement ```read-string``` with a delimiter~~
* Implement IWriter protocol
* Proper EOD handling/testing
* [Gloss](https://github.com/ztellman/gloss)-style codecs
* Integrate CORS handling with http://www.corsproxy.com/
* Test framework, travis integration & unit tests

## Known Bugs

* ~~Fix bug in binary STL example where repeatedly was not being forced.~~
* Triangle test causes PhantomJS to segfault.

## References

* http://www.html5rocks.com/en/tutorials/file/xhr2/
* https://developer.mozilla.org/en-US/docs/Web/API/DataView?redirectlocale=en-US&redirectslug=Web%2FJavaScript%2FTyped_arrays%2FDataView
* https://developer.mozilla.org/en-US/docs/Web/JavaScript/Typed_arrays?redirectlocale=en-US&redirectslug=JavaScript%2FTyped_arrays
* http://stackoverflow.com/questions/327685/is-there-a-way-to-read-binary-data-in-javascript
* https://github.com/tbuser/thingiview.js
* https://github.com/MarcoPolo/servant

## License

The MIT License (MIT)

Copyright (c) 2013 Richard Hull

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

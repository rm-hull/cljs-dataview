# cljs-dataview  [![Build Status](https://secure.travis-ci.org/rm-hull/cljs-dataview.png)](http://travis-ci.org/rm-hull/cljs-dataview)

A ClojureScript library for asynchronously fetching &amp; dicing remote binary objects

_PREAMBLE_: TODO

### Pre-requisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.3.2 or above installed.

### Building

To build and install the library locally, run:

    $ lein cljsbuild once
    $ lein cljsbuild test
    $ lein install

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
can be read in and processed with the following code sample (see [example.cljs](https://github.com/rm-hull/cljs-dataview/blob/master/src/cljs/dataview/example.cljs) 
for fully working example).

```clojure
(ns cljs.dataview.example
  (:require [cljs.dataview.loader :refer [fetch-blob]]
            [cljs.dataview.ops :refer [create-reader read-string read-float32-le
                                       read-uint16-le read-uint32-le]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def local-url "http://localhost/~rhu/test/torus.stl")
```

The ```torus.stl``` contains polygons for the classic/ubiquitous 3D torus as per:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/shaded/torus.png)

In order to read the binary data, we must first define some decoder specs, so to 
create a 3D point, a ```point-spec``` is an ordered map of _x_, _y_ and _z_ floating-point
components:

```clojure
(defn point-spec [reader]
  (array-map
    :x (read-float32-le reader)
    :y (read-float32-le reader)
    :z (read-float32-le reader)))
```
Similarly, a triange is composed of a [surface normal](https://en.wikipedia.org/wiki/Surface_normal),
followed by 3 vertex co-ordinates, and some attributes:

```clojure
(defn triangle-spec [reader]
  (array-map
    :normal (point-spec reader)
    :points (repeatedly 3 #(point-spec reader))
    :attributes (read-uint16-le reader)))
```
Finally, the overall STL spec header consists of 80 padded characters, 
followed by a triangle count: notice how this determines how many times the
```triangle-spec``` is subsequently invoked in the body:

```clojure
(defn stl-spec [reader]
  (array-map
    :header (read-string reader 80 :ascii)
    :triangles (repeatedly
                 (read-uint32-le reader) ; <== triangle-count
                 #(triangle-spec reader))))
```
So in order to fetch the binary data ```fetch-blob``` returns a channel from which
a javascript [DataView](https://developer.mozilla.org/en-US/docs/Web/API/DataView?redirectlocale=en-US&redirectslug=Web%2FJavaScript%2FTyped_arrays%2FDataView)
is produced. In order to _sort-of_ treat the DataView object as an input stream, 
it is wrapped in a reader, which is then passed to the ```stl-spec```: hence the binary
data is diced into a persistent map structure.

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
{:header Torus, created with https://github/rm-hull/wireframes [October 16 2013]         , 
 :triangles (
   {:normal {:x -0.9972646832466125, :y -0.05226442590355873, :z 0.05226442590355873}, 
    :points ({:x 2.313824024293138e-41, :y 0, :z -6.626643678231403e-16} 
             {:x 1.681875909241491e-27, :y 2.2182554690261854e-41, :z 1.453125} 
             {:x 1.6818757166484964e-27, :y -126587.671875, :z 6.845763387766029e-41}), 
    :attributes 0} 
   {:normal {:x -0.9972646832466125, :y -0.05226442590355873, :z 0.05226442590355873}, 
    :points ({:x 2.313824024293138e-41, :y 0, :z 1.453125} 
             {:x 1.6818757166484964e-27, :y -126587.671875, :z -6.559165828898756e-24} 
             {:x 2.313543764600273e-41, :y 1.678696006310313e-27, :z 6.845903517612461e-41}), 
    :attributes 0} 
   {:normal {:x -0.9863678216934204, :y -0.1562253087759018, :z 0.05169334635138512}, 
    :points ({:x 1.681875909241491e-27, :y 2.2182554690261854e-41, :z -2.5642598989143858e-23} 
             {:x -4.846373999329217e+23, :y 2.235911829676678e-41, :z 4.377216100692749e-7} 
             {:x -4.846373639041247e+23, :y -1.5134567764592248e+24, :z 6.845623257919596e-41}),
    :attributes 42559} 

...
)}
```

## TODO

* Integrate CORS handling with http://www.corsproxy.com/

## Known Bugs


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

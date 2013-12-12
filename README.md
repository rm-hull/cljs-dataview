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

_USAGE_: TODO

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

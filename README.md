# boot-midje

A [boot](https://github.com/boot-clj/boot) plugin designed to execute [midje](https://github.com/marick/Midje) test.

the initial code comes from

http://hoplon.discoursehosting.net/t/boot-and-midje-autotest/372
by [Michal Buczko](http://hoplon.discoursehosting.net/users/mbuczko)

When using in CI, like drone , it's very important to make boot-clj failure when tests failed to make sure the CI knows there is something wrong. Otherwise the CI will not report any error.

This plugin will throw exception when midje test failed to make boot-clj failure too.

## Usage

````clj
(set-env! :dependencies '[[zhuangxm/boot-midje "0.1.2" :scope "test"]])

(require '[zhuangxm.boot-midje :refer [midje]])

````

using command below to execute midje test

```bash
boot midje

;;auto refresh test
boot watch speak midje
```

## release history
* 0.1.2 base on tools.namespace refresh to enabled auto test.
* 0.1.1 add clojure.test support
* 0.1.0  first version

## License

Copyright © 2016

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# SDriver/XPath

A drop-in `javax.xml.xpath.XPathFactory` that only lets through the XPath queries it has
already seen. It is the prototype from

> Dimitris Mitropoulos, Vassilios Karakoidas and Diomidis Spinellis,
> *Fortifying Applications Against XPath Injection Attacks*, MCIS 2009: 4th Mediterranean
> Conference on Information Systems, pp. 1169–1179, 2009.
> [PDF](https://bkarak.wizhut.tech/pubs/pdfs/xpath-injection.pdf)

and it carries over the idea of SDriver, Mitropoulos and Spinellis's JDBC driver that does the
same for SQL, to XPath.

## How it works

Each query gets an **identifier**. It is built from two things: the query with its string
literals and numbers taken out, and the chain of methods that issued it. You run the
application in **training mode** first, and every identifier it produces goes into a
**registry**. After training, a query whose identifier is not in the registry is refused with
an `XPathExpressionException` and logged.

Values don't count. `//user[name='alice']` and `//user[name="bob"]` issued from the same
place have the same identifier. A query whose structure has changed gets a different one, and
so does the same query issued from code that never ran during training.

## Using it

Ask for the factory by name. The rest of the application stays as it is:

```java
XPathFactory xpf = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
        "org.sdriver.xpath.SecureXPathFactory", classLoader);

xpf.setFeature("TrainingMode", true);   // record identifiers
// ... exercise the application ...
xpf.setFeature("TrainingMode", false);  // from here on, unknown queries are refused

XPath xpath = xpf.newXPath();
xpath.evaluate(query, document);
```

| Feature | Effect |
| --- | --- |
| `TrainingMode` | `true` records identifiers; `false` checks them. Applies to XPaths created afterwards. |
| `MemoryRegistry` | Keep identifiers in memory. This is the default. |
| `FlatFileRegistry` | Keep identifiers in a text file, one per line, so a training run carries over to production. The file is `ids.registry` in the working directory, or whatever the `sdriver.xpath.registry` system property names. |

Any other feature goes to the platform's own factory. Every XPath created by one factory
shares that factory's registry. A refusal is logged at `WARNING` on the `org.sdriver.xpath`
logger (`System.Logger`, so it ends up wherever your logging is routed).

## Where it stops

- **Use variables first.** If you bind input with `$name` through an `XPathVariableResolver`,
  it never becomes part of the query text. That is the real fix. SDriver/XPath is a safety net
  for code that still builds queries by concatenating strings.
- **Training has to cover the application.** A legitimate query that never ran during training
  gets refused. The identifier includes the whole call chain, so code that reaches the same
  query by a different route counts as a different caller.
- **A refactoring invalidates the registry.** Renaming or moving a method on the chain changes
  every identifier below it, and you have to train again. The paper says so too.
- **It works at method granularity.** Two queries of the same shape issued from the same method
  share an identifier, even when they are on different lines.
- **It costs time.** See below.

## Building

JDK 17 or later, Maven:

```bash
mvn            # compiles, runs the 20 tests, writes target/sdriver-xpath-2.0.0.jar
```

There are no runtime dependencies.

## Overhead

The paper's measurement: put ten queries in the registry, then call `XPath.compile()` a million
times on one of them, after a warm-up, and take the mean of five runs.
`src/test/java/org/sdriver/xpath/Benchmark.java` does the same:

```bash
mvn -q test-compile
java -cp target/classes:target/test-classes org.sdriver.xpath.Benchmark
```

| | JAXP | SDriver/XPath | Overhead |
| --- | ---: | ---: | ---: |
| 2009, the paper: Core 2 Duo 2.4 GHz, Mac OS X 10.5, Java 1.6 | 21,311 ms | 48,761 ms | 128% |
| 2026, the 2009 code: Apple M4 Max, OpenJDK 26 | ≈1,580 ms | ≈4,400 ms | ≈178% |
| 2026, this code: Apple M4 Max, OpenJDK 26 | ≈1,580 ms | ≈3,540 ms | 121–129% |

The 2026 rows come from three runs each, run in turn on the same machine. The 2009 code was
built from the first commit with `javac --release 8`, against the original fast-md5 jar, and
measured with the same harness. What's left of the overhead is mostly walking the stack, at
about two microseconds a call.

## What changed from 2009

The first commit is the 2009 tree as it was, except for `lib/fast-md5.jar`, a third-party
LGPL library with native code. It isn't redistributed. After that:

- **The build.** Maven and JDK 17 instead of Ant and Java 1.6. The identifier is a SHA-256
  from the JDK instead of fast-md5's MD5, so there are no dependencies left.
- **The query normaliser** was three regular expressions and is now a tokenizer for XPath 1.0.
  The old version had three defects:
  - it removed every `.`, `+` and `-` from the query before anything else, so `.` and `..`,
    or `last-name` and `lastname`, produced the same identifier;
  - it never removed double-quoted literals, so a trained `name="alice"` refused
    `name="bob"`;
  - its number rule removed the operator in front of the number too (`@x=1` and `@x>1`
    collapsed), and it also swallowed names made of hex letters (`count(b)` and `count(c)`
    collapsed).

  The tokenizer replaces each string literal or number with a placeholder and keeps everything
  else.
- **The call chain** records class and method for each frame. It used to record the method
  name alone. It also skips the library's own frames, so `compile` and `evaluate` called from
  the same place give the same identifier.
- **Registries.** The default used to be an inactive registry: training recorded nothing and
  every query was then refused. It is now an in-memory one. Both registries are thread-safe.
  The flat-file registry used to:
  - loop on `BufferedReader.ready()`;
  - print a stack trace on its first run, because the file didn't exist yet;
  - rewrite the whole file for every new identifier.

  Now it reads the file once, appends new identifiers, and takes its path from the constructor
  or a system property.
- **Features.** `setFeature("MemoryRegistry", false)` used to switch the registry anyway. Now
  it doesn't. `getFeature` now answers for the library's own features instead of passing them
  to the platform factory, which rejected them.
- **Logging.** The paper describes logging refused queries, but the prototype never did it.
  Now it does.
- **The wrapped factory** comes from `XPathFactory.newDefaultInstance()`, so the lookup can
  never resolve back to this class.
- **Tests.** The demo `Main` became JUnit tests, plus the benchmark above.

## Licence

BSD 3-Clause. See [LICENSE](LICENSE). Copyright Vassilios Karakoidas and Dimitrios
Mitropoulos.

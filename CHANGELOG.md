# Changelog

All notable changes to SDriver/XPath. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and versions follow
[Semantic Versioning](https://semver.org/).

## [2.0.1] - 2026-10-04

### Fixed

- **A negative number in a trained value slot was refused.** The normaliser replaced `5`
  with a placeholder but kept the `-` of `-5` as an operator, so `//a[@x=-5]` and
  `//a[@x=5]` had different identifiers. A `-` that can only be a sign (at the start, or
  after an operator, `(`, `[` or `,`) is now folded into the number. Subtraction
  (`@x - 5`, the first `-` of `3 - -5`) is still kept as an operator.
- **A refused query could forge log lines.** The refused query was written verbatim to the
  log and to the exception message, so a newline in user input started what looked like a new
  log record. Control characters, line and paragraph separators and backslashes are now
  escaped (`\n`, `\r`, `\t`, `\\`, `\uXXXX`) in both places.
- **`FlatFileRegistry` could lose an identifier.** It added the identifier to memory before
  writing it to the file. When the write failed, the identifier passed as trained for the rest
  of the run, was missing from the file the next time, and a retry never wrote it, because
  the registry thought it already had it. It is now written first and recorded only once the
  write has succeeded.

## [2.0.0] - 2026-10-04

The 2009 prototype, rebuilt. The package, the factory class name and the three feature names
are unchanged.

### Changed

- Maven and JDK 17 instead of Ant and Java 1.6.
- The identifier is a SHA-256 from the JDK instead of fast-md5's MD5, so there are no runtime
  dependencies.
- The call chain records class and method for each frame (it used to record the method name
  alone), and skips the library's own frames, so `compile` and `evaluate` called from the same
  place give the same identifier.
- The default registry is an in-memory one. Both registries are thread-safe.
- `FlatFileRegistry` reads its file once, appends new identifiers, and takes its path from the
  constructor or the `sdriver.xpath.registry` system property.
- The wrapped factory comes from `XPathFactory.newDefaultInstance()`, so the lookup can never
  resolve back to this class.
- The demo `Main` became JUnit tests, plus a benchmark that repeats the paper's measurement.

### Added

- Refused queries are logged at `WARNING` on the `org.sdriver.xpath` logger, as the paper
  describes.

### Fixed

- The query normaliser was three regular expressions with three defects. It removed every
  `.`, `+` and `-` before anything else (`.` and `..`, `last-name` and `lastname` collapsed).
  It never removed double-quoted literals (a trained `name="alice"` refused `name="bob"`). And
  its number rule removed the operator in front of the number too (`@x=1` and `@x>1`
  collapsed), and also swallowed names made of hex letters. It is now an XPath 1.0 tokenizer.
- The default registry was an inactive one, so training recorded nothing and every query was
  then refused.
- `setFeature("MemoryRegistry", false)` switched the registry anyway; `getFeature` passed the
  library's own features to the platform factory, which rejected them.
- `FlatFileRegistry` looped on `BufferedReader.ready()`, printed a stack trace on its first run
  because the file did not exist yet, and rewrote the whole file for every new identifier.

### Removed

- `InactiveRegistry`.
- The bundled `lib/fast-md5.jar`.

## 2009

The prototype published with *Fortifying Applications Against XPath Injection Attacks*
(MCIS 2009). It is the repository's first commit, without `lib/fast-md5.jar`.

[2.0.1]: https://github.com/bkarak/sdriver-xpath/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/bkarak/sdriver-xpath/releases/tag/v2.0.0

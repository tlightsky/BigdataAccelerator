
# Bigdata Accelerator

this is a jetbrains plugin in for datagrip/database tool,
it have 2 features now:

### 1

replace sql param like this:

```SQL
--- $a=1
SELECT $a
```

### 2

sync sql snippet to other files under parent parent folder

* before sync
A.sql
```SQL
--- toSyncSnippet {
SELECT 1
--}
```

B.sql
```SQL
--- toSyncSnippet {
--}
```

* after run sql sync on file A.sql
```SQL
--- toSyncSnippet {
SELECT 1
--}
```

B.sql
```SQL
--- toSyncSnippet {
SELECT 1
--}
```

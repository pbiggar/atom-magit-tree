path = require("path");
process.chdir(__dirname + "/..")

require(path.join(path.resolve("."),"plugin", "lib", "treecommit.js"));

treecommit.test_core.run_tests()

{
  // Look at http://jslinterrors.com/ for more details on errors and to find how to suppress certain errors
  // Options
  "bitwise"       : true,     // Prohibit bitwise operators (&, |, ^, etc.).
  "newcap"        : true,     // Require capitalization of all constructor functions e.g. `new F()`.
  "curly"         : true,     // Require {} for every new block or scope.
  "camelcase"     : true,     // Require all variables to be in camelCase
  "eqeqeq"        : true,     // Require triple equals i.e. `===`.
  "forin"         : true,     // Tolerate `for in` loops without `hasOwnPrototype`.
  "immed"         : true,     // Require immediate invocations to be wrapped in parens e.g. `( function(){}() );`
  "latedef"       : true,     // Prohibit variable use before definition.
  "noarg"         : true,     // Prohibit use of `arguments.caller` and `arguments.callee`.
  "noempty"       : true,     // Prohibit use of empty blocks.
  "nonew"         : true,     // Prohibit use of constructors for side-effects.
  "undef"         : true,     // Require all non-global variables be declared before they are used.
  "unused"        : true,     // An error if a variable is not used
  "trailing"      : true,     // Prohibit trailing whitespaces.
  "indent"        : false,    // Checks for indentation [this is currently off until we find a way to make it slightly better]
  "maxparams"     : 7,        // Max number of parameters a function can take
  "quotmark"      : "single", // all quotations must be single quotes
  "maxcomplexity" : "10",     // max complexity of a function
  "maxlen"        : "150",    // max length of a line
  "indent"        : 4,        // Specify indentation spacing

  // RELAXERS

  "loopfunc"      : true,     // This is a relaxer so it allows you to make functions in a loop when set to true
  "boss"          : false,    // Tolerate assignments inside if, for & while. Usually conditions & loops are for comparison, not assignments.
  "es5"           : true,    // Allow EcmaScript 5 syntax.
  "esnext"        : false,    // Allow ES.next specific features such as `const` and `let`.
  "asi"           : false,    // Tolerate Automatic Semicolon Insertion (no semicolons).
  "debug"         : false,    // Allow debugger statements e.g. browser breakpoints.
  "eqnull"        : false,    // Tolerate use of `== null`.
  "evil"          : false,    // Tolerate use of `eval`.
  "expr"          : false,    // Tolerate `ExpressionStatement` as Programs.
  "funcscope"     : false,    // Tolerate declarations of variables inside of control structures while accessing them later from the outside.
  "globalstrict"  : false,    // Allow global "use strict" (also enables 'strict').
  "iterator"      : false,    // Allow usage of __iterator__ property.
  "lastsemic"     : false,    // Tolerat missing semicolons when the it is omitted for the last statement in a one-line block.
  "laxbreak"      : false,    // Tolerate unsafe line breaks e.g. `return [\n] x` without semicolons.
  "laxcomma"      : false,    // Suppress warnings about comma-first coding style.
  "multistr"      : false,    // Tolerate multi-line strings.
  "onecase"       : false,    // Tolerate switches with just one case.
  "proto"         : false,    // Tolerate __proto__ property. This property is deprecated.
  "regexdash"     : false,    // Tolerate unescaped last dash i.e. `[-...]`.
  "scripturl"     : false,    // Tolerate script-targeted URLs.
  "smarttabs"     : false,    // Tolerate mixed tabs and spaces when the latter are used for alignmnent only.
  "shadow"        : false,    // Allows re-define variables later in code e.g. `var x=1; x=2;`.
  "sub"           : false,    // Tolerate all forms of subscript notation besides dot notation e.g. `dict['key']` instead of `dict.key`.
  "supernew"      : false,    // Tolerate `new function () { ... };` and `new Object;`.
  "validthis"     : false,    // Tolerate strict violations when the code is running in strict mode and you use this in a non-constructor function.

  // SUPRESSIONS

  "-W080"         : false,    // Suppresses "there is no need to initialize a variable to undefined" error
  "-W082"         : false,    // Suppresses "functions should not be defined in a block" error

  // == Environments ====================================================
  //
  // These options pre-define global variables that are exposed by
  // popular JavaScript libraries and runtime environments—such as
  // browser or node.js.
  "browser" : true,

  /*
   * Globals
   */
  "globals": {
     "$": false
  }
}

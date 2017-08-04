var fs     = require('fs'),
    xml2js = require('xml2js');

/**
 *  Parses the error report xml file.
 */
const getErrorsFromFile = (filename) => {
  var parser = new xml2js.Parser();
  var result = undefined;

  var fileContents = fs.readFileSync(filename);

  parser.parseString(fileContents, function (err, obj) {
    if (obj && obj.BugCollection && obj.BugCollection.BugInstance) {
      result = obj.BugCollection.BugInstance.map(instance =>
        instance.Class.map(clazz =>
          clazz.SourceLine.map(line => ({
            classname: line.$.classname,
            lineStart: line.$.start,
            lineEnd: line.$.end,
            type: instance.$.type,
            rank: instance.$.rank,
            priority: instance.$.priority,
            messageShort: instance.ShortMessage[0],
            messageLong: instance.LongMessage[0]
            })
          )
        ));
    }
  });

  if (result && result.length > 0) {
    return [].concat.apply([], [].concat.apply([], result));
  } else {
    return [];
  }
};

/**
 *  Turns a list of errors into a (classname -> error list) map.
 */
const convert2ClassFirst = (errors) => {
  var classnames = []
  new Set(errors.map(e => e.classname)).forEach(name => classnames.push(name));
  return classnames.map(classname => ({
    name: classname,
    errors: errors.filter(e => e.classname === classname)
  }))
}

/**
 *  Pretty prints the errors.
 */
const printErrors = (classes) =>
  classes.forEach(clazz => {
    console.log(clazz.name);
    clazz.errors.forEach(error =>
      console.log('    ' + error.type + ' (lines ' + error.lineStart + '-' + error.lineEnd + '): ' + error.messageLong))
  })



var arguments = process.argv.slice(2);
console.log('Processing FindBugs report files');
console.log('Files processed:\n  ' + arguments.join('\n  '));
var allErrors = [].concat.apply([], arguments.map(getErrorsFromFile));

if (allErrors && allErrors.length > 0) {
  var errorsByClass = convert2ClassFirst(allErrors);
  console.log('Errors found!');
  printErrors(errorsByClass);
  process.exit(1);
} else {
  console.log('No errors found');
  process.exit(0);
}

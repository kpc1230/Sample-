if (typeof BF == 'undefined') { window.BF = {}; }
if (typeof BF.utils == 'undefined') { BF.utils = {}; }

BF.utils.bonJSONStringify = function(data) {
    var ret = JSON.stringify(data);
    if (ret && typeof(ret) === 'string') {
        if (/\?\?/.test(ret)) {
            var questionMark = "\\" + "u003F";
            ret = ret.replace(/\?\?/g, questionMark + questionMark);
        }
    }
    return ret;
}

BF.utils.bonGetProp = function( $element, property) {
    if (!!$element.prop) {
        return $element.prop(property);
    } else {
        return $element.attr(property);
    }
}

BF.utils.bonSetProp = function( $element, property, value) {
    if (!!$element.prop) {
        $element.prop(property, value);
    } else {
        $element.attr(property, value);
    }
}

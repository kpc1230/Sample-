var Tags = (function () {
    var Tags = function () { };

    function extend(object, source) {
        Object.keys(source).forEach(function (key) {
            object[key] = source[key];
        });
        return object;
    }

    function concat(first, second) {
        return extend(extend({}, second), first);
    }

    Tags.prototype.tag = function (tag, attributes, content) {
        attributes = attributes || {};
        switch (tag) {
        case 'h1':
        case 'h2':
        case 'h3':
        case 'h4':
        case 'h5':
        case 'h6':
        case 'p':
        case 'li':
        case 'th':
        case 'td':
        case 'ul':
        case 'ol':
            return this.open(tag, attributes) +
                content +
                this.close(tag);
        case 'table':
            return this.open(tag, concat(attributes,
                { 'class' : 'table' })) + content +
                this.close(tag);
        case 'tr':
            return this.open(tag, attributes) +
                content +
                this.close(tag);
        case 'span':
        case 'a':
            return this.open(tag, attributes) +
                this.replaceTextChars(content) +
                this.close(tag);
        case 'img':
        case 'br':
        case 'hr':
            return this.open(tag, attributes);
        case '&#8212;':
        	return '&#8212;';
        case '&#8211;':
        	return '&#8211;';
        default:
            return this.open(tag, attributes) +
                content +
                this.close(tag);
        }
    };
    
    Tags.prototype.replaceTextChars = function(text) {
    	return text.replace(/\-/g, '&#45;').replace(/\_/g, '&#95;').replace(/\*/g, '&#42;').replace(/\+/g, '&#43;');
    };

    Tags.prototype.open = function (tag, attributes) {
        return '<' + tag + this.attributes(tag, attributes) + '>';
    };

    Tags.prototype.attributes = function (tag, attributes) {
        return Object.keys(attributes || {}).map(function (name) {
            return ' ' + this.atribute(tag, name, attributes[name]);
        }.bind(this)).join('');
    };

    Tags.prototype.atribute = function (tag, name, value) {
        return name + '="' + this.attributeValue(tag, name, value) + '"';
    };

    Tags.prototype.attributeValue = function (tag, attr, text) {
        switch (tag + ':' + attr) {
        case 'a:href':
        case 'link:href':
        case 'img:src':
            return encodeURI(text);
        default:
            return text.replace(/&/g, '&#x26;').replace(/</g, '&#x3C;')
                .replace(/>/g, '&#x3E;').replace(/"/g, '&x22;');
        }
    };

    Tags.prototype.indent = function (content) {
        return content.split(/\n/).map(function (value) {
            return '' === value ? '' : ('  ' + value);
        }).join('\n');
    };

    Tags.prototype.close = function (tag) {
        return '</' + tag + '>';
    };

    return Tags;
}());

var ZephyrWikiParser = function() {
	var tags = new Tags();
 	var regex = {
        headers: [
	        [/(^|\n)h1.\s*(.+)/g, 'h1' ],
	        [/(^|\n)h2.\s*(.+)/g, 'h2' ],
	        [/(^|\n)h3.\s*(.+)/g, 'h3' ],
	        [/(^|\n)h4.\s*(.+)/g, 'h4' ],
	        [/(^|\n)h5.\s*(.+)/g, 'h5' ],
	        [/(^|\n)h6.\s*(.+)/g, 'h6' ]
        ],
        textEffects: [
        	[/(?:\s|^)\*([^\s](.*?[^\s])?)\*(?!\w)/gm, 'strong'],
        	[/(?:\s|^)\+([^\s](.*?[^\s])?)\+(?!\w)/gm, 'ins'],
        	[/(?:\s|^)\-([^\s](.*?[^\s])?)\-(?!\w)/gm, 'del'],
        	[/(?:\s|^)\?{2}([^\s]((?!\?).*?[^\s])?)\?{2}(?!\w)/gm, 'cite'],
        	[/(?:\s|^)\^([^\s](.*?[^\s])?)\^(?!\w)/gm, 'sup'],
        	[/(?:\s|^)_([^\s](.*?[^\s])?)_(?!\w)/gm, 'em'],
        	[/bq\.(.+)/gm, 'blockquote'],
        	[/\{color(:.*)*\}\s*((.|\n)*?)\s*\{color\}/gm, 'font'],
        	[/\{quote\}\s*((.|\n)*?)\s*\{quote\}/gm, 'blockquote'],
        	[/(?:\s|^)\{\{([^\s](.*?[^\s])?)\}\}(?!\w)/gm, 'tt'],
        	[/(?:\s|^)~([^\s](.*?[^\s])?)~(?!\w)/gm, 'sub']
        ],
        textBreaks: [
        	[/\\\\/gm, 'br'],
        	[/^----/gm, 'hr'],
        	[/^---/gm, '&#8212;'],
        	[/^--/gm, '&#8211;']
        ],
        links: [
        	[/\[#(.+)\]/gm, 'anchor'],
        	[/\[\|?(http|https|ftp|ftps):{1}\/{2}(.+)\]/gm, 'externalLink'],
        	[/\[(.*)\|{1}(http|https|ftp|ftps):{1}\/{2}(.+)\]/gm, 'externalLinkNamed'],
        	[/\[(mailto:){1}(.+)\]/gm, 'mailTo'],
        	[/\[(file:){1}(.+)\]/gm, 'file']
        ],
        images: [
        	[/!(.+)(.jpg|.png|.gif){1}(!)/gm, 'img']
        ],
        xss: [
            [/<script>((.|\n)*)<\/script>/gm, 'script']
        ]
 	};
 	
 	var parseHeaders = function(str, regex, tag) {
	    return str.replace(regex, function(match, $1, $2) {
	    	return tags.tag(tag, null, $2);
	    });
	};

	var escapeCharacters = function(str) {
		return str.replace(/\\-/gm, '&#45;').replace(/\\_/gm, '&#95;').replace(/\\\*/g, '&#42;').replace(/\\\+/g, '&#43;')
		.replace(/\\\?/g, '&#63;').replace(/\\\{/g, '&#123;').replace(/\\\}/g, '&#125;').replace(/\\\~/g, '&#126;')
		.replace(/(\\\<|<)/g, '&#60;').replace(/(\\>|>)/g, '&#62;');
	};

	var htmlCodesToSymbols = function(str) {
		return str.replace(/&#92;/g, '\\').replace(/(&#45;)/g, '-').replace(/&#95;/gm, '_').replace(/&#42;/g, '*').replace(/&#43;/g, '+')
		.replace(/&#63;/g, '?').replace(/&#123;/g, '{').replace(/&#125;/g, '}').replace(/&#126;/g, '~');
	};

 	var parseTextEffects = function(str, regex, tag) {
		return str.replace(regex, function(match, $1, $2) {
			if(match && (/^\s/.test(match))) {
				$1 = ' ' + $1;
			}
			if(tag == 'font') {
				var color = $1 ? $1.replace(/^:/, ''): '#333333';
				return tags.tag(tag, {"color": color}, $2);
			} else if(tag == 'blockquote') {
				return tags.tag(tag, {"style": "word\\-wrap: break\\-word; word\\-break: break\\-all;"}, $1);
			} else
				return tags.tag(tag, null, $1);
		});
	};

	var parseParagraphs = function(text) {
		text = text.replace(/^\n+/g,"");
		text = text.replace(/\n+$/g,"");

		var grafs = text.split(/\n{2,}/g);
		var grafsOut = [];
		var end = grafs.length;
		for (var i=0; i<end; i++) {
			var str = grafs[i];
			if (str.search(/~K(\d+)K/g) >= 0) {
				grafsOut.push(str);
			}
			else if (str.search(/\S/) >= 0) {
				str = str.replace(/^([ \t]*)/g,"<p>");
				str += "</p>";
				grafsOut.push(str);
			}

		}

		return grafsOut.join("");
	};

	var parseTextBreaks = function(str, regex, tag) {
		return str.replace(regex, function(match, $1, $2) {
			return tags.tag(tag, null, $1);
		});
	};

	var parseLinks = function(str, regex, notation) {
		if(notation == 'anchor') {
			return str.replace(regex, function(match, $1) {
				return tags.tag('a', {'href': '#' + $1}, $1);
			});
		} else if(notation == 'externalLink') {
			return str.replace(regex, function(match, $1, $2) {
				var link = $1 + '://' + $2;
				return tags.tag('a', {'href': link, "rel": "nofollow", "target": "_blank"}, link);
			});
		} else if(notation == 'externalLinkNamed') {
			return str.replace(regex, function(match, $1, $2, $3) {
				var link = $2 + '://' + $3;
				return tags.tag('a', {'href': link, "rel": "nofollow", "target": "_blank"}, $1);
			});
		} else if(notation == 'mailTo') {
			 return str.replace(regex, function(match, $1, $2) {
				var link = $1 + $2;
				return tags.tag('a', {'href': link, "rel": "nofollow", "target": "_blank"}, $2);
			});
		} else if(notation == 'file') {
			 return str.replace(regex, function(match, $1, $2) {
				var link = $1 + $2;
				return tags.tag('a', {'href': link, "rel": "nofollow", "target": "_blank"}, link);
			});
		}
	};

	var parseImages = function(str, regex, tag) {
		 return str.replace(regex, function(match, $1, $2) {
			var link = $1 + $2;
			return tags.tag(tag, {'src': tags.replaceTextChars(link), 'style': 'max-width: 100%; max-height: 100%;'});
		});
	};

	var parseXSS = function(str, regex, tag) {
		 return str.replace(regex, function(match, $1) {
			return '&#60;' + tag + '&#62;' + $1 + '&#60;/' + tag + '&#62;';
		});
	};

    var parseTableHeader = function(str) {
		return str.replace(/^(\|[^\s](.+)[^\s]\|{2})\n/g, function(match, $1) {
			var headers = $1.split('||');
			var headerHTML = '';
			for (var i = 0; i < headers.length; i++) {
				if(headers[i]) {
					if(i==0)
						headers[i] = headers[i].split('|')[1];
					headerHTML += tags.tag('th', {'style':'border: 1px solid #CCC; background: none repeat scroll 0% 0% #F5F5F5; padding: 2px 3px;'}, headers[i]);
				}
			}
			headerHTML = tags.tag('tr', null, headerHTML);
			return tags.tag('thead', null, headerHTML);
		}); 
	};

    var parseTableBody = function(str) {
		return str.replace(/(\|[^\s](.+)[^\s])/gm, function(match, $1) {
			var headers = $1.split('|');
			var tbodyHTML = '';
			for (var i = 0; i < headers.length; i++) {
				if(headers[i]) {
					tbodyHTML += tags.tag('td', {'style':'border: 1px solid #CCC; padding: 2px 3px;'}, headers[i]);
				}
			}
			return tags.tag('tr', null, tbodyHTML);
		}); 		
	};

	var parseTable = function(str) {
		return str.replace(/(\|\|[^\s]((.|\n)+)[^\s]\|)/g, function(match, $1) {
			var tableHTML = parseTableHeader($1);
			tableHTML = parseTableBody(tableHTML);
			return tags.tag('table', {'style': 'border\\-collapse: collapse; word\\-wrap: break\\-word; word\\-break: break\\-all;'}, tableHTML);
		});
	};
	var parseLists = function(str) {
	    return str.replace(/(?:(?:(?:^|\n)[\*|#]+\s.+)+)/g, function (match) {
	        var listType = match.match(/(^|\n)#/) ? 'ol' : 'ul';
	        match = match.replace(/(^|\n)[\*#][^\s]{0,1}/g, "$1");
	        return '<'
	                + listType + '><li>'
	                + match.replace(/^\n/, '')
	                .split(/\n/).join('</li><li>')
	                + '</li></' + listType
	                + '>';
	    });
	};

 	this.wikiToHTML = function(str) {
 		var html = str, j, x;
    
	    html = html.replace(/\r\n/g, "\n");		
		html = escapeCharacters(html);
		
	    var headersCount = regex.headers.length;
		for(j = 0; j < headersCount; ++j ) {
			x = regex.headers[j];
			html = parseHeaders(html, x[0], x[1]);
		}

	    html = parseLists(html);
		html = parseTable(html);
	    
		var textBreaksCount = regex.textBreaks.length;
		for(j = 0; j < textBreaksCount; j++) {
			x =  regex.textBreaks[j];
			html = parseTextBreaks(html, x[0], x[1]);
		}
		
		var linksCount = regex.links.length;
		for(j = 0; j < linksCount; j++) {
			x =  regex.links[j];
			html = parseLinks(html, x[0], x[1]);
		}
		
		var imagesCount = regex.images.length;
		for(j = 0; j < imagesCount; j++) {
			x =  regex.images[j];
			html = parseImages(html, x[0], x[1]);
		}

		var xssCount = regex.xss.length;
		for(j = 0; j < xssCount; j++) {
			x =  regex.xss[j];
			html = parseXSS(html, x[0], x[1]);
		}

		var textEffectsCount = regex.textEffects.length;
		for(j = 0; j < textEffectsCount; j++) {
			x =  regex.textEffects[j];
			html = parseTextEffects(html, x[0], x[1]);
		}
		
		html = parseParagraphs(html);
		
	  	return html;
 	};
 	
 	var parseHeadersToText = function(str, regex, tag) {
	    return str.replace(regex, function(match, $1, $2) {
	    	return ' ' + $2;
	    });
	};

	var parseListsToText = function(str) {
		return str.replace(/(?:(?:(?:^)[\*|#]+\s(.+))+)/gm, function(match, $1) {
			return $1 + ' ';
		});
	};

	var parseTableToText = function(str) {
		return str.replace(/(^\|[^\s]((.|\n)+)[^\s]\|)/gm, function(match, $1, $2) { 
			match = match.split('||').join(' ');
			match = match.split('|').join(' ');
			return match;
		});
	};

	var parseTextBreaksToText = function(str, regex) {
	    return str.replace(regex, function(match, $1, $2) {
			return ' ';
		});
	};

	var parseLinksToText = function(str, regex, notation) {
		if(notation == 'anchor') {
			return str.replace(regex, function(match, $1) {
				return $1;
			});
		} else if(notation == 'externalLink') {
			return str.replace(/\[\|?((http|https|ftp|ftps):{1}\/{2}(.+))\]/gm, function(match, $1, $2) {
				return $1;
			});
		} else if(notation == 'externalLinkNamed') {
			return str.replace(/\[(.*)\|{1}((http|https|ftp|ftps):{1}\/{2}(.+))\]/gm, function(match, $1, $2, $3) {
				return $2;
			});
		} else if(notation == 'mailTo') {
			 return str.replace(regex, function(match, $1, $2) {
				return $2;
			});
		} else if(notation == 'file') {
			 return str.replace(regex, function(match, $1, $2) {
			 	return $1 + $2;
			});
		}
	};

	var parseImagesToText = function(str, regex, tag) {
		return str.replace(regex, function(match, $1, $2) {
			return $1 + $2;
		});
	};

	var parseTextEffectsToText = function(str, regex, tag) {
		return str.replace(regex, function(match, $1, $2) {
			if(match && (/^\s/.test(match))) {
				$1 = ' ' + $1;
			}
			if(tag == 'font') {
				return $2;
			} else
				return $1;
		});
	};

 	this.wikiToText = function(str) {
 		var text = str;
 		
	    text = text.replace(/\r\n/g, "\n");
	    
	    var headersCount = regex.headers.length;
		for(var j = 0; j < headersCount; ++j ) {
			var x = regex.headers[j];
			text = parseHeadersToText(text, x[0], x[1]);
		}

	    
	    text = parseListsToText(text);
	    
		text = parseTableToText(text);
	    
		var textBreaksCount = regex.textBreaks.length;
		for(var j = 0; j < textBreaksCount; j++) {
			var x =  regex.textBreaks[j];
			text = parseTextBreaksToText(text, x[0]);
		}
		
		var linksCount = regex.links.length;
		for(var j = 0; j < linksCount; j++) {
			var x =  regex.links[j];
			text = parseLinksToText(text, x[0], x[1]);
		}
		
		var imagesCount = regex.images.length;
		for(var j = 0; j < imagesCount; j++) {
			var x =  regex.images[j];
			text = parseImagesToText(text, x[0], x[1]);
		}

		text = escapeCharacters(text);
		var textEffectsCount = regex.textEffects.length;
		for(var j = 0; j < textEffectsCount; j++) {
			var x =  regex.textEffects[j];
			text = parseTextEffectsToText(text, x[0], x[1]);
		}
		text = htmlCodesToSymbols(text);
		text = text.replace(/\n/g, " ");
 		return text;
 	};

 	return {
 		wikiToHTML:	this.wikiToHTML,
 		wikiToText: this.wikiToText
 	}
};
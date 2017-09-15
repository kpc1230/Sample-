var initI18nMessages = function(i18nMessages) {
	window.Messages = (function(u) {
		function f(k) {
			var m;
			if(typeof k==='object') {
				for(var i=0, l = k.length; i < l && f.messages[k[i]] === u; ++i);
					m=f.messages[k[i]]||k[0]
			} else {
				m = ((f.messages[k]!==u) ? f.messages[k] :k )
			}
			for(i = 1; i < arguments.length; i++){
				m = m.replace('{'+(i-1)+'}',arguments[i])
			}
			return m;
		};
		f.messages=i18nMessages;
		return f;
	})();
}
/*---------------------------------------------------------------------------*/
/** 
 * @fileOverview Implementa classes para manipular consultas e respostas
 *   de Servicos AcaaS (Accessibility as a Service).
 * <p>  
 * Incorpora as bibliotecas:
 * <p>
 *   <b>Json2.js</b>: http://www.JSON.org/js.html<br/> 
 *   <b>Queue.js</b>: http://code.stephenmorley.org<br/>
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 * @version 1.0
 */
/*---------------------------------------------------------------------------*/
/**
 * @class Implementa um consumidor JavaScript do AcaaS.
 * @constructor
 * @description Cria um objeto para encapsular acesso ao AcaaS.
 * @param {String} urlAcaas URL raiz do Servico AcaaS. 
 */
function Acaas(urlAcaas) {
	var tts      = new AcaasMediaService(urlAcaas + "/tts"     );
	var stt      = new AcaasService     (urlAcaas + "/stt"     );
	var t2libras = new AcaasMediaService(urlAcaas + "/t2libras");
	var s2libras = new AcaasMediaService(urlAcaas + "/s2libras");
	
	/**
	 * @memberOf Acaas 
	 * @function
	 * @description Traduz Texto para Fala (TTS).
	 * @param {String} data Texto a ser traduzido.
	 * @param {String} mediaType Tipo MIME do formato desejado
	 *   para a midia (audio).
	 * @param {function(Blob data, JsonObject info)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.getTTS = function (data, mediaType, onData, onFinish, onError) {
		tts.get(data, null, mediaType, onData, onFinish, onError);
	};

	/**
	 * @memberOf Acaas 
	 * @function
	 * @description Traduz Fala para Texto (STT).
	 * @param {Raw} data Dados do Audio a ser traduzido.
	 * @param {String} mediaType Tipo MIME do formato do audio em <em>data</em>.
	 * @param {function(JsonObject data)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.getSTT = function (data, mediaType, onData, onFinish, onError) {
		stt.get(data, mediaType, onData, onFinish, onError);
	};

	/**
	 * @memberOf Acaas 
	 * @function
	 * @description Traduz Texto para Libras (T2Libras).
	 * @param {String} data Texto a ser traduzido.
	 * @param {String} mediaType Tipo MIME do formato desejado
	 *   para a midia (video).
	 * @param {function(Blob data, JsonObject info)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.getT2Libras = function (data, mediaType, onData, onFinish, onError) {
		t2libras.get(data, null, mediaType, onData, onFinish, onError);
	};
	
	/**
	 * @memberOf Acaas 
	 * @function
	 * @description Traduz Fala para Libras (S2Libras).
	 * @param {Raw} data Dados do Audio a ser traduzido.
	 * @param {String} iMediaType Tipo MIME do formato do audio em <em>data</em>.
	 * @param {String} oMediaType Tipo MIME do formato desejado
	 *   para a midia (video).
	 * @param {function(Blob data, JsonObject info)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.getS2Libras = function (data, iMediaType, oMediaType, onData, onFinish, onError) {
		s2libras.get(data, iMediaType, oMediaType, onData, onFinish, onError);
	};
}

/*---------------------------------------------------------------------------*/
/**
 * @class Implementa um consumidor de um Servico JSON do AcaaS.
 * @constructor
 * @description Cria um objeto para acessar servicos JSON do AcaaS.
 * @param {String} urlService URL do Servico AcaaS. 
 */
function AcaasService(urlService) {
	var acaasFormatParam = "format";
	var acaas = new AcaasConnection();

	/**
	 * @memberOf AcaasService 
	 * @function
	 * @description Consulta um servico do AcaaS e retorna a resposta
	 *   como um Objeto JSON.
	 * @param {Raw} data Dados para consultar.
	 * @param {String} mediaType Tipo MIME do formato dos dados em <em>data</em>.
	 * @param {function(JsonObject data)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.get = function (data, mediaType, onData, onFinish, onError) {
		var url = urlService + "?" + acaasFormatParam + "=" + mediaType;
		acaas.queryService(url, data, true,
				function(json) { 
					if (onData) { onData(json); }
					if (onFinish) { onFinish(); }
				},
				onError
		);		
	};
}

/*---------------------------------------------------------------------------*/
/**
 * @class Implementa um consumidor de um Servico de Midia do AcaaS.
 * @constructor
 * @description Cria um objeto para acessar servicos de Midia do AcaaS.
 * @param {String} urlService URL do Servico AcaaS. 
 */
function AcaasMediaService(urlService) {
	var urlMedia = urlService + "/media"; 
	var acaasFormatParam = "format";
	var acaasIformatParam = "iformat";
	var acaasOformatParam = "oformat";
	var mediaCount = -1;
	var jsonData = null;
	var acaas = new AcaasConnection();

	/**
	 * @memberOf AcaasMediaService 
	 * @function
	 * @description Consulta um servico do AcaaS e retorna a resposta de
	 *   midia como um Blob.
	 * @param {Raw} [data] Dados para consultar.
	 * @param {String} [iMediaType] Tipo MIME do formato dos dados em <em>data</em>.
	 * @param {String} [oMediaType] Tipo MIME do formato desejado para a midia.
	 * @param {function(Blob data, JsonObject info)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function()} [onFinish] Callback do evento disparado ao final
	 *   da traducao.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.get = function (data, iMediaType, oMediaType, onData, onFinish, onError) {
		var url = urlService;
		var mediaType = null;
		if (iMediaType != null && oMediaType != null) {
			url += "?" + acaasIformatParam + "=" + iMediaType + "&" + 
						 acaasOformatParam + "=" + oMediaType;
			mediaType = oMediaType;
		} else if (iMediaType != null) {
			url += "?" + acaasFormatParam + "=" + iMediaType;
			mediaType = iMediaType;
		} else if (oMediaType != null) {
			url += "?" + acaasFormatParam + "=" + oMediaType;
			mediaType = oMediaType;
		}
		acaas.queryService(url, data, true,
				function(json) { 
					jsonData = json; 
					mediaCount = -1; 
					getMediaData(mediaType, onData, onFinish, onError); 
				},
				onError
		);		
	};
	
	/** @private */
	function getMediaData(mediaType, onData, onFinish, onError) {
		if (jsonData != null && jsonData.items != null && 
				mediaCount < jsonData.items.length - 1) {
			mediaCount++;
			var url = urlMedia + "/" + jsonData.items[mediaCount].media;
			var info = jsonData.items[mediaCount];
			acaas.getMedia(url, info, mediaType, true,
					function(mediaData, mediaInfo) {
						processMedia(mediaData, mediaInfo, mediaType, onData, onFinish, onError);	
					},
					onError
			);
		} else {
			if (onFinish) { onFinish(); }
		}
	}
	
	/** @private */
	function processMedia(data, info, mediaType, onData, onFinish, onError) {
		if (onData) { onData(data, info); }
		getMediaData(mediaType, onData, onFinish, onError);
	}
}

/*---------------------------------------------------------------------------*/
/**
 * @class Implementa conexao HTTP a um Servico AcaaS.
 * @constructor
 * @description Cria um objeto para conectar a um servico AcaaS.
 */
function AcaasConnection() {
	/**
	 * @memberOf AcaasConnection 
	 * @function
	 * @description Consulta um servico do AcaaS (via HTTP POST),
	 *   e retorna a resposta como um Objeto JSON.
	 * @param {String} url URL do Servico AcaaS,
	 *   contendo todos parametros necessarios.
	 * @param {Raw} data Dados para consultar (corpo da requisicao HTTP POST).
	 * @param {boolean} [async] Indica se a requisicao sera' assincrona.
	 * @param {function(JsonObject data)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.queryService = function (url, data, async, onData, onError) {
		if (async == null) { async = false; }
		var xhttp = new XMLHttpRequest();
		xhttp.open('POST', url, async);
		/** @private */
		xhttp.onload = function(event) {
			var jsonStr = this.response;
			var json = null;
			if (jsonStr != null) { json = JSON.parse(jsonStr); }
			if (onData) { onData(json); }
		}; 
		/** @private */
		xhttp.onerror = function(event) {
			var reqStatus = this.status; 
			var reqStatusText = this.statusText;
			if (onError) { onError(reqStatus, reqStatusText); }
		};
		xhttp.send(data);
	};
	
	/**
	 * @memberOf AcaasConnection 
	 * @function
	 * @description Consulta um servico de midia do AcaaS (via HTTP GET),
	 *   e retorna a resposta como um Blob.
	 * @param {String} url URL do Servico AcaaS,
	 *   contendo todos parametros necessarios.
	 * @param {JsonObject} [info] Objeto JSON com informacoes sobre a midia
	 *   requisitada.
	 * @param {String} mediaType Tipo MIME do formato desejado para a midia.
	 * @param {boolean} [async] Indica se a requisicao sera' assincrona.
	 * @param {function(Blob data, JsonObject info)} [onData] Callback do evento
	 *   disparado quando os dados de uma midia estao disponiveis.
	 * @param {function(Number status, String statusText)} [onError] Callback
	 *   do evento disparado quando a requisicao resultou em erro.
	 */
	this.getMedia = function (url, info, mediaType, async, onData, onError) {
		if (async == null) { async = false; }
		var xhttp = new XMLHttpRequest();
		if ("withCredentials" in xhttp) {
			xhttp.open('GET', url, async);
		} else if (typeof XDomainRequest != "undefined") {
			xhttp = new XDomainRequest();
			xhttp.open('GET', url);
		}
		xhttp.onload = function(event) {
			var blob = null;
			try {
				blob = new Blob([this.response], {type: mediaType});
			} catch(e) {
				var blobBuilder = null;
				if (typeof BlobBuilder != "undefined") {
					blobBuilder = new BlobBuilder();
				} else if (typeof WebKitBlobBuilder != "undefined") {
					blobBuilder = new WebKitBlobBuilder();
				} else if (typeof MozBlobBuilder != "undefined") {
					blobBuilder = new MozBlobBuilder();
				} else if (typeof MSBlobBuilder != "undefined") {
					blobBuilder = new MSBlobBuilder();
				}
				blobBuilder.append(this.response);
				blob = blobBuilder.getBlob(mediaType);
			}
			if (onData) { onData(blob, info); }
		};
		/** @private */
		xhttp.onerror = function(event) {
			var reqStatus = this.status; 
			var reqStatusText = this.statusText;
			if (onError) { onError(reqStatus, reqStatusText); }
		};
		try {
			xhttp.responseType = "arraybuffer";
			xhttp.send();
		} catch(e) {
			xhttp.responseType = "";
			xhttp.send();
		}
	};
}

/*-----------------------------------------------------------------------------
 * json2.js
 * 2013-05-26
 * Public Domain.
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 * See http://www.JSON.org/js.html
 * 
 * This code should be minified before deployment.
 * See http://javascript.crockford.com/jsmin.html
 * 
 * USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO
 * NOT CONTROL.
 * 
 * This file creates a global JSON object containing two methods: stringify 
 * and parse.
 * 
 * JSON.stringify(value, replacer, space)
 * value any JavaScript value, usually an object or array.
 * 
 * replacer an optional parameter that determines how object
 * values are stringified for objects. It can be a
 * function or an array of strings.
 * 
 * space an optional parameter that specifies the indentation
 * of nested structures. If it is omitted, the text will
 * be packed without extra whitespace. If it is a number,
 * it will specify the number of spaces to indent at each
 * level. If it is a string (such as '\t' or '&nbsp;'),
 * it contains the characters used to indent at each level.
 * This method produces a JSON text from a JavaScript value.
 * 
 * When an object value is found, if the object contains a toJSON
 * method, its toJSON method will be called and the result will be
 * stringified. A toJSON method does not serialize: it returns the
 * value represented by the name/value pair that should be serialized,
 * or undefined if nothing should be serialized. The toJSON method
 * will be passed the key associated with the value, and this will be
 * bound to the value
 * 
 * For example, this would serialize Dates as ISO strings.
 * 
 * Date.prototype.toJSON = function (key) {
 * function f(n) {
 * // Format integers to have at least two digits.
 * return n < 10 ? '0' + n : n;
 * }
 * return this.getUTCFullYear() + '-' +
 * f(this.getUTCMonth() + 1) + '-' +
 * f(this.getUTCDate()) + 'T' +
 * f(this.getUTCHours()) + ':' +
 * f(this.getUTCMinutes()) + ':' +
 * f(this.getUTCSeconds()) + 'Z';
 * };
 * 
 * You can provide an optional replacer method. It will be passed the
 * key and value of each member, with this bound to the containing
 * object. The value that is returned from your method will be
 * serialized. If your method returns undefined, then the member will
 * be excluded from the serialization.
 * 
 * If the replacer parameter is an array of strings, then it will be
 * used to select the members to be serialized. It filters the results
 * such that only members with keys listed in the replacer array are
 * stringified.
 * 
 * Values that do not have JSON representations, such as undefined or
 * functions, will not be serialized. Such values in objects will be
 * dropped; in arrays they will be replaced with null. You can use
 * a replacer function to replace those with JSON values.
 * JSON.stringify(undefined) returns undefined.
 * 
 * The optional space parameter produces a stringification of the
 * value that is filled with line breaks and indentation to make it
 * easier to read.
 * 
 * If the space parameter is a non-empty string, then that string will
 * be used for indentation. If the space parameter is a number, then
 * the indentation will be that many spaces.
 * 
 * Example:
 * text = JSON.stringify(['e', {pluribus: 'unum'}]);
 * // text is '["e",{"pluribus":"unum"}]'
 * 
 * text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\t');
 * // text is '[\n\t"e",\n\t{\n\t\t"pluribus": "unum"\n\t}\n]'
 * 
 * text = JSON.stringify([new Date()], function (key, value) {
 * return this[key] instanceof Date ?
 * 'Date(' + this[key] + ')' : value;
 * });
 * // text is '["Date(---current time---)"]'
 * 
 * JSON.parse(text, reviver)
 * This method parses a JSON text to produce an object or array.
 * It can throw a SyntaxError exception.
 * 
 * The optional reviver parameter is a function that can filter and
 * transform the results. It receives each of the keys and values,
 * and its return value is used instead of the original value.
 * If it returns what it received, then the structure is not modified.
 * If it returns undefined then the member is deleted.
 * 
 * Example:
 * // Parse the text. Values that look like ISO date strings will
 * // be converted to Date objects.
 * myData = JSON.parse(text, function (key, value) {
 * var a;
 * if (typeof value === 'string') {
 * a =
 * /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}(?:\.\d*)?)Z$/.exec(value);
 * if (a) {
 * return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],
 * +a[5], +a[6]));
 * }
 * }
 * return value;
 * });
 * 
 * myData = JSON.parse('["Date(09/09/2001)"]', function (key, value) {
 * var d;
 * if (typeof value === 'string' &&
 * value.slice(0, 5) === 'Date(' &&
 * value.slice(-1) === ')') {
 * d = new Date(value.slice(5, -1));
 * if (d) {
 * return d;
 * }
 * }
 * return value;
 * });
 * 
 * This is a reference implementation. You are free to copy, modify, or
 * redistribute.
 * 
 * jslint evil: true, regexp: true
 * 
 * members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON, "\\", apply,
 * call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,
 * getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,
 * lastIndex, length, parse, prototype, push, replace, slice, stringify,
 * test, toJSON, toString, valueOf
 * 
 *---------------------------------------------------------------------------*/

// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.

if (typeof JSON !== 'object') {
    JSON = {};
}

(function () {
    'use strict';

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    if (typeof Date.prototype.toJSON !== 'function') {

        Date.prototype.toJSON = function () {

            return isFinite(this.valueOf())
                ? this.getUTCFullYear() + '-' +
                    f(this.getUTCMonth() + 1) + '-' +
                    f(this.getUTCDate()) + 'T' +
                    f(this.getUTCHours()) + ':' +
                    f(this.getUTCMinutes()) + ':' +
                    f(this.getUTCSeconds()) + 'Z'
                : null;
        };

        String.prototype.toJSON =
            Number.prototype.toJSON =
            Boolean.prototype.toJSON = function () {
                return this.valueOf();
            };
    }

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap = 0,
        indent = 0,
        meta = { // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        rep = 0;


    function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            return typeof c === 'string'
                ? c
                : '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }


    function str(key, holder) {

// Produce a string from holder[key].

        var i = 0, // The loop counter.
            k = 0, // The member key.
            v = 0, // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

        if (value && typeof value === 'object' &&
                typeof value.toJSON === 'function') {
            value = value.toJSON(key);
        }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

// What happens next depends on the value's type.

        switch (typeof value) {
        case 'string':
            return quote(value);

        case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

            return isFinite(value) ? String(value) : 'null';

        case 'boolean':
        case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

            return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

        case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

            if (!value) {
                return 'null';
            }

// Make an array to hold the partial results of stringifying this object value.

            gap += indent;
            partial = [];

// Is the value an array?

            if (Object.prototype.toString.apply(value) === '[object Array]') {

// The value is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || 'null';
                }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                v = partial.length === 0
                    ? '[]'
                    : gap
                    ? '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']'
                    : '[' + partial.join(',') + ']';
                gap = mind;
                return v;
            }

// If the replacer is an array, use it to select the members to be stringified.

            if (rep && typeof rep === 'object') {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    if (typeof rep[i] === 'string') {
                        k = rep[i];
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            } else {

// Otherwise, iterate through all of the keys in the object.

                for (k in value) {
                    if (Object.prototype.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

            v = partial.length === 0
                ? '{}'
                : gap
                ? '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}'
                : '{' + partial.join(',') + '}';
            gap = mind;
            return v;
        }
    }

// If the JSON object does not yet have a stringify method, give it one.

    if (typeof JSON.stringify !== 'function') {
        JSON.stringify = function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

            var i;
            gap = '';
            indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

// If the space parameter is a string, it will be used as the indent string.

            } else if (typeof space === 'string') {
                indent = space;
            }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                    typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

            return str('', {'': value});
        };
    }


// If the JSON object does not yet have a parse method, give it one.

    if (typeof JSON.parse !== 'function') {
        JSON.parse = function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

            var j;

            function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                var k = 0, v = 0, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }


// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

            if (/^[\],:{}\s]*$/
                    .test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
                        .replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
                        .replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                return typeof reviver === 'function'
                    ? walk({'': j}, '')
                    : j;
            }

// If the text is not JSON parseable, then a SyntaxError is thrown.

            throw new SyntaxError('JSON.parse');
        };
    }
}());

/*-----------------------------------------------------------------------------
 * Queue.js
 * 
 * A function to represent a queue
 * 
 * Created by Stephen Morley - http://code.stephenmorley.org/ - and released under
 * the terms of the CC0 1.0 Universal legal code:
 * 
 * http://creativecommons.org/publicdomain/zero/1.0/legalcode
 *---------------------------------------------------------------------------*/

/* Creates a new queue. A queue is a first-in-first-out (FIFO) data structure -
 * items are added to the end of the queue and removed from the front.
 */
function Queue(){

  // initialise the queue and offset
  var queue  = [];
  var offset = 0;

  /* Returns the length of the queue.
   */
  this.getLength = function(){

    // return the length of the queue
    return (queue.length - offset);

  };

  /* Returns true if the queue is empty, and false otherwise.
   */
  this.isEmpty = function(){

    // return whether the queue is empty
    return (queue.length == 0);

  };

  /* Enqueues the specified item. The parameter is:
   *
   * item - the item to enqueue
   */
  this.enqueue = function(item){

    // enqueue the item
    queue.push(item);

  };

  /* Dequeues an item and returns it. If the queue is empty then undefined is
   * returned.
   */
  this.dequeue = function(){

    // if the queue is empty, return undefined
    if (queue.length == 0) return undefined;

    // store the item at the front of the queue
    var item = queue[offset];

    // increment the offset and remove the free space if necessary
    if (++ offset * 2 >= queue.length){
      queue  = queue.slice(offset);
      offset = 0;
    }

    // return the dequeued item
    return item;

  };

  /* Returns the item at the front of the queue (without dequeuing it). If the
   * queue is empty then undefined is returned.
   */
  this.peek = function(){

    // return the item at the front of the queue
    return (queue.length > 0 ? queue[offset] : undefined);

  };
}

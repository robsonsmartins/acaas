/*---------------------------------------------------------------------------*/
/** 
 * @fileOverview Implementa classes para verificar features do browser.
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 * @version 1.0
 */
/*---------------------------------------------------------------------------*/
/**
 * @class Implementa metodos para verificar features do browser.
 * @constructor
 * @description Cria um objeto para verificacao de features do browser.
 */
function CheckBrowser() {
	/**
	 * @memberOf CheckBrowser 
	 * @function
	 * @description Verifica se browser suporta features para
	 *   requisicao HTTP remota (XMLHttpRequest, Blob, window.URL).
	 * @returns True se browser suporta todos os recursos necessarios,
	 *   false se nao.
	 */
	this.hasRemoteRequest = function() {
		window.URL = window.URL || window.webkitURL;
		var hasXHR  = ((typeof XDomainRequest != "undefined") ||
		               (typeof XMLHttpRequest != "undefined"));
		var hasBlob = (typeof Blob != "undefined");
		var hasWindowURL = (window.URL != null && 
							window.URL != "undefined");
		return (hasXHR && hasBlob && hasWindowURL);
	};
	
	/**
	 * @memberOf CheckBrowser 
	 * @function
	 * @description Verifica se browser suporta features para
	 *   reproducao de midia (Video, Audio).
	 * @returns True se browser suporta todos os recursos necessarios,
	 *   false se nao.
	 */
	this.hasAudioVideo = function() {
		var hasVideo = !!document.createElement('video').canPlayType;
		var hasAudio = !!document.createElement('audio').canPlayType;
		return (hasVideo &&	hasAudio);
	};
	
	/**
	 * @memberOf CheckBrowser 
	 * @function
	 * @description Verifica se browser suporta features para
	 *   captura de audio (getUserMedia, AudioContext, DataView,
	 *   ArrayBuffer, Blob).
	 * @returns True se browser suporta todos os recursos necessarios,
	 *   false se nao.
	 */
	this.hasAudioCapture = function() {
		window.AudioContext = 
			window.AudioContext || window.webkitAudioContext ||
			window.mozAudioContext;
		navigator.getUserMedia = 
			navigator.getUserMedia || navigator.webkitGetUserMedia ||
	    	navigator.mozGetUserMedia || navigator.msGetUserMedia;
		var hasBlob = (typeof Blob != "undefined");
		var hasArrayBuf = (typeof ArrayBuffer != "undefined");
		var hasDataView = (typeof DataView != "undefined");
		var hasAudioContext = (window.AudioContext != null && 
				window.AudioContext != "undefined");
		var hasGetUserMedia = (navigator.getUserMedia != null && 
				navigator.getUserMedia != "undefined");
		return (hasBlob && hasArrayBuf && hasDataView && 
				hasAudioContext && hasGetUserMedia);
	};
}

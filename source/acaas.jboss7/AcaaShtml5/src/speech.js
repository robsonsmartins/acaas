/*---------------------------------------------------------------------------*/
/** 
 * @fileOverview Implementa classes para manipular captura de audio via HTML5.
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 * @version 1.0
 */
/*---------------------------------------------------------------------------*/
/**
 * @class Implementa Captura de Audio via HTML5/JavaScript.
 * @constructor
 * @description Cria um objeto para Captura de Audio via HTML5.
 */
function Speech() {

	var DEFAULT_SILENCE_LEVEL  = -20;   // dB
	var DEFAULT_SILENCE_LENGTH = 200;  // ms
	var DEFAULT_SAMPLE_RATE    = 44100; // Hz
	var RESAMPLE_FACTOR        = 4;
	
	window.AudioContext = 
		window.AudioContext || window.webkitAudioContext ||
		window.mozAudioContext;
	navigator.getUserMedia = 
		navigator.getUserMedia || navigator.webkitGetUserMedia ||
    	navigator.mozGetUserMedia || navigator.msGetUserMedia;
	
    var onSuccess = null;  
    var onError = null;
    var recording = false;
    var status = 0; /* 0: wait for sound; 1: sound; 2: silence found */
    var context = null;
    var volume = null;
    var localStream = null;
    var audioInput = null;
    var recorder = null;
    var leftchannel = [];
    var rightchannel = [];
    var recordingLength = 0;
    var leftBuffer = null;
    var rightBuffer = null;
    var monoBuffer = null;
    var silenceFrames = 0;
    var silenceLevel  = DEFAULT_SILENCE_LEVEL;
    var silenceLength = DEFAULT_SILENCE_LENGTH;
    var sampleRate    = DEFAULT_SAMPLE_RATE;
    
	/**
	 * @memberOf Speech 
	 * @function
	 * @description Obtem o nivel de silencio configurado.
	 * @returns {Number} Nivel de silencio, em dB.
	 */
    this.getSilenceLevel = function() {
    	return silenceLevel;
    };

	/**
	 * @memberOf Speech 
	 * @function
	 * @description Configura o nivel de silencio.
	 * @param {Number} level Nivel de silencio, em dB.
	 * @returns {Number} Nivel de silencio configurado, em dB.
	 */
    this.setSilenceLevel = function(level) {
    	silenceLevel = level;
    	return getSilenceLevel();
    };
    
	/**
	 * @memberOf Speech 
	 * @function
	 * @description Obtem a duracao de silencio configurada.
	 * @returns {Number} Duracao de silencio, em ms.
	 */
    this.getSilenceLength = function() {
    	return silenceLength;
    };

	/**
	 * @memberOf Speech 
	 * @function
	 * @description Configura a duracao de silencio.
	 * @param {Number} length Duracao de silencio, em ms.
	 * @returns {Number} Duracao de silencio configurada, em ms.
	 */
    this.setSilenceLength = function(length) {
    	getSilenceLength = length;
    	return getSilenceLength();
    };

	/**
	 * @memberOf Speech 
	 * @function
	 * @description Inicia captura de audio.
	 * @param { function ( DataView audio ) } [onSuccessCallback] Callback do evento
	 *   disparado quando ha audio capturado disponivel.
	 * @param { function ( Event e ) } [onErrorCallback] Callback do evento disparado
	 *   caso haja algum erro na captura.
	 */
    this.startCapture = function(onSuccessCallback, onErrorCallback){
		onSuccess = onSuccessCallback;
		onError = onErrorCallback;
	    recording = true;
	    initRecord();
		navigator.getUserMedia({audio:true}, onCapture, onError);
    };

	/**
	 * @memberOf Speech 
	 * @function
	 * @description Encerra captura de audio.
	 */
    this.stopCapture = function(){
	    if (recording) { 
		    recording = false;
		    localStream.stop();
	    	pauseRecord();
	    }
    };
    
    /** 
     * @private 
	 * @function
	 * @description Limpa buffers para iniciar captura de audio.
     */
    function initRecord() {
        leftchannel = [];
        rightchannel = [];
    	recordingLength = 0;
    	silenceFrames = 0;
    	status = 0;
    }
    
    /** 
     * @private 
	 * @function
	 * @description Salva buffer de captura no formato WAV, e dispara o evento onSuccess.
     */
    function pauseRecord() {
	    var audio = pcmToWav(leftchannel, rightchannel, recordingLength, sampleRate);
	    if (audio != null && onSuccess) { onSuccess(audio); }
    }
    
    /** 
     * @private 
	 * @function
	 * @description Callback da funcao getUserMedia(), chamado quando ha' dados na
	 *   stream de captura de audio.
	 * @param {MediaStream} stream Objeto que contem dados do audio capturado.
     */
	function onCapture(stream){
		localStream = stream;
	    if (!recording) { return; }
	    var bufferSize = 2048;
	    context = new window.AudioContext();
	    volume = context.createGain();
	    audioInput = context.createMediaStreamSource(stream);
	    sampleRate = context.sampleRate; 
	    audioInput.connect(volume);
	    recorder = context.createJavaScriptNode(bufferSize, 2, 2);
	    volume.connect(recorder);
	    recorder.connect(context.destination); 
	    
	    /** @private */
	    recorder.onaudioprocess = function(audio){
	        var left  = audio.inputBuffer.getChannelData(0);
	        var right = audio.inputBuffer.getChannelData(1);
	        leftchannel.push(new Float32Array(left));
	        rightchannel.push(new Float32Array(right));
	        recordingLength += bufferSize;
	        
	        // se detectou silencio: pausa, retorna dados gravados e continua captura.
	        if (detectSilence(left, right, silenceLevel, silenceLength)) {
	        	pauseRecord(); 
	        	initRecord();
	        } 
	    };
	}
	
    /** 
     * @private 
	 * @function
	 * @description Detecta um trecho de silencio nos buffers capturados de audio.
	 * @param {ChannelData} left Objeto que contem dados do canal esquerdo (L).
	 * @param {ChannelData} right Objeto que contem dados do canal direito (R).
	 * @param {Number} dbSilence Nivel de silencio, em dB.
	 * @param {Number} lenSilence Duracao de silencio, em ms.
	 * @returns {Boolean} True se os buffers contem um trecho de silencio.
     */
	function detectSilence(left, right, dbSilence, lenSilence) {
        var lBuf = new Float32Array(left);
        var rBuf = new Float32Array(right);
        var mono = resampleMonoByX(lBuf, rBuf, RESAMPLE_FACTOR);
	    var len = mono.length;
	    for (var i = 0; i < len; i++){
	    	if (!isSilence(mono[i] * 0x7FFF, 32767, dbSilence)) { 
	    		silenceFrames = 0;
	    		if (status == 0) { status = 1; }
	    		break;
	    	} else if (status != 0) {
	    		silenceFrames++;
	    	}
	    }
	    var silenceNeedFrames = (sampleRate / RESAMPLE_FACTOR) * lenSilence / 1000.0;
	    var hasSilence = (silenceFrames >= silenceNeedFrames); 
	    if (hasSilence && status == 1) { status = 2; }
	    return (status == 2);
	}
	
    /** 
     * @private 
	 * @function
	 * @description Retorna se uma amostra de audio (1 sample) representa silencio.
	 * @param {Number} data Amostra (1 sample).
	 * @param {Number} peek Valor maximo de pico possivel para a amostra.
	 * @param {Number} dbSilence Nivel de silencio, em dB.
	 * @returns {Boolean} True se a amostra contem silencio.
     */
	function isSilence(data, peek, dbSilence) {
		var dbRMS = 10.0 * (Math.log(Math.abs(data)/peek) / Math.LN10);
		if (dbRMS < dbSilence || data == 0) { return true; }				
		return false;
	}

    /** 
     * @private 
	 * @function
	 * @description Grava dados de audio (RAW PCM) para o formato WAV.
	 * @param {Raw} left Buffer com dados do canal esquerdo (L).
	 * @param {Raw} right Buffer com dados do canal direito (R).
	 * @param {Number} reclen Tamanho do buffer de audio.
	 * @param {Number} srate Taxa de amostragem, em Hz.
	 * @returns {DataView} Objeto que contem dados no formato WAV.
     */
	function pcmToWav(left, right, reclen, srate) {
		try {
			leftBuffer  = mergeBuffers(left , reclen);
			rightBuffer = mergeBuffers(right, reclen);
			monoBuffer  = resampleMonoByX(leftBuffer, rightBuffer, RESAMPLE_FACTOR);
		} catch(e) { return null; }
	    var monoLen = monoBuffer.length;
	    if (silenceFrames >= monoLen) { return null; }
	    var buffer = new ArrayBuffer(44 + monoBuffer.length * 2);
	    var data = new DataView(buffer);
	    // RIFF chunk descriptor
	    writeUTFBytes(data, 0, 'RIFF');
	    data.setUint32(4, 44 + monoLen * 2, true);
	    writeUTFBytes(data, 8, 'WAVE');
	    // FMT sub-chunk
	    writeUTFBytes(data, 12, 'fmt ');
	    data.setUint32(16, 16, true);
	    data.setUint16(20, 1, true);
	    // mono (1 channel)
	    data.setUint16(22, 1, true);
	    data.setUint32(24, (srate / RESAMPLE_FACTOR), true);
	    data.setUint32(28, (srate / RESAMPLE_FACTOR) * 4, true);
	    data.setUint16(32, 4, true);
	    data.setUint16(34, 16, true);
	    // data sub-chunk
	    writeUTFBytes(data, 36, 'data');
	    data.setUint32(40, monoLen * 2, true);
	    // write the PCM samples
	    var index = 44;
	    var volume = 1;
	    for (var i = 0; i < monoLen; i++){
	    	data.setInt16(index, monoBuffer[i] * (0x7FFF * volume), true);
	        index += 2;
	    }
	    return data;
	}
	
    /** 
     * @private 
	 * @function
	 * @description Reamostra e realiza mix de dois canais, resultando em
	 *   um canal mono com taxa de amostragem com valor de 1/X do original.
	 * @param {Float32Array} left Buffer com dados do canal esquerdo (L).
	 * @param {Float32Array} right Buffer com dados do canal direito (R).
	 * @param {Number} x Fator de reamostragem (2: metade; 4: um quarto, etc).
	 * @returns {Float32Array} Buffer de um canal (mono) com taxa reamostrada.
     */
	function resampleMonoByX(left, right, x) {
		if (x == undefined || x == null || x == 0){ x = 1; }
		var len = left.length;
		var result = new Float32Array(len / x);
		for (var i = 0, j = 0; i < len; i++, j+=x) {
			result[i] = (left[j] + right[j]) / 2; 
		}
		return result; 
	}

    /** 
     * @private 
	 * @function
	 * @description Une todos os buffers de audio capturados por getUserMedia().
	 * @param {Array} channelBuf Buffer com dados de um canal.
	 * @param {Number} reclen Comprimento do buffer.
	 * @returns {Float32Array} Buffer unificado de um canal.
     */
	function mergeBuffers(channelBuf, reclen){
		var result = new Float32Array(reclen);
		var offset = 0;
		var lng = channelBuf.length;
		for (var i = 0; i < lng; i++){
			var buffer = channelBuf[i];
		    result.set(buffer, offset);
		    offset += buffer.length;
		}
		return result;
	}
	
    /** 
     * @private 
	 * @function
	 * @description Escreve uma string em um DataView, usando UTF-8.
	 * @param {DataView} dview Objeto DataView.
	 * @param {Number} offset Posicao de escrita no DataView.
	 * @param {String} string Texto a ser escrito no DataView.
     */
	function writeUTFBytes(dview, offset, string){ 
		var lng = string.length;
		for (var i = 0; i < lng; i++){
			dview.setUint8(offset + i, string.charCodeAt(i));
		}
	}
}

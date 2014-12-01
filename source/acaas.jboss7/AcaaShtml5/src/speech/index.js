/*---------------------------------------------------------------------------*/
/** 
 * @fileOverview Contem o JavaScript necessario para a aplicacao 
 *   &quot;Fala para Texto/Libras&quot;.
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 * @version 1.0
 */
/*---------------------------------------------------------------------------*/

/* URL do servico AcaaS. */
var URL_ACAAS = "http://acaas.robsonmartins.com/acaas";

var STATUS_LISTEN  = "Fale ao microfone...";
var STATUS_PROCESS = "Analisando...";

var videoPlayer = document.getElementById("video_player");
var statusText = document.getElementById("status_text");
var outputText = document.getElementById("output");
var btnStart    = document.getElementById("btn_start");
var btnStop     = document.getElementById("btn_stop");

var chkBrws = new CheckBrowser();
var acaas = new Acaas(URL_ACAAS);
var speech = new Speech();

var audioMediaType = "audio/wav";
var videoMediaType = "video/ogg";

var videoBuffer = null;
var audioBuffer = null;

var videoLoaded = false;
var searching = false;
var started = false;
var poster = null;

window.URL = window.URL || window.webkitURL;

videoPlayer.addEventListener("ended",getNextVideo);

if (chkBrws.hasAudioVideo() && chkBrws.hasRemoteRequest() &&
		chkBrws.hasAudioCapture()) {
	btnStart.disabled = false;
} else {
	window.alert("Navegador n\u00E3o suportado.");
}

/** @private */
function start() {
	btnStart.disabled = true;
	btnStop.disabled  = false;
	if (poster == null) {
		poster = videoPlayer.poster;
	}
	if (videoPlayer.canPlayType("video/ogg") == "") {
		videoMediaType = "video/mp4";
	} else {
		videoMediaType = "video/ogg";
	}
	outputText.value = "";
	statusText.innerHTML = STATUS_LISTEN;
	audioBuffer = new Queue();
	videoBuffer = new Queue();
	started = true;
	speech.startCapture(onAudioCapture, onAudioError);
}

/** @private */
function stop() {
	if (!started) { return; }
	btnStart.disabled = false;
	btnStop.disabled  = true;
	started = false;
	speech.stopCapture();
	setVideoPoster();
	statusText.innerHTML = "";
	outputText.value = "";
	audioBuffer = null;
	videoBuffer = null;
}

/** @private */
function onAudioCapture(audio) {
	if (!started) { return; }
	audioBuffer.enqueue(audio);
	querySTT();
}

/** @private */
function querySTT() {
	if (!started || searching) { return; }
	var audio = audioBuffer.dequeue();
	if (audio != undefined) {
		statusText.innerHTML = STATUS_PROCESS;
		videoLoaded = false;
		searching = true;
		acaas.getSTT(audio, audioMediaType, 
				onMediaSTT, onFinishMediaSTT, onErrorSTT);
	} else {
		statusText.innerHTML = STATUS_LISTEN;
	}
}

/** @private */
function onMediaSTT(data) {
	if (!started) { return; }
	var text = ""; 
	if (data != null && data.items != null && data.items.length != 0) {
		for (var i = 0; i < data.items.length; i++) {
			text += data.items[i] + " ";
		}
		outputText.value += text;
		acaas.getT2Libras(text, videoMediaType, 
				onMediaT2Libras, onFinishMediaT2Libras, onErrorT2Libras);
	}
}

/** @private */
function onFinishMediaSTT() {
	onFinishMediaT2Libras();
	querySTT();
}

/** @private */
function onAudioError(e) {
	statusText.innerHTML = "Erro ao obter audio: " + e.message;
}

/** @private */
function onErrorSTT(status, text) {
	onFinishMediaSTT();
}

/** @private */
function onErrorT2Libras(status, text) {
	onFinishMediaT2Libras();
}

/** @private */
function onMediaT2Libras(media, info) {
	if (!started) { return; }
	videoBuffer.enqueue({"media": media, "info": info});
	if (!videoLoaded) {
		videoLoaded = true;
		videoPlayer.poster = "";
		getNextVideo();
	}
}

/** @private */
function onFinishMediaT2Libras() {
	if (!started) { return; }
	statusText.innerHTML = STATUS_LISTEN;
	videoLoaded = false;
	searching = false;
}

/** @private */
function getNextVideo() {
	if (!started) { return; }
	var data = videoBuffer.dequeue();
	if (data != undefined) {
		window.URL.revokeObjectURL(videoPlayer.src);
		videoPlayer.src = window.URL.createObjectURL(data.media);
		videoPlayer.type = videoMediaType;
		videoPlayer.load();
		videoPlayer.play();
	} else if (videoLoaded){
		setTimeOut(getNextVideo,200);
	} else {
		setVideoPoster();
	}
}

/** @private */
function setVideoPoster() {
	videoPlayer.poster = "#";
	window.URL.revokeObjectURL(videoPlayer.src);
	videoPlayer.src = "#";
	videoPlayer.poster = poster;
}

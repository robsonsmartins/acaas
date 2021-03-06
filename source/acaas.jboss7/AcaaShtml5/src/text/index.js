/*---------------------------------------------------------------------------*/
/** 
 * @fileOverview Contem o JavaScript necessario para a aplicacao 
 *   &quot;Texto para Fala/Libras&quot;.
 * 
 * @author Robson Martins (robson@robsonmartins.com)
 * @version 1.0
 */
/*---------------------------------------------------------------------------*/

/* URL do servico AcaaS. */
var URL_ACAAS = "http://acaas.robsonmartins.com/acaas";

var audioPlayer = document.getElementById("audio_player");
var videoPlayer = document.getElementById("video_player");
var input       = document.getElementById("input");
var btnStart    = document.getElementById("btn_start");

var chkBrws = new CheckBrowser();
var acaas = new Acaas(URL_ACAAS);

var audioMediaType = "audio/wav";
var videoMediaType = "video/ogg";

var videoBuffer = null;
var audioBuffer = null;

var audioLoaded = false;
var videoLoaded = false;

var poster = null;

window.URL = window.URL || window.webkitURL;

audioPlayer.addEventListener("ended",getNextAudio);
videoPlayer.addEventListener("ended",getNextVideo);
input.addEventListener("keyup",inputKeyPress);

if (chkBrws.hasAudioVideo() && chkBrws.hasRemoteRequest()) {
	input.readOnly = false;
} else {
	window.alert("Navegador n\u00E3o suportado.");
}

/** @private */
function inputKeyPress(event) {
	btnStart.disabled = (input.value == null || input.value == "");
}

/** @private */
function start() {
	btnStart.disabled = true;
	input.readOnly = true;
	if (poster == null) {
		poster = videoPlayer.poster;
	}
	audioLoaded = false;
	videoLoaded = false;
	if (videoPlayer.canPlayType("video/ogg") == "") {
		videoMediaType = "video/mp4";
	} else {
		videoMediaType = "video/ogg";
	}
	videoBuffer = new Queue();
	audioBuffer = new Queue();
	acaas.getTTS(input.value, audioMediaType, onMediaTTS, onFinishMediaTTS);
	acaas.getT2Libras(input.value, videoMediaType, onMediaT2Libras, onFinishMediaT2Libras);
}

/** @private */
function onMediaTTS(media, info) {
	audioBuffer.enqueue({"media": media, "info": info});
	if (!audioLoaded) {
		audioLoaded = true;
		getNextAudio();
	}
}

/** @private */
function onFinishMediaTTS() {
	audioLoaded = false;
	btnStart.disabled = videoLoaded;
	input.readOnly = btnStart.disabled;
}

/** @private */
function onMediaT2Libras(media, info) {
	videoBuffer.enqueue({"media": media, "info": info});
	if (!videoLoaded) {
		videoLoaded = true;
		videoPlayer.poster = "";
		getNextVideo();
	}
}

/** @private */
function onFinishMediaT2Libras() {
	videoLoaded = false;
	btnStart.disabled = audioLoaded;
	input.readOnly = btnStart.disabled;
}

/** @private */
function getNextAudio() {
	var data = audioBuffer.dequeue();
	if (data != undefined) {
		window.URL.revokeObjectURL(audioPlayer.src);
		audioPlayer.src = window.URL.createObjectURL(data.media);
		audioPlayer.type = audioMediaType;
		audioPlayer.load();
		audioPlayer.play();
	} else if (audioLoaded){
		setTimeOut(getNextAudio,200);
	}
}

/** @private */
function getNextVideo() {
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
		videoPlayer.poster = "#";
		window.URL.revokeObjectURL(videoPlayer.src);
		videoPlayer.src = "#";
		videoPlayer.poster = poster;
	}
}

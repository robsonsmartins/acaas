function Speech(){var DEFAULT_SILENCE_LEVEL=-20;var DEFAULT_SILENCE_LENGTH=200;var DEFAULT_SAMPLE_RATE=44100;var RESAMPLE_FACTOR=4;window.AudioContext=window.AudioContext||window.webkitAudioContext||window.mozAudioContext;navigator.getUserMedia=navigator.getUserMedia||navigator.webkitGetUserMedia||navigator.mozGetUserMedia||navigator.msGetUserMedia;var onSuccess=null;var onError=null;var recording=false;var status=0;var context=null;var volume=null;var localStream=null;var audioInput=null;var recorder=null;var leftchannel=[];var rightchannel=[];var recordingLength=0;var leftBuffer=null;var rightBuffer=null;var monoBuffer=null;var silenceFrames=0;var silenceLevel=DEFAULT_SILENCE_LEVEL;var silenceLength=DEFAULT_SILENCE_LENGTH;var sampleRate=DEFAULT_SAMPLE_RATE;this.getSilenceLevel=function(){return silenceLevel;};this.setSilenceLevel=function(level){silenceLevel=level;return getSilenceLevel();};this.getSilenceLength=function(){return silenceLength;};this.setSilenceLength=function(length){getSilenceLength=length;return getSilenceLength();};this.startCapture=function(onSuccessCallback,onErrorCallback){onSuccess=onSuccessCallback;onError=onErrorCallback;recording=true;initRecord();navigator.getUserMedia({audio:true},onCapture,onError);};this.stopCapture=function(){if(recording){recording=false;localStream.stop();pauseRecord();}};function initRecord(){leftchannel=[];rightchannel=[];recordingLength=0;silenceFrames=0;status=0;}function pauseRecord(){var audio=pcmToWav(leftchannel,rightchannel,recordingLength,sampleRate);if(audio!=null&&onSuccess){onSuccess(audio);}}function onCapture(stream){localStream=stream;if(!recording){return;}var bufferSize=2048;context=new window.AudioContext();volume=context.createGain();audioInput=context.createMediaStreamSource(stream);sampleRate=context.sampleRate;audioInput.connect(volume);recorder=context.createJavaScriptNode(bufferSize,2,2);volume.connect(recorder);recorder.connect(context.destination);recorder.onaudioprocess=function(audio){var left=audio.inputBuffer.getChannelData(0);var right=audio.inputBuffer.getChannelData(1);leftchannel.push(new Float32Array(left));rightchannel.push(new Float32Array(right));recordingLength+=bufferSize;if(detectSilence(left,right,silenceLevel,silenceLength)){pauseRecord();initRecord();}};}function detectSilence(left,right,dbSilence,lenSilence){var lBuf=new Float32Array(left);var rBuf=new Float32Array(right);var mono=resampleMonoByX(lBuf,rBuf,RESAMPLE_FACTOR);var len=mono.length;for(var i=0;i<len;i++){if(!isSilence(mono[i]*32767,32767,dbSilence)){silenceFrames=0;if(status==0){status=1;}break;}else{if(status!=0){silenceFrames++;}}}var silenceNeedFrames=(sampleRate/RESAMPLE_FACTOR)*lenSilence/1000;var hasSilence=(silenceFrames>=silenceNeedFrames);if(hasSilence&&status==1){status=2;}return(status==2);}function isSilence(data,peek,dbSilence){var dbRMS=10*(Math.log(Math.abs(data)/peek)/Math.LN10);if(dbRMS<dbSilence||data==0){return true;}return false;}function pcmToWav(left,right,reclen,srate){try{leftBuffer=mergeBuffers(left,reclen);rightBuffer=mergeBuffers(right,reclen);monoBuffer=resampleMonoByX(leftBuffer,rightBuffer,RESAMPLE_FACTOR);}catch(e){return null;}var monoLen=monoBuffer.length;if(silenceFrames>=monoLen){return null;}var buffer=new ArrayBuffer(44+monoBuffer.length*2);var data=new DataView(buffer);writeUTFBytes(data,0,"RIFF");data.setUint32(4,44+monoLen*2,true);writeUTFBytes(data,8,"WAVE");writeUTFBytes(data,12,"fmt ");data.setUint32(16,16,true);data.setUint16(20,1,true);data.setUint16(22,1,true);data.setUint32(24,(srate/RESAMPLE_FACTOR),true);data.setUint32(28,(srate/RESAMPLE_FACTOR)*4,true);data.setUint16(32,4,true);data.setUint16(34,16,true);writeUTFBytes(data,36,"data");data.setUint32(40,monoLen*2,true);var index=44;var volume=1;for(var i=0;i<monoLen;i++){data.setInt16(index,monoBuffer[i]*(32767*volume),true);index+=2;}return data;}function resampleMonoByX(left,right,x){if(x==undefined||x==null||x==0){x=1;}var len=left.length;var result=new Float32Array(len/x);for(var i=0,j=0;i<len;i++,j+=x){result[i]=(left[j]+right[j])/2;}return result;}function mergeBuffers(channelBuf,reclen){var result=new Float32Array(reclen);var offset=0;var lng=channelBuf.length;for(var i=0;i<lng;i++){var buffer=channelBuf[i];result.set(buffer,offset);offset+=buffer.length;}return result;}function writeUTFBytes(dview,offset,string){var lng=string.length;for(var i=0;i<lng;i++){dview.setUint8(offset+i,string.charCodeAt(i));}}}
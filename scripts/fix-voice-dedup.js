const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let c = fs.readFileSync(file, 'utf8');
let changes = 0;

// Fix 1: Extract duplicated RecognitionListener into a shared helper function
const oldLauncher = `    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            isListening = true
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        exerciseSearchQuery = matches[0]
                    }
                    isListening = false
                }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { isListening = false }
                override fun onError(error: Int) { isListening = false }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(recognizerIntent)
        }
    }`;

const newLauncher = `    fun startVoiceListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        isListening = true
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)
        }
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    exerciseSearchQuery = matches[0]
                }
                isListening = false
            }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) {
                isListening = false
                android.widget.Toast.makeText(context, strings.voiceSearchError, android.widget.Toast.LENGTH_SHORT).show()
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(recognizerIntent)
    }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startVoiceListening()
    }`;

if (c.includes(oldLauncher)) {
    c = c.replace(oldLauncher, newLauncher);
    changes++;
    console.log('1. Extracted startVoiceListening() helper + Toast on error + isRecognitionAvailable check');
}

// Fix 2: Replace mic button onClick to use the helper function
const oldMicClick = `onClick = {
                                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            if (isListening) {
                                                speechRecognizer.stopListening()
                                                isListening = false
                                            } else {
                                                isListening = true
                                                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, context.resources.configuration.locales[0])
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)
                                                }
                                                speechRecognizer.setRecognitionListener(object : RecognitionListener {
                                                    override fun onResults(results: Bundle?) {
                                                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                                        if (!matches.isNullOrEmpty()) {
                                                            exerciseSearchQuery = matches[0]
                                                        }
                                                        isListening = false
                                                    }
                                                    override fun onReadyForSpeech(params: Bundle?) {}
                                                    override fun onBeginningOfSpeech() {}
                                                    override fun onRmsChanged(rmsdB: Float) {}
                                                    override fun onBufferReceived(buffer: ByteArray?) {}
                                                    override fun onEndOfSpeech() { isListening = false }
                                                    override fun onError(error: Int) { isListening = false }
                                                    override fun onPartialResults(partialResults: Bundle?) {}
                                                    override fun onEvent(eventType: Int, params: Bundle?) {}
                                                })
                                                speechRecognizer.startListening(recognizerIntent)
                                            }
                                        }
                                    }`;

const newMicClick = `onClick = {
                                        if (isListening) {
                                            speechRecognizer.stopListening()
                                            isListening = false
                                        } else if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                            startVoiceListening()
                                        } else {
                                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                        }
                                    }`;

if (c.includes(oldMicClick)) {
    c = c.replace(oldMicClick, newMicClick);
    changes++;
    console.log('2. Replaced mic button onClick with helper + permission request + stop toggle');
}

// Fix 3: Add speechRecognizer.cancel() before destroy()
const disposeTarget = 'onDispose { speechRecognizer.destroy() }';
const disposeReplace = 'onDispose { speechRecognizer.cancel(); speechRecognizer.destroy() }';
if (c.includes(disposeTarget)) {
    c = c.replace(disposeTarget, disposeReplace);
    changes++;
    console.log('3. Added cancel() before destroy() in DisposableEffect');
}

fs.writeFileSync(file, c, 'utf8');
console.log('\nTotal: ' + changes + ' changes');

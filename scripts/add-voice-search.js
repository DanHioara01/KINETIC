const fs = require('fs');
const file = 'app/src/main/java/com/example/kinetic/MainActivity.kt';
let content = fs.readFileSync(file, 'utf8');
let changes = 0;

// 1. Add required imports (after the existing import block)
const importTarget = 'import android.content.pm.PackageManager';
const importReplace = importTarget + '\nimport android.speech.SpeechRecognizer\nimport android.speech.RecognitionListener\nimport android.content.Intent\nimport android.os.Bundle\nimport androidx.core.app.ActivityCompat';
if (content.includes(importTarget) && !content.includes('import android.speech.SpeechRecognizer')) {
    content = content.replace(importTarget, importReplace);
    changes++;
    console.log('1. Added SpeechRecognizer imports');
}

// 2. Add state variables in CalendarScreen (after exerciseEquipmentFilter)
const stateTarget = '    var exerciseEquipmentFilter by remember { mutableStateOf<String?>(null) }';
const stateReplace = stateTarget + '\n    var isListening by remember { mutableStateOf(false) }\n    var isVoiceAvailable by remember { mutableStateOf(false) }\n    val context = LocalContext.current\n    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }\n    val activity = context as? ComponentActivity\n    val audioPermissionLauncher = rememberLauncherForActivityResult(\n        contract = ActivityResultContracts.RequestPermission()\n    ) { granted ->\n        if (granted) {\n            isVoiceAvailable = true\n        }\n    }\n    DisposableEffect(Unit) {\n        onDispose { speechRecognizer.destroy() }\n    }';
if (content.includes(stateTarget) && !content.includes('isListening by remember')) {
    content = content.replace(stateTarget, stateReplace);
    changes++;
    console.log('2. Added speech-to-text state variables');
}

// 3. Replace the trailingIcon to include mic button
const trailingOld = `                    trailingIcon = {\n                            if (exerciseSearchQuery.isNotEmpty()) {\n                                IconButton(onClick = { exerciseSearchQuery = "" }) {\n                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)\n                                }\n                            }\n                        },`;
const trailingNew = `                    trailingIcon = {\n                            Row {\n                                // Mic button for voice search\n                                IconButton(\n                                    onClick = {\n                                        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {\n                                            isVoiceAvailable = true\n                                        } else {\n                                            audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)\n                                        }\n                                        if (isVoiceAvailable) {\n                                            if (isListening) {\n                                                speechRecognizer.stopListening()\n                                                isListening = false\n                                            } else {\n                                                isListening = true\n                                                val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {\n                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)\n                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, context.resources.configuration.locales[0])\n                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, strings.voiceSearch)\n                                                }\n                                                speechRecognizer.setRecognitionListener(object : RecognitionListener {\n                                                    override fun onResults(results: Bundle?) {\n                                                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)\n                                                        if (!matches.isNullOrEmpty()) {\n                                                            exerciseSearchQuery = matches[0]\n                                                        }\n                                                        isListening = false\n                                                    }\n                                                    override fun onReadyForSpeech(params: Bundle?) {}\n                                                    override fun onBeginningOfSpeech() {}\n                                                    override fun onRmsChanged(rmsdB: Float) {}\n                                                    override fun onBufferReceived(buffer: ByteArray?) {}\n                                                    override fun onEndOfSpeech() { isListening = false }\n                                                    override fun onError(error: Int) { isListening = false }\n                                                    override fun onPartialResults(partialResults: Bundle?) {}\n                                                    override fun onEvent(eventType: Int, params: Bundle?) {}\n                                                })\n                                                speechRecognizer.startListening(recognizerIntent)\n                                            }\n                                        }\n                                    }\n                                ) {\n                                    Icon(\n                                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,\n                                        contentDescription = strings.voiceSearch,\n                                        tint = if (isListening) accentColor() else textSecondary\n                                    )\n                                }\n                                // Clear button\n                                if (exerciseSearchQuery.isNotEmpty()) {\n                                    IconButton(onClick = { exerciseSearchQuery = "" }) {\n                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = textSecondary)\n                                    }\n                                }\n                            }\n                        },`;
if (content.includes(trailingOld)) {
    content = content.replace(trailingOld, trailingNew);
    changes++;
    console.log('3. Replaced trailingIcon with mic button + clear button');
}

// 4. Add VoiceSearchListeningIndicator below the search bar
const searchEnd = 'shape = RoundedCornerShape(12.dp)\n                    )';
const searchEndReplace = 'shape = RoundedCornerShape(12.dp)\n                    )\n                    // Voice search listening indicator\n                    if (isListening) {\n                        Row(\n                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),\n                            verticalAlignment = Alignment.CenterVertically,\n                            horizontalArrangement = Arrangement.Center\n                        ) {\n                            CircularProgressIndicator(\n                                modifier = Modifier.size(16.dp),\n                                strokeWidth = 2.dp,\n                                color = accentColor()\n                            )\n                            Spacer(modifier = Modifier.width(8.dp))\n                            Text(\n                                strings.listening,\n                                color = accentColor(),\n                                fontSize = 12.sp,\n                                fontWeight = FontWeight.Medium\n                            )\n                        }\n                    }';
if (content.includes(searchEnd) && !content.includes('isListening) {')) {
    content = content.replace(searchEnd, searchEndReplace);
    changes++;
    console.log('4. Added voice search listening indicator');
}

fs.writeFileSync(file, content, 'utf8');
console.log('\nMainActivity.kt saved (' + changes + ' changes)');

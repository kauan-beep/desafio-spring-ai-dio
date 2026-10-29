package br.com.budgetai.ai;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AudioService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final OpenAiAudioSpeechModel speechModel;

    public AudioService(OpenAiAudioTranscriptionModel transcriptionModel,
                        OpenAiAudioSpeechModel speechModel) {
        this.transcriptionModel = transcriptionModel;
        this.speechModel = speechModel;
    }

    /** Áudio -> texto (Transcription API). */
    public String transcribe(MultipartFile file) {
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language("pt")
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(file.getResource(), options);
        return transcriptionModel.call(prompt).getResult().getOutput();
    }

    /** Texto -> áudio MP3 (Speech API). */
    public byte[] speak(String text) {
        return speechModel.call(text);
    }
}

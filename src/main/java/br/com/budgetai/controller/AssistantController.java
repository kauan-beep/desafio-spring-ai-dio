package br.com.budgetai.controller;

import br.com.budgetai.ai.AudioService;
import br.com.budgetai.ai.BudgetAssistant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final BudgetAssistant assistant;
    private final AudioService audioService;

    public AssistantController(BudgetAssistant assistant, AudioService audioService) {
        this.assistant = assistant;
        this.audioService = audioService;
    }

    public record TextRequest(String message) {
    }

    public record AssistantResponse(String transcription, String answer) {
    }

    /** Fluxo por texto: útil para testar a IA sem áudio. */
    @PostMapping("/text")
    public AssistantResponse text(@RequestBody TextRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem é obrigatória.");
        }
        return new AssistantResponse(request.message(), assistant.ask(request.message()));
    }

    /** Fluxo principal: áudio -> texto -> IA (Tool Calling) -> resposta em texto. */
    @PostMapping(value = "/audio", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssistantResponse audio(@RequestParam("file") MultipartFile file) {
        String transcription = transcribeOrFail(file);
        return new AssistantResponse(transcription, assistant.ask(transcription));
    }

    /** Mesmo fluxo, mas a resposta final volta em áudio MP3. */
    @PostMapping(value = "/audio/speech",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = "audio/mpeg")
    public byte[] audioSpeech(@RequestParam("file") MultipartFile file) {
        String transcription = transcribeOrFail(file);
        return audioService.speak(assistant.ask(transcription));
    }

    private String transcribeOrFail(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie um arquivo de áudio.");
        }
        return audioService.transcribe(file);
    }
}

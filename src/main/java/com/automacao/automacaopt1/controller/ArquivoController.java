package com.automacao.automacaopt1.controller;

import com.automacao.automacaopt1.model.ProcessamentoStatus;
import com.automacao.automacaopt1.service.ArquivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/arquivos")
public class ArquivoController {

    @Autowired
    private ArquivoService arquivoService;

    @PostMapping("/upload")
    public ResponseEntity<ProcessamentoStatus> uploadArquivo(@RequestParam("file") MultipartFile file) {
        ProcessamentoStatus status = arquivoService.processarArquivo(file);
        return ResponseEntity.ok(status);
    }
}

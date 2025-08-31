// MetrcDocsController.java
package com.techvvs.inventory.metrcdocs;

import com.techvvs.inventory.metrcdocs.DocsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/docs/metrc")
public class MetrcDocsController {

    private final DocsService docsService;
    public MetrcDocsController(DocsService docsService) { this.docsService = docsService; }

    // a) Structured JSON (default)
    // GET /api/docs/metrc/search?q=packages
    @GetMapping("/search")
    public Map<String,Object> search(@RequestParam("q") String query) {
        return docsService.searchPackagesStructured(query);
    }

    // b) Raw Markdown (nice for quick UI rendering)
    // GET /api/docs/metrc/search/raw?q=packages
    @GetMapping(value = "/search/raw", produces = "text/markdown; charset=UTF-8")
    public String searchRaw(@RequestParam("q") String query) {
        return docsService.searchPackagesRaw(query);
    }

    // MetrcDocsController.java
    @GetMapping("/debug/raw")
    public Map<String, Object> raw(@RequestParam("q") String query) {
        return Map.of("raw", docsService.searchPackagesRaw(query));
    }

//    // New AI-summarized answer
//    // GET /api/docs/metrc/search/ai?q=How do I unfinish a package in CA?
//    @GetMapping("/search/ai")
//    public Map<String, Object> searchAI(@RequestParam("q") String query) throws Exception {
//        return docsService.searchPackagesAI(query);
//    }

    // GET /api/docs/metrc/ask?q=...
    @GetMapping("/ask")
    public Map<String,Object> ask(@RequestParam("q") String question) throws Exception {
        return docsService.askWithConnectorAndClaude(question);
    }


}

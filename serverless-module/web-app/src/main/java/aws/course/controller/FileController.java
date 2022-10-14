package aws.course.controller;

import aws.course.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping
    public ResponseEntity upload(@RequestParam("file")MultipartFile file) {
        return fileService.upload(file) ? ResponseEntity.ok("The file was uploaded"):
                ResponseEntity.badRequest().body("Something went wrong");
    }

    @GetMapping("/{name}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String name) {
        var data = fileService.download(name);
        return ResponseEntity.ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; fileName=\"" + name + "\"")
                .body(new ByteArrayResource(data));
    }

    @DeleteMapping("/{name}")
    public void delete(@PathVariable String name) {
        fileService.delete(name);
    }
}

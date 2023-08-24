package com.cubbysoftware.converter.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IFileConverterService {
    ResponseEntity<Object> convert(
            final MultipartFile inputFile,
            final String outputFormat,
            final Map<String, String> parameters);
}

package com.cubbysoftware.converter.controllers;

import com.cubbysoftware.converter.services.IFileConverterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping(path = "/api/v1/file-converter", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "File Converter Service", description = "File Converter Service")
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileConverterController {
    final IFileConverterService fileConverterService;

    @Operation(
            summary =
                    "Converts the incoming document to the specified format (provided as request param)"
                            + " and returns the converted document.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Document converted successfully."),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The input document or output format is missing."),
                    @ApiResponse(responseCode = "500", description = "An unexpected error occurred.")
            })
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        /* default */ Object convertToUsingParam(
            @Parameter(description = "The input document to convert.", required = true)
            @RequestParam("data") final MultipartFile inputFile,
            @Parameter(
                    description = "The document format to convert the input document to.",
                    required = true)
            @RequestParam(name = "format") final String convertToFormat,
            @Parameter(description = "The custom options to apply to the conversion.")
            @RequestParam(required = false) final Map<String, String> parameters) {

        log.debug("convertUsingRequestParam > Converting file to {}", convertToFormat);
        return fileConverterService.convert(inputFile, convertToFormat, parameters);
    }
}

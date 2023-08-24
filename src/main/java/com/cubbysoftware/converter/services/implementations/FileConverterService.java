package com.cubbysoftware.converter.services.implementations;

import com.cubbysoftware.converter.services.IFileConverterService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.util.FileUtils;
import org.jodconverter.core.util.StringUtils;
import org.jodconverter.local.LocalConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class FileConverterService implements IFileConverterService {
    final OfficeManager officeManager;

    private static final String FILTER_DATA = "FilterData";
    private static final String FILTER_DATA_PREFIX_PARAM = "fd";
    private static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
    private static final String LOAD_FILTER_DATA_PREFIX_PARAM =
            LOAD_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;
    private static final String STORE_PROPERTIES_PREFIX_PARAM = "s";
    private static final String STORE_FILTER_DATA_PREFIX_PARAM =
            STORE_PROPERTIES_PREFIX_PARAM + FILTER_DATA_PREFIX_PARAM;

    public ResponseEntity<Object> convert(
            final MultipartFile inputFile,
            final String outputFormat,
            final Map<String, String> parameters) {
        if (inputFile.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (StringUtils.isBlank(outputFormat)) {
            return ResponseEntity.badRequest().build();
        }

        // Here, we could have a dedicated service that would convert document
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            final DocumentFormat targetFormat =
                    DefaultDocumentFormatRegistry.getFormatByExtension(outputFormat);
            Assert.notNull(targetFormat, "targetFormat must not be null");

            // Decode the parameters to load and store properties.
            final Map<String, Object> loadProperties =
                    new HashMap<>(LocalConverter.DEFAULT_LOAD_PROPERTIES);
            final Map<String, Object> storeProperties = new HashMap<>();
            decodeParameters(parameters, loadProperties, storeProperties);

            // Create a converter with the properties.
            final DocumentConverter converter =
                    LocalConverter.builder()
                            .officeManager(officeManager)
                            .loadProperties(loadProperties)
                            .storeProperties(storeProperties)
                            .build();

            // Convert...
            converter.convert(inputFile.getInputStream()).to(baos).as(targetFormat).execute();

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(targetFormat.getMediaType()));
            headers.add(
                    "Content-Disposition",
                    "attachment; filename="
                            + FileUtils.getBaseName(inputFile.getOriginalFilename())
                            + "."
                            + targetFormat.getExtension());
            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

        } catch (OfficeException | IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex);
        }
    }

    private void addProperty(
            final String paramName,
            final Map.Entry<String, String> param,
            final Map<String, Object> properties) {

        final String name = param.getKey().substring(paramName.length());
        final String value = param.getValue();

        if ("true".equalsIgnoreCase(value)) {
            properties.put(name, Boolean.TRUE);
        } else if ("false".equalsIgnoreCase(value)) {
            properties.put(name, Boolean.FALSE);
        } else {
            try {
                final int ival = Integer.parseInt(value);
                properties.put(name, ival);
            } catch (NumberFormatException nfe) {
                properties.put(name, value);
            }
        }
    }

    private void addProperty(
            final String key,
            final String prefix,
            final Map.Entry<String, String> param,
            final Map<String, Object> properties) {
        if (key.startsWith(prefix)) {
            addProperty(key, param, properties);
        }
    }

    private void decodeParameters(
            final Map<String, String> parameters,
            final Map<String, Object> loadProperties,
            final Map<String, Object> storeProperties) {

        if (parameters == null || parameters.isEmpty()) {
            return;
        }

        final Map<String, Object> loadFilterDataProperties = new HashMap<>();
        final Map<String, Object> storeFilterDataProperties = new HashMap<>();
        for (final Map.Entry<String, String> param : parameters.entrySet()) {
            final String key = param.getKey().toLowerCase(Locale.ROOT);
            addProperty(key, LOAD_FILTER_DATA_PREFIX_PARAM, param, loadFilterDataProperties);
            addProperty(key, LOAD_PROPERTIES_PREFIX_PARAM, param, loadProperties);
            addProperty(key, STORE_FILTER_DATA_PREFIX_PARAM, param, storeFilterDataProperties);
            addProperty(key, STORE_PROPERTIES_PREFIX_PARAM, param, storeProperties);
        }

        if (!loadFilterDataProperties.isEmpty()) {
            loadProperties.put(FILTER_DATA, loadFilterDataProperties);
        }

        if (!storeFilterDataProperties.isEmpty()) {
            storeProperties.put(FILTER_DATA, storeFilterDataProperties);
        }
    }
}

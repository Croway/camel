/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.javahttp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for creating multipart form data content for HTTP requests. This class provides convenient methods to
 * build multipart/form-data bodies that can be used with the camel-java-http component.
 */
public final class JavaHttpMultipartUtils {

    private JavaHttpMultipartUtils() {
        // Utility class
    }

    /**
     * Generates a random boundary string for multipart content.
     *
     * @return a unique boundary string
     */
    public static String generateBoundary() {
        return "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Creates a simple multipart body with form fields.
     *
     * @param  boundary the boundary string to use
     * @param  fields   a map of field names to their values
     * @return          the multipart body as a string
     */
    public static String createMultipartBody(String boundary, Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
            sb.append("\r\n");
            sb.append(entry.getValue()).append("\r\n");
        }

        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    /**
     * Creates a multipart body with form fields using a generated boundary.
     *
     * @param  fields a map of field names to their values
     * @return        a MultipartData object containing the body and boundary
     */
    public static MultipartData createMultipartBody(Map<String, String> fields) {
        String boundary = generateBoundary();
        String body = createMultipartBody(boundary, fields);
        return new MultipartData(body, boundary);
    }

    /**
     * Creates a multipart body with a file upload and optional form fields.
     *
     * @param  boundary         the boundary string to use
     * @param  fileName         the name of the file being uploaded
     * @param  fileContent      the content of the file
     * @param  contentType      the content type of the file (e.g., "text/plain", "image/jpeg")
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  the multipart body as a string
     */
    public static String createFileUploadBody(
            String boundary, String fileName, String fileContent,
            String contentType, String fieldName, Map<String, String> additionalFields) {
        StringBuilder sb = new StringBuilder();

        // Add file part
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("\r\n");
        sb.append(fileContent).append("\r\n");

        // Add additional fields if provided
        if (additionalFields != null) {
            for (Map.Entry<String, String> entry : additionalFields.entrySet()) {
                sb.append("--").append(boundary).append("\r\n");
                sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
                sb.append("\r\n");
                sb.append(entry.getValue()).append("\r\n");
            }
        }

        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    /**
     * Creates a multipart body with a file upload and optional form fields using a generated boundary.
     *
     * @param  fileName         the name of the file being uploaded
     * @param  fileContent      the content of the file
     * @param  contentType      the content type of the file (e.g., "text/plain", "image/jpeg")
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  a MultipartData object containing the body and boundary
     */
    public static MultipartData createFileUploadBody(
            String fileName, String fileContent,
            String contentType, String fieldName, Map<String, String> additionalFields) {
        String boundary = generateBoundary();
        String body = createFileUploadBody(boundary, fileName, fileContent, contentType, fieldName, additionalFields);
        return new MultipartData(body, boundary);
    }

    /**
     * Creates a multipart body with a file upload using default settings.
     *
     * @param  fileName    the name of the file being uploaded
     * @param  fileContent the content of the file
     * @param  contentType the content type of the file
     * @return             a MultipartData object containing the body and boundary
     */
    public static MultipartData createFileUploadBody(String fileName, String fileContent, String contentType) {
        return createFileUploadBody(fileName, fileContent, contentType, "file", null);
    }

    /**
     * Creates a multipart body with a file upload from a File object.
     *
     * @param  boundary         the boundary string to use
     * @param  file             the file to upload
     * @param  contentType      the content type of the file (e.g., "text/plain", "image/jpeg")
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  the multipart body as a string
     * @throws IOException      if the file cannot be read
     */
    public static String createFileUploadBody(
            String boundary, File file, String contentType,
            String fieldName, Map<String, String> additionalFields)
            throws IOException {
        String fileName = file.getName();
        String fileContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        return createFileUploadBody(boundary, fileName, fileContent, contentType, fieldName, additionalFields);
    }

    /**
     * Creates a multipart body with a file upload from a File object using a generated boundary.
     *
     * @param  file             the file to upload
     * @param  contentType      the content type of the file (e.g., "text/plain", "image/jpeg")
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  a MultipartData object containing the body and boundary
     * @throws IOException      if the file cannot be read
     */
    public static MultipartData createFileUploadBody(
            File file, String contentType,
            String fieldName, Map<String, String> additionalFields)
            throws IOException {
        String boundary = generateBoundary();
        String body = createFileUploadBody(boundary, file, contentType, fieldName, additionalFields);
        return new MultipartData(body, boundary);
    }

    /**
     * Creates a multipart body with a file upload from a File object using default settings.
     *
     * @param  file        the file to upload
     * @param  contentType the content type of the file
     * @return             a MultipartData object containing the body and boundary
     * @throws IOException if the file cannot be read
     */
    public static MultipartData createFileUploadBody(File file, String contentType) throws IOException {
        return createFileUploadBody(file, contentType, "file", null);
    }

    /**
     * Creates a multipart body with a file upload from a File object, auto-detecting content type.
     *
     * @param  file             the file to upload
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  a MultipartData object containing the body and boundary
     * @throws IOException      if the file cannot be read
     */
    public static MultipartData createFileUploadBody(File file, String fieldName, Map<String, String> additionalFields)
            throws IOException {
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return createFileUploadBody(file, contentType, fieldName, additionalFields);
    }

    /**
     * Creates a multipart body with a file upload from a File object using auto-detected content type and default
     * settings.
     *
     * @param  file        the file to upload
     * @return             a MultipartData object containing the body and boundary
     * @throws IOException if the file cannot be read
     */
    public static MultipartData createFileUploadBody(File file) throws IOException {
        return createFileUploadBody(file, "file", null);
    }

    /**
     * Creates a multipart body with binary file content.
     *
     * @param  boundary         the boundary string to use
     * @param  fileName         the name of the file being uploaded
     * @param  fileContent      the binary content of the file
     * @param  contentType      the content type of the file
     * @param  fieldName        the form field name for the file
     * @param  additionalFields optional additional form fields
     * @return                  the multipart body as a byte array
     */
    public static byte[] createBinaryFileUploadBody(
            String boundary, String fileName, byte[] fileContent,
            String contentType, String fieldName, Map<String, String> additionalFields) {
        StringBuilder sb = new StringBuilder();

        // Build the header part
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("\r\n");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] newLineBytes = "\r\n".getBytes(StandardCharsets.UTF_8);

        // Build additional fields part
        StringBuilder additionalSb = new StringBuilder();
        if (additionalFields != null) {
            for (Map.Entry<String, String> entry : additionalFields.entrySet()) {
                additionalSb.append("--").append(boundary).append("\r\n");
                additionalSb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n");
                additionalSb.append("\r\n");
                additionalSb.append(entry.getValue()).append("\r\n");
            }
        }
        additionalSb.append("--").append(boundary).append("--\r\n");
        byte[] additionalBytes = additionalSb.toString().getBytes(StandardCharsets.UTF_8);

        // Combine all parts
        byte[] result = new byte[headerBytes.length + fileContent.length + newLineBytes.length + additionalBytes.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(fileContent, 0, result, headerBytes.length, fileContent.length);
        System.arraycopy(newLineBytes, 0, result, headerBytes.length + fileContent.length, newLineBytes.length);
        System.arraycopy(additionalBytes, 0, result, headerBytes.length + fileContent.length + newLineBytes.length,
                additionalBytes.length);

        return result;
    }

    /**
     * Creates the Content-Type header value for multipart form data.
     *
     * @param  boundary the boundary string
     * @return          the Content-Type header value
     */
    public static String createContentTypeHeader(String boundary) {
        return "multipart/form-data; boundary=" + boundary;
    }

    /**
     * A builder class for creating complex multipart bodies.
     */
    public static class MultipartBuilder {
        private final String boundary;
        private final StringBuilder sb;
        private boolean finished = false;

        public MultipartBuilder() {
            this.boundary = generateBoundary();
            this.sb = new StringBuilder();
        }

        public MultipartBuilder(String boundary) {
            this.boundary = boundary;
            this.sb = new StringBuilder();
        }

        public MultipartBuilder addField(String name, String value) {
            if (finished) {
                throw new IllegalStateException("Builder has already been finished");
            }
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
            sb.append("\r\n");
            sb.append(value).append("\r\n");
            return this;
        }

        public MultipartBuilder addFile(String fieldName, String fileName, String content, String contentType) {
            if (finished) {
                throw new IllegalStateException("Builder has already been finished");
            }
            sb.append("--").append(boundary).append("\r\n");
            sb.append("Content-Disposition: form-data; name=\"").append(fieldName)
                    .append("\"; filename=\"").append(fileName).append("\"\r\n");
            sb.append("Content-Type: ").append(contentType).append("\r\n");
            sb.append("\r\n");
            sb.append(content).append("\r\n");
            return this;
        }

        public MultipartBuilder addFile(String fieldName, File file, String contentType) throws IOException {
            if (finished) {
                throw new IllegalStateException("Builder has already been finished");
            }
            String fileName = file.getName();
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return addFile(fieldName, fileName, content, contentType);
        }

        public MultipartBuilder addFile(String fieldName, File file) throws IOException {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return addFile(fieldName, file, contentType);
        }

        public MultipartData build() {
            if (!finished) {
                sb.append("--").append(boundary).append("--\r\n");
                finished = true;
            }
            return new MultipartData(sb.toString(), boundary);
        }

        public String getBoundary() {
            return boundary;
        }
    }

    /**
     * A data class that holds both the multipart body and its boundary.
     */
    public static class MultipartData {
        private final String body;
        private final String boundary;

        public MultipartData(String body, String boundary) {
            this.body = body;
            this.boundary = boundary;
        }

        public String getBody() {
            return body;
        }

        public String getBoundary() {
            return boundary;
        }

        public String getContentType() {
            return createContentTypeHeader(boundary);
        }
    }
}

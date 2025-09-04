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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.javahttp.JavaHttpMultipartUtils.MultipartData;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaHttpMultipartTest extends JavaHttpTestBase {

    @Test
    public void testMultipartFormDataUpload() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"form\": {\"field1\": \"value1\", \"field2\": \"value2\"}, \"headers\": {\"Content-Type\": \"multipart/form-data\"}}")));

        Exchange exchange = template.request("direct:multipartUpload", exchange1 -> {
            String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
            String multipartBody = createMultipartBody(boundary);

            exchange1.getIn().setBody(multipartBody);
            exchange1.getIn().setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        // Verify that the multipart data was received
        assertTrue(response.contains("field1") && response.contains("value1"));
    }

    @Test
    public void testMultipartFileUpload() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"files\": {\"file\": \"This is a test file content for multipart upload.\"}, \"form\": {\"description\": \"Test file upload\"}}")));

        Exchange exchange = template.request("direct:fileUpload", exchange1 -> {
            String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
            String fileContent = "This is a test file content for multipart upload.";
            String multipartBody = createFileUploadBody(boundary, fileContent);

            exchange1.getIn().setBody(multipartBody);
            exchange1.getIn().setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        // Verify file upload was processed
        assertTrue(response.contains("files"));
    }

    @Test
    public void testMultipartWithMultipleFields() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"form\": {\"name\": \"John Doe\", \"email\": \"john.doe@example.com\", \"message\": \"This is a test message with multiple lines.\"}}")));

        Exchange exchange = template.request("direct:multipleFields", exchange1 -> {
            String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");
            String multipartBody = createMultipleFieldsBody(boundary);

            exchange1.getIn().setBody(multipartBody);
            exchange1.getIn().setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        // Verify multiple fields were received
        assertTrue(response.contains("form"));
    }

    @Test
    public void testMultipartWithUtilities() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"form\": {\"field1\": \"value1\", \"field2\": \"value2\"}, \"headers\": {\"Content-Type\": \"multipart/form-data\"}}")));

        Exchange exchange = template.request("direct:utilityUpload", exchange1 -> {
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("field1", "value1");
            fields.put("field2", "value2");

            MultipartData multipartData = JavaHttpMultipartUtils.createMultipartBody(fields);

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("field1") && response.contains("value1"));
    }

    @Test
    public void testFileUploadWithUtilities() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"files\": {\"file\": \"This is test content from utility.\"}, \"form\": {\"description\": \"Test utility upload\"}}")));

        Exchange exchange = template.request("direct:utilityFileUpload", exchange1 -> {
            String fileContent = "This is test content from utility.";
            Map<String, String> additionalFields = new LinkedHashMap<>();
            additionalFields.put("description", "Test utility upload");

            MultipartData multipartData = JavaHttpMultipartUtils.createFileUploadBody(
                    "test-utility.txt", fileContent, "text/plain", "file", additionalFields);

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("files"));
    }

    @Test
    public void testMultipartBuilderPattern() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"form\": {\"name\": \"Builder Test\", \"email\": \"builder@test.com\"}, \"files\": {\"document\": \"Builder content\"}}")));

        Exchange exchange = template.request("direct:builderUpload", exchange1 -> {
            MultipartData multipartData = new JavaHttpMultipartUtils.MultipartBuilder()
                    .addField("name", "Builder Test")
                    .addField("email", "builder@test.com")
                    .addFile("document", "test-doc.txt", "Builder content", "text/plain")
                    .build();

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("form") && response.contains("files"));
    }

    @Test
    public void testFileUploadFromResourceFile() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"files\": {\"document\": \"This is a test file for multipart upload testing.\"}, \"form\": {\"description\": \"File from resources\"}}")));

        Exchange exchange = template.request("direct:fileFromResources", exchange1 -> {
            File testFile = new File(getClass().getClassLoader().getResource("test-upload.txt").getFile());
            Map<String, String> additionalFields = new LinkedHashMap<>();
            additionalFields.put("description", "File from resources");

            MultipartData multipartData = JavaHttpMultipartUtils.createFileUploadBody(
                    testFile, "text/plain", "document", additionalFields);

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("files"));
    }

    @Test
    public void testFileUploadWithAutoDetectedContentType() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"files\": {\"data\": \"{\\\"testData\\\":{\\\"name\\\":\\\"Test JSON File\\\"}}\"}}")));

        Exchange exchange = template.request("direct:autoDetectContentType", exchange1 -> {
            File testFile = new File(getClass().getClassLoader().getResource("test-data.json").getFile());

            MultipartData multipartData = JavaHttpMultipartUtils.createFileUploadBody(testFile, "data", null);

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("files"));
    }

    @Test
    public void testMultipartBuilderWithFile() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"form\": {\"project\": \"Test Project\", \"version\": \"1.0\"}, \"files\": {\"config\": \"{\\\"testData\\\":{\\\"name\\\":\\\"Test JSON File\\\"}}\", \"readme\": \"This is a test file\"}}")));

        Exchange exchange = template.request("direct:builderWithFile", exchange1 -> {
            File jsonFile = new File(getClass().getClassLoader().getResource("test-data.json").getFile());
            File textFile = new File(getClass().getClassLoader().getResource("test-upload.txt").getFile());

            MultipartData multipartData = new JavaHttpMultipartUtils.MultipartBuilder()
                    .addField("project", "Test Project")
                    .addField("version", "1.0")
                    .addFile("config", jsonFile)
                    .addFile("readme", textFile, "text/plain")
                    .build();

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("form") && response.contains("files"));
    }

    @Test
    public void testSimpleFileUpload() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"files\": {\"file\": \"This is a test file for multipart upload testing.\"}}")));

        Exchange exchange = template.request("direct:simpleFileUpload", exchange1 -> {
            File testFile = new File(getClass().getClassLoader().getResource("test-upload.txt").getFile());

            MultipartData multipartData = JavaHttpMultipartUtils.createFileUploadBody(testFile);

            exchange1.getIn().setBody(multipartData.getBody());
            exchange1.getIn().setHeader("Content-Type", multipartData.getContentType());
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));

        String response = message.getBody(String.class);
        assertNotNull(response);

        assertTrue(response.contains("files"));
    }

    @Test
    public void testMultipartInputStream() throws Exception {
        wireMock.stubFor(post(urlPathEqualTo("/post"))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"form\": {\"field1\": \"value1\", \"field2\": \"value2\"}}")));

        Exchange exchange = template.request("direct:inputStreamUpload", exchange1 -> {
            String simpleBody = "field1=value1&field2=value2";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(simpleBody.getBytes(StandardCharsets.UTF_8));

            exchange1.getIn().setBody(inputStream);
            exchange1.getIn().setHeader("Content-Type", "application/x-www-form-urlencoded");
        });

        assertNotNull(exchange);
        Message message = exchange.getMessage();
        assertEquals(200, message.getHeader(JavaHttpConstants.HTTP_RESPONSE_CODE));
    }

    private String createMultipartBody(String boundary) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"field1\"\r\n");
        sb.append("\r\n");
        sb.append("value1\r\n");
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"field2\"\r\n");
        sb.append("\r\n");
        sb.append("value2\r\n");
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    private String createFileUploadBody(String boundary, String fileContent) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n");
        sb.append("Content-Type: text/plain\r\n");
        sb.append("\r\n");
        sb.append(fileContent).append("\r\n");
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"description\"\r\n");
        sb.append("\r\n");
        sb.append("Test file upload\r\n");
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    private String createMultipleFieldsBody(String boundary) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"name\"\r\n");
        sb.append("\r\n");
        sb.append("John Doe\r\n");
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"email\"\r\n");
        sb.append("\r\n");
        sb.append("john.doe@example.com\r\n");
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"message\"\r\n");
        sb.append("\r\n");
        sb.append("This is a test message with multiple lines.\nSecond line here.\r\n");
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:multipartUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:fileUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:multipleFields")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:inputStreamUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:utilityUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:utilityFileUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:builderUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:fileFromResources")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:autoDetectContentType")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:builderWithFile")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");

                from("direct:simpleFileUpload")
                        .to("java-http://" + getWireMockHost() + "/post?httpMethod=POST");
            }
        };
    }
}

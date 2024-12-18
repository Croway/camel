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
package org.apache.camel.dataformat.protobuf;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.util.JsonFormat;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.NoTypeConversionAvailableException;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.spi.DataFormatContentTypeHeader;
import org.apache.camel.spi.DataFormatName;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.annotations.Dataformat;
import org.apache.camel.support.service.ServiceSupport;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
import org.apache.commons.io.IOUtils;

@Dataformat("protobuf")
@Metadata(excludeProperties = "library,objectMapper,useDefaultObjectMapper,jsonViewTypeName,jsonView,include,allowJmsType," +
                              "collectionTypeName,collectionType,useList,combineUnicodeSurrogates,moduleClassNames,moduleRefs,enableFeatures,"
                              +
                              "disableFeatures,allowUnmarshallType,timezone,autoDiscoverObjectMapper," +
                              "schemaResolver,autoDiscoverSchemaResolver,unmarshalType,unmarshalTypeName")
public class ProtobufDataFormat extends ServiceSupport
        implements DataFormat, DataFormatName, DataFormatContentTypeHeader, CamelContextAware {

    public static final String CONTENT_TYPE_FORMAT_NATIVE = "native";
    public static final String CONTENT_TYPE_FORMAT_JSON = "json";

    private static final String CONTENT_TYPE_HEADER_NATIVE = "application/octet-stream";
    private static final String CONTENT_TYPE_HEADER_JSON = "application/json";

    private CamelContext camelContext;
    private Message defaultInstance;
    private String instanceClassName;
    private boolean contentTypeHeader = true;
    private String contentTypeFormat = CONTENT_TYPE_FORMAT_NATIVE;

    public ProtobufDataFormat() {
    }

    public ProtobufDataFormat(Message defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    public ProtobufDataFormat(Message defaultInstance, String contentTypeFormat) {
        this.defaultInstance = defaultInstance;
        this.contentTypeFormat = contentTypeFormat;
    }

    @Override
    public String getDataFormatName() {
        return "protobuf";
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void setDefaultInstance(Message instance) {
        this.defaultInstance = instance;
    }

    public void setDefaultInstance(Object instance) {
        if (instance instanceof Message) {
            this.defaultInstance = (Message) instance;
        } else {
            throw new IllegalArgumentException(
                    "The argument for setDefaultInstance should be subClass of com.google.protobuf.Message");
        }
    }

    public void setInstanceClass(String className) {
        ObjectHelper.notNull(className, "ProtobufDataFormat instaceClass");
        instanceClassName = className;
    }

    public void setContentTypeHeader(boolean contentTypeHeader) {
        this.contentTypeHeader = contentTypeHeader;
    }

    public boolean isContentTypeHeader() {
        return contentTypeHeader;
    }

    /*
     * Defines a content type format in which protobuf message will be
     * serialized/deserialized from(to) the Java been. It can be native protobuf
     * format or json fields representation. The default value is 'native'.
     */
    public void setContentTypeFormat(String contentTypeFormat) {
        StringHelper.notEmpty(contentTypeFormat, "ProtobufDataFormat contentTypeFormat");
        this.contentTypeFormat = contentTypeFormat;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.camel.spi.DataFormat#marshal(org.apache.camel.Exchange,
     * java.lang.Object, java.io.OutputStream)
     */
    @Override
    public void marshal(final Exchange exchange, final Object graph, final OutputStream outputStream) throws Exception {
        final Message inputMessage = convertGraphToMessage(exchange, graph);

        String contentTypeHeader = CONTENT_TYPE_HEADER_NATIVE;
        if (contentTypeFormat.equals(CONTENT_TYPE_FORMAT_JSON)) {
            IOUtils.write(JsonFormat.printer().print(inputMessage), outputStream, StandardCharsets.UTF_8);
            contentTypeHeader = CONTENT_TYPE_HEADER_JSON;
        } else if (contentTypeFormat.equals(CONTENT_TYPE_FORMAT_NATIVE)) {
            inputMessage.writeTo(outputStream);
        } else {
            throw new CamelException("Invalid protobuf content type format: " + contentTypeFormat);
        }

        if (isContentTypeHeader()) {
            exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, contentTypeHeader);
        }
    }

    private Message convertGraphToMessage(final Exchange exchange, final Object inputData)
            throws NoTypeConversionAvailableException {
        if (!(inputData instanceof Message)) {
            // we just need to make sure input data is not a proto type
            final Map<?, ?> messageInMap
                    = exchange.getContext().getTypeConverter().tryConvertTo(Map.class, exchange, inputData);
            if (messageInMap != null) {
                return ProtobufConverter.toProto(messageInMap, defaultInstance);
            }
        }
        return exchange.getContext().getTypeConverter().mandatoryConvertTo(Message.class, exchange, inputData);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.camel.spi.DataFormat#unmarshal(org.apache.camel.Exchange,
     * java.io.InputStream)
     */
    @Override
    public Object unmarshal(final Exchange exchange, final InputStream inputStream) throws Exception {
        ObjectHelper.notNull(defaultInstance, "defaultInstance or instanceClassName must be set", this);
        Builder builder = defaultInstance.newBuilderForType();

        if (contentTypeFormat.equals(CONTENT_TYPE_FORMAT_JSON)) {
            JsonFormat.parser().ignoringUnknownFields().merge(new InputStreamReader(inputStream), builder);
        } else if (contentTypeFormat.equals(CONTENT_TYPE_FORMAT_NATIVE)) {
            builder = defaultInstance.newBuilderForType().mergeFrom(inputStream);
        } else {
            throw new CamelException("Invalid protobuf content type format: " + contentTypeFormat);
        }

        if (!builder.isInitialized()) {
            // TODO which exception should be thrown here?
            throw new InvalidPayloadException(exchange, defaultInstance.getClass());
        }

        return builder.build();
    }

    protected Message loadDefaultInstance(final String className, final CamelContext context)
            throws CamelException, ClassNotFoundException {
        Class<?> instanceClass = context.getClassResolver().resolveMandatoryClass(className);
        if (Message.class.isAssignableFrom(instanceClass)) {
            try {
                Method method = instanceClass.getMethod("getDefaultInstance");
                return (Message) method.invoke(null);
            } catch (final Exception ex) {
                throw new CamelException(
                        "Can't set the defaultInstance of ProtobufferDataFormat with " + className + ", caused by " + ex);
            }
        } else {
            throw new CamelException(
                    "Can't set the defaultInstance of ProtobufferDataFormat with " + className
                                     + ", as the class is not a subClass of com.google.protobuf.Message");
        }
    }

    @Override
    protected void doStart() throws Exception {
        if (defaultInstance == null && instanceClassName != null) {
            defaultInstance = loadDefaultInstance(instanceClassName, getCamelContext());
        }
    }

    @Override
    protected void doStop() throws Exception {
        // noop
    }

}

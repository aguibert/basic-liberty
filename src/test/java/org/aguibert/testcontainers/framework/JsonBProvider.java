package org.aguibert.testcontainers.framework;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Scanner;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Produces({ "*/*" })
@Consumes({ "*/*" })
public class JsonBProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {

    private static final Jsonb jsonb = JsonbBuilder.create();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonBProvider.class);

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Object readFrom(Class<Object> clazz, Type genericType, Annotation[] annotations,
                           MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        String stringResult = convertStreamToString(entityStream);
        LOGGER.info("Response from server: " + stringResult);
        return jsonb.fromJson(stringResult, genericType);
    }

    @SuppressWarnings("resource")
    private static String convertStreamToString(java.io.InputStream is) {
        try (Scanner s = new Scanner(is).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public void writeTo(Object obj, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        String strData = jsonb.toJson(obj);
        LOGGER.info("Sending data to server: " + strData);
        jsonb.toJson(obj, entityStream);
    }
}
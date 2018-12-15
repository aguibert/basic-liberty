package org.aguibert.liberty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.annotation.PostConstruct;
import javax.json.bind.Jsonb;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

@Produces(MediaType.APPLICATION_JSON)
@Provider
public class EventMapper implements MessageBodyReader<Event>, MessageBodyWriter<Event> {

    @Context
    private Providers providers;

    private Jsonb jsonb;

    @PostConstruct
    private void init() {
        System.out.println("@AGG post construct");
        ContextResolver<Jsonb> resolver = providers.getContextResolver(Jsonb.class, MediaType.APPLICATION_JSON_TYPE);
        this.jsonb = resolver.getContext(Jsonb.class);
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == Event.class;
    }

    @Override
    public Event readFrom(Class<Event> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap,
                          InputStream inputStream) throws IOException, WebApplicationException {
        return jsonb.fromJson(inputStream, type);
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == Event.class;
    }

    @Override
    public void writeTo(Event event, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
                        OutputStream outputStream) throws IOException, WebApplicationException {
        outputStream.write(jsonb.toJson(event).getBytes());
    }

}
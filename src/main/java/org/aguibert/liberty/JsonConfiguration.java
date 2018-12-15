package org.aguibert.liberty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonConfiguration implements ContextResolver<Jsonb> {

    @Override
    public Jsonb getContext(Class<?> aClass) {
        System.out.println("@AGG getContext: " + aClass);
        return JsonbBuilder.newBuilder()
                        .withConfig(new JsonbConfig()
                                        .withPropertyVisibilityStrategy(new PrivateVisibilityJsonbStrategy()))
                        .build();
    }

    class PrivateVisibilityJsonbStrategy implements PropertyVisibilityStrategy {

        @Override
        public boolean isVisible(Field field) {
            return true;
        }

        @Override
        public boolean isVisible(Method method) {
            return false;
        }
    }
}
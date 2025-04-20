package com.preschool.libraries.base.annotation;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

// Jackson module to register custom deserializer for @SensitiveData fields
public class SensitiveDataModule extends SimpleModule {

  public SensitiveDataModule() {
    super("SensitiveDataModule");
    setDeserializerModifier(
        new BeanDeserializerModifier() {
          @Override
          public com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder updateBuilder(
              DeserializationConfig config,
              BeanDescription beanDesc,
              com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder builder) {
            // Iterate over all properties (works for records and classes)
            builder
                .getProperties()
                .forEachRemaining(
                    property -> {
                      // Check for @SensitiveData annotation
                      if (property.getMember() != null
                          && property.getMember().hasAnnotation(SensitiveData.class)) {
                        SensitiveData annotation =
                            property.getMember().getAnnotation(SensitiveData.class);
                        // Apply deserializer only for String fields
                        if (property.getType().getRawClass() == String.class) {
                          builder.addOrReplaceProperty(
                              property.withValueDeserializer(
                                  new SensitiveDataDeserializer(annotation)),
                              true);
                        }
                      }
                    });
            return builder;
          }
        });
  }
}

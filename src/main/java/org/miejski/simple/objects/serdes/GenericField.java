package org.miejski.simple.objects.serdes;

public class GenericField {
    private String objectName;
    private String data;

    public GenericField() {
    }

    public GenericField(String objectName, String data) {
        this.objectName = objectName;
        this.data = data;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getData() {
        return data;
    }
}

package com.shpandrak.datamodel.field;

import java.util.UUID;

/**
 * Created with love
 * User: shpandrak
 * Date: 3/31/13
 * Time: 14:31
 */
public class UUIDKey implements Key {
    private UUID keyValue;

    public UUIDKey() {
        generate();
    }

    public static UUIDKey fromStringValue(String value) {
        return new UUIDKey(value);
    }

    public UUIDKey(String keyValue) {
        fromString(keyValue);
    }

    public UUIDKey(UUID keyValue) {
        this.keyValue = keyValue;
    }

    @Override
    public void fromString(String value) {
        if (value == null){
            this.keyValue = null;
        }else {
            this.keyValue = UUID.fromString(value);
        }
    }

    private void generate() {
        this.keyValue = UUID.randomUUID();
    }

    @Override
    public Object toPersistentFormat() {
        return this.keyValue == null ? null : this.keyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UUIDKey uuidKey = (UUIDKey) o;

        if (!keyValue.equals(uuidKey.keyValue)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return keyValue.hashCode();
    }

    @Override
    public String toString() {
        return keyValue.toString();
    }
}

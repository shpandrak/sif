package com.shpandrak.datamodel.field;

/**
 * Created with love
 * User: shpandrak
 * Date: 4/15/13
 * Time: 13:33
 * This key implementation can be used by several persistence layer frameworks
 * Every framework is responsible for translating the key back and forth from string
 */
public class EntityKey implements Key{
    private String value;

    public EntityKey(String value) {
        this.value = value;
    }

    @Override
    public void fromString(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public Object toPersistentFormat() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityKey entityKey = (EntityKey) o;

        if (value != null ? !value.equals(entityKey.value) : entityKey.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}

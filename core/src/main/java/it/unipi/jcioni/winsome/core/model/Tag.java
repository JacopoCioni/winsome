package it.unipi.jcioni.winsome.core.model;

import java.util.Objects;

public class Tag {
    private String value;

    public Tag(String value) {
        this.value = value.toLowerCase();
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return value.equals(tag.value);
    }

}

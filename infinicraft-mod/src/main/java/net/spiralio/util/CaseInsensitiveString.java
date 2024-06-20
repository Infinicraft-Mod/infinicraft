package net.spiralio.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;

public record CaseInsensitiveString(@Nullable String string) implements Comparable<CaseInsensitiveString> {
    public static final Comparator<? super CaseInsensitiveString> COMPARATOR = CaseInsensitiveString::compareTo;

    @Override
    public int compareTo(@NotNull CaseInsensitiveString o) {
        return Objects.compare(string, o.string, String.CASE_INSENSITIVE_ORDER);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseInsensitiveString that = (CaseInsensitiveString) o;
        if (string == null) return that.string == null;
        return string.equalsIgnoreCase(that.string);
    }

    @Override
    public int hashCode() {
        if (string == null) {
            return 0;
        }
        int len = string.length();
        int hash = len;

        for (int i = 0; i < len; ) {
            int codePoint1 = string.codePointAt(i);
            int codePoint = Character.toUpperCase(codePoint1);
            hash = 31 * hash + codePoint;

            i += Character.charCount(codePoint1);
        }

        return hash;
    }

    @Override
    public String toString() {
        return string;
    }
}

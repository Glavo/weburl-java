package org.glavo.url.internal;

import org.jetbrains.annotations.NotNullByDefault;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a packed index range value.
///
/// The annotated value stores the start index in the high 32 bits and the end index in the low 32 bits.
@Documented
@NotNullByDefault
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.RECORD_COMPONENT,
        ElementType.TYPE_USE
})
public @interface IndexRange {
    /// Returns the name of the local variable, parameter, or field indexed by this range.
    String value() default "";
}

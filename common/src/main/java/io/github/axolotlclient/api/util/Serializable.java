package io.github.axolotlclient.api.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.netty.buffer.ByteBuf;

public interface Serializable<T> {
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.PARAMETER, ElementType.FIELD})
	@interface Length {
		int value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.CONSTRUCTOR})
	@interface Exclude {

	}

	default ByteBuf serialize(){
		return BufferUtil.serialize(this);
	}
}

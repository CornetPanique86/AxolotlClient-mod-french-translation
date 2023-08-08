/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.api.util;

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.primitives.Primitives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BufferUtil {

	public String getString(ByteBuf buffer, int index, int byteLength) {
		return buffer.toString(index, byteLength, StandardCharsets.UTF_8);
	}

	public String padString(String s, int length) {
		return Strings.padEnd(s, length, Character.MIN_VALUE).substring(0, length);
	}

	public byte[] toArray(ByteBuf buf) {
		if (buf.hasArray()) {
			return buf.array();
		}
		byte[] bytes = new byte[buf.readableBytes()];
		buf.getBytes(buf.readerIndex(), bytes);
		return bytes;
	}

	/**
	 * Serializes an object into a ByteBuf.
	 *
	 * @implNote Strings that have a set length should be annotated with <code>@Serializable.Length</code>, otherwise
	 * it is assumed that the string fills the entire ByteBuf and terminates it. Fields that should be excluded should
	 * be annotated with <code>@Serializable.Exclude</code>. Static fields are excluded by default. For more control
	 * over the serialization process the <code>Serializable</code> interface may be implemented.
	 * @param o Object that should be serialized
	 * @return the serialized ByteBuf
	 */
	public ByteBuf wrap(Object o) {

		if(o instanceof Serializable){
			return ((Serializable<?>) o).serialize();
		}

		return serialize(o);

	}

	ByteBuf serialize(Object o){
		ByteBuf buf = Unpooled.buffer();

		Class<?> c = o.getClass();
		int length = getPrimitiveByteLength(c);
		if (length != 0) {
			buf.writeBytes(getPrimitiveBytes(o, length));
			return buf;
		} else if (o instanceof CharSequence) {
			buf.writeCharSequence((CharSequence) o, StandardCharsets.UTF_8);
			return buf;
		}

		for (Field f : o.getClass().getDeclaredFields()) {
			f.setAccessible(true);

			if (Modifier.isStatic(f.getModifiers()) ||
				f.isAnnotationPresent(Serializable.Exclude.class)) {
				continue;
			}

			try {
				Object obj = f.get(o);
				Class<?> cl = obj.getClass();
				int l = getPrimitiveByteLength(cl);
				if (l != 0) {
					buf.writeBytes(getPrimitiveBytes(obj, l));
				} else if (obj instanceof String) {
					if (f.isAnnotationPresent(Serializable.Length.class)) {
						Serializable.Length s = f.getAnnotation(Serializable.Length.class);
						int stringLength = s.value();
						if (stringLength > 0) {
							buf.writeCharSequence(padString((String) obj, stringLength), StandardCharsets.UTF_8);
						} else {
							buf.writeCharSequence((CharSequence) obj, StandardCharsets.UTF_8);
							break;
						}
					} else {
						buf.writeCharSequence((CharSequence) obj, StandardCharsets.UTF_8);
						break;
					}

				} else {
					buf.writeBytes(wrap(obj));
				}
			} catch (IllegalAccessException ignored) {
			}
		}
		return buf.setIndex(0, buf.capacity());
	}

	private byte[] getPrimitiveBytes(Object o, int length) {
		Class<?> c = Primitives.wrap(o.getClass());
		ByteBuf buf = Unpooled.buffer(length);
		if (c == Integer.class) {
			buf.writeInt((Integer) o);
		} else if (c == Long.class) {
			buf.writeLong((Long) o);
		} else if (c == Boolean.class) {
			buf.writeBoolean((Boolean) o);
		} else if (c == Byte.class) {
			buf.writeByte((Byte) o);
		} else if (c == Character.class) {
			buf.writeChar((Character) o);
		} else if (c == Short.class) {
			buf.writeShort((Short) o);
		}
		return buf.array();
	}

	/**
	 * Deserializes a ByteBuf into an object.
	 *
	 * @implNote Strings in the target class' constructor with the least parameters <strong>should</strong>
	 * be annotated with the <code>@Serializable.Length</code> annotation to be deserialized correctly,
	 * otherwise it is assumed that they consume the entire rest length of the ByteBuf. Other Constructors can be marked
	 * with <code>@Serializable.Exclude</code> to exclude them from being used in this process.
	 * @param buf the buffer
	 * @param clazz the target object's class
	 * @return the deserialized object
	 * @param <T> the target object
	 */
	public <T> T unwrap(ByteBuf buf, Class<T> clazz) {

		return deserialize(buf, clazz);
	}

	<T> T deserialize(ByteBuf buf, Class<T> clazz) {
		T object;
		buf.setIndex(0, buf.capacity());
		List<Object> params = new ArrayList<>();

		Constructor<T> con = getConstructor(clazz);
		for (Parameter f : con.getParameters()) {

			int length;
			if ((length = getPrimitiveByteLength(f.getType())) != 0) {
				byte[] bytes = new byte[length];
				buf.readBytes(bytes);
				Object o = constructPrimitive(bytes, f.getType());
				params.add(o);
			} else if (f.getType() == String.class) {
				Field stringField = null;
				if(f.isNamePresent()) {
					try {
						stringField = clazz.getDeclaredField(f.getName());
					} catch (NoSuchFieldException ignored) {
					}
				}
				if (f.isAnnotationPresent(Serializable.Length.class)
					|| (stringField != null && stringField.isAnnotationPresent(Serializable.Length.class))) {
					Serializable.Length s = f.getAnnotation(Serializable.Length.class);
					if(s == null && stringField != null){
						s = stringField.getAnnotation(Serializable.Length.class);
					}
					assert s != null;
					int stringLength = s.value();
					if (stringLength > 0) {
						params.add(getString(buf, buf.readerIndex(), stringLength).trim());
						buf.setIndex(buf.readerIndex() + stringLength, buf.writerIndex());
					} else {
						params.add(getString(buf, buf.readerIndex(), buf.readableBytes()).trim());
						break;
					}
				} else {
					params.add(getString(buf, buf.readerIndex(), buf.readableBytes()).trim());
					break;
				}
			} else {
				params.add(unwrap(buf, f.getType()));
			}
		}

		try {
			object = con.newInstance(params.toArray());
		} catch (InvocationTargetException | InstantiationException |
				 IllegalAccessException e) {
			return null;
		}

		buf.clear();
		return object;
	}

	@SuppressWarnings("unchecked")
	private <T> Constructor<T> getConstructor(Class<T> clazz){
		return (Constructor<T>) Arrays.stream(clazz.getDeclaredConstructors()).filter(c -> Modifier.isPublic(c.getModifiers()))
			.filter(c -> !c.isAnnotationPresent(Serializable.Exclude.class))
			.sorted(Comparator.comparingInt(Constructor::getParameterCount)).toArray(Constructor[]::new)[0];
	}

	@SuppressWarnings("unchecked")
	private <T> T constructPrimitive(byte[] bytes, Class<T> type) {
		Class<?> c = Primitives.wrap(type);
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		if (c == Integer.class) {
			return (T) (Integer) buf.getInt();
		} else if (c == Long.class) {
			return (T) (Long) buf.getLong();
		} else if (c == Boolean.class) {
			return (T) (Boolean) (buf.getInt() == 1);
		} else if (c == Byte.class) {
			return (T) (Byte) buf.get();
		} else if (c == Character.class) {
			return (T) (Character) buf.getChar();
		} else if (c == Short.class) {
			return (T) (Short) buf.getShort();
		}
		return null;
	}

	private int getPrimitiveByteLength(Class<?> field) {
		Class<?> c = Primitives.wrap(field);
		try {
			return (int) c.getDeclaredField("BYTES").get(null);

		} catch (NoSuchFieldException | IllegalAccessException ignored) {
		}
		return 0;
	}
}

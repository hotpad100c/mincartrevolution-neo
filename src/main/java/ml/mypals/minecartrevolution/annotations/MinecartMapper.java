package ml.mypals.minecartrevolution.annotations;

import ml.mypals.minecartrevolution.behaviours.MinecartTransformManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation to mark a class as a minecart mapper.
 * If you use this, your class can be recognized by the {@link MinecartTransformManager} and be automatic registered
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MinecartMapper {}
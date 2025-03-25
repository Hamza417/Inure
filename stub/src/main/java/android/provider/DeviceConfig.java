package android.provider;

import android.os.Build;

import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi (api = Build.VERSION_CODES.Q)
public class DeviceConfig {
    
    /**
     * Privacy related properties definitions.
     */
    public static final String NAMESPACE_PRIVACY = "privacy";
    
    /**
     * Look up the value of a property for a particular namespace.
     *
     * @param namespace The namespace containing the property to look up.
     * @param name      The name of the property to look up.
     * @return the corresponding value, or null if not present.
     */
    public static String getProperty(@NonNull String namespace, @NonNull String name) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the values of multiple properties for a particular namespace. The lookup is atomic,
     * such that the values of these properties cannot change between the time when the first is
     * fetched and the time when the last is fetched.
     * <p>
     * Each call to {@link #setProperties(Properties)} is also atomic and ensures that either none
     * or all of the change is picked up here, but never only part of it.
     *
     * @param namespace The namespace containing the properties to look up.
     * @param names     The names of properties to look up, or empty to fetch all properties for the
     *                  given namespace.
     * @return {@link Properties} object containing the requested properties. This reflects the
     * state of these properties at the time of the lookup, and is not updated to reflect any
     * future changes. The keyset of this Properties object will contain only the intersection
     * of properties already set and properties requested via the names parameter. Properties
     * that are already set but were not requested will not be contained here. Properties that
     * are not set, but were requested will not be contained here either.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    @NonNull
    public static Properties getProperties(@NonNull String namespace, @NonNull String... names) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the String value of a property for a particular namespace.
     *
     * @param namespace    The namespace containing the property to look up.
     * @param name         The name of the property to look up.
     * @param defaultValue The value to return if the property does not exist or has no non-null
     *                     value.
     * @return the corresponding value, or defaultValue if none exists.
     */
    public static String getString(@NonNull String namespace, @NonNull String name,
            @Nullable String defaultValue) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the boolean value of a property for a particular namespace.
     *
     * @param namespace    The namespace containing the property to look up.
     * @param name         The name of the property to look up.
     * @param defaultValue The value to return if the property does not exist or has no non-null
     *                     value.
     * @return the corresponding value, or defaultValue if none exists.
     */
    public static boolean getBoolean(@NonNull String namespace, @NonNull String name,
            boolean defaultValue) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the int value of a property for a particular namespace.
     *
     * @param namespace    The namespace containing the property to look up.
     * @param name         The name of the property to look up.
     * @param defaultValue The value to return if the property does not exist, has no non-null
     *                     value, or fails to parse into an int.
     * @return the corresponding value, or defaultValue if either none exists or it does not parse.
     */
    public static int getInt(@NonNull String namespace, @NonNull String name, int defaultValue) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the long value of a property for a particular namespace.
     *
     * @param namespace    The namespace containing the property to look up.
     * @param name         The name of the property to look up.
     * @param defaultValue The value to return if the property does not exist, has no non-null
     *                     value, or fails to parse into a long.
     * @return the corresponding value, or defaultValue if either none exists or it does not parse.
     */
    public static long getLong(@NonNull String namespace, @NonNull String name, long defaultValue) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Look up the float value of a property for a particular namespace.
     *
     * @param namespace    The namespace containing the property to look up.
     * @param name         The name of the property to look up.
     * @param defaultValue The value to return if the property does not exist, has no non-null
     *                     value, or fails to parse into a float.
     * @return the corresponding value, or defaultValue if either none exists or it does not parse.
     */
    public static float getFloat(@NonNull String namespace, @NonNull String name,
            float defaultValue) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Create a new property with the the provided name and value in the provided namespace, or
     * update the value of such a property if it already exists. The same name can exist in multiple
     * namespaces and might have different values in any or all namespaces.
     * <p>
     * The method takes an argument indicating whether to make the value the default for this
     * property.
     * <p>
     * All properties stored for a particular scope can be reverted to their default values
     * by passing the namespace to {@link #resetToDefaults(int, String)}.
     *
     * @param namespace   The namespace containing the property to create or update.
     * @param name        The name of the property to create or update.
     * @param value       The value to store for the property.
     * @param makeDefault Whether to make the new value the default one.
     * @return True if the value was set, false if the storage implementation throws errors.
     * @see #resetToDefaults(int, String).
     */
    public static boolean setProperty(@NonNull String namespace, @NonNull String name,
            @Nullable String value, boolean makeDefault) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Set all of the properties for a specific namespace. Pre-existing properties will be updated
     * and new properties will be added if necessary. Any pre-existing properties for the specific
     * namespace which are not part of the provided {@link Properties} object will be deleted from
     * the namespace. These changes are all applied atomically, such that no calls to read or reset
     * these properties can happen in the middle of this update.
     * <p>
     * Each call to {@link #getProperties(String, String...)} is also atomic and ensures that either
     * none or all of this update is picked up, but never only part of it.
     *
     * @param properties the complete set of properties to set for a specific namespace.
     * @return True if the values were set, false otherwise.
     * @throws BadConfigException if the provided properties are banned by RescueParty.
     */
    public static boolean setProperties(@NonNull Properties properties) throws BadConfigException {
        throw new RuntimeException("STUB");
        
    }
    
    /**
     * Reset properties to their default values.
     * <p>
     * The method accepts an optional namespace parameter. If provided, only properties set within
     * that namespace will be reset. Otherwise, all properties will be reset.
     *
     * @param resetMode The reset mode to use.
     * @param namespace Optionally, the specific namespace which resets will be limited to.
     * @see #setProperty(String, String, String, boolean)
     */
    public static void resetToDefaults(int resetMode, @Nullable String namespace) {
        throw new RuntimeException("STUB");
    }
    
    /**
     * Thrown by {@link #setProperties(Properties)} when a configuration is rejected. This
     * happens if RescueParty has identified a bad configuration and reset the namespace.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static class BadConfigException extends Exception {
    }
    
    /**
     * A mapping of properties to values, as well as a single namespace which they all belong to.
     */
    @RequiresApi (api = Build.VERSION_CODES.S)
    public static class Properties {
        
        /**
         * Create a mapping of properties to values and the namespace they belong to.
         *
         * @param namespace   The namespace these properties belong to.
         * @param keyValueMap A map between property names and property values.
         */
        public Properties(@NonNull String namespace, @Nullable Map <String, String> keyValueMap) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * @return the namespace all properties within this instance belong to.
         */
        @NonNull
        public String getNamespace() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * @return the non-null set of property names.
         */
        @NonNull
        public Set <String> getKeyset() {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Look up the String value of a property.
         *
         * @param name         The name of the property to look up.
         * @param defaultValue The value to return if the property has not been defined.
         * @return the corresponding value, or defaultValue if none exists.
         */
        @Nullable
        public String getString(@NonNull String name, @Nullable String defaultValue) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Look up the boolean value of a property.
         *
         * @param name         The name of the property to look up.
         * @param defaultValue The value to return if the property has not been defined.
         * @return the corresponding value, or defaultValue if none exists.
         */
        public boolean getBoolean(@NonNull String name, boolean defaultValue) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Look up the int value of a property.
         *
         * @param name         The name of the property to look up.
         * @param defaultValue The value to return if the property has not been defined or fails to
         *                     parse into an int.
         * @return the corresponding value, or defaultValue if no valid int is available.
         */
        public int getInt(@NonNull String name, int defaultValue) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Look up the long value of a property.
         *
         * @param name         The name of the property to look up.
         * @param defaultValue The value to return if the property has not been defined. or fails to
         *                     parse into a long.
         * @return the corresponding value, or defaultValue if no valid long is available.
         */
        public long getLong(@NonNull String name, long defaultValue) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Look up the int value of a property.
         *
         * @param name         The name of the property to look up.
         * @param defaultValue The value to return if the property has not been defined. or fails to
         *                     parse into a float.
         * @return the corresponding value, or defaultValue if no valid float is available.
         */
        public float getFloat(@NonNull String name, float defaultValue) {
            throw new RuntimeException("STUB");
        }
        
        /**
         * Builder class for the construction of {@link Properties} objects.
         */
        public static final class Builder {
            
            /**
             * Create a new Builders for the specified namespace.
             *
             * @param namespace non null namespace.
             */
            public Builder(@NonNull String namespace) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Add a new property with the specified key and value.
             *
             * @param name  non null name of the property.
             * @param value nullable string value of the property.
             * @return this Builder object
             */
            @NonNull
            public Builder setString(@NonNull String name, @Nullable String value) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Add a new property with the specified key and value.
             *
             * @param name  non null name of the property.
             * @param value nullable string value of the property.
             * @return this Builder object
             */
            @NonNull
            public Builder setBoolean(@NonNull String name, boolean value) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Add a new property with the specified key and value.
             *
             * @param name  non null name of the property.
             * @param value int value of the property.
             * @return this Builder object
             */
            @NonNull
            public Builder setInt(@NonNull String name, int value) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Add a new property with the specified key and value.
             *
             * @param name  non null name of the property.
             * @param value long value of the property.
             * @return this Builder object
             */
            @NonNull
            public Builder setLong(@NonNull String name, long value) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Add a new property with the specified key and value.
             *
             * @param name  non null name of the property.
             * @param value float value of the property.
             * @return this Builder object
             */
            @NonNull
            public Builder setFloat(@NonNull String name, float value) {
                throw new RuntimeException("STUB");
            }
            
            /**
             * Create a new {@link Properties} object.
             *
             * @return non null Properties.
             */
            @NonNull
            public Properties build() {
                throw new RuntimeException("STUB");
            }
        }
    }
}

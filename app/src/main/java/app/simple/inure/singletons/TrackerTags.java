package app.simple.inure.singletons;

import java.util.HashSet;
import java.util.Set;

public class TrackerTags {
    
    private static Set <String> trackerPackages = new HashSet <>();
    
    public static synchronized void setTrackerPackages(Set <String> packages) {
        trackerPackages = packages;
    }
    
    public static synchronized void addPackage(String packageName) {
        try {
            trackerPackages.add(packageName);
        } catch (NullPointerException ignored) {
        }
    }
    
    public static synchronized void removePackage(String packageName) {
        try {
            trackerPackages.remove(packageName);
        } catch (NullPointerException ignored) {
        }
    }
    
    public static synchronized boolean isPackageTracked(String packageName) {
        try {
            return trackerPackages.contains(packageName);
        } catch (NullPointerException ignored) {
            return false;
        }
    }
    
    public static synchronized Set <String> getTrackedPackages() {
        /*
         * In the getTrackedPackages() method, a new HashSet is
         * returned with the contents of trackerPackages. This
         * is to avoid any concurrent modification exceptions
         * that might occur if the set is modified while it's
         * being iterated over in another thread. This technique
         * is known as "defensive copying".
         */
        return new HashSet <>(trackerPackages);
    }
    
    public static synchronized void clear() {
        try {
            trackerPackages.clear();
        } catch (NullPointerException ignored) {
        }
    }
    
    public static synchronized void addAll(Set <String> packages) {
        try {
            trackerPackages.addAll(packages);
        } catch (NullPointerException ignored) {
        }
    }
    
    public static synchronized void removeAll(Set <String> packages) {
        try {
            trackerPackages.removeAll(packages);
        } catch (NullPointerException ignored) {
        }
    }
    
    public static synchronized boolean isEmpty() {
        try {
            return trackerPackages.isEmpty();
        } catch (NullPointerException ignored) {
            return true;
        }
    }
    
    public static synchronized int size() {
        try {
            return trackerPackages.size();
        } catch (NullPointerException ignored) {
            return 0;
        }
    }
    
    public static synchronized boolean contains(String packageName) {
        try {
            return trackerPackages.contains(packageName);
        } catch (NullPointerException ignored) {
            return false;
        }
    }
}
